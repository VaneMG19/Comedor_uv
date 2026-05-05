package mx.uv.comedor.model;

/**
 * Estado de la solicitud de beca.
 * Debe coincidir con est_solicitud_enum de PostgreSQL.
 */
public enum EstSolicitudEnum {
    PENDIENTE,
    CONFIRMADA,
    CANCELADA,
    EXPIRADA
}
