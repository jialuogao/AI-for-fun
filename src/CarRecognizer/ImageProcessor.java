package CarRecognizer;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class ImageProcessor {
	/**
	 * scale image
	 * 
	 * @param sbi image to scale
	 * @param imageType type of image
	 * @param dWidth width of destination image(after scaling)
	 * @param dHeight height of destination image(after scaling)
	 * @param factorWidth x-factor for transformation / scaling
	 * @param factorHeight y-factor for transformation / scaling
	 * @return scaled image
	 */
	public static BufferedImage scale(BufferedImage sbi, int dWidth, int dHeight, double factorWidth, double factorHeight) {
	    BufferedImage dbi = null;
	    if(sbi != null) {
	        dbi = new BufferedImage(dWidth, dHeight, BufferedImage.TYPE_INT_RGB);
	        Graphics2D g = dbi.createGraphics();
	        g.drawImage(sbi, 0, 0, dWidth, dHeight, null);
	        g.dispose();
	    }
	    return dbi;
	}
	
	public static void compressImages(String inDir, String outDir) throws IOException{
		File dirin = new File(inDir);
        for (final File f : dirin.listFiles()) {
			InputStream inpStream = new BufferedInputStream(new FileInputStream(f));
			BufferedImage image = ImageIO.read(inpStream);
			
			int dWidth = 640;
			int dHeight = 480;
			double factorWidth = dWidth/image.getWidth();
			double factorHeight = dHeight/image.getHeight();
			image = ImageProcessor.scale(image, dWidth, dHeight, factorWidth, factorHeight);
			
			File dir = new File(outDir);
			if (! dir.exists()){
		        dir.mkdirs();
		    }
			File dirout = new File(outDir+f.getName());
			
			ImageIO.write(image, "jpg", dirout);
        }
	}
	
	public static <T> void print2dMatrix(T[][] matrix) {
		for(T[] y:matrix) {
			for(T x:y) {
				System.out.print(x+" ");
			}
			System.out.println();
		}
	}
	public static <T> void print3dMatrix(T[][][] matrix) {
		for(T[][] z:matrix) {
			print2dMatrix(z);
			System.out.println();
		}
	}
	
	public static void print2dMatrix(int[][] matrix) {
		for(int[] y:matrix) {
			for(int x:y) {
				System.out.print(x+" ");
			}
			System.out.println();
		}
	}
	public static void print3dMatrix(int[][][] matrix) {
		for(int[][] z:matrix) {
			print2dMatrix(z);
			System.out.println();
		}
	}
	
	public static void print2dMatrix(double[][] matrix) {
		for(double[] y:matrix) {
			for(double x:y) {
				System.out.print(x+" ");
			}
			System.out.println();
		}
	}
	public static void print3dMatrix(double[][][] matrix) {
		for(double[][] z:matrix) {
			print2dMatrix(z);
			System.out.println();
		}
	}
//	private static final String trainInDir = "cars_train/";
//	private static final String trainOutDir = "cars_train_compressed/";
//	private static final String testInDir = "cars_test/";
//	private static final String testOutDir = "cars_test_compressed/";
	private static final String trainInDir = "trees_train/";
	private static final String trainOutDir = "trees_train_compressed/";
	
	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
		compressImages(trainInDir,trainOutDir);
//		compressImages(testInDir,testOutDir);
		
	}
}
