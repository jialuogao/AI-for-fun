package CarRecognizer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.*;

/**
 * The main class that handles the entire network
 * Has multiple attributes each with its own use
 */

public class NNImpl {
   private ArrayList<Instance> trainingSet;    //the training set

    private double learningRate;    // variable to store the learning rate
    private int maxEpoch;   // variable to store the maximum number of epochs
    private Random random;  // random number generator to shuffle the training set

    private static double[][][] layer;
    private static double[][][][] weights;
    private static double[][][][] weights2;
    /**
     * This constructor creates the nodes necessary for the neural network
     * Also connects the nodes of different layers
     * After calling the constructor the last node of both inputNodes and
     * hiddenNodes will be bias nodes.
     * @throws IOException 
     * @throws NumberFormatException 
     */

    NNImpl(ArrayList<Instance> trainingSet, int hiddenNodeCount, Double learningRate, int maxEpoch, Random random, Double[][] hiddenWeights, Double[][] outputWeights) throws NumberFormatException, IOException {
        this.trainingSet = trainingSet;
        Main.learningRate = learningRate;
        this.maxEpoch = maxEpoch;
        this.random = random;

        File infoFile = new File(Main.infofile);
        LineNumberReader lineNumReader = new LineNumberReader(new InputStreamReader(new FileInputStream(infoFile)));
		while ((lineNumReader.readLine()) != null);
        int length = lineNumReader.getLineNumber();
        Main.info = new int[length][][];
        
		BufferedReader layerInfo = new BufferedReader(new InputStreamReader(new FileInputStream(infoFile)));
		String line;
		int layerNum = 0;
		while((line = layerInfo.readLine())!=null) {
			String[] lineInfo = line.split(" ");
			String[] layerDetail = lineInfo[1].split(",");
			Main.info[layerNum] = new int[lineInfo.length][];
			int[] temp= {Integer.parseInt(lineInfo[0])};
			Main.info[layerNum][0] = temp;
			Main.info[layerNum][1] = new int[layerDetail.length];
			for(int i = 0;i<layerDetail.length;i++) {
				if(layerDetail[i].equals("null")) {
					layerDetail[i]="-1";
				}
				Main.info[layerNum][1][i] = Integer.parseInt(layerDetail[i]);
			}
			layerNum++;
		}
		
        	int inputNodeCount = trainingSet.get(0).attributes.size();
        	int outputNodeCount = trainingSet.get(0).classValues.size();

        	layer = new double[1][1][inputNodeCount+1];
        	

        	
        	weights = new double[hiddenWeights.length][1][1][hiddenWeights[0].length+1];
        	for(int i=0;i<hiddenNodeCount;i++) {
        		for(int j=0;j<inputNodeCount+1;j++) {
        			weights[i][0][0][j] = hiddenWeights[i][j];
        		}
        	}
        	
        	
        	weights2 = new double[outputNodeCount][1][1][hiddenNodeCount+1];
        	for (int i = 0; i < outputNodeCount; i++) {
        		for (int j = 0; j < hiddenNodeCount+1; j++) {
        			weights2[i][0][0][j] = outputWeights[i][j];
        		}
        	}
        	
       
        	Main.weights.add(null);
    		Main.weights.add(weights);
    		Main.weights.add(weights2);
    }

    /**
     * Get the prediction from the neural network for a single instance
     * Return the idx with highest output values. For example if the outputs
     * of the outputNodes are [0.1, 0.5, 0.2], it should return 1.
     * The parameter is a single instance
     * @throws Exception 
     */

    public int predict(Instance instance) throws Exception {
        // TODO: add code here
    	useNN(instance);
    	int predict = 0;
    	double max = Double.MIN_VALUE;
    	for(int i=0; i<layer[0][0].length-1; i++) {
    		double out = layer[0][0][i];
    		if(out > max) {
    			max = out;
    			predict = i;
    		}
    	}
        return predict;
    }


    /**
     * Train the neural networks with the given parameters
     * <p>
     * The parameters are stored as attributes of this class
     * @throws Exception 
     */

    public void train() throws Exception {
        // TODO: add code here
    	for(int j = 0; j < maxEpoch; j++){
	    	double totalE = 0;
	    	Collections.shuffle(trainingSet,random);
	    	for(Instance instance: trainingSet) {
	    		layer = new double[1][1][instance.attributes.size()];
	    		double[] target = new double[instance.classValues.size()];
	    		for(int i=0;i<target.length;i++) {
	    			target[i] = instance.classValues.get(i);
	    		}
	    		for(int x=0; x< layer[0][0].length-1;x++) {
	        		layer[0][0][x] = instance.attributes.get(x);
	        	}
	        	layer[0][0][layer[0][0].length-1] = 1.0;
	    		Main.nextLayer(layer, 0, true, target);
	    		
	        }
	    	for(Instance inst : trainingSet) {
	    		totalE+=loss(inst);
	    	}
	    	totalE/=trainingSet.size();
	    	System.out.print("Epoch: " + j + ", Loss: ");
	    	System.out.format("%.8e", totalE);
	    	System.out.println();
    	}
    }

    /**
     * Calculate the cross entropy loss from the neural network for
     * a single instance.
     * The parameter is a single instance
     * @throws Exception 
     */
    private double loss(Instance instance) throws Exception {
        // TODO: add code here
    	useNN(instance);
    	double ce = 0;
    	for(int i =0 ; i<layer[0][0].length-1;i++) {
    		double g = layer[0][0][i];
    		ce -= instance.classValues.get(i)*Math.log(g);
    	}
        return ce;
    }
    
    private void useNN(Instance instance) throws Exception {
    	layer = new double[1][1][instance.attributes.size()];
    	for(int x=0; x< layer[0][0].length-1;x++) {
    		layer[0][0][x] = instance.attributes.get(x);
    	}
    	layer[0][0][layer[0][0].length-1] = 1.0;
    	
    	layer = Main.weightedsum(layer, (double[][][][])Main.weights.get(1), weights.length);
    	layer = Main.weightedsum(layer, (double[][][][])Main.weights.get(2), weights2.length);
    }
}
