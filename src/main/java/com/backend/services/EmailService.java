package com.backend.services;

import io.github.cdimascio.dotenv.Dotenv;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    private static final String CORREO_REMITENTE = Dotenv.load().get("EMAIL_USER");
    private static final String CONTRASENA_APP   = Dotenv.load().get("EMAIL_PASS");

    private static Session crearSesion() {
        Properties propiedades = new Properties();
        propiedades.put("mail.smtp.host", "smtp.gmail.com");
        propiedades.put("mail.smtp.port", "587");
        propiedades.put("mail.smtp.auth", "true");
        propiedades.put("mail.smtp.starttls.enable", "true");

        return Session.getInstance(propiedades, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(CORREO_REMITENTE, CONTRASENA_APP);
            }
        });
    }

    public static boolean enviarCorreo(String correoDestino, String asunto, String cuerpoHtml) {
        try {
            MimeMessage mensaje = new MimeMessage(crearSesion());
            mensaje.setFrom(new InternetAddress(CORREO_REMITENTE, "UrbanLife"));
            mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(correoDestino));
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpoHtml, "utf-8", "html");
            Transport.send(mensaje);
            System.out.println("Correo enviado a: " + correoDestino);
            return true;
        } catch (Exception excepcion) {
            System.out.println("Error EmailService.enviarCorreo: " + excepcion.getMessage());
            return false;
        }
    }

    public static boolean enviarCorreoRecuperacion(String correoDestino, String token) {
        String link = "http://localhost:5500/reset-password.html?token=" + token;
        String asunto = "Recuperacion de contrasena - UrbanLife";
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

        return enviarCorreo(correoDestino, asunto, cuerpo);
    }
}
