package mx.uv.comedor.model;

import java.time.LocalDate;
import java.util.List;

public class MenuSemanal {

    private Long      idMenu;
    private Long      idAdmin;
    private int       semana;
    private int       anio;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean   activo;

    private List<DiaMenu> dias;

    public MenuSemanal() {}

    public MenuSemanal(Long idAdmin, int semana, int anio,
                       LocalDate fechaInicio, LocalDate fechaFin) {
        this.idAdmin     = idAdmin;
        this.semana      = semana;
        this.anio        = anio;
        this.fechaInicio = fechaInicio;
        this.fechaFin    = fechaFin;
        this.activo      = false;
    }

    public void activarMenu()    { this.activo = true; }
    public void desactivarMenu() { this.activo = false; }

    public boolean estaVigente() {
        LocalDate hoy = LocalDate.now();
        return activo && !hoy.isBefore(fechaInicio) && !hoy.isAfter(fechaFin);
    }

    public Long getIdMenu()               { return idMenu; }
    public void setIdMenu(Long id)        { this.idMenu = id; }
    public Long getIdAdmin()              { return idAdmin; }
    public void setIdAdmin(Long id)       { this.idAdmin = id; }
    public int getSemana()                { return semana; }
    public void setSemana(int s)          { this.semana = s; }
    public int getAnio()                  { return anio; }
    public void setAnio(int a)            { this.anio = a; }
    public LocalDate getFechaInicio()     { return fechaInicio; }
    public void setFechaInicio(LocalDate f){ this.fechaInicio = f; }
    public LocalDate getFechaFin()        { return fechaFin; }
    public void setFechaFin(LocalDate f)  { this.fechaFin = f; }
    public boolean isActivo()             { return activo; }
    public void setActivo(boolean a)      { this.activo = a; }
    public List<DiaMenu> getDias()        { return dias; }
    public void setDias(List<DiaMenu> d)  { this.dias = d; }
}