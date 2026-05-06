package mx.uv.comedor.model;

import java.util.List;

public class DiaMenu {

    private Long    idDiaMenu;
    private Long    idMenu;
    private DiaEnum diaSemana;
    private boolean disponible;
    private int     cupoTotal;
    private int     cupoApartadoBecados;
    private int     cupoVentaGeneral;
    private int     cupoRestante;

    private List<DiaMenuPlatillo> platillos;

    public DiaMenu() {}

    public DiaMenu(Long idMenu, DiaEnum diaSemana, int cupoTotal) {
        this.idMenu       = idMenu;
        this.diaSemana    = diaSemana;
        this.cupoTotal    = cupoTotal;
        this.cupoRestante = cupoTotal;
        this.disponible   = true;
    }

    public boolean hayDisponibilidad() {
        return disponible && cupoRestante > 0;
    }

    public List<Platillo> getPlatillosPorCategoria(CatMenuEnum categoria) {
        if (platillos == null) return java.util.Collections.emptyList();
        List<Platillo> resultado = new java.util.ArrayList<>();
        for (DiaMenuPlatillo dmp : platillos) {
            if (dmp.getCategoria() == categoria && dmp.getPlatillo() != null) {
                resultado.add(dmp.getPlatillo());
            }
        }
        return resultado;
    }

    public Long getIdDiaMenu()              { return idDiaMenu; }
    public void setIdDiaMenu(Long id)       { this.idDiaMenu = id; }
    public Long getIdMenu()                 { return idMenu; }
    public void setIdMenu(Long id)          { this.idMenu = id; }
    public DiaEnum getDiaSemana()           { return diaSemana; }
    public void setDiaSemana(DiaEnum d)     { this.diaSemana = d; }
    public boolean isDisponible()           { return disponible; }
    public void setDisponible(boolean d)    { this.disponible = d; }
    public int getCupoTotal()               { return cupoTotal; }
    public void setCupoTotal(int c)         { this.cupoTotal = c; }
    public int getCupoApartadoBecados()     { return cupoApartadoBecados; }
    public void setCupoApartadoBecados(int c){ this.cupoApartadoBecados = c; }
    public int getCupoVentaGeneral()        { return cupoVentaGeneral; }
    public void setCupoVentaGeneral(int c)  { this.cupoVentaGeneral = c; }
    public int getCupoRestante()            { return cupoRestante; }
    public void setCupoRestante(int c)      { this.cupoRestante = c; }
    public List<DiaMenuPlatillo> getPlatillos() { return platillos; }
    public void setPlatillos(List<DiaMenuPlatillo> p){ this.platillos = p; }
}