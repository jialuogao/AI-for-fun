package CarRecognizer;

import java.io.IOException;
import java.util.Random;
//image size 640x480
public class Main {

	public static final String trainingDir = "cars_train/";
	public static final String testingDir = "cars_test/";
	private static Random generator = new Random();
	private static Node[][] network;
	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
		
		buildNN(NNInfo);
		
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