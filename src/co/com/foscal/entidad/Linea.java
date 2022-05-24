package co.com.foscal.entidad;

import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * Clase que representa una linea
 * @author Feisar Moreno
 * @date 30/07/2020
 */
public class Linea {
    private CapaLineas capaLineas;
    private long idLinea;
    private double largoLinea;
    private double largoAcumulado;
    private ArrayList<Punto> listaPuntos = new ArrayList<>();
    
    public Linea() {
    }
    
    public Linea(CapaLineas capaLineas, long idLinea) {
        this.capaLineas = capaLineas;
        this.idLinea = idLinea;
    }
    
    public Linea(ResultSet rs) {
    }
    
    public long getIdCapa() {
        return capaLineas.getIdCapa();
    }
    
    public long getIdLinea() {
        return idLinea;
    }
    
    public double getLargoLinea() {
        return largoLinea;
    }
    
    public double getLargoAcumulado() {
        return largoAcumulado;
    }
    
    public ArrayList<Punto> getListaPuntos() {
        return listaPuntos;
    }
    
    public void setListaPuntos(ArrayList<Punto> listaPuntos) {
        this.listaPuntos = listaPuntos;
    }
    
    public void addPunto(Punto punto) {
        this.listaPuntos.add(punto);
        this.capaLineas.actualizarExtremos(punto.getCoordenadaUTM());
    }
    
    public int getCantPuntos() {
        return this.listaPuntos.size();
    }
}
