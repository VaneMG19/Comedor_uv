package mx.uv.comedor.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * POJO que representa la tabla 'pedido'.
 * tipo=INMEDIATO → recoge en el momento
 * tipo=ANTICIPADO → tiene ProgramacionPedido asociada
 */
public class Pedido {

    private Long              idPedido;
    private Long              idUsuario;
    private String            folio;
    private LocalDateTime     fechaCreacion;
    private TipoPedidoEnum    tipo;
    private EstadoPedidoEnum  estado;
    private BigDecimal        subtotal;
    private BigDecimal        descuentoBeca;
    private BigDecimal        total;
    private String            notas;

    // Relaciones cargadas por el DAO
    private List<DetallePedido>    detalles       = new ArrayList<>();
    private Pago                   pago;
    private ProgramacionPedido     programacion;  // solo si tipo=ANTICIPADO

    public Pedido() {}

    public Pedido(Long idUsuario, TipoPedidoEnum tipo) {
        this.idUsuario     = idUsuario;
        this.tipo          = tipo;
        this.estado        = EstadoPedidoEnum.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
        this.subtotal      = BigDecimal.ZERO;
        this.descuentoBeca = BigDecimal.ZERO;
        this.total         = BigDecimal.ZERO;
    }

    // ── Métodos de negocio ─────────────────────────────────────────

    /**
     * Recalcula subtotal, descuentoBeca y total a partir de los detalles.
     * Llamar antes de persistir.
     */
    public void calcularTotal() {
        this.subtotal      = BigDecimal.ZERO;
        this.descuentoBeca = BigDecimal.ZERO;

        for (DetallePedido d : detalles) {
            this.subtotal = this.subtotal.add(d.getSubtotal());
            if (d.isCubiertoPorBeca()) {
                this.descuentoBeca = this.descuentoBeca.add(d.getSubtotal());
            }
        }
        this.total = this.subtotal.subtract(this.descuentoBeca);
    }

    public void cancelar() {
        if (estado == EstadoPedidoEnum.ENTREGADO) {
            throw new IllegalStateException("No se puede cancelar un pedido ya entregado");
        }
        this.estado = EstadoPedidoEnum.CANCELADO;
    }

    public void confirmar() {
        if (estado != EstadoPedidoEnum.PENDIENTE) {
            throw new IllegalStateException("Solo se puede confirmar un pedido PENDIENTE");
        }
        this.estado = EstadoPedidoEnum.PREPARANDO;
    }

    /**
     * Indica si este pedido tiene programación de recogida.
     */
    public boolean esProgramado() {
        return tipo == TipoPedidoEnum.ANTICIPADO;
    }

    /**
     * Agrega un detalle al pedido.
     */
    public void agregarDetalle(DetallePedido detalle) {
        detalle.setIdPedido(this.idPedido);
        detalles.add(detalle);
    }

    // ── Getters y Setters ──────────────────────────────────────────

    public Long getIdPedido()               { return idPedido; }
    public void setIdPedido(Long id)        { this.idPedido = id; }
    public Long getIdUsuario()              { return idUsuario; }
    public void setIdUsuario(Long id)       { this.idUsuario = id; }
    public String getFolio()                { return folio; }
    public void setFolio(String f)          { this.folio = f; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime f) { this.fechaCreacion = f; }
    public TipoPedidoEnum getTipo()         { return tipo; }
    public void setTipo(TipoPedidoEnum t)   { this.tipo = t; }
    public EstadoPedidoEnum getEstado()     { return estado; }
    public void setEstado(EstadoPedidoEnum e){ this.estado = e; }
    public BigDecimal getSubtotal()         { return subtotal; }
    public void setSubtotal(BigDecimal s)   { this.subtotal = s; }
    public BigDecimal getDescuentoBeca()    { return descuentoBeca; }
    public void setDescuentoBeca(BigDecimal d){ this.descuentoBeca = d; }
    public BigDecimal getTotal()            { return total; }
    public void setTotal(BigDecimal t)      { this.total = t; }
    public String getNotas()                { return notas; }
    public void setNotas(String n)          { this.notas = n; }
    public List<DetallePedido> getDetalles(){ return detalles; }
    public void setDetalles(List<DetallePedido> d){ this.detalles = d; }
    public Pago getPago()                   { return pago; }
    public void setPago(Pago p)             { this.pago = p; }
    public ProgramacionPedido getProgramacion(){ return programacion; }
    public void setProgramacion(ProgramacionPedido p){ this.programacion = p; }

    @Override
    public String toString() {
        return "Pedido{folio='" + folio + "', estado=" + estado + ", total=" + total + "}";
    }
}
