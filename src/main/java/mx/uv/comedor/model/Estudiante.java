package mx.uv.comedor.model;

/*
  POJO que representa la tabla 'estudiante'.
  Extiende los datos de Usuario cuando rol = ESTUDIANTE o BECADO.
 */
public class Estudiante {

    private Long    idEstudiante;
    private Long    idUsuario;
    private String  matricula;
    private String  carrera;
    private int     semestre;

    // Referencia al objeto Usuario completo (cargado por DAO con JOIN)
    private Usuario usuario;

    // Constructores

    public Estudiante() {}

    public Estudiante(Long idUsuario, String matricula,
                      String carrera, int semestre) {
        this.idUsuario = idUsuario;
        this.matricula = matricula;
        this.carrera   = carrera;
        this.semestre  = semestre;
    }

    //Métodos de negocio

    /*
      Retorna el nombre completo si el usuario fue cargado.
     */
    public String getNombreCompleto() {
        return usuario != null ? usuario.getNombreCompleto() : "Sin datos";
    }

    /*
      Indica si este estudiante tiene beca activa.
      El DAO de AlumnoBecado lo determina al consultar.
     */
    public boolean esBecado() {
        return usuario != null && usuario.getRol() == RolEnum.BECADO;
    }

    /*
      Realiza un pedido — la lógica se delega al servlet.
     */
    public void realizarPedido() {
        // Implementado en PedidoServlet
    }

    // Getters y Setters

    public Long getIdEstudiante() { return idEstudiante; }
    public void setIdEstudiante(Long idEstudiante) { this.idEstudiante = idEstudiante; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }

    public int getSemestre() { return semestre; }
    public void setSemestre(int semestre) { this.semestre = semestre; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    @Override
    public String toString() {
        return "Estudiante{id=" + idEstudiante + ", matricula='" + matricula + "'}";
    }
}
