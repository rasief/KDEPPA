package co.com.foscal.utilidades;

import java.util.ArrayList;

/**
 * Clase para el manejo y creación de distribuciones de grupos de datos
 * @author Feisar Moreno
 * @date frmPrincipal
 */
public class CalculadorDistribucion {
    public double[] ordenarArreglo(double[] listaDatos) {
        return this.quicksort(listaDatos, 0, listaDatos.length - 1);
    }
    
    public ArrayList<Double> ordenarLista(ArrayList<Double> listaDatos) {
        return this.quicksort(listaDatos, 0, listaDatos.size() - 1);
    }
    
    public double[] obtenerCuartiles(double[] listaDatos) {
        //Se obtiene la posición del primer valor que no es cero
        int posIni = 0;
        if (listaDatos[0] == 0) {
            for (int i = 0; i < listaDatos.length; i++) {
                if (listaDatos[i] > 0) {
                    posIni = i;
                    break;
                }
            }
        }
        
        //Se obtienen los cuartiles
        int cantidad = listaDatos.length - posIni;
        double[] q = new double[3];
        double[] nq = new double[3];
        
        nq[0] = (cantidad + 1) / 4 - 1;
	if (nq[0] < 0) {
            nq[0] = 0;
        }
        nq[0] += posIni;
        
        nq[1] = (cantidad + 1) / 2 - 1;
        if (nq[1] < 0) {
            nq[1] = 0;
        }
        nq[1] += posIni;
        
        nq[2] = 3 * (cantidad + 1) / 4 - 1;
        if (nq[2] < 0) {
            nq[2] = 0;
        }
        nq[2] += posIni;
        if (nq[2] > listaDatos.length - 1) {
            nq[2] = listaDatos.length - 1;
        }
        
        q[0] = this.obtenerValorPosicion(listaDatos, nq[0]);
        q[1] = this.obtenerValorPosicion(listaDatos, nq[1]);
        q[2] = this.obtenerValorPosicion(listaDatos, nq[2]);
        
        return q;
    }
    
    public double[] obtenerDeciles(double[] listaDatos) {
        //Se obtiene la posición del primer valor que no es cero
        int posIni = 0;
        if (listaDatos[0] == 0) {
            for (int i = 0; i < listaDatos.length; i++) {
                if (listaDatos[i] > 0) {
                    posIni = i;
                    break;
                }
            }
        }
        
        //Se obtienen los deciles
        int cantidad = listaDatos.length - posIni;
        double[] d = new double[9];
        for (int i = 1; i < 10; i++) {
            int posAux = (int)Math.round(i * cantidad / 10 + 0.5) - 1;
            if (posAux < 0) {
                posAux = 0;
            }
            posAux += posIni;
            if (posAux > listaDatos.length - 1) {
                posAux = listaDatos.length - 1;
            }
            
            d[i - 1] = listaDatos[posAux];
        }
        
        return d;
    }
    
    public double[] obtenerDeciles(ArrayList<Double> listaDatos) {
        //Se obtiene la posición del primer valor que no es cero
        int posIni = 0;
        if (listaDatos.get(0) == 0) {
            for (int i = 0; i < listaDatos.size(); i++) {
                if (listaDatos.get(i) > 0) {
                    posIni = i;
                    break;
                }
            }
        }
        
        //Se obtienen los deciles
        int cantidad = listaDatos.size() - posIni;
        double[] d = new double[9];
        for (int i = 1; i < 10; i++) {
            int posAux = (int)Math.round(i * cantidad / 10 + 0.5) - 1;
            if (posAux < 0) {
                posAux = 0;
            }
            posAux += posIni;
            if (posAux > listaDatos.size() - 1) {
                posAux = listaDatos.size() - 1;
            }
            
            d[i - 1] = listaDatos.get(posAux);
        }
        
        return d;
    }
    
    public double[] obtenerDivisiones(ArrayList<Double> listaDatos, int cantDivisiones) {
        //Se obtiene la posición del primer valor que no es cero
        int posIni = 0;
        if (listaDatos.get(0) == 0) {
            for (int i = 0; i < listaDatos.size(); i++) {
                if (listaDatos.get(i) > 0) {
                    posIni = i;
                    break;
                }
            }
        }
        
        //Se obtienen los valores de las divisiones
        double[] d = new double[cantDivisiones];
        double valorMin = listaDatos.get(posIni);
        double valorMax = listaDatos.get(listaDatos.size() - 1);
        for (int i = 0; i < cantDivisiones; i++) {
            d[i] = (i + 1) * (valorMax - valorMin) / cantDivisiones;
        }
        
        return d;
    }
    
    public double[] obtenerPercentiles(double[] listaDatos) {
        //Se obtiene la posición del primer valor que no es cero
        int posIni = 0;
        if (listaDatos[0] == 0) {
            for (int i = 0; i < listaDatos.length; i++) {
                if (listaDatos[i] > 0) {
                    posIni = i;
                    break;
                }
            }
        }
        
        //Se obtienen los percentiles
        int cantidad = listaDatos.length - posIni;
        double[] p = new double[99];
        for (int i = 1; i < 100; i++) {
            int posAux = (int)Math.round(i * cantidad / 100 + 0.5) - 1;
            if (posAux < 0) {
                posAux = 0;
            }
            posAux += posIni;
            if (posAux > listaDatos.length - 1) {
                posAux = listaDatos.length - 1;
            }
            
            p[i - 1] = listaDatos[posAux];
        }
        
        return p;
    }
    
