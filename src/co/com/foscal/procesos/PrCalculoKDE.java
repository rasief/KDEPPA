package co.com.foscal.procesos;

import co.com.foscal.entidad.Punto;
import co.com.foscal.entidad.Pixel;
import co.com.foscal.entidad.Capa;
import co.com.foscal.entidad.CapaPuntos;
import co.com.foscal.entidad.CoordenadaUTM;
import co.com.foscal.entidad.FuncionNucleo;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Clase para el cálculo del método KDE
 * @author Feisar Moreno
 * @date 06/08/2020
 */
public class PrCalculoKDE {

    private final String capaEventos;
    private final double anchoBanda;
    private final double resolucion;
    private final FuncionNucleo funcionNucleo;
    private final LinkedHashMap<String, Capa> mapaCapasKML;
    private final boolean indInicioCalculo;

    /**
     * Constructor de la clase
     * @param capaEventos Ruta de la capa de eventos
     * @param anchoBanda Ancho de banda en metros
     * @param resolucion Resolución en metros
     * @param funcionNucleo Función de núcleo
     * @param mapaCapasKML Mapa con todas las capa a incluir en el resultado
     */
    public PrCalculoKDE(String capaEventos, double anchoBanda, double resolucion, FuncionNucleo funcionNucleo, LinkedHashMap<String, Capa> mapaCapasKML) {
        this.capaEventos = capaEventos;
        this.anchoBanda = anchoBanda;
        this.resolucion = resolucion;
        this.funcionNucleo = funcionNucleo;
        this.mapaCapasKML = mapaCapasKML;
        this.indInicioCalculo = false;
    }

    public boolean getIndInicioCalculo() {
        return this.indInicioCalculo;
    }

