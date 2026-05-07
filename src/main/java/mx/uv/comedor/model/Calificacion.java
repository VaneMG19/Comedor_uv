package mx.uv.comedor.model;

import java.time.LocalDateTime;

/*
  Una calificación de 1-5 estrellas dejada por un usuario
 después de recibir un pedido ENTREGADO.
  Un pedido solo puede tener una calificación (UNIQUE id_pedido).
 */
public class Calificacion {

    private Long          idCalificacion;
    private Long          idUsuario;
    private Long          idPlatillo;
    private Long          idPedido;
    private int           puntuacion;    // 1 a 5
    private String        comentario;
    private LocalDateTime fecha;
    private boolean       aprobada;

    // Cargados por DAO con JOIN
    private String        nombreUsuario;
    private String        nombrePlatillo;
    private RespuestaCalificacion respuesta;

    public Calificacion() {}

    public Calificacion(Long idUsuario, Long idPlatillo,
                        Long idPedido, int puntuacion, String comentario) {
        this.idUsuario   = idUsuario;
        this.idPlatillo  = idPlatillo;
        this.idPedido    = idPedido;
        this.puntuacion  = puntuacion;
        this.comentario  = comentario;
        this.aprobada    = true;
        this.fecha       = LocalDateTime.now();
    }

    // Métodos de negocio

    public void aprobar()  { this.aprobada = true; }
    public void rechazar() { this.aprobada = false; }

    public boolean tieneRespuesta() {
        return respuesta != null;
    }

    /*
      Retorna estrellas como texto para los JSP.
      Ej: puntuacion=4 → "★★★★☆"
     */
    public String getEstrellas() {
        return "★".repeat(puntuacion) + "☆".repeat(5 - puntuacion);
    }

    // Getters y Setters

    public Long getIdCalificacion()           { return idCalificacion; }
    public void setIdCalificacion(Long id)    { this.idCalificacion = id; }
    public Long getIdUsuario()                { return idUsuario; }
    public void setIdUsuario(Long id)         { this.idUsuario = id; }
    public Long getIdPlatillo()               { return idPlatillo; }
    public void setIdPlatillo(Long id)        { this.idPlatillo = id; }
    public Long getIdPedido()                 { return idPedido; }
    public void setIdPedido(Long id)          { this.idPedido = id; }
    public int getPuntuacion()                { return puntuacion; }
    public void setPuntuacion(int p)          { this.puntuacion = p; }
    public String getComentario()             { return comentario; }
    public void setComentario(String c)       { this.comentario = c; }
    public LocalDateTime getFecha()           { return fecha; }
    public void setFecha(LocalDateTime f)     { this.fecha = f; }
    public boolean isAprobada()               { return aprobada; }
    public void setAprobada(boolean a)        { this.aprobada = a; }
    public String getNombreUsuario()          { return nombreUsuario; }
    public void setNombreUsuario(String n)    { this.nombreUsuario = n; }
    public String getNombrePlatillo()         { return nombrePlatillo; }
    public void setNombrePlatillo(String n)   { this.nombrePlatillo = n; }
    public RespuestaCalificacion getRespuesta(){ return respuesta; }
    public void setRespuesta(RespuestaCalificacion r){ this.respuesta = r; }
}
