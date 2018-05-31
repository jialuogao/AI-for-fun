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
import java.util.Random;

import javax.imageio.ImageIO;
//image size 640x480
public class Main {

	public static final String trainingDir = "cars_train/";
	public static final String testingDir = "cars_test/";
	private static Random generator = new Random();
	private static Node[][] network;
	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
		loadInfoDataFile();
		//buildNN(NNInfo);
		
	}
	//Layer type: 0 Input,1 Convolutional, 2 ReLU, 3 Max Pooling, 4 Full, 5 Output Soft Max
	private static int[][][] NNInfo = 
		{//Type of the layer, Layers it connects to, Layer Information
/*0*/			{{0},{1,4},{921600}},			//Input	640x480x3

/*1*/			{{1},{2},{5,5,3,24}},		//Convol: filter size x, y, stride, node size	213*160*24
/*2*/			{{2},{3}},					//ReLU
/*3*/			{{3},{4,7},{2,2}},			//MaxPool	107*80*24

/*4*/			{{1},{5},{5,5,1,18}},		//Convol	103*76*18
/*5*/			{{2},{6}},
/*6*/			{{3},{7,8},{2,2}},			//			52*38*18

/*7*/			{{4},{8,9},{120}},			//Full		120
/*8*/			{{4},{9,12},{120}},			//Full		120

/*9*/			{{1},{10},{5,5,1,48}},		//			48*34*48
/*10*/			{{2},{11}},
/*11*/			{{3},{12,15},{2,2}},		//			24*17*48

/*12*/			{{1},{13},{3,3,1,96}},		//			22*15*96
/*13*/			{{2},{14}},
/*14*/			{{3},{15,16},{2,2}},		//			11*8*96

/*15*/			{{4},{16,17},{120}},		//			120
/*16*/			{{4},{17},{180}},			//			180
/*17*/			{{4},{18},{80}},			//			80
/*18*/			{{5},{},{1}},				//			1
		};
	private static final String datafile = "src/CarRecongnizer/info&dataFile.txt";
	public static void loadInfoDataFile() throws IOException{
		BufferedReader file = new BufferedReader(new InputStreamReader(new FileInputStream(datafile)));
		String line;
		while((line = file.readLine()) != null) {
			String[] detail = line.split(" ");
			if(detail[0].equals("l")) {
				if(true);
			}
			else if(detail[0].equals("d")) {
				
			}
		}
	}
	
	
	private static final String trainOutDir = "cars_train_compressed/";
	
	public static boolean forwardPassing(int dataNum) throws IOException{
		String num = "0000"+dataNum;
		num = num.substring(num.length()-5,num.length());
		String fileName = num+".jpg";
		
		File img = new File(trainOutDir+fileName);
		if(!img.exists()) {
			return false;
		}
		InputStream inpStream = new BufferedInputStream(new FileInputStream(trainOutDir+fileName));
		BufferedImage image = ImageIO.read(inpStream);
		
		int dWidth = 640;
		int dHeight = 480;
		double[][][] layer = new double[3][dHeight][dWidth];
		//input layer
		for(int color = 0; color <3;color++) {
			for(int y = 0; y <dHeight;y++) {
				for(int x = 0;x<dWidth;x++) {
					Color c = new Color(image.getRGB(x, y));
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
		boolean init = true;
		layer = convolutional(layer, weights[cha][y][x], bias, stride, size);
		layer = relu(layer);
		layer = maxpool(layer, 3, 3, 3);
		layer = convolutional(layer, weights[cha][y][x], bias, stride, size);
		layer = relu(layer);
		layer = maxpool(layer, 2, 2, 2);
		layer = convolutional(layer, weights[cha][y][x], bias, stride, size);
		layer = relu(layer);
		layer = maxpool(layer, 2, 2, 2);
		//if training: layer = dropout(layer, 0.3);
		layer = weightedsum(layer, new double[0][][][], 120, true);
		layer = weightedsum(layer, new double[0][][][], 180, true);
		layer = weightedsum(layer, new double[0][][][], 80, true);
		layer = weightedsum(layer, new double[0][][][], 1, true);
		layer = softmax(layer);
		boolean prediction = false;
		if(layer[0][0][0]>=0.5) {
			prediction = true;
		}
		return prediction;
	}
	
	
	//TODO
	public static double[][][] convolutional(double[][][] layer, double[][][] weights, double[]? bias, int stride, int size){
		double [][][] newLayer = new double[size][][];
		
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
			weights = new double[size][layer.length][layer[0].length][layer[0][0].length];
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static void buildNN(int[][][] NNInfo) {
		network = new Node[NNInfo.length][];
		for(int layer = NNInfo.length-1; layer>-1; layer--) {
			int[][] line = NNInfo[layer];
			int type = line[0][0];
			switch (type) {
			case 0:
				int nodeNum = line[2][0];
				//create the layer
				network[layer]= new Node[nodeNum];
				int[] connection = line[1];
				//create node and connect them to the network
				for(int con = 0; con<connection.length;con++) {
					//if dosen't exist, create and connect
					if(network[layer][0]==null) {
						for(int node = 0; node<nodeNum;node++) {
							//create current node
							Node currentNode = new Node();
							int conNum = network[con].length;
							//find each children from the first connection layer and add them to "children" 
							for(int edge = 0; edge<conNum;edge++) {
								Node childNode = network[con][edge];
								double weight = generator.nextDouble();
								NodeWeightWrapper child = new NodeWeightWrapper(childNode,weight);
								currentNode.getChildren().add(child);
							}
							network[layer][node] = currentNode;
						}
					}
					//connect without recreating
					else {
						for(int node = 0; node<nodeNum;node++) {
							Node currentNode = network[layer][node];
							int conNum = network[con].length;
							for(int edge = 0; edge<conNum;edge++) {
								Node childNode = network[con][edge];
								double weight = generator.nextDouble();
								NodeWeightWrapper child = new NodeWeightWrapper(childNode,weight);
								currentNode.getChildren().add(child);
							}
						}
					}
				}
				break;
				//{{1},{2},{5,5,3,24}},		//Convol: filter size x, y, stride, node size	213*160*24
			case 1:
				
			}
		}
	}

}