package mx.uv.comedor.model;

import java.time.LocalDateTime;

/**
 * POJO de tarjeta_usuario.
 * IMPORTANTE: solo se guarda info no sensible:
 *  - últimos 4 dígitos
 *  - marca (Visa/Mastercard)
 *  - mes y año de vencimiento
 *  - alias
 * NUNCA se guarda el número completo ni el CVV.
 */
public class TarjetaUsuario {

    private Long          idTarjeta;
    private Long          idUsuario;
    private String        alias;
    private String        marca;
    private String        ultimos4;
    private String        nombreTitular;
    private int           mesVencimiento;
    private int           anioVencimiento;
    private boolean       esPredeterminada;
    private LocalDateTime fechaRegistro;
    private boolean       activa;

    public TarjetaUsuario() {}

    /** Devuelve algo como "•••• •••• •••• 1234" */
    public String getNumeroEnmascarado() {
        return "•••• •••• •••• " + (ultimos4 != null ? ultimos4 : "----");
    }

    /** Devuelve "12/2027" */
    public String getVencimientoFormateado() {
        return String.format("%02d/%d", mesVencimiento, anioVencimiento);
    }

    /** Emoji o letra de la marca */
    public String getIconoMarca() {
        if (marca == null) return "💳";
        switch (marca) {
            case "VISA":       return "💳";
            case "MASTERCARD": return "💳";
            case "AMEX":       return "💳";
            default:           return "💳";
        }
    }

    public Long getIdTarjeta() { return idTarjeta; }
    public void setIdTarjeta(Long id) { this.idTarjeta = id; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long id) { this.idUsuario = id; }

    public String getAlias() { return alias; }
    public void setAlias(String a) { this.alias = a; }

    public String getMarca() { return marca; }
    public void setMarca(String m) { this.marca = m; }

    public String getUltimos4() { return ultimos4; }
    public void setUltimos4(String u) { this.ultimos4 = u; }

    public String getNombreTitular() { return nombreTitular; }
    public void setNombreTitular(String n) { this.nombreTitular = n; }

    public int getMesVencimiento() { return mesVencimiento; }
    public void setMesVencimiento(int m) { this.mesVencimiento = m; }

    public int getAnioVencimiento() { return anioVencimiento; }
    public void setAnioVencimiento(int a) { this.anioVencimiento = a; }

    public boolean isEsPredeterminada() { return esPredeterminada; }
    public void setEsPredeterminada(boolean e) { this.esPredeterminada = e; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime f) { this.fechaRegistro = f; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean a) { this.activa = a; }
}
