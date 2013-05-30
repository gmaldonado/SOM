
/**
 * Interfaz que define el cálculo de distancia entre dos vectores
 * Sigue el patrón de diseño estrategia
 * @author Gonzalo Maldonado
 */
public interface Distance {
    /**
     * Calcula la distancia entre dos puntos del espacio
     * @param item1 primer vector
     * @param item2 segundo vector
     * @return resultado del calculo de distancia
     */
    public double calculateDistance(double[] item1, double[] item2);
    
}