    public double obtenerPercentil(double[] listaDatos, int percentil) {
        if (percentil > 0 && percentil < 100) {
            //Se obtiene la posición del primer valor que no es cero
            int posIni = 0;
            if (listaDatos[0] == 0) {
                for (int i = 0; i < listaDatos.length; i++) {
                    if (listaDatos[i] > 0) {
                        posIni = i;
                        break;
                    }
                }
            }
            
            //Se obtienen los percentiles
            int cantidad = listaDatos.length - posIni;
            double[] p = new double[99];
            for (int i = 1; i < 100; i++) {
                int posAux = (int)Math.round(i * cantidad / 100 + 0.5) - 1;
                if (posAux < 0) {
                    posAux = 0;
                }
                posAux += posIni;
                if (posAux > listaDatos.length - 1) {
                    posAux = listaDatos.length - 1;
                }
                
                p[i - 1] = listaDatos[posAux];
            }
            
            return p[percentil - 1];
        } else if (percentil >= 100) {
            return Double.POSITIVE_INFINITY;
        } else {
            return Double.NEGATIVE_INFINITY;
        }
    }
    
    public double obtenerPercentil(ArrayList<Double> listaDatos, int percentil) {
        if (percentil > 0 && percentil < 100) {
            //Se obtiene la posición del primer valor que no es cero
            int posIni = 0;
            if (listaDatos.get(0) == 0) {
                for (int i = 0; i < listaDatos.size(); i++) {
                    if (listaDatos.get(i) > 0) {
                        posIni = i;
                        break;
                    }
                }
            }
            
            //Se obtienen los percentiles
            int cantidad = listaDatos.size() - posIni;
            double[] p = new double[99];
            for (int i = 1; i < 100; i++) {
                int posAux = (int)Math.round(i * cantidad / 100 + 0.5) - 1;
                if (posAux < 0) {
                    posAux = 0;
                }
                posAux += posIni;
                if (posAux > listaDatos.size() - 1) {
                    posAux = listaDatos.size() - 1;
                }
                
                p[i - 1] = listaDatos.get(posAux);
            }
            
            return p[percentil - 1];
        } else if (percentil >= 100) {
            return Double.POSITIVE_INFINITY;
        } else {
            return Double.NEGATIVE_INFINITY;
        }
    }
    
    public double obtenerMinimo(double[] listaDatos) {
        double resultado = 0;
        if (listaDatos.length > 0) {
            boolean inicial = true;
            for (double datoAux : listaDatos) {
                if (datoAux < resultado || inicial) {
                    resultado = datoAux;
                    inicial = false;
                }
            }
        }
        
        return resultado;
    }
    
    public double obtenerMinimoSinCeros(double[] listaDatos) {
        double resultado = 0;
        if (listaDatos.length > 0) {
            boolean inicial = true;
            for (double datoAux : listaDatos) {
                if (datoAux != 0 && (datoAux < resultado || inicial)) {
                    resultado = datoAux;
                    inicial = false;
                }
            }
        }
        
        return resultado;
    }
    
    public double obtenerMaximo(double[] listaDatos) {
        double resultado = 0;
        if (listaDatos.length > 0) {
            boolean inicial = true;
            for (double datoAux : listaDatos) {
                if (datoAux > resultado || inicial) {
                    resultado = datoAux;
                    inicial = false;
                }
            }
        }
        
        return resultado;
    }
    
    private double obtenerValorPosicion(double[] arrDatos, double posicion) {
        double resultado;
        if (arrDatos.length == 1 || posicion < 0) {
            resultado = arrDatos[0];
        } else {
            int posEntero = (int)Math.floor(posicion);
            double posFraccion = posicion - posEntero;
            if (posicion == posEntero) {
                resultado = arrDatos[posEntero];
            } else {
                double fracAux = (arrDatos[posEntero + 1] - arrDatos[posEntero]) * posFraccion;
                resultado = arrDatos[posEntero] + fracAux;
            }
        }
        
        return resultado;
    }
    
    private double[] quicksort(double[] arr, int izq, int der) {
        int i = izq;
        int j = der;
        double x = arr[(int)Math.floor((izq + der) / 2)];
        do {
            while (arr[i] < x && j <= der) {
                i++;
            }
            while (x < arr[j] && j > izq) {
                j--;
            }
            if (i <= j) {
                double aux = arr[i];
                arr[i] = arr[j];
                arr[j] = aux;
                i++;
                j--;
            }
        } while (i <= j);
        
        if (izq < j) {
            arr = this.quicksort(arr, izq, j);
        }
        if (i < der) {
            arr = this.quicksort(arr, i, der);
        }
        
        return arr;
    }
    
    private ArrayList<Double> quicksort(ArrayList<Double> lista, int izq, int der) {
        int i = izq;
        int j = der;
        double x = lista.get((int)Math.floor((izq + der) / 2));
        do {
            while (lista.get(i) < x && j <= der) {
                i++;
            }
            while (x < lista.get(j) && j > izq) {
                j--;
            }
            if (i <= j) {
                double aux = lista.get(i);
                lista.set(i, lista.get(j));
                lista.set(j, aux);
                i++;
                j--;
            }
        } while (i <= j);
        
        if (izq < j) {
            lista = this.quicksort(lista, izq, j);
        }
        if (i < der) {
            lista = this.quicksort(lista, i, der);
        }
        
        return lista;
    }
}
