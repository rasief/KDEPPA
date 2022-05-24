package co.com.foscal.entidad;

import co.com.foscal.utilidades.Utilidades;

/**
 * Clase que representa un punto
 * @author Feisar Moreno
 * @date 30/07/2020
 */
public class Punto {
    private long idCapa;
    private long idPunto;
    private double latitud;
    private double longitud;
    private CoordenadaUTM coordenadaUTM;
    
    public Punto() {
    }
    
    public Punto(long idCapa, long idPunto, double latitud, double longitud) {
        this.idCapa = idCapa;
        this.idPunto = idPunto;
        this.latitud = latitud;
        this.longitud = longitud;
        this.coordenadaUTM = Utilidades.convertirGeograficasAUTM(latitud, longitud);
    }
    
    public long getIdCapa() {
        return idCapa;
    }
    
    public void setIdCapa(long idCapa) {
        this.idCapa = idCapa;
    }
    
    public long getIdPunto() {
        return idPunto;
    }
    
    public void setIdPunto(long idPunto) {
        this.idPunto = idPunto;
    }
    
    public double getLatitud() {
        return latitud;
    }
    
    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }
    
    public double getLongitud() {
        return longitud;
    }
    
    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public CoordenadaUTM getCoordenadaUTM() {
        return coordenadaUTM;
    }

    public void setCoordenadaUTM(CoordenadaUTM coordenadaUTM) {
        this.coordenadaUTM = coordenadaUTM;
    }
    
}
