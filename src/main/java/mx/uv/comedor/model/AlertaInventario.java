package mx.uv.comedor.model;

import java.time.LocalDateTime;

/**
 * Alerta generada automáticamente por el trigger trg_verificar_stock
 * cuando el stock de un ingrediente cae por debajo del mínimo.
 */
public class AlertaInventario {

    private Long              idAlerta;
    private Long              idIngrediente;
    private Long              idAdmin;
    private TipoAlertaEnum    tipo;
    private String            mensaje;
    private NivelAlertaEnum   nivel;
    private LocalDateTime     fechaGenerada;
    private boolean           atendida;

    private Ingrediente ingrediente; // cargado por DAO

    public AlertaInventario() {}

    // ── Métodos de negocio ─────────────────────────────────────────

    public void atender() {
        this.atendida = true;
    }

    /**
     * Retorna un ícono según el nivel para usar en el JSP.
     */
    public String getIconoNivel() {
        return switch (nivel) {
            case CRITICO -> "🔴";
            case WARNING -> "🟡";
            case INFO    -> "🔵";
        };
    }

    // ── Getters y Setters ──────────────────────────────────────────

    public Long getIdAlerta()                  { return idAlerta; }
    public void setIdAlerta(Long id)           { this.idAlerta = id; }
    public Long getIdIngrediente()             { return idIngrediente; }
    public void setIdIngrediente(Long id)      { this.idIngrediente = id; }
    public Long getIdAdmin()                   { return idAdmin; }
    public void setIdAdmin(Long id)            { this.idAdmin = id; }
    public TipoAlertaEnum getTipo()            { return tipo; }
    public void setTipo(TipoAlertaEnum t)      { this.tipo = t; }
    public String getMensaje()                 { return mensaje; }
    public void setMensaje(String m)           { this.mensaje = m; }
    public NivelAlertaEnum getNivel()          { return nivel; }
    public void setNivel(NivelAlertaEnum n)    { this.nivel = n; }
    public LocalDateTime getFechaGenerada()    { return fechaGenerada; }
    public void setFechaGenerada(LocalDateTime f){ this.fechaGenerada = f; }
    public boolean isAtendida()                { return atendida; }
    public void setAtendida(boolean a)         { this.atendida = a; }
    public Ingrediente getIngrediente()        { return ingrediente; }
    public void setIngrediente(Ingrediente i)  { this.ingrediente = i; }
}
