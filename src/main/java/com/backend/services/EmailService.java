package com.backend.services;

// Para leer EMAIL_USER y EMAIL_PASS del archivo .env
import io.github.cdimascio.dotenv.Dotenv;

// Clases de la API JavaMail para envio de correos SMTP
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
// Para las propiedades de configuracion SMTP
import java.util.Properties;

/**
 * Servicio responsable del envío de correos electrónicos via Gmail SMTP.
 * Utiliza JavaMail para enviar correos HTML con autenticación OAuth2.
 */
public class EmailService {

    /** Correo remitente leído desde .env (ej: urbanlife@gmail.com) */
    private static final String CORREO_REMITENTE = Dotenv.load().get("EMAIL_USER");
    /** Contraseña de aplicación de Gmail (no es la contraseña normal, es una app password) */
    private static final String CONTRASENA_APP   = Dotenv.load().get("EMAIL_PASS");

    /**
     * Crea y retorna una sesión SMTP autenticada con Gmail.
     * @return Session configurada para enviar correos
     */
    private static Session crearSesion() {
        // Configurar propiedades del servidor SMTP de Gmail
        Properties propiedades = new Properties();
        // Servidor SMTP de Gmail
        propiedades.put("mail.smtp.host", "smtp.gmail.com");
        // Puerto TLS de Gmail
        propiedades.put("mail.smtp.port", "587");
        // Requerir autenticacion
        propiedades.put("mail.smtp.auth", "true");
        // Habilitar cifrado TLS
        propiedades.put("mail.smtp.starttls.enable", "true");

        // Crear sesion con autenticador que provee las credenciales al conectar
        return Session.getInstance(propiedades, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                // Retornar el correo y contrasena de aplicacion para autenticar con Gmail
                return new PasswordAuthentication(CORREO_REMITENTE, CONTRASENA_APP);
            }
        });
    }

    /**
     * Envía un correo HTML genérico al destinatario indicado.
     * @param correoDestino Correo electrónico del destinatario
     * @param asunto Asunto del correo
     * @param cuerpoHtml Contenido HTML del correo
     * @return true si se envió correctamente, false si falló
     */
    public static boolean enviarCorreo(String correoDestino, String asunto, String cuerpoHtml) {
        try {
            // Crear el mensaje MIME usando la sesion SMTP autenticada
            MimeMessage mensaje = new MimeMessage(crearSesion());
            // Establecer el remitente con nombre visible "UrbanLife"
            mensaje.setFrom(new InternetAddress(CORREO_REMITENTE, "UrbanLife"));
            // Agregar el destinatario como receptor principal (TO)
            mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(correoDestino));
            // Establecer el asunto del correo
            mensaje.setSubject(asunto);
            // Establecer el cuerpo como HTML con codificacion UTF-8
            mensaje.setText(cuerpoHtml, "utf-8", "html");
            // Enviar el mensaje a traves del servidor SMTP configurado
            Transport.send(mensaje);
            System.out.println("Correo enviado a: " + correoDestino);
            return true;
        } catch (Exception excepcion) {
            // Si falla el envio, registrar el error y retornar false
            System.out.println("Error EmailService.enviarCorreo: " + excepcion.getMessage());
            return false;
        }
    }

    /**
     * Genera y envía el correo de recuperación de contraseña con el enlace de restablecimiento.
     * Construye una plantilla HTML con un botón de acceso directo al formulario de reset.
     * @param correoDestino Correo electrónico del usuario que solicitó la recuperación
     * @param token Token UUID único para validar el restablecimiento de contraseña
     * @return true si el correo fue enviado exitosamente, false si ocurrió un error
     */
    public static boolean enviarCorreoRecuperacion(String correoDestino, String token) {
        // Construir el link de recuperacion con el token UUID como parametro
        String link = "http://localhost:5173/view/nueva-password.html?token=" + token;
        String asunto = "Recuperacion de contrasena - UrbanLife";
        // Plantilla HTML del correo con el boton de restablecimiento y el link alternativo
        // .formatted(link, link) reemplaza los dos %s con el link: uno en el boton y otro como texto
        String cuerpo = """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 30px;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff;
                                padding: 30px; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                        <h2 style="color: #2c3e50; text-align: center;">Recuperacion de contrasena</h2>
                        <p style="color: #555; font-size: 15px;">Hola,</p>
                        <p style="color: #555; font-size: 15px;">
                            Recibimos una solicitud para restablecer la contrasena de tu cuenta en <strong>UrbanLife</strong>.
                        </p>
                        <p style="color: #555; font-size: 15px;">
                            Haz clic en el siguiente boton para crear una nueva contrasena:
                        </p>
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s"
                               style="background-color: #2c3e50; color: #ffffff; padding: 12px 28px;
                                      text-decoration: none; border-radius: 6px; font-size: 15px; font-weight: bold;">
                                Restablecer contrasena
                            </a>
                        </div>
                        <p style="color: #888; font-size: 13px;">O copia y pega este enlace en tu navegador:</p>
                        <p style="background-color: #f0f0f0; padding: 10px; border-radius: 5px;
                                  word-break: break-all; color: #2c3e50; font-family: monospace; font-size: 13px;">
                            %s
                        </p>
                        <hr style="border: none; border-top: 1px solid #eee; margin: 25px 0;">
                        <p style="color: #aaa; font-size: 12px;">
                            Este enlace expira en <strong>1 hora</strong>. Si no solicitaste este correo, puedes ignorarlo.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(link, link);

        // Delegar el envio del correo HTML al metodo generico
        return enviarCorreo(correoDestino, asunto, cuerpo);
    }
}
