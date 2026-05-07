package mx.uv.comedor.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
  Orden de compra programada por el administrador.
  Al recepcionarse, genera movimientos de ENTRADA en inventario.
 */
public class CompraAnticipada {

    private Long            idCompra;
    private Long            idAdmin;
    private String          proveedor;
    private LocalDateTime   fechaEmision;
    private LocalDate       fechaEntregaEsperada;
    private EstCompraEnum   estado;
    private BigDecimal      totalEstimado;
    private String          notas;

    private List<DetalleCompra> detalles = new ArrayList<>();

    public CompraAnticipada() {}

    public CompraAnticipada(Long idAdmin, String proveedor,
                             LocalDate fechaEntregaEsperada) {
        this.idAdmin               = idAdmin;
        this.proveedor             = proveedor;
        this.fechaEntregaEsperada  = fechaEntregaEsperada;
        this.estado                = EstCompraEnum.PROGRAMADA;
        this.fechaEmision          = LocalDateTime.now();
    }

    // Métodos de negocio

    public void programar() {
        this.estado = EstCompraEnum.PROGRAMADA;
    }

    public void enviar() {
        if (estado != EstCompraEnum.PROGRAMADA)
            throw new IllegalStateException("Solo se puede enviar una compra PROGRAMADA");
        this.estado = EstCompraEnum.ENVIADA;
    }

    public void cancelar() {
        if (estado == EstCompraEnum.RECIBIDA)
            throw new IllegalStateException("No se puede cancelar una compra ya RECIBIDA");
        this.estado = EstCompraEnum.CANCELADA;
    }

    /*
      Recalcula el total estimado sumando los subtotales de los detalles.
     */
    public BigDecimal calcularTotal() {
        this.totalEstimado = detalles.stream()
            .map(DetalleCompra::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return this.totalEstimado;
    }

    // Getters y Setters

    public Long getIdCompra()                      { return idCompra; }
    public void setIdCompra(Long id)               { this.idCompra = id; }
    public Long getIdAdmin()                       { return idAdmin; }
    public void setIdAdmin(Long id)                { this.idAdmin = id; }
    public String getProveedor()                   { return proveedor; }
    public void setProveedor(String p)             { this.proveedor = p; }
    public LocalDateTime getFechaEmision()         { return fechaEmision; }
    public void setFechaEmision(LocalDateTime f)   { this.fechaEmision = f; }
    public LocalDate getFechaEntregaEsperada()     { return fechaEntregaEsperada; }
    public void setFechaEntregaEsperada(LocalDate f){ this.fechaEntregaEsperada = f; }
    public EstCompraEnum getEstado()               { return estado; }
    public void setEstado(EstCompraEnum e)         { this.estado = e; }
    public BigDecimal getTotalEstimado()           { return totalEstimado; }
    public void setTotalEstimado(BigDecimal t)     { this.totalEstimado = t; }
    public String getNotas()                       { return notas; }
    public void setNotas(String n)                 { this.notas = n; }
    public List<DetalleCompra> getDetalles()       { return detalles; }
    public void setDetalles(List<DetalleCompra> d) { this.detalles = d; }
}
