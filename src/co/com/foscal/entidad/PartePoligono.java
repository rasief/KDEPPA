package co.com.foscal.entidad;

import java.util.ArrayList;

/**
 * Clase que representa un registro de parte de polígono
 * @author Feisar Moreno
 * @date 31/07/2020
 */
public class PartePoligono {
    private CapaPoligonos capaPoligonos;
    private long idParte;
    private char tipoParte;
    private ArrayList<Punto> listaPuntos = new ArrayList<>();
    
    public PartePoligono() {
    }
    
    public PartePoligono(CapaPoligonos capaPoligonos, long idParte, char tipoParte) {
        this.capaPoligonos = capaPoligonos;
        this.idParte = idParte;
        this.tipoParte = tipoParte;
    }
    
    public long getIdParte() {
        return idParte;
    }
    
    public void setIdParte(long idParte) {
        this.idParte = idParte;
    }
    
    public char getTipoParte() {
        return tipoParte;
    }
    
    public void setTipoParte(char tipoParte) {
        this.tipoParte = tipoParte;
    }
    
    public ArrayList<Punto> getListaPuntos() {
        return listaPuntos;
    }
    
    public void setListaPuntos(ArrayList<Punto> listaPuntos) {
        this.listaPuntos = listaPuntos;
    }
    
    public void addPunto(Punto punto) {
        this.listaPuntos.add(punto);
        this.capaPoligonos.actualizarExtremos(punto.getCoordenadaUTM());
    }
    
    public int getCantPuntos() {
        return this.listaPuntos.size();
    }
}
