package CarRecognizer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;
//image size 640x480
public class Main {

	public static final String trainingPosDir = "cars_train_compressed/";
	public static final String testingPosDir = "cars_test_compressed/";
	public static final String trainingNegDir = "trees_train_compressed/";
	public static final String testingNegDir = "trees_test_compressed/";
	private static Random generator = new Random();
	//private static Node[][] network;
	
	private static boolean init;
	private static double weights[][][][][][][][][];
	public static void main(String[] args) throws Exception{
		loadInfoDataFile();
		
		File dirin = new File(trainingPosDir);
        for (final File f : dirin.listFiles()) {
			InputStream inpStream = new BufferedInputStream(new FileInputStream(f));
			BufferedImage image = ImageIO.read(inpStream);
			boolean isCar = true;
			train(image, isCar);
        }
        
        dirin = new File(trainingNegDir);
        for (final File f : dirin.listFiles()) {
			InputStream inpStream = new BufferedInputStream(new FileInputStream(f));
			BufferedImage image = ImageIO.read(inpStream);
			boolean isCar = true;
			train(image, isCar);
        }
		
	}
	//Layer type: 0 Input,1 Convolutional, 2 ReLU, 3 Max Pooling, 4 Full, 5 Output Soft Max, 6 Droop out
	private static int[][][] info = 
		{//Type of the layer, Layers it connects to, Layer Information
/*0*/			{{0},{1,4},{921600}},			//Input	640x480x3

/*1*/			{{1},{2},{5,5,3,24}},		//Convol: filter size x, y, stride, node size	213*160*24
/*2*/			{{2},{3}},					//ReLU
/*3*/			{{3},{4,7},{2,2,2}},			//MaxPool	107*80*24

/*4*/			{{1},{5},{5,5,1,18}},		//Convol	103*76*18
/*5*/			{{2},{6}},
/*6*/			{{3},{7,8},{2,2,2}},			//			52*38*18

/*7*/			{{4},{8,9},{120}},			//Full		120
/*8*/			{{4},{9,12},{120}},			//Full		120

/*9*/			{{1},{10},{5,5,1,48}},		//			48*34*48
/*10*/			{{2},{11}},
/*11*/			{{3},{12,15},{2,2,2}},		//			24*17*48

/*12*/			{{1},{13},{3,3,1,96}},		//			22*15*96
/*13*/			{{2},{14}},
/*14*/			{{3},{15,16},{2,2,2}},		//			11*8*96

/*15*/			{{4},{16,17},{120}},		//			120
/*16*/			{{4},{17},{180}},			//			180
/*17*/			{{4},{18},{80}},			//			80
/*18*/			{{5},{},{1}},				//			1
		};
	
	private static final String infofile = "src/CarRecognizer/infoFile.txt";
	private static final String datadir = "src/CarRecognizer/Data/";
	
	//for each layer, there is a data file naming as the layer number
	public static void loadInfoDataFile() throws Exception{
		//see if dir exist
		File infoFile = new File(infofile);
		if(! infoFile.exists()) {
			throw new Exception("No valid info file found");
		}
		else {
			BufferedReader layerInfo = new BufferedReader(new InputStreamReader(new FileInputStream(infoFile)));
			String line;
			int layerNum = 0;
			while((line = layerInfo.readLine())!=null) {
				String[] lineInfo = line.split(" ");
				String[] layerDetail = lineInfo[1].split(",");
				info[layerNum] = new int[lineInfo.length][];
				info[layerNum][0][0] = Integer.parseInt(lineInfo[0]);
				info[layerNum][1] = new int[layerDetail.length];
				for(int i = 0;i<layerDetail.length;i++) {
					info[layerNum][1][i] = Integer.parseInt(layerDetail[i]);
				}
			}
			
			Integer[][][] i = Arrays.stream( info ).toArray( Integer[][][]::new );
			ImageProcessor.print3dMatrix(i);
		}
		File dataFile = new File(datadir);
		if (! dataFile.exists()){
	        dataFile.mkdirs();
	        init = true;
	    }
		else {
			//no file exist, initiate weights
			File[] data = dataFile.listFiles();
			if(data.length==0) {
				init = true;
			}
			else {
				//data file present, load data
				init = false;
				for(int[][] l:info) {
					String layerNum = l[0][0]+"";
					boolean hasFile = false;
					for(final File f: data) {
						if(f.getName().equals(layerNum)) {
							hasFile = true;
							//TODO:read in data
							//weights = 
						}
					}
					if(!hasFile) {
						throw new Exception("Data file missing");
					}
					
				}
			}			
		}
	}
	
