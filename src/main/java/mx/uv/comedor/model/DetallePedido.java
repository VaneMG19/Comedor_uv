package mx.uv.comedor.model;

import java.math.BigDecimal;

/**
 * Un renglón dentro de un pedido.
 * cubiertoPorBeca=true → precio $0 (solo platillos tipo=MENU para becados)
 */
public class DetallePedido {

    private Long       idDetalle;
    private Long       idPedido;
    private Long       idPlatillo;
    private int        cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private boolean    cubiertoPorBeca;
    private String     personalizaciones;

    private Platillo platillo;

    public DetallePedido() {}

    public DetallePedido(Long idPlatillo, int cantidad,
                         BigDecimal precioUnitario, boolean cubiertoPorBeca) {
        this.idPlatillo      = idPlatillo;
        this.cantidad        = cantidad;
        this.precioUnitario  = precioUnitario;
        this.cubiertoPorBeca = cubiertoPorBeca;
        calcularSubtotal();
    }

    public BigDecimal calcularSubtotal() {
        this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        return this.subtotal;
    }

    public void aplicarBeca() {
        if (platillo != null && platillo.getTipo() != TipoPlatEnum.MENU) {
            throw new IllegalStateException(
                    "La beca no aplica para platillos a la carta");
        }
        this.precioUnitario  = BigDecimal.ZERO;
        this.subtotal        = BigDecimal.ZERO;
        this.cubiertoPorBeca = true;
    }

    public Long getIdDetalle()               { return idDetalle; }
    public void setIdDetalle(Long id)        { this.idDetalle = id; }
    public Long getIdPedido()                { return idPedido; }
    public void setIdPedido(Long id)         { this.idPedido = id; }
    public Long getIdPlatillo()              { return idPlatillo; }
    public void setIdPlatillo(Long id)       { this.idPlatillo = id; }
    public int getCantidad()                 { return cantidad; }
    public void setCantidad(int c)           { this.cantidad = c; }
    public BigDecimal getPrecioUnitario()    { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal p){ this.precioUnitario = p; }
    public BigDecimal getSubtotal()          { return subtotal; }
    public void setSubtotal(BigDecimal s)    { this.subtotal = s; }
    public boolean isCubiertoPorBeca()       { return cubiertoPorBeca; }
    public void setCubiertoPorBeca(boolean b){ this.cubiertoPorBeca = b; }
    public String getPersonalizaciones()     { return personalizaciones; }
    public void setPersonalizaciones(String p){ this.personalizaciones = p; }
    public Platillo getPlatillo()            { return platillo; }
    public void setPlatillo(Platillo p)      { this.platillo = p; }
}