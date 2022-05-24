package co.com.foscal.ppa;

import co.com.foscal.entidad.Punto;
import co.com.foscal.entidad.Pixel;
import co.com.foscal.entidad.CapaLineas;
import co.com.foscal.entidad.Linea;
import co.com.foscal.entidad.UnidadMedida;
import co.com.foscal.entidad.CapaPuntos;
import co.com.foscal.entidad.Capa;
import co.com.foscal.entidad.CapaPoligonos;
import co.com.foscal.entidad.EntradaGenerica;
import co.com.foscal.entidad.PartePoligono;
import co.com.foscal.entidad.Poligono;
import co.com.foscal.utilidades.CalculadorDistribucion;
import co.com.foscal.utilidades.ManejadorColor;
import co.com.foscal.utilidades.Utilidades;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JComponent;

/**
 * Clase para visualizar mapas y resultados KDE
 *
 * @author Feisar Moreno date 03/08/2020
 */
public class VisorMapa extends JComponent implements MouseListener, MouseMotionListener, MouseWheelListener {

    private final double anchoBanda;
    private final double resolucion;
    private final LinkedHashMap<String, Capa> mapaCapasKML;
    private final EntradaGenerica datosCapaEventos;
    private final Pixel[][] arrPixels;
    private final UnidadMedida unidadMedida;
    private double latitudMin;
    private double latitudMed;
    private double latitudMax;
    private double longitudMin;
    private double longitudMed;
    private double longitudMax;
    private double anchoLienzo;
    private double altoLienzo;
    private double xCentro;
    private double yCentro;
    private double xIni;
    private double yIni;
    private double xPar;
    private double yPar;
    private double xFin;
    private double yFin;
    private double xZoom;
    private double yZoom;
    private double factorZoom;
    private boolean indVerLeyenda;
    private BufferedImage imagenFondo;
    private boolean isDragging;
    private ArrayList<Double> listaValoresOrdenados;
    private double[] arrDivisiones;
    private Color[] arrColores;
    private final int cantDivisiones;
    private final double[] arrEscalasZoom;
    private int posEscalaZoom;

    public VisorMapa(double anchoBanda, double resolucion, LinkedHashMap<String, Capa> mapaCapasKML, EntradaGenerica datosCapaEventos, Pixel[][] arrPixels, int cantDivisiones) {
        this.anchoBanda = anchoBanda;
        this.resolucion = resolucion;
        this.datosCapaEventos = datosCapaEventos;
        this.arrPixels = arrPixels;
        this.cantDivisiones = cantDivisiones;
        this.unidadMedida = new UnidadMedida(0, "Metros", 1, 0);
        this.xCentro = Double.NaN;
        this.yCentro = Double.NaN;
        this.xZoom = Double.NaN;
        this.yZoom = Double.NaN;
        this.factorZoom = 1;
        this.isDragging = false;
        this.arrEscalasZoom = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1, 1.25, 1.5, 2, 3, 4, 5, 6, 8, 10, 12.5, 15, 20, 30, 40, 50, 60, 80, 100};
        this.posEscalaZoom = 9;

        //Se agregan las capas a graficar en un orden específico
        this.mapaCapasKML = new LinkedHashMap<>();

        //Primero se agregan los políginos
        for (String nombreCapa : mapaCapasKML.keySet()) {
            Capa capaAux = mapaCapasKML.get(nombreCapa);

            if (capaAux.getTipoCapa() == 'a') {
                this.mapaCapasKML.put(nombreCapa, capaAux);
            }
        }

        //En segundo lugar se agregan las lineas
        for (String nombreCapa : mapaCapasKML.keySet()) {
            Capa capaAux = mapaCapasKML.get(nombreCapa);

            if (capaAux.getTipoCapa() == 'l') {
                this.mapaCapasKML.put(nombreCapa, capaAux);
            }
        }

        //En tercer lugar se agregan los puntos diferentes a la capa del evento
        for (String nombreCapa : mapaCapasKML.keySet()) {
            Capa capaAux = mapaCapasKML.get(nombreCapa);

            if (capaAux.getTipoCapa() == 'p' && (datosCapaEventos == null || !capaAux.getNombreCapa().equals(datosCapaEventos.getLlave()))) {
                this.mapaCapasKML.put(nombreCapa, capaAux);
            }
        }

