package mx.uv.comedor.model;

import java.time.LocalDateTime;

/**
 * Notificación enviada a un usuario.
 * Las notificaciones se crean automáticamente desde triggers de BD.
 * El frontend las consulta via polling (GET /notificaciones/nuevas).
 */
public class Notificacion {

    private Long            idNotificacion;
    private Long            idUsuario;
    private String          titulo;
    private String          mensaje;
    private TipoNotifEnum   tipo;
    private boolean         leida;
    private LocalDateTime   fechaEnvio;
    private LocalDateTime   fechaLectura;
    private CanalNotifEnum  canal;
    private Long            idReferencia;
    private String          moduloReferencia;

    public Notificacion() {}

    // ── Métodos de negocio ─────────────────────────────────────────

    public void marcarLeida() {
        this.leida        = true;
        this.fechaLectura = LocalDateTime.now();
    }

    /**
     * Retorna un ícono según el tipo para los JSP.
     */
    public String getIcono() {
        return switch (tipo) {
            case PEDIDO_LISTO     -> "🍽️";
            case PEDIDO_CANCELADO -> "❌";
            case BECA_AGOTADA     -> "🎫";
            case MENU_NUEVO       -> "📋";
            case ALERTA_STOCK     -> "⚠️";
            default               -> "🔔";
        };
    }

    // ── Getters y Setters ──────────────────────────────────────────

    public Long getIdNotificacion()             { return idNotificacion; }
    public void setIdNotificacion(Long id)      { this.idNotificacion = id; }
    public Long getIdUsuario()                  { return idUsuario; }
    public void setIdUsuario(Long id)           { this.idUsuario = id; }
    public String getTitulo()                   { return titulo; }
    public void setTitulo(String t)             { this.titulo = t; }
    public String getMensaje()                  { return mensaje; }
    public void setMensaje(String m)            { this.mensaje = m; }
    public TipoNotifEnum getTipo()              { return tipo; }
    public void setTipo(TipoNotifEnum t)        { this.tipo = t; }
    public boolean isLeida()                    { return leida; }
    public void setLeida(boolean l)             { this.leida = l; }
    public LocalDateTime getFechaEnvio()        { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime f)  { this.fechaEnvio = f; }
    public LocalDateTime getFechaLectura()      { return fechaLectura; }
    public void setFechaLectura(LocalDateTime f){ this.fechaLectura = f; }
    public CanalNotifEnum getCanal()            { return canal; }
    public void setCanal(CanalNotifEnum c)      { this.canal = c; }
    public Long getIdReferencia()               { return idReferencia; }
    public void setIdReferencia(Long id)        { this.idReferencia = id; }
    public String getModuloReferencia()         { return moduloReferencia; }
    public void setModuloReferencia(String m)   { this.moduloReferencia = m; }
}
