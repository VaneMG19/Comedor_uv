package mx.uv.comedor.model;

import java.math.BigDecimal;

/*
  POJO que representa la tabla 'informacion_nutricional'.
  Relación 1:1 con Platillo.
 */
public class InformacionNutricional {

    private Long            idNutricional;
    private Long            idPlatillo;
    private BigDecimal      calorias;
    private BigDecimal      proteinas;
    private BigDecimal      carbohidratos;
    private BigDecimal      grasas;
    private BigDecimal      fibra;
    private BigDecimal      sodio;
    private BigDecimal      azucar;
    private String          alergenos;
    private boolean         esVegetariano;
    private boolean         esVegano;
    private boolean         esGlutenFree;
    private BigDecimal      huellaCarbonoKg;
    private NivelHuellaEnum nivelHuella;

    public InformacionNutricional() {}

    public String getResumenNutricional() {
        return String.format(
                "Cal: %.0f kcal | Prot: %.1fg | Carbs: %.1fg | Grasas: %.1fg",
                calorias != null      ? calorias.doubleValue()      : 0,
                proteinas != null     ? proteinas.doubleValue()     : 0,
                carbohidratos != null ? carbohidratos.doubleValue() : 0,
                grasas != null        ? grasas.doubleValue()        : 0
        );
    }

    public String getNivelHuellaDescripcion() {
        if (nivelHuella == null) return "Sin datos";
        switch (nivelHuella) {
            case BAJO:  return "Bajo (< 1 kg CO2eq)";
            case MEDIO: return "Medio (1-3 kg CO2eq)";
            case ALTO:  return "Alto (> 3 kg CO2eq)";
            default:    return "Sin datos";
        }
    }

    public Long getIdNutricional()             { return idNutricional; }
    public void setIdNutricional(Long id)      { this.idNutricional = id; }
    public Long getIdPlatillo()                { return idPlatillo; }
    public void setIdPlatillo(Long id)         { this.idPlatillo = id; }
    public BigDecimal getCalorias()            { return calorias; }
    public void setCalorias(BigDecimal c)      { this.calorias = c; }
    public BigDecimal getProteinas()           { return proteinas; }
    public void setProteinas(BigDecimal p)     { this.proteinas = p; }
    public BigDecimal getCarbohidratos()       { return carbohidratos; }
    public void setCarbohidratos(BigDecimal c) { this.carbohidratos = c; }
    public BigDecimal getGrasas()              { return grasas; }
    public void setGrasas(BigDecimal g)        { this.grasas = g; }
    public BigDecimal getFibra()               { return fibra; }
    public void setFibra(BigDecimal f)         { this.fibra = f; }
    public BigDecimal getSodio()               { return sodio; }
    public void setSodio(BigDecimal s)         { this.sodio = s; }
    public BigDecimal getAzucar()              { return azucar; }
    public void setAzucar(BigDecimal a)        { this.azucar = a; }
    public String getAlergenos()               { return alergenos; }
    public void setAlergenos(String a)         { this.alergenos = a; }
    public boolean isEsVegetariano()           { return esVegetariano; }
    public void setEsVegetariano(boolean v)    { this.esVegetariano = v; }
    public boolean isEsVegano()                { return esVegano; }
    public void setEsVegano(boolean v)         { this.esVegano = v; }
    public boolean isEsGlutenFree()            { return esGlutenFree; }
    public void setEsGlutenFree(boolean g)     { this.esGlutenFree = g; }
    public BigDecimal getHuellaCarbonoKg()     { return huellaCarbonoKg; }
    public void setHuellaCarbonoKg(BigDecimal h){ this.huellaCarbonoKg = h; }
    public NivelHuellaEnum getNivelHuella()    { return nivelHuella; }
    public void setNivelHuella(NivelHuellaEnum n){ this.nivelHuella = n; }
}