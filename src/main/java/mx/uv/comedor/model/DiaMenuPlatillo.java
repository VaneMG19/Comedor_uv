package mx.uv.comedor.model;

public class DiaMenuPlatillo {

    private Long        idDiaMenuPlatillo;
    private Long        idDiaMenu;
    private Long        idPlatillo;
    private CatMenuEnum categoria;
    private int         cantidad;

    private Platillo platillo;

    public DiaMenuPlatillo() {}

    public DiaMenuPlatillo(Long idDiaMenu, Long idPlatillo,
                           CatMenuEnum categoria, int cantidad) {
        this.idDiaMenu  = idDiaMenu;
        this.idPlatillo = idPlatillo;
        this.categoria  = categoria;
        this.cantidad   = cantidad;
    }

    public boolean verificarDisponibilidad() {
        return platillo != null && platillo.isDisponible() && cantidad > 0;
    }

    public Long getIdDiaMenuPlatillo()        { return idDiaMenuPlatillo; }
    public void setIdDiaMenuPlatillo(Long id) { this.idDiaMenuPlatillo = id; }
    public Long getIdDiaMenu()                { return idDiaMenu; }
    public void setIdDiaMenu(Long id)         { this.idDiaMenu = id; }
    public Long getIdPlatillo()               { return idPlatillo; }
    public void setIdPlatillo(Long id)        { this.idPlatillo = id; }
    public CatMenuEnum getCategoria()         { return categoria; }
    public void setCategoria(CatMenuEnum c)   { this.categoria = c; }
    public int getCantidad()                  { return cantidad; }
    public void setCantidad(int c)            { this.cantidad = c; }
    public Platillo getPlatillo()             { return platillo; }
    public void setPlatillo(Platillo p)       { this.platillo = p; }
}