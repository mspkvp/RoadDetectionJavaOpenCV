package utility;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

public class HoughLines {
	
	private Mat original_image, canny;
	
	public int roiTopClipping = 30;
	public int roiBottomClipping = 30;
	public int roiLeftClipping = 0;
	public int roiRightClipping = 0;
	public double roiRatioClipping = 0.6;
	public int cannyThresholdLow = 130;
	public int cannyThresholdHigh = 270;
	public int houghThreshold = 100;
	public int minLineLength = 25;
	public int maxLineGap = 25;
	public int gaussianBlurAmount = 3;
	
	public HoughLines() { 
		original_image = new Mat();
		canny = new Mat();
	}
	
	public HoughLines(Mat mat){
		original_image = new Mat();
		canny = new Mat();
		mat.copyTo(original_image);
	}
	
	public void setImage(Mat img){
		img.copyTo(original_image);
	}
	
	public Mat getCanny(){
		Mat res = new Mat();
		canny.copyTo(res);
		return res;
	}
	
	public Mat getNormal(){
		Mat img = new Mat();
		original_image.copyTo(img);
		
		Mat roi = new Mat(img, new Rect(roiLeftClipping, img.height() - (int) (img.height()*roiRatioClipping-roiTopClipping), img.width()-roiRightClipping, (int) (img.height()*roiRatioClipping-roiTopClipping-roiBottomClipping)));
		Mat thresholdImage = new Mat(roi.height() + roi.height() / 2, roi.width(), CvType.CV_8UC1);
	    Imgproc.cvtColor(roi, thresholdImage, Imgproc.COLOR_RGB2GRAY, 4);
	    Imgproc.GaussianBlur(thresholdImage, thresholdImage, new Size(gaussianBlurAmount,gaussianBlurAmount), 0);
	    Imgproc.Canny(thresholdImage, thresholdImage, cannyThresholdLow, cannyThresholdHigh);
	    
	    Mat lines = new Mat();
	    Imgproc.HoughLines(thresholdImage, lines, 1, Math.PI/180, houghThreshold);

	    Scalar color = new Scalar(0, 0, 255);
	    double[] data;
	    double rho, theta;
	    Point pt1 = new Point();
	    Point pt2 = new Point();
	    double a, b;
	    double x0, y0;
	    int imageSize = img.width() > img.height() ? img.width() : img.height();
	    for (int x = 0; x < lines.rows(); x++) 
	    {
	        data = lines.get(x, 0);
	        rho = data[0];
	        theta = data[1];
	        a = Math.cos(theta);
	        b = Math.sin(theta);
	        x0 = a*rho;
	        y0 = b*rho;
	        pt1.x = Math.round(x0 + imageSize*(-b))+roiLeftClipping;
	        pt1.y = Math.round(y0 + imageSize*a)+ img.height() *(1-roiRatioClipping)+roiTopClipping;
	        pt2.x = Math.round(x0 - imageSize*(-b))+roiLeftClipping;
	        pt2.y = Math.round(y0 - imageSize *a) + img.height() *(1-roiRatioClipping)+roiTopClipping;
	        Imgproc.line(img, pt1, pt2, color, 2);
	    }
	    thresholdImage.copyTo(canny);
	    return img;
	}
	
	public Mat getProbabilistic(){
		Mat img = new Mat();
		original_image.copyTo(img);
		
		Mat roi = new Mat(img, new Rect(roiLeftClipping, img.height() - (int) (img.height()*roiRatioClipping-roiTopClipping), img.width()-roiRightClipping, (int) (img.height()*roiRatioClipping-roiTopClipping-roiBottomClipping)));
		Mat thresholdImage = new Mat(roi.height() + roi.height() / 2, roi.width(), CvType.CV_8UC1);
	    Imgproc.cvtColor(roi, thresholdImage, Imgproc.COLOR_RGB2GRAY, 4);
	    Imgproc.GaussianBlur(thresholdImage, thresholdImage, new Size(gaussianBlurAmount,gaussianBlurAmount), 0);
	    Imgproc.Canny(thresholdImage, thresholdImage, cannyThresholdLow, cannyThresholdHigh);
		/*CLAHE cl = Imgproc.createCLAHE();
        cl.setClipLimit(16);
        cl.apply(thresholdImage, thresholdImage);*/
	    Mat lines = new Mat();

	    Imgproc.HoughLinesP(thresholdImage, lines, 1, Math.PI/180, houghThreshold, minLineLength, maxLineGap);

	    for (int x = 0; x < lines.rows(); x++) 
	    {
	          double[] vec = lines.get(x, 0);
	          double x1 = vec[0], 
	                 y1 = vec[1],
	                 x2 = vec[2],
	                 y2 = vec[3];
	          Point start = new Point(x1+roiLeftClipping, y1 + img.height() *(1-roiRatioClipping)+roiTopClipping);
	          Point end = new Point(x2+roiLeftClipping, y2 +  img.height()*(1-roiRatioClipping)+roiTopClipping);

	          Imgproc.line(img, start, end, new Scalar(255,0,0), 2);

	    }
	    thresholdImage.copyTo(canny);
	    return img;
	}

	public Mat getOriginal() {
		return original_image;
	}
	
}
