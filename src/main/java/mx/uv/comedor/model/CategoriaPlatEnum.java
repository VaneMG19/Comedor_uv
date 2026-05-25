package mx.uv.comedor.model;

/**
 * Categorías de platillos para clasificación en el menú a la carta.
 */
public enum CategoriaPlatEnum {
    PRINCIPAL(" Platillos principales"),
    BEBIDA("Bebidas"),
    POSTRE("Postres"),
    SANDWICH_TORTA("Sandwiches y Tortas"),
    EXTRA(" Ingredientes extra"),
    SUGERENCIA_CHEF(" Sugerencia del chef"),
    OTRO(" Otros");

    private final String etiqueta;

    CategoriaPlatEnum(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
