package mx.uv.comedor.model;

import java.math.BigDecimal;

/*
  Un renglón de la CompraAnticipada.
  El subtotal es calculado en BD: cantidad_solicitada * precio_unitario.
 */
public class DetalleCompra {

    private Long       idDetalleCompra;
    private Long       idCompra;
    private Long       idIngrediente;
    private BigDecimal cantidadSolicitada;
    private BigDecimal cantidadRecibida;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;  // calculado en BD (GENERATED ALWAYS AS)

    private Ingrediente ingrediente; // cargado por DAO

    public DetalleCompra() {}

    public DetalleCompra(Long idIngrediente, BigDecimal cantidadSolicitada,
                          BigDecimal precioUnitario) {
        this.idIngrediente      = idIngrediente;
        this.cantidadSolicitada = cantidadSolicitada;
        this.precioUnitario     = precioUnitario;
        this.cantidadRecibida   = BigDecimal.ZERO;
    }

    /*
      Calcula subtotal en Java — coincide con la columna GENERATED en BD.
     */
    public BigDecimal getSubtotal() {
        if (subtotal != null) return subtotal;
        if (cantidadSolicitada != null && precioUnitario != null)
            return cantidadSolicitada.multiply(precioUnitario);
        return BigDecimal.ZERO;
    }

    // Getters y Setters

    public Long getIdDetalleCompra()             { return idDetalleCompra; }
    public void setIdDetalleCompra(Long id)      { this.idDetalleCompra = id; }
    public Long getIdCompra()                    { return idCompra; }
    public void setIdCompra(Long id)             { this.idCompra = id; }
    public Long getIdIngrediente()               { return idIngrediente; }
    public void setIdIngrediente(Long id)        { this.idIngrediente = id; }
    public BigDecimal getCantidadSolicitada()    { return cantidadSolicitada; }
    public void setCantidadSolicitada(BigDecimal c){ this.cantidadSolicitada = c; }
    public BigDecimal getCantidadRecibida()      { return cantidadRecibida; }
    public void setCantidadRecibida(BigDecimal c){ this.cantidadRecibida = c; }
    public BigDecimal getPrecioUnitario()        { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal p)  { this.precioUnitario = p; }
    public void setSubtotal(BigDecimal s)        { this.subtotal = s; }
    public Ingrediente getIngrediente()          { return ingrediente; }
    public void setIngrediente(Ingrediente i)    { this.ingrediente = i; }
}
