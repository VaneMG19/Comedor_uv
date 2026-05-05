package mx.uv.comedor.model;

import java.time.LocalDate;

/**
 * POJO que representa la tabla 'alumno_becado'.
 * La beca SOLO cubre platillos del menú del día (tipo = MENU).
 * Los platillos a la carta siempre se pagan con efectivo o tarjeta.
 */
public class AlumnoBecado {

    private Long       idBecado;
    private Long       idEstudiante;
    private String     tipoBeca;
    private int        comidasDisponiblesSemana;
    private int        comidasUsadasSemana;
    private LocalDate  vigenciaDesde;
    private LocalDate  vigenciaHasta;
    private String     aplicaSolo = "MENU"; // Siempre MENU — regla de negocio

    private Estudiante estudiante;

    public AlumnoBecado() {}

    public AlumnoBecado(Long idEstudiante, String tipoBeca,
                        int comidasDisponiblesSemana,
                        LocalDate vigenciaDesde, LocalDate vigenciaHasta) {
        this.idEstudiante             = idEstudiante;
        this.tipoBeca                 = tipoBeca;
        this.comidasDisponiblesSemana = comidasDisponiblesSemana;
        this.vigenciaDesde            = vigenciaDesde;
        this.vigenciaHasta            = vigenciaHasta;
    }

    // ── Métodos de negocio ─────────────────────────────────────────

    /**
     * Verifica si la beca está vigente a la fecha de hoy.
     */
    public boolean esBecaVigente() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(vigenciaDesde) && !hoy.isAfter(vigenciaHasta);
    }

    /**
     * Verifica si aún tiene comidas disponibles esta semana.
     */
    public boolean puedeUsarBeca() {
        return esBecaVigente() &&
               comidasUsadasSemana < comidasDisponiblesSemana;
    }

    /**
     * Valida que la beca aplica (siempre retorna true porque
     * la lógica de qué tipo de platillo cubre se valida en el DAO).
     */
    public boolean validarBeca() {
        return puedeUsarBeca();
    }

    /**
     * Registra el uso de una comida de beca.
     * Llama al DAO para persistir el cambio.
     */
    public void registrarUso() {
        if (!puedeUsarBeca()) {
            throw new IllegalStateException(
                "El becado no tiene comidas disponibles esta semana o la beca está vencida");
        }
        this.comidasUsadasSemana++;
    }

    /**
     * Calcula cuántas comidas le quedan esta semana.
     */
    public int getComidasRestantesSemana() {
        return comidasDisponiblesSemana - comidasUsadasSemana;
    }

    // ── Getters y Setters ──────────────────────────────────────────

    public Long getIdBecado() { return idBecado; }
    public void setIdBecado(Long idBecado) { this.idBecado = idBecado; }

    public Long getIdEstudiante() { return idEstudiante; }
    public void setIdEstudiante(Long idEstudiante) { this.idEstudiante = idEstudiante; }

    public String getTipoBeca() { return tipoBeca; }
    public void setTipoBeca(String tipoBeca) { this.tipoBeca = tipoBeca; }

    public int getComidasDisponiblesSemana() { return comidasDisponiblesSemana; }
    public void setComidasDisponiblesSemana(int n) { this.comidasDisponiblesSemana = n; }

    public int getComidasUsadasSemana() { return comidasUsadasSemana; }
    public void setComidasUsadasSemana(int n) { this.comidasUsadasSemana = n; }

    public LocalDate getVigenciaDesde() { return vigenciaDesde; }
    public void setVigenciaDesde(LocalDate vigenciaDesde) { this.vigenciaDesde = vigenciaDesde; }

    public LocalDate getVigenciaHasta() { return vigenciaHasta; }
    public void setVigenciaHasta(LocalDate vigenciaHasta) { this.vigenciaHasta = vigenciaHasta; }

    public String getAplicaSolo() { return aplicaSolo; }

    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
}
