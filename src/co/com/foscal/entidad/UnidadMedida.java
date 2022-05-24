package co.com.foscal.entidad;

/**
 * Clase que representa un registro de unidad de medida
 * @author Feisar Moreno
 * @date 03/08/2020
 */
public class UnidadMedida {
    private long idUnidad;
    private String nombreUnidad;
    private double factorMetros;
    private int indGrados;
    
    public UnidadMedida() {
    }
    
    public UnidadMedida(long idUnidad, String nombreUnidad, double factorMetros, int indGrados) {
        this.idUnidad = idUnidad;
        this.nombreUnidad = nombreUnidad;
        this.factorMetros = factorMetros;
        this.indGrados = indGrados;
    }
    
    public long getIdUnidad() {
        return idUnidad;
    }
    
    public String getNombreUnidad() {
        return nombreUnidad;
    }
    
    public double getFactorMetros() {
        return factorMetros;
    }
    
    public int getIndGrados() {
        return indGrados;
    }
    
    @Override
    public String toString() {
        return this.nombreUnidad;
    }
}
