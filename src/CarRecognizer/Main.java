package CarRecognizer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
//image size 640x480
public class Main {

	public static final String trainingPosDir = "cars_train_compressed/";
	public static final String testingPosDir = "cars_test_compressed/";
	public static final String trainingNegDir = "trees_train_compressed/";
	public static final String testingNegDir = "trees_train_compressed/";
	//Layer type: 0 Input,1 Convolutional, 2 ReLU, 3 Max Pooling, 4 Full, 5 Output Soft Max, 6 Droop out
	public static int[][][] info;
	
	public static final String infofile = "src/CarRecognizer/infoFile.txt";
	public static final String datadir = "src/CarRecognizer/Data/";
	private static Random generator = new Random();
	//private static Node[][] network;
	public static ArrayList weights = new ArrayList();
	private static boolean init;
	public static double learningRate;
	
	private static int predCar = 0;
	private static int predTree = 0;
	public static void main(String[] args) throws Exception{
		if(args.length!=1) {
			System.out.println("Please input learning rate!");
            System.exit(-1);
		}
		learningRate = Double.parseDouble(args[0]);
		
		loadInfoDataFile();
		
		train();
		
		predict();
	}
	
	public static void writeDataFile(boolean isTraining) throws Exception{
		//System.out.println("writeDataFile");
		for(int i=0;i<weights.size();i++) {
			if(weights.get(i)!=null) {
				if(weights.get(i).getClass().equals(new double[0][][].getClass())) {
					double[][][] weightmatrix = (double[][][])weights.get(i);
					if(isTraining) {
						weightmatrix = normalize(weightmatrix);
					}
					BufferedWriter writer = new BufferedWriter(new FileWriter(datadir+i+".txt"));
					int z = weightmatrix.length, y = weightmatrix[0].length, x = weightmatrix[0][0].length;
					writer.write(z+" "+y+" "+x);
					writer.newLine();
					for(double[][] plane: weightmatrix) {
						String line = "";
						for(double[] row: plane) {
							for(double data: row) {
								line+=Math.round(data * 10000000.0)/10000000.0+",";
							}
							line+=" ";
						}
						writer.write(line);
						writer.newLine();
					}
					writer.close();
				}
				else if(weights.get(i).getClass().equals(new double[0][][][].getClass())) {
					double[][][][] weightmatrix = (double[][][][])weights.get(i);
					if(isTraining) {
						weightmatrix = normalize(weightmatrix);
					}
					BufferedWriter writer = new BufferedWriter(new FileWriter(datadir+i+".txt"));
					int z = weightmatrix.length, y = weightmatrix[0].length, x = weightmatrix[0][0].length, a = weightmatrix[0][0][0].length;
					writer.write(z+" "+y+" "+x+" "+a);
					writer.newLine();
					for(double[][][] cube: weightmatrix) {
						for(double[][] plane: cube) {
							String line = "";
							for(double[] row: plane) {
								for(double data: row) {
									line+=Math.round(data * 10000000.0)/10000000.0+",";
								}
								line+=" ";
							}
							writer.write(line);
							writer.newLine();
						}
						writer.newLine();
					}
					writer.close();
				}				
			}
		}
		System.out.println("Writing complated!");
	}
	//for each layer, there is a data file naming as the layer number
	public static void loadInfoDataFile() throws Exception{
		//see if dir exist
		File infoFile = new File(infofile);
		if(! infoFile.exists()) {
			throw new Exception("No valid info file found");
		}
		else {
			LineNumberReader lineNumReader = new LineNumberReader(new InputStreamReader(new FileInputStream(infoFile)));
			while ((lineNumReader.readLine()) != null);
	        int length = lineNumReader.getLineNumber();
	        info = new int[length][][];
	        
			BufferedReader layerInfo = new BufferedReader(new InputStreamReader(new FileInputStream(infoFile)));
			String line;
			int layerNum = 0;
			while((line = layerInfo.readLine())!=null) {
				String[] lineInfo = line.split(" ");
				String[] layerDetail = lineInfo[1].split(",");
				info[layerNum] = new int[lineInfo.length][];
				int[] temp= {Integer.parseInt(lineInfo[0])};
				info[layerNum][0] = temp;
				info[layerNum][1] = new int[layerDetail.length];
				for(int i = 0;i<layerDetail.length;i++) {
					if(layerDetail[i].equals("null")) {
						layerDetail[i]="-1";
					}
					info[layerNum][1][i] = Integer.parseInt(layerDetail[i]);
				}
				layerNum++;
			}
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
				//System.out.println("loading data");
				init = false;
				for(int i = 0; i<info.length; i++) {
					weights.add(null);
				}
				for(int layerNum = 0; layerNum<info.length;layerNum++) {
					String numStr = layerNum+".txt";
					for(final File f: data) {
						if(f.getName().equals(numStr)) {
							System.out.println("Reading file "+layerNum);
							//TODO:read in data
							BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
							int[] detail =  Stream.of(reader.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
							if(detail.length==3) {
								double[][][] weight = new double[detail[0]][detail[1]][detail[2]];
								String line;
								int lineNum = 0;
								while((line = reader.readLine()) != null) {
									String[] d2 = Arrays.stream(line.split(", ")).filter(val->val!=null&&val.length()>0).toArray(size->new String[size]);
									for(int i=0;i<d2.length;i++) {
										String[] d1 = d2[i].split(",");
										String[] removeNull = Arrays.stream(d1).filter(val -> val!=null && val.length()>0).toArray(size->new String[size]);
										weight[lineNum][i] = Stream.of(removeNull).mapToDouble(Double::parseDouble).toArray();
									}
									lineNum++;
								}
								weights.set(layerNum, weight); 
							}
							else if(detail.length==4) {
								double[][][][] weight = new double[detail[0]][detail[1]][detail[2]][detail[3]];
								for(int block =0; block<detail[0];block++) {
									for(int lineNum=0;lineNum<detail[1];lineNum++) {
										String line = reader.readLine();
										String[] d2 = Arrays.stream(line.split(", ")).filter(val->val!=null&&val.length()>0).toArray(size->new String[size]);
										for(int i=0;i<d2.length;i++) {
											String[] d1 = d2[i].split(",");
											String[] removeNull = Arrays.stream(d1).filter(val -> val!=null && val.length()>0).toArray(size->new String[size]);
											weight[block][lineNum][i] = Stream.of(removeNull).mapToDouble(Double::parseDouble).toArray();
										}
									}
									reader.readLine();
								}
								weights.set(layerNum, weight);
							}
						}
					}
				}
			}			
		}
	}
	