    /**
     * Método que realiza el cálculo
     *
     * @return Identificador del registro de resultados del método NetKDE.
     */
    public Pixel[][] calcularKDE() {
        Pixel[][] arrPixels;
        try {
            //Se obtiene el listado de puntos del evento
            CapaPuntos capaPuntos = (CapaPuntos) this.mapaCapasKML.get(this.capaEventos);
            HashMap<Long, Punto> mapaPuntosEvento = capaPuntos.getMapaPuntos();

            //Se crea el listado de pixels, incluyendo la cantidad de puntos por pixel
            arrPixels = this.generarPixels(mapaPuntosEvento);
            
            //Se realiza el cálculo del valor de densidad de cada pixel
            for (int i = 0; i < arrPixels.length; i++) {
                int iMin = i - (int) Math.ceil(this.anchoBanda / this.resolucion);
                if (iMin < 0) {
                    iMin = 0;
                }
                int iMax = i + (int) Math.ceil(this.anchoBanda / this.resolucion);
                if (iMax >= arrPixels.length) {
                    iMax = arrPixels.length - 1;
                }
                
                for (int j = 0; j < arrPixels[i].length; j++) {
                    Pixel pixelBaseAux = arrPixels[i][j];
                    
                    if (pixelBaseAux.getCantidadPuntos() > 0) {
                        double factorDensidadAux = pixelBaseAux.getCantidadPuntos() / (Math.PI * Math.pow(this.anchoBanda / 1000, 2));
                        
                        //Se calculan los rangos de búsqueda de pixels influyentes
                        int jMin = j - (int) Math.ceil(this.anchoBanda / this.resolucion);
                        if (jMin < 0) {
                            jMin = 0;
                        }
                        int jMax = j + (int) Math.ceil(this.anchoBanda / this.resolucion);
                        if (jMax >= arrPixels[i].length) {
                            jMax = arrPixels[i].length - 1;
                        }
                        
                        //Se recorren los pixels sobre los que el pixel base puede tener influencia
                        for (int k = iMin; k <= iMax; k++) {
                            for (int l = jMin; l <= jMax; l++) {
                                Pixel pixelAux = arrPixels[k][l];
                                double distanciaAux = this.calcularDistanciaPixels(pixelBaseAux, pixelAux);
                                if (distanciaAux <= this.anchoBanda) {
                                    double densidadAux = this.calcularFuncionNucleo(distanciaAux) * factorDensidadAux;
                                    
                                    pixelAux.addDensidad(densidadAux);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            arrPixels = new Pixel[0][0];
        }

        return arrPixels;
    }

    private double calcularDistanciaPixels(Pixel pixel1, Pixel pixel2) {
        double distanciaX = Math.abs(pixel1.getxCentro() - pixel2.getxCentro());
        double distanciaY = Math.abs(pixel1.getyCentro() - pixel2.getyCentro());

        return Math.sqrt(Math.pow(distanciaX, 2) + Math.pow(distanciaY, 2));
    }
    
    private double calcularFuncionNucleo(double distancia) {
        double resultado = 0;

        if (distancia >= 0 && distancia <= this.anchoBanda) {
            switch (this.funcionNucleo.getIdFuncion()) {
                case 1: //Función Gaussiana
                    resultado = Math.pow((Math.sqrt(2 * Math.PI)), -1) * Math.exp(-Math.pow(distancia, 2) / (2 * Math.pow(this.anchoBanda, 2)));
                    break;

                case 2: //Función de Epanechnikov
                    resultado = 0.75 * (1 - Math.pow(distancia, 2) / Math.pow(this.anchoBanda, 2));
                    break;

                case 3: //Función de Varianza Mínima
                    resultado = (3.0 / 8.0) * (3 - 5 * Math.pow(distancia, 2) / Math.pow(this.anchoBanda, 2));
                    break;

                case 4: //Función Uniforme
                    resultado = 0.5;
                    break;

                case 5: //Función Triangular
                    resultado = 1 - Math.abs(distancia / this.anchoBanda);
                    break;
            }
        }

        return resultado;
    }

    private Pixel[][] generarPixels(HashMap<Long, Punto> mapaPuntosEvento) {
        //Se buscan las coordenadas mínima y máxima
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (Map.Entry<Long, Punto> entradaAux : mapaPuntosEvento.entrySet()) {
            CoordenadaUTM utmAux = entradaAux.getValue().getCoordenadaUTM();

            if (utmAux.getX() < minX) {
                minX = utmAux.getX();
            }
            if (utmAux.getY() < minY) {
                minY = utmAux.getY();
            }
            if (utmAux.getX() > maxX) {
                maxX = utmAux.getX();
            }
            if (utmAux.getY() > maxY) {
                maxY = utmAux.getY();
            }
        }

        minX -= this.anchoBanda;
        minY -= this.anchoBanda;
        maxX += this.anchoBanda;
        maxY += this.anchoBanda;
        
        //Se crean los lixels
        Pixel[][] arrPixels = new Pixel[(int) Math.ceil((maxY - minY) / this.resolucion)][(int) Math.ceil((maxX - minX) / this.resolucion)];
        long contAux = 0;
        int i = arrPixels.length - 1;
        for (double yAux = minY; yAux < maxY; yAux += this.resolucion) {
            int j = 0; //arrPixels[i].length - 1;
            for (double xAux = minX; xAux < maxX; xAux += this.resolucion) {
                double xCentro = xAux + this.resolucion / 2;
                double yCentro = yAux + this.resolucion / 2;

                arrPixels[i][j] = new Pixel(contAux, xCentro, yCentro);

                contAux++;
                j++;
            }
            i--;
        }

        //Se agregan los puntos a los pixels
        for (Map.Entry<Long, Punto> entradaAux : mapaPuntosEvento.entrySet()) {
            CoordenadaUTM utmAux = entradaAux.getValue().getCoordenadaUTM();
            double xAux = utmAux.getX();
            double yAux = utmAux.getY();

            int iAux = (int) Math.floor((maxY - yAux) / this.resolucion);
            int jAux = (int) Math.floor((xAux - minX) / this.resolucion);
            arrPixels[iAux][jAux].addPunto(entradaAux.getValue());
        }

        return arrPixels;
    }
    
}
