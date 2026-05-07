package mx.uv.comedor.model;

import java.math.BigDecimal;

/*
  POJO que representa la tabla 'platillo'.
  tipo=MENU  → aparece en el menú del día, puede ser cubierto por beca
  tipo=CARTA → siempre disponible, NUNCA cubierto por beca
 */
public class Platillo {

    private Long          idPlatillo;
    private String        nombre;
    private String        descripcion;
    private BigDecimal    precio;
    private BigDecimal    precioSubsidiado;
    private String        imagen;
    private boolean       disponible;
    private TipoPlatEnum  tipo;
    private int           tiempoPrep;

    private InformacionNutricional informacionNutricional;

    public Platillo() {}

    public Platillo(String nombre, BigDecimal precio, TipoPlatEnum tipo) {
        this.nombre     = nombre;
        this.precio     = precio;
        this.tipo       = tipo;
        this.disponible = true;
        this.tiempoPrep = 15;
    }

    // Métodos de negocio

    /*
      Calcula el precio que paga el usuario según su rol.
      Becado + tipo=MENU → $0.00 (gratis)
      Cualquier usuario + tipo=CARTA → precio normal
     */
    public BigDecimal calcularPrecioFinal(RolEnum rol) {
        if (rol == RolEnum.BECADO && tipo == TipoPlatEnum.MENU) {
            return BigDecimal.ZERO;
        }
        return precio;
    }

    public boolean esDelMenu() { return tipo == TipoPlatEnum.MENU; }
    public boolean esDeCarta() { return tipo == TipoPlatEnum.CARTA; }

    public void toggleDisponibilidad() {
        this.disponible = !this.disponible;
    }

    // Getters y Setters

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
    public int getTiempoPrep()               { return tiempoPrep; }
    public void setTiempoPrep(int t)         { this.tiempoPrep = t; }
    public InformacionNutricional getInformacionNutricional() { return informacionNutricional; }
    public void setInformacionNutricional(InformacionNutricional i) { this.informacionNutricional = i; }

    @Override
    public String toString() {
        return "Platillo{id=" + idPlatillo + ", nombre='" + nombre + "', tipo=" + tipo + "}";
    }
}