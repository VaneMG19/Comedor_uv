package mx.uv.comedor.model;

/*
 * POJO que representa la tabla 'administrador'.
 */
public class Administrador {

    private Long    idAdmin;
    private Long    idUsuario;
    private int     nivelAcceso;   // 1=básico, 2=gestión, 3=superadmin
    private String  departamento;

    private Usuario usuario;

    public Administrador() {}

    public Administrador(Long idUsuario, int nivelAcceso, String departamento) {
        this.idUsuario   = idUsuario;
        this.nivelAcceso = nivelAcceso;
        this.departamento = departamento;
    }

    // Métodos de negocio

    public boolean esSuperAdmin() {
        return nivelAcceso == 3;
    }

    public void gestionarMenu() {
        // Implementado en MenuServlet
    }

    public void verReportes() {
        // Implementado en ReporteServlet
    }

    //  Getters y Setters

    public Long getIdAdmin() { return idAdmin; }
    public void setIdAdmin(Long idAdmin) { this.idAdmin = idAdmin; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public int getNivelAcceso() { return nivelAcceso; }
    public void setNivelAcceso(int nivelAcceso) { this.nivelAcceso = nivelAcceso; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
}
