package mx.uv.comedor.model;

public enum DiaEnum {
    LUNES("Lunes"),
    MARTES("Martes"),
    MIERCOLES("Miércoles"),
    JUEVES("Jueves"),
    VIERNES("Viernes"),
    SABADO("Sábado");

    private final String etiqueta;

    DiaEnum(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Mapea java.time.DayOfWeek (1=LUN, 7=DOM) al enum DiaEnum.
     * Retorna LUNES para domingo (día sin operación).
     */
    public static DiaEnum desdeDayOfWeek(java.time.DayOfWeek dow) {
        switch (dow) {
            case MONDAY:    return LUNES;
            case TUESDAY:   return MARTES;
            case WEDNESDAY: return MIERCOLES;
            case THURSDAY:  return JUEVES;
            case FRIDAY:    return VIERNES;
            case SATURDAY:  return SABADO;
            default:        return LUNES;
        }
    }
}
