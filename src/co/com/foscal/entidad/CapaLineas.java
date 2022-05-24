package co.com.foscal.entidad;

import java.util.HashMap;

/**
 * Clase que representa una capa de lineas
 * @author Feisar Moreno
 * @date 30/07/2020
 */
public class CapaLineas extends Capa {
    private String fechaCrea;
    private int indCierreNodos;
    private double distCierreNodos;
    private int cantAtributos;
    private double largoRed;
    private HashMap<Long, Linea> mapaLineas = new HashMap<>();
    
    public CapaLineas() {
    }
    
    public CapaLineas(String nombreCapa, String descCapa) {
        super();
        this.tipoCapa = 'l';
        this.nombreCapa = nombreCapa;
        this.descCapa = descCapa;
    }
    
    public String getFechaCrea() {
        return fechaCrea;
    }
    
    public void setFechaCrea(String fechaCrea) {
        this.fechaCrea = fechaCrea;
    }
    
    public int getIndCierreNodos() {
        return indCierreNodos;
    }
    
    public void setIndCierreNodos(int indCierreNodos) {
        this.indCierreNodos = indCierreNodos;
    }
    
    public double getDistCierreNodos() {
        return distCierreNodos;
    }
    
    public void setDistCierreNodos(double distCierreNodos) {
        this.distCierreNodos = distCierreNodos;
    }
    
    public int getCantAtributos() {
        return cantAtributos;
    }
    
    public void setCantAtributos(int cantAtributos) {
        this.cantAtributos = cantAtributos;
    }
    
    public int getCantLineas() {
        return this.mapaLineas.size();
    }
    
    public double getLargoRed() {
        return largoRed;
    }

    public HashMap<Long, Linea> getMapaLineas() {
        return mapaLineas;
    }

    public void setMapaLineas(HashMap<Long, Linea> mapaLineas) {
        this.mapaLineas = mapaLineas;
    }
    
    public void addLinea(Linea linea) {
        this.mapaLineas.put(linea.getIdLinea(), linea);
    }
    
}
