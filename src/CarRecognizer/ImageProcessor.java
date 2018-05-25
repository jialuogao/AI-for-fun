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
	
	public static void compressImages(String inDir, String outDir, int imgCount) throws IOException{
		for(int dataNum = 1; dataNum <= imgCount; dataNum++) {
			String num = "0000"+dataNum;
			num = num.substring(num.length()-5,num.length());
			String fileName = num+".jpg";
			
			File img = new File(inDir+fileName);
			if(!img.exists()) {
				break;
			}
			InputStream inpStream = new BufferedInputStream(new FileInputStream(inDir+fileName));
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
			File file = new File(outDir+fileName);
			
			ImageIO.write(image, "jpg", file);
		}
	}
	
	private static final String trainInDir = "cars_train/";
	private static final String trainOutDir = "cars_train_compressed/";
	private static final String testInDir = "cars_test/";
	private static final String testOutDir = "cars_test_compressed/";
	
	private static final int totleCount = 8144;
	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
		compressImages(trainInDir,trainOutDir,totleCount);
		compressImages(testInDir,testOutDir,totleCount);
		
	}
}