	public static void train(BufferedImage img, boolean isCar) throws Exception {
		//TODO: multivariable
		final double target = isCar ? 1 : 0;
		runNN(img, true,target);
		
		
	}
	
	public static void runNN(BufferedImage img, boolean isTraining, double target) throws Exception{
		if(info[0][0][0]!=0) {
			throw new Exception("Info file error, did not start with an input layer");
		}
		int dWidth = img.getWidth();
		int dHeight = img.getHeight();
		double[][][] layer = new double[3][dHeight][dWidth];
		//input layer
		for(int color = 0; color <3;color++) {
			for(int y = 0; y <dHeight;y++) {
				for(int x = 0;x<dWidth;x++) {
					Color c = new Color(img.getRGB(x, y));
					if(color==0) {
						layer[color][y][x]= c.getRed();						
					}
					else if(color==1) {
						layer[color][y][x] = c.getBlue();
					}
					else {
						layer[color][y][x] = c.getGreen();
					}
				}
			}
		}
		nextLayer(layer,0,isTraining,target);
//		layer = convolutional(layer, new double[0][0][0], 5, 5, 3, 24, init);
//		layer = relu(layer);
//		layer = maxpool(layer, 3, 3, 3);
//		layer = convolutional(layer, new double[0][0][0], 5, 5, 1, 18, init);
//		layer = relu(layer);
//		layer = maxpool(layer, 2, 2, 2);
//		layer = convolutional(layer, new double[0][0][0], 3, 3, 1, 48, init);
//		layer = relu(layer);
//		layer = maxpool(layer, 2, 2, 2);
//		//if training: layer = dropout(layer, 0.3);
//		layer = weightedsum(layer, new double[0][0][0][0], 120, init);
//		layer = weightedsum(layer, new double[0][0][0][0], 180, init);
//		layer = weightedsum(layer, new double[0][0][0][0], 80, init);
//		layer = weightedsum(layer, new double[0][0][0][0], 1, init);
//		layer = softmax(layer);
//		return layer[0][0][0];
	}

	public static double[][][] nextLayer(double[][][] layer, int layerNum, boolean isTraining, final double target){
		int type = info[layerNum][0][0];
		double[][][] deltaWeights;
		switch(type) {
		//convolutional
		case 1:
			layer = convolutional(layer, weightmatrix, info[layerNum][1][0], info[layerNum][1][1], info[layerNum][1][2], info[layerNum][1][3], init);
			if(layerNum<layer.length-1) {
				deltaWeights = nextLayer(layer,layerNum+1,isTraining,target);
			}
			if(isTraining) {
				backpropagation{
					change weights
				}
			}
			return deltaWeights;
			break;
		//ReLU
		case 2:
			layer = relu(layer);
			if(layerNum<layer.length-1) {
				deltaWeights = nextLayer(layer, layerNum+1, isTraining,target);
			}
			return deltaWeights;
			break;
		//Max pooling
		case 3:
			layer = maxpool(layer, info[layerNum][1][0], info[layerNum][1][1], info[layerNum][1][2]);
			if(layerNum<layer.length-1) {
				deltaWeights = nextLayer(layer,layerNum+1,isTraining,target);
			}
			return deltaWeights;
			break;
		//weighted sum
		case 4:
			layer = weightedsum(layer, new double[0][0][0][0], 120, init);
			if(layerNum<layer.length-1) {
				deltaWeights = nextLayer(layer,layerNum+1,isTraining,target);
			}
			if(isTraining) {
				backpropagation;
			}
			return deltaWeights;
			break;
		//soft max
		case 5:
			layer = softmax(layer);
			if(layerNum<layer.length-1) {
				nextLayer(layer,info,layerNum+1,isTraining,target);
			}
			else {
				System.out.println("Prediction ended at layer "+layerNum);
				System.out.println("The prediction is:");
				double pred = layer[0][0][0];
				System.out.println(pred >= 0.5 ? "is a car":"is not a car");
				
				if(isTraining) {
					double delta = target - pred;
					deltaWeights = new double[][][] = {{{delta}}};
				}
			}
			return deltaWeights;
			break;
		//drop out
		case 6:
			if(isTraining) {
				layer = dropout(layer, 0.3);				
			}
			else {
				deltaWeights = nextLayer(layer, info, layerNum+1, isTraining, target);
			}
			return deltaWeights;
			break;
		default:
			throw new Exception("Unknown layer type");
			break;
		}
	}
	
