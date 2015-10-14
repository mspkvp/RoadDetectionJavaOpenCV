package utility;

/**
 * Class Mat2Image
 * @description Converts a Mat object to a Java BufferedImage
 * Adapted from source: http://computervisionandjava.blogspot.pt/2013/10/java-opencv-webcam.html 
 */

import java.awt.image.BufferedImage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Size;


public class Mat2Image {
    Mat mat = new Mat();
    BufferedImage img;
    byte[] dat;
    
    public Mat2Image() {
    }
    
    public Mat2Image(Mat mat) {
        getSpace(mat);
    }
    
    
    private void matPrep(Mat mat, boolean edge){
    	if(!edge)
    		Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2BGR);
    	else {
    		edgeCanny(mat).copyTo(mat);
    	}
    }
    
    public void getSpace(Mat mat) {
        this.mat = mat;
        int w = mat.cols(), h = mat.rows();
        int imgType = BufferedImage.TYPE_3BYTE_BGR;
        
        if (dat == null){
        	if(mat.channels() == 1){
        		dat = new byte[w * h];
            	imgType = BufferedImage.TYPE_BYTE_GRAY;
            }
            else {
            	dat = new byte[w * h * 3];
            	imgType = BufferedImage.TYPE_3BYTE_BGR;
            }
        }

	    if (img == null || img.getWidth() != w || img.getHeight() != h)
	       img = new BufferedImage(w, h, imgType);
    }
    
    BufferedImage getImage(Mat mat, boolean edge){
    	matPrep(mat, edge);
        getSpace(mat);
        mat.get(0, 0, dat);
        img.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), dat);
        return img;
    }
    
    BufferedImage getImage(Mat mat){
        getSpace(mat);
        mat.get(0, 0, dat);
        img.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), dat);
        return img;
    }
    
    public static Mat cvtGrayscale(Mat mat){
    	Mat result = new Mat();
    	Imgproc.cvtColor(mat, result, Imgproc.COLOR_BGR2GRAY);
    	return result;
    }
    
    public static Mat edgeCanny(Mat mat){
    	Mat result = new Mat();
    	mat.copyTo(result);
    	
    	if(result.channels() > 1) 
    		cvtGrayscale(result).copyTo(result);
    	
    	Imgproc.GaussianBlur(result, result, new Size(639,479), 1.5, 1.5);
    	Imgproc.Canny(result, result, 0.0, 30);
    	return result;
    }
    
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
}