package mx.uv.comedor.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * POJO que representa la tabla 'solicitud_beca'.
 * El becado registra con anticipación qué días quiere comer.
 */
public class SolicitudBeca {

    private Long              idSolicitud;
    private Long              idBecado;
    private Long              idDiaMenu;
    private Long              idMenu;
    private TipoComidaEnum    tipoComida;
    private EstSolicitudEnum  estado;
    private LocalDateTime     fechaSolicitud;
    private LocalDate         fechaLimite;

    private AlumnoBecado alumnoBecado;

    public SolicitudBeca() {}

    public SolicitudBeca(Long idBecado, Long idDiaMenu, Long idMenu,
                         TipoComidaEnum tipoComida, LocalDate fechaLimite) {
        this.idBecado      = idBecado;
        this.idDiaMenu     = idDiaMenu;
        this.idMenu        = idMenu;
        this.tipoComida    = tipoComida;
        this.fechaLimite   = fechaLimite;
        this.estado        = EstSolicitudEnum.PENDIENTE;
        this.fechaSolicitud = LocalDateTime.now();
    }

    // ── Métodos de negocio ─────────────────────────────────────────

    /**
     * Verifica si la solicitud aún puede modificarse.
     */
    public boolean estaVigente() {
        return LocalDate.now().isBefore(fechaLimite) &&
               estado == EstSolicitudEnum.PENDIENTE;
    }

    public void confirmar() {
        if (estado != EstSolicitudEnum.PENDIENTE) {
            throw new IllegalStateException("Solo se puede confirmar una solicitud PENDIENTE");
        }
        this.estado = EstSolicitudEnum.CONFIRMADA;
    }

    public void cancelar() {
        if (estado == EstSolicitudEnum.EXPIRADA) {
            throw new IllegalStateException("No se puede cancelar una solicitud ya expirada");
        }
        this.estado = EstSolicitudEnum.CANCELADA;
    }

    /**
     * Indica si esta solicitud es solo para desayuno.
     */
    public boolean incluyeDesayuno() {
        return tipoComida == TipoComidaEnum.DESAYUNO ||
               tipoComida == TipoComidaEnum.AMBOS;
    }

    /**
     * Indica si esta solicitud incluye comida del mediodía.
     */
    public boolean incluyeComida() {
        return tipoComida == TipoComidaEnum.COMIDA ||
               tipoComida == TipoComidaEnum.AMBOS;
    }

    // ── Getters y Setters ──────────────────────────────────────────

    public Long getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Long idSolicitud) { this.idSolicitud = idSolicitud; }

    public Long getIdBecado() { return idBecado; }
    public void setIdBecado(Long idBecado) { this.idBecado = idBecado; }

    public Long getIdDiaMenu() { return idDiaMenu; }
    public void setIdDiaMenu(Long idDiaMenu) { this.idDiaMenu = idDiaMenu; }

    public Long getIdMenu() { return idMenu; }
    public void setIdMenu(Long idMenu) { this.idMenu = idMenu; }

    public TipoComidaEnum getTipoComida() { return tipoComida; }
    public void setTipoComida(TipoComidaEnum tipoComida) { this.tipoComida = tipoComida; }

    public EstSolicitudEnum getEstado() { return estado; }
    public void setEstado(EstSolicitudEnum estado) { this.estado = estado; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime f) { this.fechaSolicitud = f; }

    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }

    public AlumnoBecado getAlumnoBecado() { return alumnoBecado; }
    public void setAlumnoBecado(AlumnoBecado alumnoBecado) { this.alumnoBecado = alumnoBecado; }
}
