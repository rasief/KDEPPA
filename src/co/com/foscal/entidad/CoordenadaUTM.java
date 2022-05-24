package co.com.foscal.entidad;

/**
 * Clase que representa una coordenada UTM
 * @author Feisar Moreno
 * @date 03/08/2020
 */
public class CoordenadaUTM {
    private double x;
    private double y;
    private int zona;
    private char hemisferio;
    
    public CoordenadaUTM(double x, double y, int zona, char hemisferio) {
        this.x = x;
        this.y = y;
        this.zona = zona;
        this.hemisferio = hemisferio;
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public double getY() {
        return y;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public int getZona() {
        return zona;
    }
    
    public void setZona(int zona) {
        this.zona = zona;
    }
    
    public char getHemisferio() {
        return hemisferio;
    }
    
    public void setHemisferio(char hemisferio) {
        this.hemisferio = hemisferio;
    }
    
}
