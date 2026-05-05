package mx.uv.comedor.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Resumen estadístico de un platillo.
 * Se recalcula automáticamente vía trigger al insertar/actualizar calificaciones.
 * 1:1 con Platillo — se crea automáticamente al insertar el platillo.
 */
public class EstadisticaPlatillo {

    private Long          idEst;
    private Long          idPlatillo;
    private BigDecimal    promedioCalif;
    private int           totalCalif;
    private int           totalPedidos;
    private BigDecimal    ingresoTotal;
    private LocalDateTime ultimaActualizacion;

    public EstadisticaPlatillo() {}

    // ── Métodos de negocio ─────────────────────────────────────────

    /**
     * Retorna el promedio como estrellas para los JSP.
     */
    public String getPromedioEstrellas() {
        if (promedioCalif == null || totalCalif == 0) return "Sin calificaciones";
        int llenas  = promedioCalif.intValue();
        int vacias  = 5 - llenas;
        return "★".repeat(llenas) + "☆".repeat(vacias) +
               String.format(" (%.1f)", promedioCalif.doubleValue());
    }

    // ── Getters y Setters ──────────────────────────────────────────

    public Long getIdEst()                       { return idEst; }
    public void setIdEst(Long id)                { this.idEst = id; }
    public Long getIdPlatillo()                  { return idPlatillo; }
    public void setIdPlatillo(Long id)           { this.idPlatillo = id; }
    public BigDecimal getPromedioCalif()         { return promedioCalif; }
    public void setPromedioCalif(BigDecimal p)   { this.promedioCalif = p; }
    public int getTotalCalif()                   { return totalCalif; }
    public void setTotalCalif(int t)             { this.totalCalif = t; }
    public int getTotalPedidos()                 { return totalPedidos; }
    public void setTotalPedidos(int t)           { this.totalPedidos = t; }
    public BigDecimal getIngresoTotal()          { return ingresoTotal; }
    public void setIngresoTotal(BigDecimal i)    { this.ingresoTotal = i; }
    public LocalDateTime getUltimaActualizacion(){ return ultimaActualizacion; }
    public void setUltimaActualizacion(LocalDateTime u){ this.ultimaActualizacion = u; }
}
