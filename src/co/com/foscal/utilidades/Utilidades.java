package co.com.foscal.utilidades;

import co.com.foscal.entidad.Capa;
import co.com.foscal.entidad.CoordenadaUTM;
import co.com.foscal.entidad.EntradaGenerica;
import co.com.foscal.entidad.UnidadMedida;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.JTable;

/**
 * Clase con métodos varios estático
 *
 * @author Feisar Moreno
 * @date 06/08/2020
 */
public abstract class Utilidades {

    public static final double GEOIDE = 6372795.477598;

    /**
     * Método que retorna los caracteres a la izquierda de una cadena
     *
     * @param cadena Cadena de la que se quiere extraer la parte izquierda
     * @param longitud Número de caracteres a extraer
     * @return <code>String</code> con los caracteres a la izquierda de la
     * cadena original
     */
    public static String izquierda(String cadena, int longitud) {
        String retorno = cadena;
        if (cadena.length() > longitud) {
            retorno = cadena.substring(0, longitud);
        }

        return retorno;
    }

    /**
     * Método que retorna los caracteres a la derecha de una cadena
     *
     * @param cadena Cadena de la que se quiere extraer la parte derecha
     * @param longitud Número de caracteres a extraer
     * @return <code>String</code> con los caracteres a la derecha de la cadena
     * original
     */
    public static String derecha(String cadena, int longitud) {
        String retorno = cadena;
        int longCad = cadena.length();
        if (longCad > longitud) {
            retorno = cadena.substring(longCad - longitud);
        }

        return retorno;
    }

    /**
     * Método que calcula la distancia en metros entre dos puntos
     *
     * @param latitudIni Latitud del primer punto
     * @param longitudIni Longitud del primer punto
     * @param latitudFin Latitud del segundo punto
     * @param longitudFin Longitud del segundo punto
     * @param unidadMedida Unidad de medida del sistema de puntos
     * @param numDecimales Cantidad máxima de números decimales a tener en
     * cuenta
     * @return Distancia en metros entre los dos puntos.
     */
    public static double calcularDistanciaPuntos(double latitudIni, double longitudIni, double latitudFin, double longitudFin, UnidadMedida unidadMedida, int numDecimales) {
        double distancia;
        if (unidadMedida.getIndGrados() == 1) {
            distancia = GEOIDE * Math.acos(Math.sin(Math.toRadians(latitudIni)) * Math.sin(Math.toRadians(latitudFin)) + Math.cos(Math.toRadians(latitudIni)) * Math.cos(Math.toRadians(latitudFin)) * Math.cos(Math.toRadians(longitudIni) - Math.toRadians(longitudFin)));
        } else {
            distancia = Math.sqrt(Math.pow(latitudIni - latitudFin, 2) + Math.pow(longitudIni - longitudFin, 2)) * unidadMedida.getFactorMetros();
        }

        if (numDecimales >= 0) {
            distancia = Math.round(distancia * Math.pow(10, numDecimales)) / Math.pow(10, numDecimales);
        }

        return distancia;
    }

    /**
     * Método que calcula la distancia en metros entre dos puntos
     *
     * @param latitudIni Latitud del primer punto
     * @param longitudIni Longitud del primer punto
     * @param latitudFin Latitud del segundo punto
     * @param longitudFin Longitud del segundo punto
     * @param unidadMedida Unidad de medida del sistema de puntos
     * @return Distancia en metros entre los dos puntos.
     */
    public static double calcularDistanciaPuntos(double latitudIni, double longitudIni, double latitudFin, double longitudFin, UnidadMedida unidadMedida) {
        return calcularDistanciaPuntos(latitudIni, longitudIni, latitudFin, longitudFin, unidadMedida, -1);
    }

    /**
     * Función que transforma a coordenadas planas las coordenadas geográficas
     * recibidas con respecto a unas coordenadas base que se tomarán como el
     * punto (0, 0).
     *
     * @param latBase Latitud del punto base
     * @param lonBase Longitud del punto base
     * @param latPunto Latitud del punto a transformar
     * @param lonPunto Longitud del punto a transformar
     * @param unidadMedida Unidad de medidad de la red
     * @return Array con las coordenadas planas ([0] latitud - [1] Longitud)
     * correspondientes a las coordenadas geográficas recibidas
     */
    public static double[] coordenadasGeograficasAPlanas(double latBase, double lonBase, double latPunto, double lonPunto, UnidadMedida unidadMedida) {
        double[] coordenadas = new double[2];

        coordenadas[0] = calcularDistanciaPuntos(latPunto, lonPunto, latBase, lonPunto, unidadMedida);
        if (latBase < latPunto) {
            coordenadas[0] = coordenadas[0] * -1;
        }

        coordenadas[1] = calcularDistanciaPuntos(latPunto, lonPunto, latPunto, lonBase, unidadMedida);
        if (lonBase < lonPunto) {
            coordenadas[1] = coordenadas[1] * -1;
        }

        return coordenadas;
    }

