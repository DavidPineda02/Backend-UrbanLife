package com.backend.services;

import com.backend.dao.UsuarioDAO;
import com.backend.dto.CreateUserRequest;
import com.backend.dto.UpdateUserRequest;
import com.backend.helpers.PasswordHelper;
import com.backend.models.Usuario;

import java.util.List;

public class UserService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public List<Usuario> getAll() {
        return usuarioDAO.findAll();
    }

    public Usuario getById(int id) {
        return usuarioDAO.findById(id);
    }

    public Usuario create(CreateUserRequest req) {
        if (!req.isValid())
            throw new IllegalArgumentException("Nombre, correo y contraseña son requeridos");

        if (usuarioDAO.findByCorreo(req.getCorreo()) != null)
            throw new IllegalArgumentException("El correo ya está registrado");

        String estado = (req.getEstado() != null && !req.getEstado().trim().isEmpty())
                ? req.getEstado() : "Activo";

        Usuario nuevo = new Usuario(
                req.getNombre(),
                req.getCorreo(),
                PasswordHelper.hashPassword(req.getContrasena()),
                estado);

        return usuarioDAO.create(nuevo);
    }

    public Usuario update(int id, UpdateUserRequest req) {
        Usuario usuario = usuarioDAO.findById(id);
        if (usuario == null)
            throw new IllegalArgumentException("Usuario no encontrado");

        if (!req.isValid())
            throw new IllegalArgumentException("Al menos nombre o correo deben ser proporcionados");

        if (req.getCorreo() != null && !req.getCorreo().equals(usuario.getCorreo())) {
            if (usuarioDAO.findByCorreo(req.getCorreo()) != null)
                throw new IllegalArgumentException("El correo ya está en uso por otro usuario");
        }

        if (req.getNombre() != null && !req.getNombre().trim().isEmpty())
            usuario.setNombre(req.getNombre());
        if (req.getCorreo() != null && !req.getCorreo().trim().isEmpty())
            usuario.setCorreo(req.getCorreo());
        if (req.getEstado() != null && !req.getEstado().trim().isEmpty())
            usuario.setEstado(req.getEstado());

        if (!usuarioDAO.update(usuario))
            throw new RuntimeException("Error al actualizar el usuario");

        if (req.getContrasena() != null && !req.getContrasena().trim().isEmpty())
            usuarioDAO.updatePassword(id, PasswordHelper.hashPassword(req.getContrasena()));

        return usuarioDAO.findById(id);
    }

    public boolean delete(int id) {
        if (usuarioDAO.findById(id) == null)
            throw new IllegalArgumentException("Usuario no encontrado");
        return usuarioDAO.delete(id);
    }
}
