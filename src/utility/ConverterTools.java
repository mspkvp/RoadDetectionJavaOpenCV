package utility;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

public class ConverterTools {
	
	public static BufferedImage Mat2Image(Mat mat){
		if( mat.empty() ) throw new NullPointerException();
		
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
    		Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2BGR);
        }
		
		img = new BufferedImage(w, h, imgType);
		mat.get(0, 0, dat);
		img.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), dat);
		return img;
	}
	
	public static Mat Image2Mat(BufferedImage image){
		Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
		byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		mat.put(0, 0, pixels);
		//Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2BGR);
		return mat;
	}
	
	public static Mat getGrayscale(Mat mat){
    	Mat result = new Mat();
    	Imgproc.cvtColor(mat, result, Imgproc.COLOR_BGR2GRAY);
    	return result;
    }
	
	public static Mat equalize(Mat mat, int type){
		Mat equalized = new Mat();
		Imgproc.cvtColor(mat, equalized, Imgproc.COLOR_BGR2GRAY );
        if( type == 0 )
        	Imgproc.equalizeHist(equalized, equalized);
        else {
        	CLAHE cl = Imgproc.createCLAHE();
        	cl.setClipLimit(8);
        	cl.apply(equalized, equalized);
        	
        }
        Imgproc.cvtColor(equalized, equalized, Imgproc.COLOR_GRAY2BGR );
        return equalized;
	}
	
	public static Mat gaussian(Mat mat){
		Mat result = new Mat();
		mat.copyTo(result);
		Imgproc.GaussianBlur(result, result, new Size(5,5), 0);
		return result;
	}
	
	static {
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
	
	/*public static void main(String[] args){
		Mat mat = Imgcodecs.imread("noisy.png");
		Mat blur = new Mat(), gaussian = new Mat(), median = new Mat(), bilateral = new Mat();
		
		mat.copyTo(blur);
		Imgproc.blur(blur, blur, new Size(5, 5));
		
		mat.copyTo(gaussian);
		Imgproc.GaussianBlur(gaussian, gaussian, new Size(5,5), 0);
		
		mat.copyTo(median);
		Imgproc.medianBlur(median, median, 3);
		
		Mat aux = new Mat();
		mat.copyTo(aux);
		Imgproc.bilateralFilter(aux, bilateral, 5, 10.0, 5/2.0);
		
		new Window("Original", mat).setVisible(true);
		new Window("Simple Blur", blur).setVisible(true);
		new Window("Gaussian Blur", gaussian).setVisible(true);
		new Window("Median Blur", median).setVisible(true);
		new Window("Bilateral", bilateral).setVisible(true);
	}*/
}
