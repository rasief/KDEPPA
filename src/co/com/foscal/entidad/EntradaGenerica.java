package co.com.foscal.entidad;

import java.util.Objects;

/**
 * Clase genérica con un par llave - valor
 * @author Feisar Moreno
 * @date 30/07/2020
 */
public class EntradaGenerica {
    private String llave;
    private String valor;

    public EntradaGenerica(String llave, String valor) {
        this.llave = llave;
        this.valor = valor;
    }

    public String getLlave() {
        return llave;
    }

    public void setLlave(String llave) {
        this.llave = llave;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        return valor;
    }    

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.llave);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EntradaGenerica other = (EntradaGenerica) obj;
        return Objects.equals(this.llave, other.llave);
    }
    
}
