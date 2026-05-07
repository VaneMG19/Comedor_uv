package mx.uv.comedor.model;

import java.math.BigDecimal;

/*
  POJO que representa la tabla 'ingrediente'.
  El stock se actualiza automáticamente via trigger en BD
  cada vez que se inserta un movimiento_inventario.
 */
public class Ingrediente {

    private Long       idIngrediente;
    private String     nombre;
    private String     descripcion;
    private String     unidadMedida;
    private BigDecimal stockActual;
    private BigDecimal stockMinimo;
    private BigDecimal stockMaximo;
    private BigDecimal precioUnitario;
    private String     proveedor;
    private String     categoria;
    private boolean    activo;

    public Ingrediente() {}

    public Ingrediente(String nombre, String unidadMedida,
                       BigDecimal stockMinimo, BigDecimal precioUnitario) {
        this.nombre         = nombre;
        this.unidadMedida   = unidadMedida;
        this.stockMinimo    = stockMinimo;
        this.precioUnitario = precioUnitario;
        this.stockActual    = BigDecimal.ZERO;
        this.activo         = true;
    }

    // Métodos de negocio

    /*
      Verifica si el stock está por debajo del mínimo.
      El trigger en BD genera la alerta automáticamente,
      pero este método sirve para validaciones en Java.
     */
    public boolean verificarStockBajo() {
        return stockActual.compareTo(stockMinimo) < 0;
    }

    public boolean estaAgotado() {
        return stockActual.compareTo(BigDecimal.ZERO) == 0;
    }

    /*
      Calcula el porcentaje de stock respecto al máximo.
      Retorna -1 si no hay stock máximo definido.
     */
    public double getPorcentajeStock() {
        if (stockMaximo == null || stockMaximo.compareTo(BigDecimal.ZERO) == 0)
            return -1;
        return stockActual.divide(stockMaximo, 4, java.math.RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100))
                          .doubleValue();
    }

    /*
      Retorna una etiqueta de estado para mostrar en la vista.
     */
    public String getEstadoStock() {
        if (estaAgotado())        return "AGOTADO";
        if (verificarStockBajo()) return "BAJO";
        if (stockMaximo != null && stockActual.compareTo(stockMaximo) >= 0)
            return "LLENO";
        return "NORMAL";
    }

    // Getters y Setters

    public Long getIdIngrediente()           { return idIngrediente; }
    public void setIdIngrediente(Long id)    { this.idIngrediente = id; }
    public String getNombre()                { return nombre; }
    public void setNombre(String n)          { this.nombre = n; }
    public String getDescripcion()           { return descripcion; }
    public void setDescripcion(String d)     { this.descripcion = d; }
    public String getUnidadMedida()          { return unidadMedida; }
    public void setUnidadMedida(String u)    { this.unidadMedida = u; }
    public BigDecimal getStockActual()       { return stockActual; }
    public void setStockActual(BigDecimal s) { this.stockActual = s; }
    public BigDecimal getStockMinimo()       { return stockMinimo; }
    public void setStockMinimo(BigDecimal s) { this.stockMinimo = s; }
    public BigDecimal getStockMaximo()       { return stockMaximo; }
    public void setStockMaximo(BigDecimal s) { this.stockMaximo = s; }
    public BigDecimal getPrecioUnitario()    { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal p){ this.precioUnitario = p; }
    public String getProveedor()             { return proveedor; }
    public void setProveedor(String p)       { this.proveedor = p; }
    public String getCategoria()             { return categoria; }
    public void setCategoria(String c)       { this.categoria = c; }
    public boolean isActivo()                { return activo; }
    public void setActivo(boolean a)         { this.activo = a; }
}
