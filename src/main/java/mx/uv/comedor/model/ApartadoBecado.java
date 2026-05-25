package mx.uv.comedor.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/* apartado_becado: comidas que el becado aparta
  para días futuros del menú semanal.
 */
public class ApartadoBecado {

    private Long          idApartado;
    private Long          idBecado;
    private LocalDate     fechaConsumo;
    private String        tipoComida;       // DESAYUNO o COMIDA
    private String        estado;           // APARTADO, CONSUMIDO, CANCELADO, NO_RECOGIDO
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaConsumoReal;
    private String        notas;

    public ApartadoBecado() {}

    public Long getIdApartado() { return idApartado; }
    public void setIdApartado(Long id) { this.idApartado = id; }

    public Long getIdBecado() { return idBecado; }
    public void setIdBecado(Long id) { this.idBecado = id; }

    public LocalDate getFechaConsumo() { return fechaConsumo; }
    public void setFechaConsumo(LocalDate d) { this.fechaConsumo = d; }

    public String getTipoComida() { return tipoComida; }
    public void setTipoComida(String t) { this.tipoComida = t; }

    public String getEstado() { return estado; }
    public void setEstado(String e) { this.estado = e; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime f) { this.fechaCreacion = f; }

    public LocalDateTime getFechaConsumoReal() { return fechaConsumoReal; }
    public void setFechaConsumoReal(LocalDateTime f) { this.fechaConsumoReal = f; }

    public String getNotas() { return notas; }
    public void setNotas(String n) { this.notas = n; }
}
