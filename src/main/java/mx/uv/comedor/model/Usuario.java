package mx.uv.comedor.model;

import java.time.LocalDateTime;

/*
  POJO que representa la tabla 'usuario'.
  Clase base para todos los roles del sistema.
 */
public class Usuario {

    private Long          idUsuario;
    private String        nombre;
    private String        apellidos;
    private String        email;
    private String        passwordHash;
    private String        telefono;
    private String        fotoPerfil;
    private boolean       activo;
    private LocalDateTime fechaRegistro;
    private RolEnum       rol;

    // Constructores
    public Usuario() {}

    public Usuario(String nombre, String apellidos, String email,
                   String passwordHash, RolEnum rol) {
        this.nombre       = nombre;
        this.apellidos    = apellidos;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.rol          = rol;
        this.activo       = true;
    }

    // Métodos de negocio

    /*
      Retorna el nombre completo del usuario.
     */
    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    /*
      Verifica si el usuario tiene un rol específico.
     */
    public boolean tieneRol(RolEnum rolRequerido) {
        return this.rol == rolRequerido;
    }

    /*
      Verifica si la cuenta está activa.
     */
    public boolean estaActivo() {
        return activo;
    }

    // Getters y Setters

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public RolEnum getRol() { return rol; }
    public void setRol(RolEnum rol) { this.rol = rol; }

    @Override
    public String toString() {
        return "Usuario{id=" + idUsuario + ", email='" + email + "', rol=" + rol + "}";
    }
}
