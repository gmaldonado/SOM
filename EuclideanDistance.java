


/**
 * Implementación de la distancia Euclideana entre dos vectores 
 * @author Gonzalo Maldonado
 */
public class EuclideanDistance implements Distance{

    @Override
    public double calculateDistance(double[] item1, double[] item2) {
        double distance = 0;
        for(int i=0;i<item1.length;i++){
            distance += Math.pow((item1[i] - item2[i]),2);
        }
        return Math.sqrt(distance);
    }
  
}
