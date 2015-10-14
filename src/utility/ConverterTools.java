package utility;

import java.awt.image.BufferedImage;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class ConverterTools {
	
	public static BufferedImage Mat2Image(Mat mat){
		BufferedImage img;
		byte[] dat;
		int w = mat.cols(), h = mat.rows();
		int imgType = BufferedImage.TYPE_3BYTE_BGR;
		
		if(mat.channels() == 1){
    		dat = new byte[w * h];
        	imgType = BufferedImage.TYPE_BYTE_GRAY;
        }
        else {
        	dat = new byte[w * h * 3];
        	imgType = BufferedImage.TYPE_3BYTE_BGR;
        }
		
		Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2BGR);
		
		img = new BufferedImage(w, h, imgType);
		mat.get(0, 0, dat);
		img.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), dat);
		return img;
	}
	
	public static Mat Image2Mat(BufferedImage image){
		return null;
		
	}
	
	public static Mat getGrayscale(Mat mat){
    	Mat result = new Mat();
    	Imgproc.cvtColor(mat, result, Imgproc.COLOR_BGR2GRAY);
    	return result;
    }
}
