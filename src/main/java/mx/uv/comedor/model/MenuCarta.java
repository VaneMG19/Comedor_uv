package mx.uv.comedor.model;

import java.time.LocalTime;

public class MenuCarta {

    private Long      idMenuCarta;
    private Long      idPlatillo;
    private String    nombre;
    private String    descripcion;
    private LocalTime disponibleDesde;
    private LocalTime disponibleHasta;
    private String    diasDisponibles;
    private boolean   activo;

    private Platillo platillo;

    public MenuCarta() {}

    public boolean estaDisponibleAhora() {
        if (!activo) return false;
        LocalTime ahora = LocalTime.now();
        return !ahora.isBefore(disponibleDesde) && !ahora.isAfter(disponibleHasta);
    }

    public void activar()    { this.activo = true; }
    public void desactivar() { this.activo = false; }

    public Long getIdMenuCarta()              { return idMenuCarta; }
    public void setIdMenuCarta(Long id)       { this.idMenuCarta = id; }
    public Long getIdPlatillo()               { return idPlatillo; }
    public void setIdPlatillo(Long id)        { this.idPlatillo = id; }
    public String getNombre()                 { return nombre; }
    public void setNombre(String n)           { this.nombre = n; }
    public String getDescripcion()            { return descripcion; }
    public void setDescripcion(String d)      { this.descripcion = d; }
    public LocalTime getDisponibleDesde()     { return disponibleDesde; }
    public void setDisponibleDesde(LocalTime t){ this.disponibleDesde = t; }
    public LocalTime getDisponibleHasta()     { return disponibleHasta; }
    public void setDisponibleHasta(LocalTime t){ this.disponibleHasta = t; }
    public String getDiasDisponibles()        { return diasDisponibles; }
    public void setDiasDisponibles(String d)  { this.diasDisponibles = d; }
    public boolean isActivo()                 { return activo; }
    public void setActivo(boolean a)          { this.activo = a; }
    public Platillo getPlatillo()             { return platillo; }
    public void setPlatillo(Platillo p)       { this.platillo = p; }
}