	public static void train() throws Exception {
		int versionCount = 0;
		boolean isTraining = true;
		File dirin = new File(trainingPosDir);
		File dirin2 = new File(trainingNegDir);
		if(dirin.list().length==0 || dirin2.list().length==0) {
			throw new Exception("no data Available");
		}
		int totalpic = dirin.list().length+dirin2.list().length;
		int maxpic = Math.max(dirin.list().length,dirin2.list().length);
		boolean firstmax = dirin.list().length>=dirin2.list().length;
		double ratio = (double)maxpic/(double)totalpic;
        int pointermax = 0;
        int pointermin = 0;
        File[] maxfiles = firstmax ? dirin.listFiles(): dirin2.listFiles();
        File[] minfiles = firstmax ? dirin2.listFiles(): dirin.listFiles();
		for (int i=0;i<totalpic;i++) {
        	File f;
        	boolean isCar;
        	if((generator.nextDouble()<ratio||pointermin>=minfiles.length)&&(pointermax<maxfiles.length)) {
    			if(firstmax) {
    				isCar = true;
    			}
    			else {
    				isCar = false;
    			}
    			f = maxfiles[pointermax];
    			pointermax++;        			
        	}
        	else {
    			if(firstmax) {
    				isCar = false;
    			}
    			else {
    				isCar = true;
    			}
    			f = minfiles[pointermin];
    			pointermin++;        			
        	}
        	System.out.println();
        	System.out.println("The file name is "+f.getName()+" and it is "+(isCar? "a car":"not a car"));
			InputStream inpStream = new BufferedInputStream(new FileInputStream(f));
			BufferedImage image = ImageIO.read(inpStream);
			//TODO: multivariable
			final double[] target = {isCar? 1.0:0.0, isCar? 0.0:1.0};
			learningRate = isCar ? 1:8;
			runNN(image,isTraining,target);
			versionCount++;
			if(versionCount==500) {
				System.out.println(versionCount);
				writeDataFile(isTraining);
				versionCount = 0;
			}
        }
		
	}
	
