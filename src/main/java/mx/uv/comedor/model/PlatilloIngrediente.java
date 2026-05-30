package mx.uv.comedor.model;

import java.math.BigDecimal;

/**
 * Receta: vincula un platillo con un ingrediente y la cantidad
 * que se necesita para preparar UNA porcion del platillo.
 *
 * Por ejemplo: el platillo "Arroz con pollo" usa:
 *   - 0.150 kg de arroz
 *   - 0.200 kg de pollo
 *   - 1 pieza de tomate
 */
public class PlatilloIngrediente {

    private Long       idReceta;
    private Long       idPlatillo;
    private Long       idIngrediente;
    private BigDecimal cantidad;

    // Campos auxiliares para mostrar info del ingrediente sin un JOIN extra
    private String nombreIngrediente;
    private String unidadIngrediente;
    private BigDecimal stockActualIngrediente;

    public PlatilloIngrediente() {}

    public PlatilloIngrediente(Long idPlatillo, Long idIngrediente, BigDecimal cantidad) {
        this.idPlatillo    = idPlatillo;
        this.idIngrediente = idIngrediente;
        this.cantidad      = cantidad;
    }

    public Long getIdReceta()              { return idReceta; }
    public void setIdReceta(Long id)       { this.idReceta = id; }
    public Long getIdPlatillo()            { return idPlatillo; }
    public void setIdPlatillo(Long id)     { this.idPlatillo = id; }
    public Long getIdIngrediente()         { return idIngrediente; }
    public void setIdIngrediente(Long id)  { this.idIngrediente = id; }
    public BigDecimal getCantidad()        { return cantidad; }
    public void setCantidad(BigDecimal c)  { this.cantidad = c; }

    public String getNombreIngrediente()        { return nombreIngrediente; }
    public void setNombreIngrediente(String n)  { this.nombreIngrediente = n; }
    public String getUnidadIngrediente()        { return unidadIngrediente; }
    public void setUnidadIngrediente(String u)  { this.unidadIngrediente = u; }
    public BigDecimal getStockActualIngrediente()       { return stockActualIngrediente; }
    public void setStockActualIngrediente(BigDecimal s) { this.stockActualIngrediente = s; }
}
