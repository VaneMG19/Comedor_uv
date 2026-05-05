package mx.uv.comedor.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Solo existe cuando pedido.tipo = ANTICIPADO.
 * Guarda cuándo y dónde el usuario recogerá su pedido.
 */
public class ProgramacionPedido {

    private Long          idProgramacion;
    private Long          idPedido;
    private LocalDate     fechaRecogida;
    private LocalTime     horaRecogida;
    private String        lugarRecogida;
    private boolean       recordatorioEnviado;
    private int           minutosAnticipacion;
    private EstProgEnum   estadoProg;

    public ProgramacionPedido() {}

    public ProgramacionPedido(Long idPedido, LocalDate fechaRecogida,
                               LocalTime horaRecogida, String lugarRecogida) {
        this.idPedido            = idPedido;
        this.fechaRecogida       = fechaRecogida;
        this.horaRecogida        = horaRecogida;
        this.lugarRecogida       = lugarRecogida;
        this.minutosAnticipacion = 30;
        this.estadoProg          = EstProgEnum.PENDIENTE;
        this.recordatorioEnviado = false;
    }

    // ── Métodos de negocio ─────────────────────────────────────────

    /**
     * Calcula los minutos que faltan para la hora de recogida.
     * Retorna negativo si ya pasó la hora.
     */
    public long calcularTiempoRestante() {
        LocalDateTime horaRecogidaDT =
            LocalDateTime.of(fechaRecogida, horaRecogida);
        return ChronoUnit.MINUTES.between(LocalDateTime.now(), horaRecogidaDT);
    }

    /**
     * Verifica si ya es momento de enviar el recordatorio.
     */
    public boolean debeEnviarRecordatorio() {
        long minutos = calcularTiempoRestante();
        return !recordatorioEnviado &&
               minutos > 0 &&
               minutos <= minutosAnticipacion;
    }

    public void marcarRecordatorioEnviado() {
        this.recordatorioEnviado = true;
    }

    public void cancelar() {
        this.estadoProg = EstProgEnum.EXPIRADO;
    }

    public boolean estaVencido() {
        return calcularTiempoRestante() < -15; // 15 min de gracia
    }

    // ── Getters y Setters ──────────────────────────────────────────

    public Long getIdProgramacion()           { return idProgramacion; }
    public void setIdProgramacion(Long id)    { this.idProgramacion = id; }
    public Long getIdPedido()                 { return idPedido; }
    public void setIdPedido(Long id)          { this.idPedido = id; }
    public LocalDate getFechaRecogida()       { return fechaRecogida; }
    public void setFechaRecogida(LocalDate f) { this.fechaRecogida = f; }
    public LocalTime getHoraRecogida()        { return horaRecogida; }
    public void setHoraRecogida(LocalTime h)  { this.horaRecogida = h; }
    public String getLugarRecogida()          { return lugarRecogida; }
    public void setLugarRecogida(String l)    { this.lugarRecogida = l; }
    public boolean isRecordatorioEnviado()    { return recordatorioEnviado; }
    public void setRecordatorioEnviado(boolean r){ this.recordatorioEnviado = r; }
    public int getMinutosAnticipacion()       { return minutosAnticipacion; }
    public void setMinutosAnticipacion(int m) { this.minutosAnticipacion = m; }
    public EstProgEnum getEstadoProg()        { return estadoProg; }
    public void setEstadoProg(EstProgEnum e)  { this.estadoProg = e; }
}
