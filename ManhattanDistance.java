



/**
 * Implementación de la distancia de Manhattan entre dos vectores 
 * @author Gonzalo Maldonado
 */
public class ManhattanDistance implements Distance {

    @Override
    public double calculateDistance(double item1[],double item2[]) {
        double distance = 0;
        for(int i=0;i<item1.length;i++){
            distance += Math.abs(item1[i]-item2[i]);
        }
        
        return distance;
    }
    
}
