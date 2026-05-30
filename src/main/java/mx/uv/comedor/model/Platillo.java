package mx.uv.comedor.model;

import java.math.BigDecimal;

public class Platillo {

    private Long          idPlatillo;
    private String        nombre;
    private String        descripcion;
    private BigDecimal    precio;
    private BigDecimal    precioSubsidiado;
    private String        imagen;
    private boolean       disponible;
    private TipoPlatEnum  tipo;
    private CategoriaPlatEnum categoria;
    private int           tiempoPrep;
    private InformacionNutricional informacionNutricional;
    // Campos auxiliares para cupo (solo se llenan cuando viene del menu del dia)
    private Integer cupo;       // Cantidad total programada (ej: 50)
    private Integer vendidos;   // Cantidad ya pedida


    public Platillo() {}

    public Platillo(String nombre, BigDecimal precio, TipoPlatEnum tipo) {
        this.nombre = nombre;
        this.precio = precio;
        this.tipo   = tipo;
        this.categoria = CategoriaPlatEnum.OTRO;
        this.disponible = true;
        this.tiempoPrep = 15;
    }

    /**
     * Calcula el precio final aplicando precio subsidiado si el usuario es estudiante/becado.
     */
    public BigDecimal calcularPrecioFinal(RolEnum rol) {
        if (precioSubsidiado != null
                && (rol == RolEnum.ESTUDIANTE || rol == RolEnum.BECADO)) {
            return precioSubsidiado;
        }
        return precio;
    }

    public Long getIdPlatillo()              { return idPlatillo; }
    public void setIdPlatillo(Long id)       { this.idPlatillo = id; }
    public String getNombre()                { return nombre; }
    public void setNombre(String n)          { this.nombre = n; }
    public String getDescripcion()           { return descripcion; }
    public void setDescripcion(String d)     { this.descripcion = d; }
    public BigDecimal getPrecio()            { return precio; }
    public void setPrecio(BigDecimal p)      { this.precio = p; }
    public BigDecimal getPrecioSubsidiado()  { return precioSubsidiado; }
    public void setPrecioSubsidiado(BigDecimal p) { this.precioSubsidiado = p; }
    public String getImagen()                { return imagen; }
    public void setImagen(String i)          { this.imagen = i; }
    public boolean isDisponible()            { return disponible; }
    public void setDisponible(boolean d)     { this.disponible = d; }
    public TipoPlatEnum getTipo()            { return tipo; }
    public void setTipo(TipoPlatEnum t)      { this.tipo = t; }
    public CategoriaPlatEnum getCategoria()  { return categoria; }
    public void setCategoria(CategoriaPlatEnum c) { this.categoria = c; }
    public int getTiempoPrep()               { return tiempoPrep; }
    public void setTiempoPrep(int t)         { this.tiempoPrep = t; }
    public InformacionNutricional getInformacionNutricional() { return informacionNutricional; }
    public void setInformacionNutricional(InformacionNutricional i) { this.informacionNutricional = i; }
    public Integer getCupo()              { return cupo; }
    public void setCupo(Integer c)        { this.cupo = c; }
    public Integer getVendidos()          { return vendidos; }
    public void setVendidos(Integer v)    { this.vendidos = v; }

    /** Disponibilidad real: stock no agotado y disponible activo. */
    public boolean isAgotado() {
        if (cupo == null) return false;
        int vend = vendidos != null ? vendidos : 0;
        return vend >= cupo;
    }

    public int getRestante() {
        if (cupo == null) return -1;
        int vend = vendidos != null ? vendidos : 0;
        return Math.max(0, cupo - vend);
    }

}