	public static void predict() throws Exception{
		predCar = 0;
		predTree = 0;
		int count = 0;
        File testin1 = new File(testingPosDir);
        for (final File f : testin1.listFiles()) {
			count++;
			System.out.println("Current at Car: "+count);
        	InputStream inpStream = new BufferedInputStream(new FileInputStream(f));
			BufferedImage image = ImageIO.read(inpStream);
			double[] target = {1.0, 0.0};
			runNN(image, false, target);
        }
        count = 0;
        File testin2 = new File(testingNegDir);
        for (final File f : testin2.listFiles()) {
        	count++;
    		System.out.println("Current at Tree: "+count);
			InputStream inpStream = new BufferedInputStream(new FileInputStream(f));
			BufferedImage image = ImageIO.read(inpStream);
			double[] target = {0.0, 1.0};
			runNN(image, false, target);
        }
        System.out.println();
        System.out.println("Prediction finished and here is the result:");
        System.out.println(predCar+" "+predTree+" "+testin1.list().length+" "+testin2.list().length);
        System.out.println((double)predCar/testin1.list().length);
        System.out.println((double)predTree/testin2.list().length);
	}
	
	public static void runNN(BufferedImage img, boolean isTraining, double[] target) throws Exception{
		//System.out.println("runNN");
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
						layer[color][y][x]= c.getRed()/255.0;						
					}
					else if(color==1) {
						layer[color][y][x] = c.getBlue()/255.0;
					}
					else {
						layer[color][y][x] = c.getGreen()/255.0;
					}
				}
			}
		}
		if(init) {
			for(int i = 0; i<info.length;i++) {
				weights.add(null);
			}
		}
		nextLayer(layer,0,isTraining,target);
	}
	//TODO: add bias to cnn
	public static double[][][] nextLayer(double[][][] layer, int layerNum, boolean isTraining, final double[] target) throws Exception{
		int type = info[layerNum][0][0];
		//System.out.println("nextLayer "+layerNum+" type: "+type);
		double[][][] deltaWeights = null;
		switch(type) {
		//input
		case 0:
			deltaWeights = nextLayer(layer, layerNum+1, isTraining, target);
			return deltaWeights;
		//convolutional
		case 1:
			int x = info[layerNum][1][0];
			int y = info[layerNum][1][1];
			int stride = info[layerNum][1][2];
			double ignoreRate = (double)info[layerNum][1][4]/100.0;
			if(init) {
				//System.out.println("init");
				double[][][] weightmatrix = new double[layer.length][y][x];
				for(int z=0;z<weightmatrix.length;z++) {
					for(int height=0;height<y;height++) {
						for(int width=0;width<x;width++) {
							weightmatrix[z][height][width]=generator.nextGaussian() * 0.01;
						}
					}
				}
				weights.set(layerNum, weightmatrix);
				writeDataFile(false);
			}
			double[][][] weightConv = (double[][][])weights.get(layerNum);
			double[][][] newClayer = convolutional(layer, weightConv, x, y, stride, info[layerNum][1][3]);
			if(layerNum<info.length-1) {
				deltaWeights = nextLayer(newClayer,layerNum+1,isTraining,target);
			}
			if(isTraining) {
				double[][][] newDW = new double[layer.length][layer[0].length][layer[0][0].length];
				//all the forward weights, dimension for convol
				for(int k=0;k<deltaWeights.length;k++) {
					for(int j=0;j<weightConv[0].length;j++) {
						for(int i =0;i<weightConv[0][j].length;i++) {
							//filter weight matrix * error of next layer = error of first layer
							for(int a = 0; a<newDW.length;a++) {
								for(int b=0;b<deltaWeights[0].length-weightConv[0].length;b++) {
									for(int c=0;c<deltaWeights[0][0].length-weightConv[0][0].length;c++) {
										newDW[a][b+j][c+i] += deltaWeights[k][b][c] * weightConv[a][j][i] * (generator.nextDouble()<ignoreRate ? 0.0:1.0);
									}
								}
							}
						}
					}
				}
				
				//use this layer errors calculate filter error sum(newDW[one block] * weight[one block])
				for(int j=0;j<weightConv[0].length;j++) {
					for(int i =0;i<weightConv[0][j].length;i++) {
						for(int a=0;a<newDW.length;a++) {
							double weightedSum = 0;
							//all the backward errors, dimension for next layer/delta weights
							for(int b=0;b<newDW[0].length-weightConv[0].length;b++) {
								for(int c=0;c<newDW[0][0].length-weightConv[0][0].length;c++) {
									//TODO: might be too big, over reacting or one directional reaction???
									//filter error
									weightedSum += weightConv[a][j][i]*newDW[a][b+j][c+i];
								}
							}
							//use filter error to calculate weight delta = output[one block] * filter error[one block]
							//this layer
							double d=0;
							for(int b=0;b<layer[0].length-weightConv[0].length;b++) {
								for(int c=0;c<layer[0][0].length-weightConv[0][0].length;c++) {
									d += layer[a][b+j][c+i] * weightedSum;
								}
							}
							double delta = learningRate * d;
							//update weights
							weightConv[a][j][i] += delta;
						}
					}
				}
				weights.set(layerNum,weightConv);
				deltaWeights = newDW;				
			}
			return deltaWeights;
		//ReLU
		case 2:
			layer = relu(layer);
			if(layerNum<info.length-1) {
				deltaWeights = nextLayer(layer, layerNum+1, isTraining,target);
			}
			return deltaWeights;
		//Max pooling
		case 3:
			layer = maxpool(layer, info[layerNum][1][0], info[layerNum][1][1], info[layerNum][1][2]);
			if(layerNum<info.length-1) {
				deltaWeights = nextLayer(layer,layerNum+1,isTraining,target);
			}
			return deltaWeights;
		//weighted sum
		case 4:
			int size = info[layerNum][1][0];
			if(init) {
				//System.out.println("init2");
				double[][][][] weightmatrix = new double[size][layer.length][layer[0].length][layer[0][0].length];
				for(int node = 0;node<weightmatrix.length;node++) {
					for(int k=0;k<weightmatrix[node].length;k++) {
						for(int j=0;j<weightmatrix[node][k].length;j++) {
							for(int i=0;i<weightmatrix[node][k][j].length;i++) {
								weightmatrix[node][k][j][i]=generator.nextGaussian()*0.01;
							}
						}
					}
				}
				weights.set(layerNum, weightmatrix);
				writeDataFile(false);
			}
			
			double[][][][] weight = (double[][][][])weights.get(layerNum);
			double[][][] newDW = new double[layer.length][layer[0].length][layer[0][0].length-1];
			double[][][] newlayer = weightedsum(layer, weight, size);
			if(layerNum<info.length-1) {
				deltaWeights = nextLayer(newlayer,layerNum+1,isTraining,target);
			}
			//TODO: ???
			if(isTraining) {
				for(int a=0;a<newDW.length;a++) {
					for(int b=0;b<newDW[a].length;b++) {
						for(int c=0;c<newDW[a][b].length+1;c++) {
							//calculate weighted delta sum
							double weightedSum = 0;
							for(int d=0;d<weight.length;d++) {
								for(int i=0;i<deltaWeights.length;i++) {
									for(int j=0;j<deltaWeights.length;j++) {
										//TODO: ???
										weightedSum += weight[d][a][b][c]*deltaWeights[i][j][d];
										//update weights
										double delta = learningRate * layer[a][b][c] * deltaWeights[i][j][d];
										weight[d][a][b][c] += delta;										
									}
								}
								weightedSum = (layer[a][b][c] < 0.0000001 ? 0.0:1.0) * weightedSum;
								if(c!=newDW[0][0].length) {
									newDW[a][b][c] = weightedSum;								
								}															
							}
						}
					}
				}
				weights.set(layerNum,weight);
				deltaWeights = newDW;
			}
			return deltaWeights;
		//soft max
		case 5:
			layer = softmax(layer);
			if(layerNum<info.length-1) {
				deltaWeights = nextLayer(layer,layerNum+1,isTraining,target);
			}
			else {
				double max = 0;
				int pred = -1;
				for(double[][] a:layer) {
					for(double[] b:a) {
						for(int c=0;c<b.length-1;c++) {
							if(b[c]>max) {
								max = b[c];
								pred = c;
							}
						}
					}
				}
				//TODO: more output node
//				System.out.println("The prediction is: "+(pred == 0 ? "a car":"not a car"));
//				System.out.println(layer[0][0][0]+"  "+layer[0][0][1]);
					predCar += (pred == 0 ? 1:0)*target[0];
					predTree += (pred == 1 ? 1:0)*target[1];					
				if(init) {
					init = false;
				}
				if(isTraining) {
					double[] deltas = new double[target.length];
					for(int a=0;a<deltas.length;a++) {
						deltas[a] = target[a] - layer[0][0][a];
					}
					double[][][] d = {{deltas}};
					deltaWeights = d;
//					System.out.println("The delta is: "+deltas[0]+"  "+deltas[1]);
				}
			}
			return deltaWeights;
		//drop out
		case 6:
			if(isTraining) {
				int prob = info[layerNum][1][0];
				double probD = (double)prob/100.0;
				layer = dropout(layer, probD);
			}
			if(layerNum<info.length-1) {
				deltaWeights = nextLayer(layer,layerNum+1,isTraining,target);
			}
			return deltaWeights;
		//Average pooling
		case 7:
			layer = avgpool(layer, info[layerNum][1][0], info[layerNum][1][1], info[layerNum][1][2]);
			if(layerNum<info.length-1) {
				deltaWeights = nextLayer(layer,layerNum+1,isTraining,target);
			}
			return deltaWeights;
		default:
			throw new Exception("Unknown layer type");
		}
	}
	
	//TODO: check
	public static double[][][] convolutional(double[][][] layer, double[][][] weights, int x, int y, int stride, int size){
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
					layer[plane][row][val] = Math.max(layer[plane][row][val], 0);
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
	
	public static double[][][] avgpool(double[][][] layer,int x,int y,int stride){
		// x and y in new max-pooled layer
		int newx = (int)(Math.ceil(layer[0][0].length - x)/stride + 1);
		int newy = (int)(Math.ceil(layer[0].length - y)/stride + 1);
		int area = x*y;
		//create new layer
		double[][][] newLayer = new double[layer.length][newy][newx];
		for(int plane = 0; plane<layer.length; plane++) {
			for(int j=0;j<newy;j++) {
				//calculate corresponding y value
				int currenty = j*stride;
				for(int i =0;i<newx;i++) {
					//calculate corresponding x value
					int currentx = i*stride;
					double sum = 0;
					//find the max(pooling)
					for(int height = 0; height<y; height++) {
						if(currenty+height==layer[plane].length)
							break;
						for(int width = 0; width<x;width++) {
							if(currentx+width==layer[plane][currenty].length)
								break;
							sum+=layer[plane][currenty+height][currentx+width];
						}
					}
					//save to the new layer
					newLayer[plane][j][i]=sum/area;
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
	public static double[][][] weightedsum(double[][][] layer, double[][][][] weights, int size){
		double[][][] newLayer = new double[1][1][size+1];
		//add bias
		//dot weights for each node with layer and calculate weighted sum for each node(amount == size)
		for(int i=0;i<size-1;i++) {
			double weightedsum = 0;
			for(int z=0;z<layer.length;z++) {
				for(int y=0; y<layer[0].length;y++) {
					for(int x=0;x<layer[0][0].length;x++) {
						weightedsum += layer[z][y][x] * weights[i][z][y][x];
					}
				}
			}
			newLayer[0][0][i]=weightedsum;
		}
		//newLayer = normalize(newLayer);
		newLayer[0][0][size] = 1.0;		
		return newLayer;
	}
	
	public static double[][][] softmax(double[][][] layer) throws Exception{
		double sum = 0;
		for(int x=0;x<layer[0][0].length-1;x++) {
			sum+=Math.exp(layer[0][0][x]);
		}
		if(sum-0<=0.00000001) {
			throw new Exception("Divided by zero error!");
		}
		for(int i = 0; i<layer[0][0].length-1;i++) {
			layer[0][0][i] = Math.exp(layer[0][0][i])/sum;
		}
		return layer;
	}
	
//	public static double[][][] sigmoid(double[][][] mat){
//		int zl = mat.length, yl = mat[0].length, xl = mat[0][0].length;
//		double[][][] newLayer = new double[zl][yl][xl];
//		for(int z=0;z<zl;z++) {
//			for(int y=0;y<yl;y++) {
//				for(int x=0;x<xl;x++) {
//					newLayer[z][y][x] = Math.exp(mat[z][y][x]+1 * ;
//				}
//			}
//		}
//	}
	public static double[][][] normalize(double[][][] mat) {
		double total = 0;
		int nodes = 0;
		for(double[][]z:mat) {
			for(double[]y:z) {
				for(double x:y) {
					total+=Math.abs(x);
					nodes++;
				}
			}
		}
		int zl = mat.length, yl = mat[0].length, xl = mat[0][0].length;
		double[][][] newMat = new double[zl][yl][xl];
		for(int z=0;z<zl;z++) {
			for(int y=0;y<yl;y++) {
				for(int x=0;x<xl;x++) {
					newMat[z][y][x] = mat[z][y][x] * (double)nodes / total;
				}
			}
		}
		return newMat;
	}
	
	private static double[][][][] normalize(double[][][][] mat) {
		double total = 0;
		int nodes = 0;
		for(double[][][]z:mat) {
			for(double[][]y:z) {
				for(double[] x:y) {
					for(double w:x) {
						total+=Math.abs(w);
						nodes++;						
					}
				}
			}
		}
		int zl = mat.length, yl = mat[0].length, xl = mat[0][0].length, wl = mat[0][0][0].length;
		double[][][][] newMat = new double[zl][yl][xl][wl];
		for(int z=0;z<zl;z++) {
			for(int y=0;y<yl;y++) {
				for(int x=0;x<xl;x++) {
					for(int w=0;w<wl;w++) {
						newMat[z][y][x][w] = mat[z][y][x][w] * (double)nodes / total;						
					}
				}
			}
		}
		return newMat;
	}
}