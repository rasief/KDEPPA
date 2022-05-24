package co.com.foscal.utilidades;

import java.awt.Color;

/**
 * Clase para el manejo y creación de gamas de colores
 * @author Feisar Moreno
 * @date frmPrincipal
 */
public class ManejadorColor {
    /**
     * Método que convierte un color HSV en un color RGB
     * @param h Componente matriz del color
     * @param s Componente saturación del color
     * @param v Componente valor del color
     * @return Array con los valores R, G y B en formato hexadecimal
     */
    public String[] hsvTorgb(int h, int s, int v) {
        int hi = (int)Math.floor(h / 60) % 6;
        float f = (h / 60.0f) - hi;
        float p = v * (100 - s) / 100.0f;
        float q = v * (100 - f * s) / 100.0f;
        float t = v * (100 - (1 - f) * s) / 100.0f;
        
        float rn = 0;
        float gn = 0;
        float bn = 0;
        switch (hi) {
            case 0:
                rn = v;
                gn = t;
                bn = p;
                break;
            case 1:
                rn = q;
                gn = v;
                bn = p;
                break;
            case 2:
                rn = p;
                gn = v;
                bn = t;
                break;
            case 3:
                rn = p;
                gn = q;
                bn = v;
                break;
            case 4:
                rn = t;
                gn = p;
                bn = v;
                break;
            case 5:
                rn = v;
                gn = p;
                bn = q;
                break;
        }
        
        String r = Long.toHexString(Math.round(rn * 2.55)).toUpperCase();
        String g = Long.toHexString(Math.round(gn * 2.55)).toUpperCase();
        String b = Long.toHexString(Math.round(bn * 2.55)).toUpperCase();
        if (r.length() < 2) {
            r = "0" + r;
        }
        if (g.length() < 2) {
            g = "0" + g;
        }
        if (b.length() < 2) {
            b = "0" + b;
        }
        String[] resultado = {r, g, b};
        
        return resultado;
    }
    
    /**
     * Método que convierte un color RGB en un color HSV
     * @param r Componente rojo del color
     * @param g Componente verde del color
     * @param b Componente azul del color
     * @return Array con los valores H, S y V
     */
    public int[] rgbTohsv(String r, String g, String b) {
        float rn = Integer.parseInt(r, 16);
        float gn = Integer.parseInt(g, 16);
        float bn = Integer.parseInt(b, 16);
        float max = this.maximoRgb((int)rn, (int)gn, (int)bn);
        float min = this.minimoRgb((int)rn, (int)gn, (int)bn);
        
        int h;
        int s;
        int v;
        
        if (max == min) {
            h = 0;
        } else if (max == rn && gn >= bn) {
            h = Math.round(60 * ((gn - bn) / (max - min)));
        } else if (max == rn && gn < bn) {
            h = Math.round(60 * ((gn - bn) / (max - min))) + 360;
        } else if (max == gn) {
            h = Math.round(60 * ((bn - rn) / (max - min))) + 120;
        } else if (max == bn) {
            h = Math.round(60 * ((rn - gn) / (max - min))) + 240;
        } else {
            h = 0;
        }
        
        if (max == 0) {
            s = 0;
        } else {
            s = Math.round((1 - min / max) * 100);
        }
        
        v = (int)Math.round(max / 2.55);
        
        int[] resultado = {h, s, v};
        
        return resultado;
    }
    
    private int maximoRgb(int r, int g, int b) {
        int maximo = r;
        if (g > maximo) {
            maximo = g;
        }
        if (b > maximo) {
            maximo = b;
        }
        
        return maximo;
    }
    
    private int minimoRgb(int r, int g, int b) {
        int minimo = r;
        if (g < minimo) {
            minimo = g;
        }
        if (b < minimo) {
            minimo = b;
        }
        
        return minimo;
    }
    
