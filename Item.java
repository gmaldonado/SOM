

import java.io.Serializable;

/**
 * Representa un vector d-dimensional, que puede ser un vector de 
 * pesos o un vector de instancias para utilizar en el SOM.
 * @author Gonzalo Maldonado
 */
public class Item implements Serializable{
    
    private double[] item;
    
    public Item(int dimension){
        this.item = new double[dimension];
    }

    public Item(double[] item){
        this.item = item;
    }
    
    public double[] getItem() {
        return item;
    }

    public void setItem(double[] item) {
        this.item = item;
    }
    
}
