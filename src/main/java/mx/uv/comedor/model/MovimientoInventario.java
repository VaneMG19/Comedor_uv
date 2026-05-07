package mx.uv.comedor.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
  Registro de cada cambio de stock.
  El trigger en BD actualiza stock_actual del ingrediente
  automáticamente al insertar este movimiento.
 */
public class MovimientoInventario {

    private Long              idMov;
    private Long              idIngrediente;
    private Long              idUsuario;
    private TipoMovInvEnum    tipo;
    private BigDecimal        cantidad;
    private BigDecimal        stockResultante;  // calculado por trigger
    private String            motivo;
    private LocalDateTime     fecha;

    private Ingrediente ingrediente; // cargado por DAO

    public MovimientoInventario() {}

    public MovimientoInventario(Long idIngrediente, Long idUsuario,
                                 TipoMovInvEnum tipo, BigDecimal cantidad,
                                 String motivo) {
        this.idIngrediente = idIngrediente;
        this.idUsuario     = idUsuario;
        this.tipo          = tipo;
        this.cantidad      = cantidad;
        this.motivo        = motivo;
        this.fecha         = LocalDateTime.now();
    }

    // Getters y Setters

    public Long getIdMov()                    { return idMov; }
    public void setIdMov(Long id)             { this.idMov = id; }
    public Long getIdIngrediente()            { return idIngrediente; }
    public void setIdIngrediente(Long id)     { this.idIngrediente = id; }
    public Long getIdUsuario()                { return idUsuario; }
    public void setIdUsuario(Long id)         { this.idUsuario = id; }
    public TipoMovInvEnum getTipo()           { return tipo; }
    public void setTipo(TipoMovInvEnum t)     { this.tipo = t; }
    public BigDecimal getCantidad()           { return cantidad; }
    public void setCantidad(BigDecimal c)     { this.cantidad = c; }
    public BigDecimal getStockResultante()    { return stockResultante; }
    public void setStockResultante(BigDecimal s){ this.stockResultante = s; }
    public String getMotivo()                 { return motivo; }
    public void setMotivo(String m)           { this.motivo = m; }
    public LocalDateTime getFecha()           { return fecha; }
    public void setFecha(LocalDateTime f)     { this.fecha = f; }
    public Ingrediente getIngrediente()       { return ingrediente; }
    public void setIngrediente(Ingrediente i) { this.ingrediente = i; }
}
