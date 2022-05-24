package co.com.foscal.entidad;

import java.util.ArrayList;

/**
 * Clase que representa un registro de polígono
 * @author Feisar Moreno
 * @date 31/07/2020
 */
public class Poligono {
    private CapaPoligonos capaPoligonos;
    private long idPoligono;
    private ArrayList<PartePoligono> listaPartes = new ArrayList<>();
    
    public Poligono() {
    }
    
    public Poligono(CapaPoligonos capaPoligonos, long idPoligono) {
        this.capaPoligonos = capaPoligonos;
        this.idPoligono = idPoligono;
    }
    
    public long getIdCapa() {
        return capaPoligonos.getIdCapa();
    }
    
    public long getIdPoligono() {
        return idPoligono;
    }
    
    public void setIdPoligono(long idPoligono) {
        this.idPoligono = idPoligono;
    }
    
    public ArrayList<PartePoligono> getListaPartes() {
        return listaPartes;
    }
    
    public void setListaPartes(ArrayList<PartePoligono> listaPartes) {
        this.listaPartes = listaPartes;
    }
    
    public void addParte(PartePoligono partePoligono) {
        this.listaPartes.add(partePoligono);
    }
    
    public int getCantPartes() {
        return this.listaPartes.size();
    }
    
}