    /**
     * Método que retorna el listado de colores RGB que se encuentran entre los dos colores dados
     * @param rgbIni Color RGB inicial
     * @param rgbFin Color RGB final
     * @param cantidad Cantidad de colores a generar
     * @param blancoCentral Indicador de color blanco en el centro de los colores a generar
     * @return Arreglo con los colores generardos
     */
    public String[] obtenerEscala(String rgbIni, String rgbFin, int cantidad, boolean blancoCentral) {
        String ri = rgbIni.substring(0, 2);
        String gi = rgbIni.substring(2, 4);
        String bi = rgbIni.substring(4);
        String rf = rgbFin.substring(0, 2);
        String gf = rgbFin.substring(2, 4);
        String bf = rgbFin.substring(4);
        
        int[] arrHSV = this.rgbTohsv(ri, gi, bi);
        int hi = arrHSV[0];
        int si = arrHSV[1];
        int vi = arrHSV[2];
        
        arrHSV = this.rgbTohsv(rf, gf, bf);
        int hf = arrHSV[0];
        int sf = arrHSV[1];
        int vf = arrHSV[2];
        
        String[] arrResultado = new String[cantidad];
        if (!blancoCentral) {
            float ph = (hf - hi) / (cantidad - 1.0f);
            float ps = (sf - si) / (cantidad - 1.0f);
            float pv = (vf - vi) / (cantidad - 1.0f);
            
            for (int i = 0; i < cantidad; i++) {
                int hAux = hi + Math.round(i * ph);
                int sAux = si + Math.round(i * ps);
                int vAux = vi + Math.round(i * pv);
                String[] arrRGB = this.hsvTorgb(hAux, sAux, vAux);
                
                arrResultado[i] = arrRGB[0] + arrRGB[1] + arrRGB[2];
            }
        } else {
            int cantidad1 = Math.round(cantidad / 2.0f);
            int cantidad2 = cantidad - cantidad1;
            
            float ph = 0;
            float ps = (0 - si) / (cantidad1 - 1.0f);
            float pv = (100 - vi) / (cantidad1 - 1.0f);
            
            for (int i = 0; i < cantidad1; i++) {
                int hAux = hi;
                int sAux = si + Math.round(i * ps);
                int vAux = vi + Math.round(i * pv);
                String[] arrRGB = this.hsvTorgb(hAux, sAux, vAux);
                
                arrResultado[i] = arrRGB[0] + arrRGB[1] + arrRGB[2];
            }
            
            ph = 0;
            ps = (sf - 0.0f) / (cantidad2);
            pv = (vf - 100.0f) / (cantidad2);
            
            for (int i = 0; i <= cantidad2; i++) {
                int hAux = hf;
                int sAux = 0 + Math.round(i * ps);
                int vAux = 100 + Math.round(i * pv);
                String[] arrRGB = this.hsvTorgb(hAux, sAux, vAux);
                
                if (i > 0) {
                    arrResultado[i - 1 + cantidad1] = arrRGB[0] + arrRGB[1] + arrRGB[2];
                }
            }
        }
        
        return arrResultado;
    }
    
    /**
     * Método que retorna el listado de colores RGB que se encuentran entre los dos colores dados
     * @param rgbIni Color RGB inicial
     * @param rgbFin Color RGB final
     * @param cantidad Cantidad de colores a generar
     * @param blancoCentral Indicador de color blanco en el centro de los colores a generar
     * @return Arreglo con los colores generardos
     */
    public Color[] obtenerEscalaColores(String rgbIni, String rgbFin, int cantidad, boolean blancoCentral) {
        String[] arrColoresTexto = this.obtenerEscala(rgbIni, rgbFin, cantidad, blancoCentral);
        
        Color[] arrColores = new Color[arrColoresTexto.length];
        for (int i = 0; i < arrColoresTexto.length; i++) {
            String colorAux = arrColoresTexto[i];
            int rAux = Integer.valueOf(colorAux.substring(0, 2), 16);
            int gAux = Integer.valueOf(colorAux.substring(2, 4), 16);
            int bAux = Integer.valueOf(colorAux.substring(4), 16);

            arrColores[i] = new Color(rAux, gAux, bAux);
        }
        
        return arrColores;
    }
    
}
