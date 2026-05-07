package mx.uv.comedor.model;

/*
 POJO que representa la tabla 'docente'.
  Paga con efectivo o tarjeta — sin saldo UV ni beca.
 */
public class Docente {

    private Long   idDocente;
    private Long   idUsuario;
    private String numEmpleadoDocente;
    private String facultad;
    private String departamento;
    private String categoria;

    private Usuario usuario;

    public Docente() {}

    public Docente(Long idUsuario, String numEmpleadoDocente,
                   String facultad, String departamento, String categoria) {
        this.idUsuario          = idUsuario;
        this.numEmpleadoDocente = numEmpleadoDocente;
        this.facultad           = facultad;
        this.departamento       = departamento;
        this.categoria          = categoria;
    }

    //  Métodos de negocio

    public void realizarPedido() {
        // Implementado en PedidoServlet
    }

    // Getters y Setters

    public Long getIdDocente() { return idDocente; }
    public void setIdDocente(Long idDocente) { this.idDocente = idDocente; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getNumEmpleadoDocente() { return numEmpleadoDocente; }
    public void setNumEmpleadoDocente(String numEmpleadoDocente) { this.numEmpleadoDocente = numEmpleadoDocente; }

    public String getFacultad() { return facultad; }
    public void setFacultad(String facultad) { this.facultad = facultad; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
