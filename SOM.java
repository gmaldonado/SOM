


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class SOM {
    
    private static int rows; //m1
    private static int columns; //m2
    private static double initialRadius; //r0
    private static double finalRadius; //rf
    private static double radius;
    private static double learningRate;
    private static double iterations;
    private static double initialLearningRate; //a0
    private static double finalLearningRate; //af
    private static String filePath;
    private static ArrayList<Item> dataSet;
    private static int instancesCount;
    private static int dimensionality;
    private static Distance distance;
    private static int rowBmu;
    private static int columnBmu;
    private static Item[][] lattice;
    private static Item bmu;
    private static String readSerializePath;
    private static String writeSerializePath;
    private static int distanceType;
    private static final String INPUT = "-i";
    private static final String OUTPUT = "-o";
        
    public static void main(String[] args) {
        
        try{
            filePath = args[0];
            iterations = Double.parseDouble(args[1]);
            rows = Integer.parseInt(args[2]);
            columns = Integer.parseInt(args[3]);
            initialRadius = Double.parseDouble(args[4]);
            finalRadius = Double.parseDouble(args[5]);
            initialLearningRate = Double.parseDouble(args[6]);
            finalLearningRate = Double.parseDouble(args[7]);
            distanceType = Integer.parseInt(args[8]);   
        }
        catch(Exception exception){
            System.out.println("How to use: PATH_DATASET ITERATIONS ROWS_LATTICE COLUMNS_LATTICE"
                              +"INITIAL_RADIUS FINAL_RADIUS INITIAL_LEARNING_RATE FINAL_LEARNING_RATE"
                              + "DISTANCE_TYPE SERIALIZE_FROM(optional) SERIALIZE_TO(optional)");
            System.exit(0);
        }

        String firstOption="";
        String pathFirstOption="";
        String secondOption="";
        String pathSecondOption="";
        try{
            firstOption = args[9];
            try{
                pathFirstOption = args[10];
            }
            catch(Exception exception){
                System.out.println("Error, mal escrita la ruta");
                System.exit(0);
            }
            if(firstOption.equals(INPUT)){
                readSerializePath = pathFirstOption;
            }
            else if(firstOption.equals(OUTPUT)){
                writeSerializePath = pathFirstOption;
            }
            try{
               secondOption = args[11]; 
               try{
                   pathSecondOption = args[12];
               }
               catch(Exception exception){
                   System.out.println("Error, mal escrita la ruta");
                   System.exit(0);
               }
               if(secondOption.equals(INPUT)){
                   readSerializePath = pathSecondOption;
               }
               else if(secondOption.equals(OUTPUT)){
                   writeSerializePath = pathSecondOption;
               }
            }
            catch(Exception exception){
                //System.out.println("Hay primera pero no segunda");
            }
        }
        catch(Exception exception){
            //System.out.println("No hay lectura ni escritura");
        }

        if(readSerializePath == null){
            lattice = new Item[rows][columns];
        }
        else{
            readSerializableObject(readSerializePath);
            System.out.println("This is the lattice that you loaded!");
            printWeightsMatrix();
        }
        

        switch(distanceType){
            case 0:
                distance = new EuclideanDistance();
                break;
            default:
                distance = new ManhattanDistance();
                break;
        }
        
        radius = initialRadius;
        learningRate = initialLearningRate;
        
        dataSet = new ArrayList<Item>();   
        readArff(dataSet, filePath);    
        initWeights(lattice);
        for(int i=0;i<iterations;i++){
            for(Item item : dataSet){
                rowBmu = -1;
                columnBmu = -1;
                bmu = calculateBMU(item, lattice, distance);
                ArrayList<Item> neighbors = calculateNeighbors(rowBmu,columnBmu,lattice, radius);
                neighbors.add(bmu); //ademas de la vecindad, se agrega a si mismo para actualizar los pesos
                for(Item neighbor : neighbors){
                    updateWeight(item,neighbor,learningRate);
                }
            }
            double deltaLearningRate = (initialLearningRate-finalLearningRate)/iterations;
            double deltaRadius = (initialRadius - finalRadius)/iterations;
            
            learningRate -= deltaLearningRate;
            radius -= deltaRadius;
        }
        printWeightsMatrix();
        if(writeSerializePath != null){
            writeSerializableObject(writeSerializePath);
        }
    }
    
    
     /**
      * Método que actualiza los pesos de un vector
      * @param dataItem item original del conjunto de datos
      * @param neighborVector vector que se desea actualizar
      * @param learningRate radio de aprendizaje
      */
     public static void updateWeight(Item dataItem,Item neighborVector, double learningRate){
        double[] weight = neighborVector.getItem();
        double[] dataVector = dataItem.getItem();
        double[] result = new double[weight.length];
        for(int i=0;i<weight.length;i++){
            result[i] = weight[i] + (learningRate*(dataVector[i]-weight[i]));  
        }
        neighborVector.setItem(result);
        
    }
     
    public static void printVector(double[] vector){
        for(int i=0;i<vector.length;i++){
            System.out.print(vector[i]+" ");
        }
    }

    public static void printWeightsMatrix(){
        for(int i=0;i<rows;i++){
            for(int j=0;j<columns;j++){
                System.out.print("[");
                printVector(lattice[i][j].getItem());
                System.out.print("]");
            }
            System.out.println("");
        }
        System.out.println("");
    }
    
    /**
     * Calcula los vecinos correspondientes a un vector que se encuentra
     * en la malla 
     * @param row fila del vector de pesos
     * @param column columna del vector de pesos 
     * @param map malla 
     * @param radius radio de vecindad
     * @return lista con los vectores vecinos
     */
    public static ArrayList<Item> calculateNeighbors(int row, int column, Item[][] map,double radius){
        ArrayList<Item> neighbors = new ArrayList<Item>();
        int realRadius = (int) Math.round(radius);
        int rowLimit = map.length-1;
        int columnLimit = map[0].length-1;
        
        for(int i = Math.max(0, row-realRadius); i <= Math.min(row+realRadius, rowLimit); i++){
            for(int j = Math.max(0, column-realRadius); j <= Math.min(column+realRadius, columnLimit); j++){
              if(i != row || j != column){
                  neighbors.add(map[i][j]);
              }
            }
        }
        
        return neighbors;
    }
  
    /**
     * Escribe un objeto serializable en un archivo según su ruta
     * @param path ruta donde se desea escribir el archivo
     */
    private static void writeSerializableObject(String path){
        ObjectOutputStream out;
        try {
            out = new ObjectOutputStream(new FileOutputStream(path));
            out.writeObject(lattice);
            out.flush();
            out.close();
        } 
        catch (IOException ex) {
            System.out.println("Path "+path+" not found");
        } 
    }
    
    /**
     * Lee un objeto serialiable de un archivo según su ruta
     * @param path ruta desde donde se desea leer el archivo
     */
    private static void readSerializableObject(String path){
        ObjectInputStream in;
        try {
            in = new ObjectInputStream(new FileInputStream(path));
            lattice = (Item[][]) in.readObject();
            in.close();
        } 
        catch (IOException ex) {
           System.out.println("File "+path+" not found. "+"Initializing new lattice");
           lattice = new Item[rows][columns];
        } 
        catch (ClassNotFoundException ex) {
           System.out.println("ERROR!");
        } 
    }
    
    
    
    
   
    /**
     * Inicializa la matriz de pesos 
     * @param weights matriz de pesos donde se guardarán los valores
     */
    public static void initWeights(Item[][] weights){
        int rowsWeights = weights.length;
        int columnsWeights = weights[0].length;
        Random random = new Random();
        double[] randomVector = new double[dimensionality];
        for(int i=0;i<rowsWeights;i++){
            for(int j=0;j<columnsWeights;j++){
                int randomValue = random.nextInt(instancesCount);
                weights[i][j] = dataSet.get(randomValue);
            }
        }
    }
    
    

    /**
     * Calcula el Best Matching Unit (BMU), dado un item del conjunto de entrada
     * @param item vector del conjunto de entrada
     * @param weights matriz de pesos
     * @param distance tipo de distancia que se desea aplicar
     * @return BMU en comparación al item del conjunto de entrada
     */
    public static Item calculateBMU(Item item, Item[][] weights, Distance distance){
        int rowsWeights = weights.length;
        int columnsWeights = weights[0].length;
        double minimumDistance = Double.MAX_VALUE;
        Item winner = null;
        for(int i=0;i<rowsWeights;i++){
            for(int j=0;j<columnsWeights;j++){  
                double currentDistance = distance.calculateDistance(item.getItem(),weights[i][j].getItem());

                if(currentDistance<minimumDistance){
                    minimumDistance = currentDistance;
                    rowBmu = i;
                    columnBmu = j;
                    winner = weights[i][j];   
                   
                }
            }
        }

        return winner;
        
    }
    
    /**
     * Lee un archivo ARFF y lo carga en una lista de vectores
     * @param items lista donde se van a cargar los datos
     * @param path ruta del archivo
     */
    public static void readArff(ArrayList<Item> items, String path){
        try {
            DataSource dataSource = new DataSource(path);
            Instances instances = dataSource.getDataSet();
            instances.deleteAttributeAt(instances.numAttributes()-1);
            instancesCount = instances.numInstances();
            dimensionality = instances.numAttributes();
            for(int i=0;i<instancesCount;i++){
                Instance currentInstance = instances.instance(i);
                double[] item = currentInstance.toDoubleArray();
                items.add(new Item(item));
            }

        } catch (Exception ex) {
            System.out.println("ERROR!");
        }
    }
    
    
}
        
