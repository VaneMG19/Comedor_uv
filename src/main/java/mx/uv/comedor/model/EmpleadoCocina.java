package mx.uv.comedor.model;

/*
  POJO que representa la tabla 'empleado_cocina'.
 */
public class EmpleadoCocina {

    private Long       idEmpleado;
    private Long       idUsuario;
    private String     numEmpleado;
    private TurnoEnum  turno;
    private String     puesto;

    private Usuario usuario;

    public EmpleadoCocina() {}

    public EmpleadoCocina(Long idUsuario, String numEmpleado,
                          TurnoEnum turno, String puesto) {
        this.idUsuario   = idUsuario;
        this.numEmpleado = numEmpleado;
        this.turno       = turno;
        this.puesto      = puesto;
    }

    // Métodos de negocio

    public boolean esTurnoManana() {
        return turno == TurnoEnum.MANANA;
    }

    public void actualizarEstadoPedido() {
        // Implementado en PedidoServlet
    }

    public void verPedidosPendientes() {
        // Implementado en PedidoServlet
    }

    // Getters y Setters

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getNumEmpleado() { return numEmpleado; }
    public void setNumEmpleado(String numEmpleado) { this.numEmpleado = numEmpleado; }

    public TurnoEnum getTurno() { return turno; }
    public void setTurno(TurnoEnum turno) { this.turno = turno; }

    public String getPuesto() { return puesto; }
    public void setPuesto(String puesto) { this.puesto = puesto; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
