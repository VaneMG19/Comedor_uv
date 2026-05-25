package mx.uv.comedor.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/* la tabla becados_autorizados.
 Lista pre-aprobada por la universidad que el admin precarga.
 */
public class BecadoAutorizado {

    private Long          idBecadoAut;
    private String        email;
    private String        matricula;
    private String        nombreCompleto;
    private String        tipoBeca;
    private int           comidasSemana;
    private LocalDate     vigenciaDesde;
    private LocalDate     vigenciaHasta;
    private String        estado;
    private LocalDateTime fechaCarga;
    private String        notas;

    public BecadoAutorizado() {}

    public boolean estaVigente() {
        LocalDate hoy = LocalDate.now();
        return "PENDIENTE_REGISTRO".equals(estado)
            && !hoy.isBefore(vigenciaDesde)
            && !hoy.isAfter(vigenciaHasta);
    }

    public Long getIdBecadoAut() { return idBecadoAut; }
    public void setIdBecadoAut(Long id) { this.idBecadoAut = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String n) { this.nombreCompleto = n; }

    public String getTipoBeca() { return tipoBeca; }
    public void setTipoBeca(String t) { this.tipoBeca = t; }

    public int getComidasSemana() { return comidasSemana; }
    public void setComidasSemana(int c) { this.comidasSemana = c; }

    public LocalDate getVigenciaDesde() { return vigenciaDesde; }
    public void setVigenciaDesde(LocalDate d) { this.vigenciaDesde = d; }

    public LocalDate getVigenciaHasta() { return vigenciaHasta; }
    public void setVigenciaHasta(LocalDate d) { this.vigenciaHasta = d; }

    public String getEstado() { return estado; }
    public void setEstado(String e) { this.estado = e; }

    public LocalDateTime getFechaCarga() { return fechaCarga; }
    public void setFechaCarga(LocalDateTime f) { this.fechaCarga = f; }

    public String getNotas() { return notas; }
    public void setNotas(String n) { this.notas = n; }
}
