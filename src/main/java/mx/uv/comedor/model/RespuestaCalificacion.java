package mx.uv.comedor.model;

import java.time.LocalDateTime;

/**
 * Respuesta del administrador a una calificación.
 * Relación 1:1 con Calificacion.
 */
public class RespuestaCalificacion {

    private Long          idRespuesta;
    private Long          idCalificacion;
    private Long          idAdmin;
    private String        respuesta;
    private LocalDateTime fecha;
    private String        nombreAdmin; // cargado por DAO con JOIN

    public RespuestaCalificacion() {}

    public RespuestaCalificacion(Long idCalificacion, Long idAdmin,
                                  String respuesta) {
        this.idCalificacion = idCalificacion;
        this.idAdmin        = idAdmin;
        this.respuesta      = respuesta;
        this.fecha          = LocalDateTime.now();
    }

    public Long getIdRespuesta()              { return idRespuesta; }
    public void setIdRespuesta(Long id)       { this.idRespuesta = id; }
    public Long getIdCalificacion()           { return idCalificacion; }
    public void setIdCalificacion(Long id)    { this.idCalificacion = id; }
    public Long getIdAdmin()                  { return idAdmin; }
    public void setIdAdmin(Long id)           { this.idAdmin = id; }
    public String getRespuesta()              { return respuesta; }
    public void setRespuesta(String r)        { this.respuesta = r; }
    public LocalDateTime getFecha()           { return fecha; }
    public void setFecha(LocalDateTime f)     { this.fecha = f; }
    public String getNombreAdmin()            { return nombreAdmin; }
    public void setNombreAdmin(String n)      { this.nombreAdmin = n; }
}