        //Por último se agrega la capa de eventos
        if (datosCapaEventos != null) {
            Capa capaAux = mapaCapasKML.get(datosCapaEventos.getLlave());
            this.mapaCapasKML.put(capaAux.getNombreCapa(), capaAux);
        }

        this.cargarKDE();
    }

    private void cargarKDE() {
        //Se gregan los listeners de eventos del mouse
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);

        //Se obtienen las coordenadas mínimas y máximas del resultado
        double[] arrExtremos = Utilidades.buscarCoordenadasExtremos(this.datosCapaEventos, this.mapaCapasKML, this.anchoBanda);
        this.latitudMin = arrExtremos[1];
        this.longitudMin = arrExtremos[0];
        this.latitudMax = arrExtremos[3];
        this.longitudMax = arrExtremos[2];
        this.latitudMed = (this.latitudMin + this.latitudMax) / 2;
        this.longitudMed = (this.longitudMin + this.longitudMax) / 2;

        //Se obtienen los valores ordenados
        if (this.arrPixels != null) {
            CalculadorDistribucion cd = new CalculadorDistribucion();
            ArrayList<Double> listaValoresAux = this.obtenerValoresDensidadKDE();
            this.listaValoresOrdenados = cd.ordenarLista(listaValoresAux);

            //Se hallan los valores de las divisiones de colores
            this.arrDivisiones = cd.obtenerDivisiones(this.listaValoresOrdenados, this.cantDivisiones);

            //Se hallan los colores de los pixels
            ManejadorColor mc = new ManejadorColor();
            this.arrColores = mc.obtenerEscalaColores("FFFF00", "AA0000", this.cantDivisiones, false);
        }
    }

    private ArrayList<Double> obtenerValoresDensidadKDE() {
        ArrayList<Double> listaValores = new ArrayList<>();
        for (Pixel[] arrPixel : this.arrPixels) {
            for (Pixel pixelAux : arrPixel) {
                if (pixelAux.getDensidad() > 0) {
                    listaValores.add(pixelAux.getDensidad());
                }
            }
        }

        return listaValores;
    }

    @Override
    public void paint(Graphics g) {
        //Se obtienen las dimensiones para el lienzo
        this.anchoLienzo = this.getWidth();
        this.altoLienzo = this.getHeight();

        if (Double.isNaN(this.xCentro)) {
            this.xCentro = this.anchoLienzo / 2.0;
            this.yCentro = this.altoLienzo / 2.0;
        }

        //Se crea el fondo blanco
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, (int) this.anchoLienzo, (int) this.altoLienzo);

        if (this.isDragging) {
            g.drawImage(this.imagenFondo, (int) (this.xPar - this.xIni), (int) (this.yPar - this.yIni), null);
        } else {
            Graphics2D g2d = (Graphics2D) g;

            //Se calcula el factor de escala que tendrá el gráfico
            double factorEscala = this.altoLienzo / (this.latitudMax - this.latitudMin);
            double factorEscalaAux = this.anchoLienzo / (this.longitudMax - this.longitudMin);
            if (factorEscalaAux < factorEscala) {
                factorEscala = factorEscalaAux;
            }
            factorEscala *= this.factorZoom;

            //Se pinta el resultado KDE
            int ladoBase = (int) (Math.round(this.resolucion * factorEscala));
            if (ladoBase == 0) {
                ladoBase = 1;
            }
            g2d.setStroke(new BasicStroke(1));
            if (this.arrPixels != null) {
                for (Pixel[] arrPixels2 : this.arrPixels) {
                    for (Pixel pixel : arrPixels2) {
                        if (pixel.getDensidad() > 0) {
                            //Se determina el valor del pixel de acuerdo a los deciles
                            int decilAux = this.cantDivisiones - 1;
                            for (int i = 0; i < this.cantDivisiones; i++) {
                                if (pixel.getDensidad() <= this.arrDivisiones[i]) {
                                    decilAux = i;
                                    break;
                                }
                            }
                            g.setColor(this.arrColores[decilAux]);

                            //Se hallan las coordenadas del pixel
                            int xAux = (int) Math.round((pixel.getxCentro() - this.resolucion / 2 - this.longitudMed) * factorEscala + this.xCentro);
                            int yAux = (int) Math.round((pixel.getyCentro() - this.resolucion / 2 - this.latitudMed) * -factorEscala + this.yCentro);

                            if (xAux >= 0 || xAux + ladoBase <= this.anchoLienzo || yAux >= 0 || yAux + ladoBase <= this.altoLienzo) {
                                g.fillRect(xAux, yAux, ladoBase, ladoBase);
                                g.drawLine(xAux, yAux, xAux + ladoBase, yAux);
                                g.drawLine(xAux, yAux, xAux, yAux + ladoBase);
                            }
                        }
                    }
                }
            }
            
            //Se dibujan las capas
            for (Map.Entry<String, Capa> entradaAux : this.mapaCapasKML.entrySet()) {
                switch (entradaAux.getValue().getTipoCapa()) {
                    case 'p': //Capa de puntos
                        CapaPuntos capaPuntos = (CapaPuntos) entradaAux.getValue();

                        //Se define el color de los puntos
                        if (this.datosCapaEventos != null && capaPuntos.getNombreCapa().equals(this.datosCapaEventos.getLlave())) {
                            g.setColor(new Color(0, 127, 0));
                        } else {
                            g.setColor(new Color(0, 0, 127));
                        }
                        
                        //Se recorren los puntos y se pintan
                        for (long idPuntoAux : capaPuntos.getMapaPuntos().keySet()) {
                            Punto puntoAux = capaPuntos.getMapaPuntos().get(idPuntoAux);

                            double latitudAux = puntoAux.getCoordenadaUTM().getY();
                            double longitudAux = puntoAux.getCoordenadaUTM().getX();

                            //Se hallan las coordenadas del punto
                            int xPuntoAux = Integer.parseInt("" + Math.round((longitudAux - this.longitudMed) * factorEscala + this.xCentro));
                            int yPuntoAux = Integer.parseInt("" + Math.round((latitudAux - this.latitudMed) * -factorEscala + this.yCentro));

                            if (xPuntoAux - 3 >= 0 || xPuntoAux + 3 <= this.anchoLienzo || yPuntoAux - 3 >= 0 || yPuntoAux + 3 <= this.altoLienzo) {
                                g.fillOval(xPuntoAux - 3, yPuntoAux - 3, 6, 6);
                            }
                        }
                        break;

                    case 'l': //Capa de lineas
                        CapaLineas capaLineas = (CapaLineas) entradaAux.getValue();

                        //Se recorren las líneas
                        for (long idLineaAux : capaLineas.getMapaLineas().keySet()) {
                            Linea lineaAux = capaLineas.getMapaLineas().get(idLineaAux);

                            double latitudAnt = Double.NaN;
                            double longitudAnt = Double.NaN;
                            for (Punto puntoAux : lineaAux.getListaPuntos()) {
                                if (!Double.isNaN(latitudAnt)) {
                                    double latitudAct = puntoAux.getCoordenadaUTM().getY();
                                    double longitudAct = puntoAux.getCoordenadaUTM().getX();

                                    //Se selecciona el color que tendrá el segmento de línea
                                    Color colorSegmento = Color.DARK_GRAY;
                                    g.setColor(colorSegmento);

                                    //Se define el ancho de la línea
                                    g2d.setStroke(new BasicStroke(1));

                                    //Se hallan las coordenadas iniciales y finales del segmento
                                    int xIniAux = Integer.parseInt("" + Math.round((longitudAnt - this.longitudMed) * factorEscala + this.xCentro));
                                    int yIniAux = Integer.parseInt("" + Math.round((latitudAnt - this.latitudMed) * -factorEscala + this.yCentro));
                                    int xFinAux = Integer.parseInt("" + Math.round((longitudAct - this.longitudMed) * factorEscala + this.xCentro));
                                    int yFinAux = Integer.parseInt("" + Math.round((latitudAct - this.latitudMed) * -factorEscala + this.yCentro));

                                    if (xIniAux >= 0 || xIniAux <= this.anchoLienzo || xFinAux >= 0 || xFinAux <= this.anchoLienzo
                                            || yIniAux >= 0 || yIniAux <= this.altoLienzo || yFinAux >= 0 || yFinAux <= this.altoLienzo) {
                                        g.drawLine(xIniAux, yIniAux, xFinAux, yFinAux);
                                    }
                                }

                                latitudAnt = puntoAux.getCoordenadaUTM().getY();
                                longitudAnt = puntoAux.getCoordenadaUTM().getX();
                            }
                        }
                        break;

                    case 'a': //Capa de polígonos
                        CapaPoligonos capaPoligonos = (CapaPoligonos) entradaAux.getValue();

                        //Se recorren los poligonos
                        for (long idPoligonoAux : capaPoligonos.getMapaPoligonos().keySet()) {
                            Poligono poligonoAux = capaPoligonos.getMapaPoligonos().get(idPoligonoAux);

                            //Se recorren las partes del polígono una a una
                            for (PartePoligono parteAux : poligonoAux.getListaPartes()) {
                                double latitudIni = Double.NaN;
                                double longitudIni = Double.NaN;
                                double latitudAnt = Double.NaN;
                                double longitudAnt = Double.NaN;
                                for (Punto puntoAux : parteAux.getListaPuntos()) {
                                    if (Double.isNaN(latitudIni)) {
                                        latitudIni = puntoAux.getCoordenadaUTM().getY();
                                        longitudIni = puntoAux.getCoordenadaUTM().getX();
                                    }

                                    if (!Double.isNaN(latitudAnt)) {
                                        double latitudAct = puntoAux.getCoordenadaUTM().getY();
                                        double longitudAct = puntoAux.getCoordenadaUTM().getX();

                                        //Se selecciona el color que tendrá el segmento de línea
                                        Color colorSegmento = Color.GRAY;
                                        g.setColor(colorSegmento);

                                        //Se define el ancho de la línea
                                        g2d.setStroke(new BasicStroke(1));

                                        //Se hallan las coordenadas iniciales y finales del segmento
                                        int xIniAux = Integer.parseInt("" + Math.round((longitudAnt - this.longitudMed) * factorEscala + this.xCentro));
                                        int yIniAux = Integer.parseInt("" + Math.round((latitudAnt - this.latitudMed) * -factorEscala + this.yCentro));
                                        int xFinAux = Integer.parseInt("" + Math.round((longitudAct - this.longitudMed) * factorEscala + this.xCentro));
                                        int yFinAux = Integer.parseInt("" + Math.round((latitudAct - this.latitudMed) * -factorEscala + this.yCentro));

                                        if (xIniAux >= 0 || xIniAux <= this.anchoLienzo || xFinAux >= 0 || xFinAux <= this.anchoLienzo
                                                || yIniAux >= 0 || yIniAux <= this.altoLienzo || yFinAux >= 0 || yFinAux <= this.altoLienzo) {
                                            g.drawLine(xIniAux, yIniAux, xFinAux, yFinAux);
                                        }
                                    }

                                    latitudAnt = puntoAux.getCoordenadaUTM().getY();
                                    longitudAnt = puntoAux.getCoordenadaUTM().getX();
                                }

                                //Se traza el último segmento entre el punto inicial y el punto final de la parte del polígono
                                int xIniAux = Integer.parseInt("" + Math.round((longitudIni - this.longitudMed) * factorEscala + this.xCentro));
                                int yIniAux = Integer.parseInt("" + Math.round((latitudIni - this.latitudMed) * -factorEscala + this.yCentro));
                                int xFinAux = Integer.parseInt("" + Math.round((longitudAnt - this.longitudMed) * factorEscala + this.xCentro));
                                int yFinAux = Integer.parseInt("" + Math.round((latitudAnt - this.latitudMed) * -factorEscala + this.yCentro));

                                g.drawLine(xIniAux, yIniAux, xFinAux, yFinAux);
                            }
                        }
                        break;
                }
            }

            //Se agrega la leyenda
            if (this.indVerLeyenda) {
                this.pintarLeyenda(g2d);
            }

            //Se agrega la escala
            if (factorEscala > 0) {
                this.pintarEscala(g2d, factorEscala);
            }
        }
    }

    private double[] hallarCoordenadasTexto(int xIni, int yIni, int xFin, int yFin, double factorDist) {
        //Se halla el punto base de acuerdo al fator de distancia
        double xBase = xIni + (xFin - xIni) * factorDist;
        double yBase = yIni + (yFin - yIni) * factorDist;

        double distAux = 7;
        if (xIni == xFin) {
            //Línea vertical
            xBase += distAux;
        } else if (yIni == yFin) {
            //Línea horizontal
            yBase -= distAux;
        } else {
            double pendienteAux = -(float) (xFin - xIni) / (float) (yFin - yIni);
            double valorAux = distAux * Math.sqrt(1 / (Math.pow(pendienteAux, 2) + 1));
            xBase += valorAux;
            yBase += pendienteAux * valorAux;
        }

        double[] arrCoordenadas = {xBase, yBase};
        return arrCoordenadas;
    }

    private void pintarEscala(Graphics2D g, double factorEscala) {
        //Se halla el valor en metros de un pixel en el lienzo
        double valorBase = Utilidades.calcularDistanciaPuntos(this.latitudMed, this.longitudMed, this.latitudMed, this.longitudMed + 1, this.unidadMedida, 8) / factorEscala;

        //La línea de escala tendrá un valor máximo de 200 pixeles
        int valorBaseEscala = (int) Math.floor(valorBase * 200);
        int cantUnidades = (valorBaseEscala + "").length();
        int valorEscala = Math.floorDiv(valorBaseEscala, (int) Math.pow(10, cantUnidades - 1));
        switch (valorEscala) {
            case 3:
            case 4:
                valorEscala = 2;
                break;
            case 6:
            case 7:
            case 8:
            case 9:
                valorEscala = 5;
                break;
        }
        valorEscala *= (int) Math.pow(10, cantUnidades - 1);

        int largoLinea = valorEscala * 200 / valorBaseEscala;

        //Se agrega el rectángulo que contendrá la escala
        g.setColor(new Color(1, 1, 1, 0.8f));
        g.fillRect((int) this.anchoLienzo - largoLinea - 15, (int) this.altoLienzo - 30, largoLinea + 10, 25);

        //Se pinta la línea de escala
        g.setColor(Color.GRAY);
        g.drawLine((int) this.anchoLienzo - largoLinea - 10, (int) this.altoLienzo - 20, (int) this.anchoLienzo - 10, (int) this.altoLienzo - 20);
        g.drawLine((int) this.anchoLienzo - largoLinea - 10, (int) this.altoLienzo - 20, (int) this.anchoLienzo - largoLinea - 10, (int) this.altoLienzo - 25);
        g.drawLine((int) this.anchoLienzo - 10, (int) this.altoLienzo - 20, (int) this.anchoLienzo - 10, (int) this.altoLienzo - 25);

        //Se agrega el texto de la escala
        String textoEscala = valorEscala + " m";
        if (valorEscala >= 1000) {
            textoEscala = (valorEscala / 1000) + " km";
        }
        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
        g.drawString(textoEscala, (int) this.anchoLienzo - (largoLinea / 2) - (textoEscala.length() * 3) - 10, (int) this.altoLienzo - 9);
    }

    private void pintarLeyenda(Graphics2D g) {
        //Se definen las coordenadas del rectángulo
        int xBase = (int) this.anchoLienzo - 180;
        int yBase = 5;
        int anchoBase = 175;
        int altoBase = 250;

        //Se crea el rectángulo contenedor
        g.setColor(Color.WHITE);
        g.fillRect(xBase, yBase, anchoBase, altoBase);
        g.setStroke(new BasicStroke(2));
        g.setColor(Color.BLACK);
        g.drawRect(xBase, yBase, anchoBase, altoBase);
        g.setStroke(new BasicStroke(1));

        //Título
        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g.drawString("Densidades KDE", xBase + 10, yBase + 15);

        //Se agregan los rectángulos de colores
        for (int i = this.arrColores.length - 1; i >= 0; i--) {
            Color colorAux = this.arrColores[i];

            //Se agrega el rectángulo del color
            g.setColor(colorAux);
            g.fillRect(xBase + 10, yBase + 20 + (9 - i) * 20, 40, 20);
            g.setColor(Color.BLACK);
            g.drawRect(xBase + 10, yBase + 20 + (9 - i) * 20, 40, 20);

            //Se agrega el texto del rango
            String textoAux;
            if (i < this.arrDivisiones.length - 1) {
                double valorAux1 = 0;
                if (i > 0) {
                    valorAux1 = Math.round(this.arrDivisiones[i - 1] * 1000) / 1000.0;
                }
                double valorAux2 = Math.round(this.arrDivisiones[i] * 1000) / 1000.0;
                textoAux = "(" + valorAux1 + ", " + valorAux2 + "]";
            } else {
                double valorAux1 = Math.round(this.arrDivisiones[i - 1] * 1000) / 1000.0;
                textoAux = "(" + valorAux1 + ", ∞)";
            }
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            g.drawString(textoAux, xBase + 55, yBase + 34 + (9 - i) * 20);
        }

        //Se agrega el rectángulo de color blanco
        g.setColor(Color.WHITE);
        g.fillRect(xBase + 10, yBase + 20 + 200, 40, 20);
        g.setColor(Color.BLACK);
        g.drawRect(xBase + 10, yBase + 20 + 200, 40, 20);

        //Se agrega el texto del cero
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.drawString("0", xBase + 55, yBase + 34 + 200);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.imagenFondo = null;

        this.iniciarClic(e);
    }

    private void iniciarClic(MouseEvent e) {
        this.xIni = e.getX();
        this.yIni = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.soltarClic(e);
    }

    private void soltarClic(MouseEvent e) {
        this.isDragging = false;
        this.xFin = e.getX();
        this.yFin = e.getY();

        //Se calculan las coordenadas del nuevo centro del gráfico
        this.xCentro += (this.xFin - this.xIni);
        this.yCentro += (this.yFin - this.yIni);

        //Se pinta de nuevo el gráfico
        this.repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (this.imagenFondo == null) {
            //Se almacena la gráfica como imagen para el efecto drag and drop
            this.imagenFondo = new BufferedImage((int) this.anchoLienzo, (int) this.altoLienzo, BufferedImage.TYPE_INT_RGB);
            Graphics2D gAux = this.imagenFondo.createGraphics();
            this.paintAll(gAux);
        }

        this.moverRaton(e);

        //Se pinta el gráfico al ser arrastrado
        this.repaint();
    }

    private void moverRaton(MouseEvent e) {
        this.isDragging = true;
        this.xPar = e.getX();
        this.yPar = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        this.hacerZoom(e);
    }

    private void hacerZoom(MouseWheelEvent e) {
        this.xZoom = e.getX();
        this.yZoom = e.getY();

        //Se halla el nuevo factor de zoom y se recalculan las coordenadas del centro del gráfico
        boolean hacerZoom = false;
        int rotacion = e.getWheelRotation() * -1;
        if (rotacion < 0 && this.posEscalaZoom >= 0) {
            this.posEscalaZoom--;
            this.factorZoom = this.arrEscalasZoom[this.posEscalaZoom];
            this.xCentro += (this.xZoom - this.xCentro) * (1 - this.arrEscalasZoom[this.posEscalaZoom] / this.arrEscalasZoom[this.posEscalaZoom + 1]);
            this.yCentro += (this.yZoom - this.yCentro) * (1 - this.arrEscalasZoom[this.posEscalaZoom] / this.arrEscalasZoom[this.posEscalaZoom + 1]);
            hacerZoom = true;
        } else if (rotacion > 0 && this.posEscalaZoom <= this.arrEscalasZoom.length - 1) {
            this.posEscalaZoom++;
            this.factorZoom = this.arrEscalasZoom[this.posEscalaZoom];
            this.xCentro -= (this.xZoom - this.xCentro) * (this.arrEscalasZoom[this.posEscalaZoom] / this.arrEscalasZoom[this.posEscalaZoom - 1] - 1);
            this.yCentro -= (this.yZoom - this.yCentro) * (this.arrEscalasZoom[this.posEscalaZoom] / this.arrEscalasZoom[this.posEscalaZoom - 1] - 1);
            hacerZoom = true;
        }

        //Se pinta el gráfico con el nuevo zoom
        if (hacerZoom) {
            this.repaint();
        }
    }

    public boolean isIndVerLeyenda() {
        return indVerLeyenda;
    }

    private void drawRotate(Graphics2D g2d, double x, double y, int angle, String text) {
        g2d.translate((float) x, (float) y);
        g2d.rotate(Math.toRadians(angle));
        g2d.drawString(text, 0, 0);
        g2d.rotate(-Math.toRadians(angle));
        g2d.translate(-(float) x, -(float) y);
    }

    public void setIndVerLeyenda(boolean indVerLeyenda) {
        this.indVerLeyenda = indVerLeyenda;
    }

    public double getXCentro() {
        return xCentro;
    }

    public void setXCentro(double xCentro) {
        this.xCentro = xCentro;
    }

    public double getYCentro() {
        return yCentro;
    }

    public void setYCentro(double yCentro) {
        this.yCentro = yCentro;
    }

    public double getFactorZoom() {
        return factorZoom;
    }

    public void setFactorZoom(double factorZoom) {
        this.factorZoom = factorZoom;
    }

    public double[] getArrDivisiones() {
        return arrDivisiones;
    }

    public Color[] getArrColores() {
        return arrColores;
    }

}
