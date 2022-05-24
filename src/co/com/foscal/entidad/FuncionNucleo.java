package co.com.foscal.entidad;

/**
 * Clase que representa un registro de función de núcleo
 * @author Feisar Moreno
 * @date 30/07/2020
 */
public class FuncionNucleo {
    private int idFuncion;
    private String nombreFuncion;
    
    public FuncionNucleo() {
    }
    
    public FuncionNucleo(int idFuncion, String nombreFuncion) {
        this.idFuncion = idFuncion;
        this.nombreFuncion = nombreFuncion;
    }
    
    public int getIdFuncion() {
        return idFuncion;
    }
    
    public void setIdFuncion(int idFuncion) {
        this.idFuncion = idFuncion;
    }
    
    public String getNombreFuncion() {
        return nombreFuncion;
    }
    
    public void setNombreFuncion(String nombreFuncion) {
        this.nombreFuncion = nombreFuncion;
    }
    
    @Override
    public String toString() {
        return this.nombreFuncion;
    }
}