	//TODO: check
	public static double[][][] convolutional(double[][][] layer, double[][][] weights, int x, int y, int stride, int size, boolean init){
		if(init) {
			weights = new double[layer.length][y][x];
			for(int z=0;z<weights.length;z++) {
				for(int height=0;height<y;height++) {
					for(int width=0;width<x;width++) {
						weights[z][height][width]=generator.nextDouble();
					}
				}
			}
		}
		
		
		int newx = (int)(Math.ceil((layer[0][0].length - x)/stride) + 1);
		int newy = (int)(Math.ceil((layer[0].length - y)/stride) + 1);
		double [][][] newLayer = new double[size][newy][newx];
		for(int plane = 0; plane<size; plane++) {
			for(int j=0;j<newy;j++) {
				//calculate corresponding y value
				int currenty = j*stride;
				for(int i =0;i<newx;i++) {
					//calculate corresponding x value
					int currentx = i*stride;
					double weightedSum = 0;

					for(int height = 0; height<y; height++) {
						if(currenty+height==layer[0].length)
							break;
						for(int width = 0; width<x;width++) {
							if(currentx+width==layer[0][currenty].length)
								break;
							for(int depth = 0; depth<layer.length; depth++) {
								weightedSum+=layer[depth][currenty+height][currentx+width] * weights[depth][height][width];								
							}
						}
					}
					//save to the new layer
					newLayer[plane][j][i]=weightedSum;
				}
			}
		}
		return newLayer;
	}
	
	public static double[][][] relu(double[][][] layer) {
		for(int plane = 0; plane<layer.length;plane++) {
			for(int row  = 0; row<layer[plane].length;row++) {
				for(int val = 0; val<layer[plane][row].length;val++) {
					//System.out.println(layer[plane][row][val]);
					layer[plane][row][val] = Math.max(layer[plane][row][val], 0);
					//System.out.println(layer[plane][row][val]);
				}
			}
		}
		return layer;
	}
	//??
	public static double[][][] maxpool(double[][][] layer,int x,int y,int stride){
		// x and y in new max-pooled layer
		int newx = (int)(Math.ceil(layer[0][0].length - x)/stride + 1);
		int newy = (int)(Math.ceil(layer[0].length - y)/stride + 1);
		//create new layer
		double[][][] newLayer = new double[layer.length][newy][newx];
		for(int plane = 0; plane<layer.length; plane++) {
			for(int j=0;j<newy;j++) {
				//calculate corresponding y value
				int currenty = j*stride;
				for(int i =0;i<newx;i++) {
					//calculate corresponding x value
					int currentx = i*stride;
					double max = 0;
					//find the max(pooling)
					for(int height = 0; height<y; height++) {
						if(currenty+height==layer[plane].length)
							break;
						for(int width = 0; width<x;width++) {
							if(currentx+width==layer[plane][currenty].length)
								break;
							max=Math.max(layer[plane][currenty+height][currentx+width],max);
						}
					}
					//save to the new layer
					newLayer[plane][j][i]=max;
				}
			}
		}
		return newLayer;
	}
	
