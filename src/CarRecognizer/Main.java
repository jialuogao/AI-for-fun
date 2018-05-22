package CarRecognizer;

import java.io.IOException;
//image size 640x480
public class Main {

	public static final String trainingDir = "cars_train/";
	public static final String testingDir = "cars_test/";
	
	
	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
		
		
		
	}
	//Layer type: 0 Input,1 Convolutional, 2 ReLU, 3 Max Pooling, 4 Local Response Normalization, 5 Full, 6 Output
	private int[][][] NNInfo = 
		{//Type of the layer, Layers it connects to, Layer Information
/*0*/			{{0},{1,4....}},			//Input	640x480

/*1*/			{{1},{2},{11,11,5,12}},		//Convol: filter size x, y, stride, node size	127*95*12
/*2*/			{{2},{3}},					//ReLU
/*3*/			{{3},{4},{2,2}},			//MaxPool	64*48*12
/*4*/			{{4},{5,9},....},			//LRNorm

/*5*/			{{1},{6},{5,5,1,18}},		//Convol	60*44*18
/*6*/			{{2},{7}},
/*7*/			{{3},{8},{2,2}},			//			30*22*18
/*8*/			{{4},{9,10},....}

/*9*/			{{5},{10,11},....}			//Full		240
/*10*/			{{5},{11,15},....}			//Full		240

/*11*/			{{1},{12},{3,3,1,48}},		//			28*20*48
/*12*/			{{2},{13}},
/*13*/			{{3},{14},{2,2}},			//			14*10*48
/*14*/			{{4},{15,19},....},

/*15*/			{{1},{16},{3,3,1,48}},		//			12*8*48
/*16*/			{{2},{17}},
/*17*/			{{3},{18},{2,2}},			//			6*4*48
/*18*/			{{4},{19,20},....},

/*19*/			{{5},{20,21},....},			//			24
/*20*/			{{5},{21},....},			//			18
/*21*/			{{5},{22},....},			//			6
/*22*/			{{6},{}},					//			1
		};

	public static void buildNN() {

	}

}