package co.com.foscal.entidad;

import java.util.HashMap;

/**
 * Clase que representa un registro de capa de puntos
 * @author Feisar Moreno
 * @date 30/07/2020
 */
public class CapaPuntos extends Capa {
    private CapaLineas capaLineas;
    private String fechaCrea;
    private int indProy;
    private double distProy;
    private long idAtributoFecha;
    private long idAtributoHora;
    private HashMap<Long, Punto> mapaPuntos = new HashMap<>();
    
    public CapaPuntos() {
    }

    public CapaPuntos(String nombreCapa, String descCapa) {
        super();
        this.tipoCapa = 'p';
        this.nombreCapa = nombreCapa;
        this.descCapa = descCapa;
    }
    
    public CapaLineas getCapaLineas() {
        return capaLineas;
    }
    
    public void setCapaLineas(CapaLineas capaLineas) {
        this.capaLineas = capaLineas;
    }
    
    public String getFechaCrea() {
        return fechaCrea;
    }
    
    public void setFechaCrea(String fechaCrea) {
        this.fechaCrea = fechaCrea;
    }
    
    public int getIndProy() {
        return indProy;
    }
    
    public void setIndProy(int indProy) {
        this.indProy = indProy;
    }
    
    public double getDistProy() {
        return distProy;
    }
    
    public void setDistProy(double distProy) {
        this.distProy = distProy;
    }
    
    public long getIdAtributoFecha() {
        return idAtributoFecha;
    }
    
    public void setIdAtributoFecha(long idAtributoFecha) {
        this.idAtributoFecha = idAtributoFecha;
    }
    
    public long getIdAtributoHora() {
        return idAtributoHora;
    }
    
    public void setIdAtributoHora(long idAtributoHora) {
        this.idAtributoHora = idAtributoHora;
    }
    
    public HashMap<Long, Punto> getMapaPuntos() {
        return mapaPuntos;
    }

    public void setMapaPuntos(HashMap<Long, Punto> mapaPuntos) {
        this.mapaPuntos = mapaPuntos;
    }
    
    public void addPunto(Punto punto) {
        this.mapaPuntos.put(punto.getIdPunto(), punto);
        this.actualizarExtremos(punto.getCoordenadaUTM());
    }
    
    public int getCantPuntos() {
        return this.mapaPuntos.size();
    }
    
}
