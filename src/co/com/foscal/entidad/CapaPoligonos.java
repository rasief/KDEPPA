package co.com.foscal.entidad;

import java.util.HashMap;

/**
 * Clase que representa un registro de capa de polígonos
 * @author Feisar Moreno
 * @date 31/07/2020
 */
public class CapaPoligonos extends Capa {
    private HashMap<Long, Poligono> mapaPoligonos = new HashMap<>();
    
    public CapaPoligonos() {
    }
    
    public CapaPoligonos(String nombreCapa, String descCapa) {
        super();
        this.tipoCapa = 'a';
        this.nombreCapa = nombreCapa;
        this.descCapa = descCapa;
    }
    
    public HashMap<Long, Poligono> getMapaPoligonos() {
        return mapaPoligonos;
    }
    
    public void setMapaPoligonos(HashMap<Long, Poligono> mapaPoligonos) {
        this.mapaPoligonos = mapaPoligonos;
    }
    
    public void addPoligono(Poligono poligono) {
        this.mapaPoligonos.put(poligono.getIdPoligono(), poligono);
    }
    
    public int getCantPoligonos() {
        return this.mapaPoligonos.size();
    }
}