    /**
     * Método que valida si los valores aplicados a una tabla de filtros son
     * correctos.
     *
     * @date 25/05/2016
     * @param tablaAtributos Tabla que contiene los atributos
     * @return Cadena de texto nula (null) si no se encontraron errores, de lo
     * contrario se entrega una cadena de texto con el error hallado.
     */
    public static String validarFiltros(JTable tablaAtributos) {
        for (int i = 0; i < tablaAtributos.getRowCount(); i++) {
            String filtroAux = tablaAtributos.getValueAt(i, 2).toString();
            if (!filtroAux.equals("")) {
                String nombreAtributo = tablaAtributos.getValueAt(i, 0).toString();
                String tipoAtributo = tablaAtributos.getValueAt(i, 1).toString();

                //Se quita el símbolo de negación si existe
                if (filtroAux.substring(0, 1).equals("!")) {
                    filtroAux = filtroAux.substring(1);
                }

                //Se valida si se trata de varios datos separados por coma o un rango de valores
                int posComa = filtroAux.indexOf(",");
                int posNumeral = filtroAux.indexOf("#");

                if (posComa >= 0 && posNumeral >= 0) {
                    return "Unable to combine individual values and ranges of values in the filter [" + nombreAtributo + "].";
                }

                List<String> listaValoresAux = new ArrayList<>();
                if (posComa >= 0) {
                    //Lista de valores
                    listaValoresAux = Arrays.asList(filtroAux.split(","));
                } else if (posNumeral >= 0) {
                    //Rango de valores
                    listaValoresAux = Arrays.asList(filtroAux.split("#"));
                } else {
                    //Valor individual
                    listaValoresAux.add(filtroAux);
                }

                //Se valida que cada uno de los componentes de la lista correspondan al tipo de dato del filtro
                for (String valorAux : listaValoresAux) {
                    valorAux = valorAux.trim();

                    //Se validan los campos numéricos
                    try {
                        switch (tipoAtributo.toLowerCase()) {
                            case "int":
                                Integer.parseInt(valorAux);
                                break;
                            case "float":
                                Float.parseFloat(valorAux);
                                break;
                            case "double":
                                Double.parseDouble(valorAux);
                                break;
                        }
                    } catch (NumberFormatException e) {
                        return "Filter [" + nombreAtributo + "] contains values not corresponding to type [" + tipoAtributo + "].";
                    }

                    //Se validan los campos de fecha y hora
                    if (tipoAtributo.equals("dd/mm/yyyy") || tipoAtributo.equals("hh:mm")) {
                        boolean indFechaIncorrecta = false;
                        Date fechaAux = null;
                        String formatoAux = "dd/MM/yyyy";
                        if (tipoAtributo.equals("hh:mm")) {
                            formatoAux = "HH:mm";
                        }
                        try {
                            //Se validan los campos de fecha
                            fechaAux = new SimpleDateFormat(formatoAux).parse(valorAux);
                        } catch (ParseException ex) {
                            indFechaIncorrecta = true;
                        }

                        if (fechaAux == null) {
                            indFechaIncorrecta = true;
                        }

                        if (indFechaIncorrecta) {
                            return "Filter [" + nombreAtributo + "] contains values not corresponding to type [" + tipoAtributo + "].";
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Esta función halla la proyección de una coordenada de latitud en
     * coordenadas geográficas con respecto a un punto base que en la proyección
     * se toma como (0, 0).
     *
     * @param latitud Latitud a convertir
     * @param latitudPunto Latitud del punto base
     * @param longitudPunto Longitud del punto base
     * @param unidadMedida Unidad de medida
     * @param numDecimales Número de decimales a tomar en cuenta
     * @return Latitud transformada a coordenadas planas
     */
    public static double transformarLatitudPlana(double latitud, double latitudPunto, double longitudPunto, UnidadMedida unidadMedida, int numDecimales) {
        double latitudConv = calcularDistanciaPuntos(latitudPunto, longitudPunto, latitud, longitudPunto, unidadMedida, numDecimales);
        if (latitud < latitudPunto) {
            latitudConv *= -1;
        }

        return latitudConv;
    }

    /**
     * Esta función halla la proyección de una coordenada de longitud en
     * coordenadas geográficas con respecto a un punto base que en la proyección
     * se toma como (0, 0).
     *
     * @param longitud Longitud a convertir
     * @param latitudPunto Latitud del punto base
     * @param longitudPunto Longitud del punto base
     * @param unidadMedida Unidad de medida
     * @param numDecimales Número de decimales a tomar en cuenta
     * @return Longitud transformada a coordenadas planas
     */
    public static double transformarLongitudPlana(double longitud, double latitudPunto, double longitudPunto, UnidadMedida unidadMedida, int numDecimales) {
        double longitudConv = calcularDistanciaPuntos(latitudPunto, longitudPunto, latitudPunto, longitud, unidadMedida, numDecimales);
        if (longitud < longitudPunto) {
            longitudConv *= -1;
        }

        return longitudConv;
    }

    /**
     * Función que convierte una coordenada geográfica al sistema de coordenadas
     * UTM
     *
     * @param latitud Latitud del punto a transformar
     * @param longitud Longitud del punto a transformar
     * @return Coordena da en el sistema UTM
     */
    public static CoordenadaUTM convertirGeograficasAUTM(double latitud, double longitud) {
        //Se calculan los valores base
        double latitudRad = Math.toRadians(latitud);
        double longitudRad = Math.toRadians(longitud);
        double k0 = 0.9996;
        double x0 = 500;
        double y0 = latitudRad >= 0 ? 0 : 10000;
        double f = 1 / 298.257223563;
        double n = f / (2 - f);
        double a = 6378.137;
        double aa = (a / (1 + n)) * (1 + Math.pow(n, 2) / 4 + Math.pow(n, 4) / 64 + Math.pow(n, 6) / 256 + Math.pow(n, 8) * 25 / 16384 + Math.pow(n, 10) * 49 / 65536);
        double a1 = n / 2 - Math.pow(n, 2) * 2 / 3 + Math.pow(n, 3) * 5 / 16;
        double a2 = Math.pow(n, 2) * 13 / 48 - Math.pow(n, 3) * 3 / 5;
        double a3 = Math.pow(n, 3) * 61 / 240;

        //Se calculan los valores intermedios
        double long0 = Math.toRadians(Math.floor(longitud / 6) * 6 + 3);
        double t = Math.sinh(atanh(Math.sin(latitudRad)) - (2 * Math.sqrt(n) / (1 + n)) * atanh((2 * Math.sqrt(n) / (1 + n)) * Math.sin(latitudRad)));
        double ep = Math.atan(t / Math.cos(longitudRad - long0));
        double np = atanh(Math.sin(longitudRad - long0) / Math.sqrt(1 + Math.pow(t, 2)));

        //Se calculan los valores de las coordenadas
        double x = 1000 * (x0 + k0 * aa * (np + a1 * Math.cos(2 * ep) * Math.sinh(2 * np) + a2 * Math.cos(4 * ep) * Math.sinh(4 * np) + a3 * Math.cos(6 * ep) * Math.sinh(6 * np)));
        double y = 1000 * (y0 + k0 * aa * (ep + a1 * Math.sin(2 * ep) * Math.cosh(2 * np) + a2 * Math.sin(4 * ep) * Math.cosh(4 * np) + a3 * Math.sin(6 * ep) * Math.cosh(6 * np)));

        int zona = (int) (Math.floor((longitud + 180) / 6) + 1);

        char hemisferio = latitud >= 0 ? 'N' : 'S';

        return new CoordenadaUTM(x, y, zona, hemisferio);
    }

    /**
     * Función que convierte una coordenada UTM al sistema de coordenadas
     * geográficas
     *
     * @param coordenadaUTM Objeto de coordenada UTM
     * @return arreglo de dos posiciones con la latitud y la longitud
     */
    public static double[] convertirUTMAGeograficas(CoordenadaUTM coordenadaUTM) {
        //Se calculan los valores base
        double x = coordenadaUTM.getX() / 1000;
        double y = coordenadaUTM.getY() / 1000;
        double k0 = 0.9996;
        double x0 = 500;
        double y0 = coordenadaUTM.getHemisferio() == 'N' ? 0 : 10000;
        double f = 1 / 298.257223563;
        double n = f / (2 - f);
        double a = 6378.137;
        double aa = (a / (1 + n)) * (1 + Math.pow(n, 2) / 4 + Math.pow(n, 4) / 64 + Math.pow(n, 6) / 256 + Math.pow(n, 8) * 25 / 16384 + Math.pow(n, 10) * 49 / 65536);
        double b1 = n / 2 - Math.pow(n, 2) * 2 / 3 + Math.pow(n, 3) * 37 / 96;
        double b2 = Math.pow(n, 2) / 48 + Math.pow(n, 3) / 15;
        double b3 = Math.pow(n, 3) * 17 / 480;
        double d1 = n * 2 - Math.pow(n, 2) * 2 / 3 - Math.pow(n, 3) * 2;
        double d2 = Math.pow(n, 2) * 7 / 3 - Math.pow(n, 3) * 8 / 5;
        double d3 = Math.pow(n, 3) * 56 / 15;

        //Se calculan los valores intermedios
        double long0 = coordenadaUTM.getZona() * 6 - 183;
        double e = (y - y0) / (k0 * aa);
        double nn = (x - x0) / (k0 * aa);
        double ep = e - b1 * Math.sin(2 * e) * Math.cosh(2 * nn) - b2 * Math.sin(4 * e) * Math.cosh(4 * nn) - b3 * Math.sin(6 * e) * Math.cosh(6 * nn);
        double np = nn - b1 * Math.cos(2 * e) * Math.sinh(2 * nn) - b2 * Math.cos(4 * e) * Math.sinh(4 * nn) - b3 * Math.cos(6 * e) * Math.sinh(6 * nn);
        double xx = Math.asin(Math.sin(ep) / Math.cosh(np));

        //Se calculan los valores de las coordenadas
        double latitud = Math.toDegrees(xx + d1 * Math.sin(2 * xx) + d2 * Math.sin(4 * xx) + d3 * Math.sin(6 * xx));
        double longitud = long0 + Math.toDegrees(Math.atan(Math.sinh(np) / Math.cos(ep)));

        double[] resultado = new double[2];
        resultado[0] = latitud;
        resultado[1] = longitud;

        return resultado;
    }

    private static double atanh(double valor) {
        return (Math.log(1 + valor) - Math.log(1 - valor)) / 2;
    }

    /**
     * Método que obtien las coordenadas de los extremos de las capas
     * seleccionadas
     *
     * @param datosCapaEventos Datos de la capa de eventos
     * @param mapaCapasKML Mapa con todas las capas
     * @param anchoBanda Andcho de banda de cálculo para el método KDE
     * @return array con las cuatro coordenadas de los extremos
     */
    public static double[] buscarCoordenadasExtremos(EntradaGenerica datosCapaEventos, LinkedHashMap<String, Capa> mapaCapasKML, double anchoBanda) {
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        
        if (datosCapaEventos != null) {
            Capa capaAux = mapaCapasKML.get(datosCapaEventos.getLlave());
            
            if (capaAux.getMinX() < minX) {
                minX = capaAux.getMinX();
            }
            if (capaAux.getMinY() < minY) {
                minY = capaAux.getMinY();
            }
            if (capaAux.getMaxX() > maxX) {
                maxX = capaAux.getMaxX();
            }
            if (capaAux.getMaxY() > maxY) {
                maxY = capaAux.getMaxY();
            }
        } else {
            for (Map.Entry<String, Capa> entradaAux : mapaCapasKML.entrySet()) {
                Capa capaAux = entradaAux.getValue();
                
                if (capaAux.getMinX() < minX) {
                    minX = capaAux.getMinX();
                }
                if (capaAux.getMinY() < minY) {
                    minY = capaAux.getMinY();
                }
                if (capaAux.getMaxX() > maxX) {
                    maxX = capaAux.getMaxX();
                }
                if (capaAux.getMaxY() > maxY) {
                    maxY = capaAux.getMaxY();
                }
            }
        }
        
        double[] arrExtremos = new double[4];
        arrExtremos[0] = minX - anchoBanda;
        arrExtremos[1] = minY - anchoBanda;
        arrExtremos[2] = maxX + anchoBanda;
        arrExtremos[3] = maxY + anchoBanda;

        return arrExtremos;
    }

    /**
     * Método que obtiene la ruta base de búsqueda de archivos
     *
     * @return Texto con la ruta base
     */
    public static String obtenerRutaBase() {
        Properties propiedades = new Properties();
        String rutaBase = "";

        try {
            FileInputStream is = new FileInputStream("KDEPPA.properties");
            propiedades.load(is);

            for (Enumeration e = propiedades.keys(); e.hasMoreElements();) {
                String key = (String) e.nextElement();
                if (key.equals("ruta_base")) {
                    rutaBase = propiedades.getProperty(key);
                }
            }
        } catch (IOException ex) {
            rutaBase = "";
        }

        return rutaBase;
    }

    /**
     * Método que almacena una ruta base en el archivo de propiedades
     *
     * @param rutaBase Ruta a guardar
     * @return <code>true</code> si se realizó el proceso de guardado, de lo
     * contrario <code>false</code>.
     */
    public static boolean guardarRutaBase(String rutaBase) {
        boolean resultado;
        try {
            Properties props = new Properties();
            props.setProperty("ruta_base", rutaBase);
            File f = new File("KDEPPA.properties");
            FileOutputStream out = new FileOutputStream(f);
            props.store(out, "Ruta de carga KML");

            resultado = true;
        } catch (IOException ex) {
            resultado = false;
        }

        return resultado;
    }

}
