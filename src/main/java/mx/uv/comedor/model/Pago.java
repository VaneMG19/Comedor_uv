package mx.uv.comedor.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Un pago por pedido.
 * - EFECTIVO / TARJETA → estudiante o docente paga todo
 * - BECA               → becado, solo platillos tipo=MENU gratis
 * - MIXTO              → becado con menú (beca) + carta (efectivo/tarjeta)
 */
public class Pago {

    private Long            idPago;
    private Long            idPedido;
    private Long            idUsuario;
    private BigDecimal      monto;          // total = montoBeca + montoEfectivo
    private BigDecimal      montoBeca;      // lo que cubrió la beca
    private BigDecimal      montoEfectivo;  // lo que pagó el usuario
    private MetodoPagoEnum  metodoPago;
    private EstPagoEnum     estado;
    private LocalDateTime   fechaPago;
    private String          referencia;
    private String          comprobante;

    public Pago() {}

    public Pago(Long idPedido, Long idUsuario, BigDecimal monto,
                BigDecimal montoBeca, MetodoPagoEnum metodoPago) {
        this.idPedido       = idPedido;
        this.idUsuario      = idUsuario;
        this.monto          = monto;
        this.montoBeca      = montoBeca;
        this.montoEfectivo  = monto.subtract(montoBeca);
        this.metodoPago     = metodoPago;
        this.estado         = EstPagoEnum.PENDIENTE;
        this.fechaPago      = LocalDateTime.now();
    }

    // ── Métodos de negocio ─────────────────────────────────────────

    public boolean procesarPago() {
        // La lógica real se hace en PagoDAO
        // Aquí solo validamos que el monto sea consistente
        if (monto.compareTo(BigDecimal.ZERO) < 0) return false;
        if (montoBeca.add(montoEfectivo).compareTo(monto) != 0) return false;
        return true;
    }

    /**
     * Calcula el descuento que aplica la beca.
     * Solo renglones con platillo tipo=MENU y becado con beca vigente.
     */
    public BigDecimal aplicarDescuentoBeca(BigDecimal subtotalMenu) {
        return subtotalMenu; // el descuento es el 100% del menú para becados
    }

    public void aprobar() {
        this.estado = EstPagoEnum.APROBADO;
    }

    public void rechazar() {
        this.estado = EstPagoEnum.RECHAZADO;
    }

    // ── Getters y Setters ──────────────────────────────────────────

    public Long getIdPago()                  { return idPago; }
    public void setIdPago(Long id)           { this.idPago = id; }
    public Long getIdPedido()                { return idPedido; }
    public void setIdPedido(Long id)         { this.idPedido = id; }
    public Long getIdUsuario()               { return idUsuario; }
    public void setIdUsuario(Long id)        { this.idUsuario = id; }
    public BigDecimal getMonto()             { return monto; }
    public void setMonto(BigDecimal m)       { this.monto = m; }
    public BigDecimal getMontoBeca()         { return montoBeca; }
    public void setMontoBeca(BigDecimal m)   { this.montoBeca = m; }
    public BigDecimal getMontoEfectivo()     { return montoEfectivo; }
    public void setMontoEfectivo(BigDecimal m){ this.montoEfectivo = m; }
    public MetodoPagoEnum getMetodoPago()    { return metodoPago; }
    public void setMetodoPago(MetodoPagoEnum m){ this.metodoPago = m; }
    public EstPagoEnum getEstado()           { return estado; }
    public void setEstado(EstPagoEnum e)     { this.estado = e; }
    public LocalDateTime getFechaPago()      { return fechaPago; }
    public void setFechaPago(LocalDateTime f){ this.fechaPago = f; }
    public String getReferencia()            { return referencia; }
    public void setReferencia(String r)      { this.referencia = r; }
    public String getComprobante()           { return comprobante; }
    public void setComprobante(String c)     { this.comprobante = c; }
}
