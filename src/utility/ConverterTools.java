package utility;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
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
		Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2BGR);
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
	
	static {
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
	
	/*public static void main(String[] args){
		Mat mat = Imgcodecs.imread("Horario.png");
		Window win = new Window("original", mat);
		win.setVisible(true);
		
		Mat mat2 = ConverterTools.Image2Mat(ConverterTools.Mat2Image(mat));
		Window wind = new Window("converted", mat2);
		wind.setVisible(true);
		
	}*/
}
