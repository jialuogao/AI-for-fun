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
	public static final String testingNegDir = "trees_test_compressed/";
	private static Random generator = new Random();
	//private static Node[][] network;
	private static ArrayList weights = new ArrayList();
	private static boolean init;
	private static double learningRate;
	public static void main(String[] args) throws Exception{
		if(args.length!=1) {
			System.out.println("Please input learning rate!");
            System.exit(-1);
		}
		learningRate = Double.parseDouble(args[0]);
		loadInfoDataFile();
		File dirin = new File(trainingPosDir);
		int versionCount = 0;
        for (final File f : dirin.listFiles()) {
			InputStream inpStream = new BufferedInputStream(new FileInputStream(f));
			BufferedImage image = ImageIO.read(inpStream);
			boolean isCar = true;
			train(image, isCar);
			versionCount++;
			if(versionCount==100) {
				writeDataFile();
				versionCount = 0;
			}
        }
        
        versionCount = 0;
        dirin = new File(trainingNegDir);
        for (final File f : dirin.listFiles()) {
			InputStream inpStream = new BufferedInputStream(new FileInputStream(f));
			BufferedImage image = ImageIO.read(inpStream);
			boolean isCar = false;
			train(image, isCar);
			versionCount++;
			if(versionCount==100) {
				writeDataFile();
				versionCount = 0;
			}
        }
		
        File testin = new File(testingPosDir);
        for (final File f : testin.listFiles()) {
			InputStream inpStream = new BufferedInputStream(new FileInputStream(f));
			BufferedImage image = ImageIO.read(inpStream);
			predict(image);
        }
        
        testin = new File(testingNegDir);
        for (final File f : testin.listFiles()) {
			InputStream inpStream = new BufferedInputStream(new FileInputStream(f));
			BufferedImage image = ImageIO.read(inpStream);
			predict(image);
        }
	}
	//Layer type: 0 Input,1 Convolutional, 2 ReLU, 3 Max Pooling, 4 Full, 5 Output Soft Max, 6 Droop out
	private static int[][][] info;
	
	private static final String infofile = "src/CarRecognizer/infoFile.txt";
	private static final String datadir = "src/CarRecognizer/Data/";
	
	public static void writeDataFile() throws Exception{
		//System.out.println("writeDataFile");
		for(int i=0;i<weights.size();i++) {
			if(weights.get(i)!=null) {
				if(weights.get(i).getClass().equals(new double[0][][].getClass())) {
					double[][][] weightmatrix = (double[][][])weights.get(i);
					BufferedWriter writer = new BufferedWriter(new FileWriter(datadir+i+".txt"));
					int z = weightmatrix.length, y = weightmatrix[0].length, x = weightmatrix[0][0].length;
					writer.write(z+" "+y+" "+x);
					writer.newLine();
					for(double[][] plane: weightmatrix) {
						String line = "";
						for(double[] row: plane) {
							for(double data: row) {
								line+=data+",";
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
					BufferedWriter writer = new BufferedWriter(new FileWriter(datadir+i+".txt"));
					int z = weightmatrix.length, y = weightmatrix[0].length, x = weightmatrix[0][0].length, a = weightmatrix[0][0][0].length;
					writer.write(z+" "+y+" "+x+" "+a);
					writer.newLine();
					for(double[][][] cube: weightmatrix) {
						for(double[][] plane: cube) {
							String line = "";
							for(double[] row: plane) {
								for(double data: row) {
									line+=data+",";
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
	
	public static void train(BufferedImage img, boolean isCar) throws Exception {
		//TODO: multivariable
		final double target = isCar ? 1 : 0;
		runNN(img, true,target);
	}
	
	public static void predict(BufferedImage img) throws Exception{
		runNN(img, false, 0);
	}
	
	public static void runNN(BufferedImage img, boolean isTraining, double target) throws Exception{
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
		if(init) {
			for(int i = 0; i<info.length;i++) {
				weights.add(null);
			}
		}
		nextLayer(layer,0,isTraining,target);
	}

	public static double[][][] nextLayer(double[][][] layer, int layerNum, boolean isTraining, final double target) throws Exception{
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
			if(init) {
				//System.out.println("init");
				double[][][] weightmatrix = new double[layer.length][y][x];
				for(int z=0;z<weightmatrix.length;z++) {
					for(int height=0;height<y;height++) {
						for(int width=0;width<x;width++) {
							weightmatrix[z][height][width]=generator.nextDouble();
						}
					}
				}
				weights.set(layerNum, weightmatrix);
				writeDataFile();
				
			}
			double[][][] weightConv = (double[][][])weights.get(layerNum);
			layer = convolutional(layer, weightConv, x, y, stride, info[layerNum][1][3]);
			if(layerNum<info.length-1) {
				deltaWeights = nextLayer(layer,layerNum+1,isTraining,target);
			}
			if(isTraining) {
				double[][][] newDW = new double[layer.length][layer[0].length][layer[0][0].length];
				//all the forward weights, dimension for convol
				for(int j=0;j<weightConv[0].length;j++) {
					for(int i =0;i<weightConv[0][j].length;i++) {
						for(int a=0;a<deltaWeights.length;a++) {
							//filter weight matrix * error of next layer = error of first layer
							for(int b=0;b<deltaWeights[0].length-weightConv[0].length;b++) {
								for(int c=0;c<deltaWeights[0][0].length-weightConv[0][0].length;c++) {
									newDW[a][b][c] += deltaWeights[a][b+i][c+j] * weightConv[a][j][i];
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
							for(int b=i;b<newDW[0].length-weightConv[0].length+i;b++) {
								for(int c=j;c<newDW[0][0].length-weightConv[0][0].length+j;c++) {
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
				double[][][][] weightmatrix = new double[size+1][layer.length][layer[0].length][layer[0][0].length];
				for(int node = 0;node<weightmatrix.length;node++) {
					for(int k=0;k<weightmatrix[node].length;k++) {
						for(int j=0;j<weightmatrix[node][k].length;j++) {
							for(int i=0;i<weightmatrix[node][k][j].length;i++) {
								weightmatrix[node][k][j][i]=generator.nextDouble();
							}
						}
					}
				}
				weights.set(layerNum, weightmatrix);
				writeDataFile();
			}
			double [][][][] weight = (double[][][][])weights.get(layerNum);
			layer = weightedsum(layer, weight, size);
			if(layerNum<info.length-1) {
				deltaWeights = nextLayer(layer,layerNum+1,isTraining,target);
			}
			//TODO: ???
			if(isTraining) {
				double[][][] newDW = new double[layer.length][layer[0].length][layer[0][0].length];
				for(int a=0;a<layer.length;a++) {
					for(int b=0;b<layer[a].length;b++) {
						for(int c=0;c<layer[a][b].length;c++) {
							//calculate weighted delta sum
							double weightedSum = 0;
							for(int i=0;i<weight[0].length;i++) {
								for(int j=0;j<weight[0][i].length;j++) {
									for(int d=0;d<weight.length-1;d++) {
										//TODO: ???
										weightedSum += weight[d][a][b][c]*deltaWeights[i][j][d];										
										//update weights
										double delta = learningRate * layer[a][b][c] * deltaWeights[i][j][d];
										weight[d][a][b][c] += delta;
									}
								}
							}
							weightedSum = Math.max(layer[a][b][c], 0) * weightedSum;
							newDW[a][b][c] = weightedSum;
						}
					}
				}
				deltaWeights = newDW;
				
			}
			return deltaWeights;
		//soft max
		case 5:
			layer = softmax(layer);
			if(layerNum<info.length-1) {
				nextLayer(layer,layerNum+1,isTraining,target);
			}
			else {
				System.out.println("Prediction ended at layer "+layerNum);
				System.out.println("The prediction is:");
				double pred = layer[0][0][0];
				System.out.println(pred >= 0.5 ? "is a car":"is not a car");
				if(init) {
					init = false;
				}
				if(isTraining) {
					double delta = target - pred;
					double[][][] d = {{{delta}}};
					deltaWeights = d;
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
	public static double[][][] weightedsum(double[][][] layer, double[][][][] weights, int size){
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