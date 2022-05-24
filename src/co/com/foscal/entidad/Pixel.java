package co.com.foscal.entidad;

import java.util.ArrayList;

/**
 * Clase que representa un pixel generado de acuerdo a la resolución
 * @author Feisar Moreno
 * @date 03/08/2020
 */
public class Pixel implements Cloneable {
    private long idPixel;
    private double xCentro;
    private double yCentro;
    private double densidad;
    private ArrayList<Punto> listaPuntos = new ArrayList<>();
    
    public Pixel() {
    }
    
    public Pixel(long idPixel, double xCentro, double yCentro) {
        this.idPixel = idPixel;
        this.xCentro = xCentro;
        this.yCentro = yCentro;
        this.densidad = 0;
    }
    
    public long getIdPixel() {
        return idPixel;
    }

    public void setIdPixel(long idPixel) {
        this.idPixel = idPixel;
    }

    public double getxCentro() {
        return xCentro;
    }

    public void setxCentro(double xCentro) {
        this.xCentro = xCentro;
    }

    public double getyCentro() {
        return yCentro;
    }

    public void setyCentro(double yCentro) {
        this.yCentro = yCentro;
    }

    public int getCantidadPuntos() {
        return this.listaPuntos.size();
    }

    public double getDensidad() {
        return densidad;
    }

    public void setDensidad(double densidad) {
        this.densidad = densidad;
    }

    public ArrayList<Punto> getListaPuntos() {
        return listaPuntos;
    }

    public void setListaPuntos(ArrayList<Punto> listaPuntos) {
        this.listaPuntos = listaPuntos;
    }
    
    public void addPunto(Punto punto) {
        this.listaPuntos.add(punto);
    }
    
    public void addDensidad(double densidad) {
        this.densidad += densidad;
    }
    
    @Override
    public Pixel clone() throws CloneNotSupportedException {
        return (Pixel)super.clone();
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (int)(this.idPixel ^ (this.idPixel >>> 32));
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final Pixel other = (Pixel)obj;
        return this.idPixel == other.idPixel;
    }
}