	public static double[][][] dropout(double[][][] layer, double probability){
		for(int z = 0;z<layer.length;z++)
			for(int y=0; y<layer[0].length;y++)
				for(int x=0;x<layer[0][0].length;x++)
					if(generator.nextDouble()<probability)
						layer[z][y][x] = 0;
		return layer;
	}
	//??
	public static double[][][] weightedsum(double[][][] layer, double[][][][] weights, int size, boolean init){
		if(init) {
			weights = new double[size+1][layer.length][layer[0].length][layer[0][0].length];
			for(int node = 0;node<weights.length;node++) {
				for(int z=0;z<weights[node].length;z++) {
					for(int y=0;y<weights[node][z].length;y++) {
						for(int x=0;x<weights[node][z][y].length;x++) {
							weights[node][z][y][x]=generator.nextDouble();
						}
					}
				}
			}
		}
		
		double[][][] newLayer = new double[1][1][size+1];
		//add bias
		newLayer[0][0][size] = 1;
		//dot weights for each node with layer and calculate weighted sum for each node(amount == size)
		for(;size>0;size--) {
			double weightedsum = 0;
			for(int z=0;z<layer.length;z++) {
				for(int y=0; y<layer[0].length;y++) {
					for(int x=0;x<layer[0][0].length;x++) {
						//System.out.println(z+" "+y+" "+x+" "+size);
						weightedsum += layer[z][y][x] * weights[size][z][y][x];
					}
				}
			}
			newLayer[0][0][size]=weightedsum;
		}
		return newLayer;
	}
	
	public static double[][][] softmax(double[][][] layer){
		double sum = 0;
		for(double x : layer[0][0]) {
			sum+=x;
		}
		double denum = Math.exp(sum);
		for(int i = 0; i<layer[0][0].length;i++) {
			layer[0][0][i] = Math.exp(layer[0][0][i])/denum;
		}
		return layer;
	}
	
}

	
	
	
//	
//	
//	
//	public static void buildNN(int[][][] NNInfo) {
//		network = new Node[NNInfo.length][];
//		for(int layer = NNInfo.length-1; layer>-1; layer--) {
//			int[][] line = NNInfo[layer];
//			int type = line[0][0];
//			switch (type) {
//			case 0:
//				int nodeNum = line[2][0];
//				//create the layer
//				network[layer]= new Node[nodeNum];
//				int[] connection = line[1];
//				//create node and connect them to the network
//				for(int con = 0; con<connection.length;con++) {
//					//if dosen't exist, create and connect
//					if(network[layer][0]==null) {
//						for(int node = 0; node<nodeNum;node++) {
//							//create current node
//							Node currentNode = new Node();
//							int conNum = network[con].length;
//							//find each children from the first connection layer and add them to "children" 
//							for(int edge = 0; edge<conNum;edge++) {
//								Node childNode = network[con][edge];
//								double weight = generator.nextDouble();
//								NodeWeightWrapper child = new NodeWeightWrapper(childNode,weight);
//								currentNode.getChildren().add(child);
//							}
//							network[layer][node] = currentNode;
//						}
//					}
//					//connect without recreating
//					else {
//						for(int node = 0; node<nodeNum;node++) {
//							Node currentNode = network[layer][node];
//							int conNum = network[con].length;
//							for(int edge = 0; edge<conNum;edge++) {
//								Node childNode = network[con][edge];
//								double weight = generator.nextDouble();
//								NodeWeightWrapper child = new NodeWeightWrapper(childNode,weight);
//								currentNode.getChildren().add(child);
//							}
//						}
//					}
//				}
//				break;
//				//{{1},{2},{5,5,3,24}},		//Convol: filter size x, y, stride, node size	213*160*24
//			case 1:
//				
//			}
//		}
//	}
//
//}