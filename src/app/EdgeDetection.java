package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.RowFilter.Entry;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import utility.Window;

public class EdgeDetection {
	
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
	private RoadSegmentation rs;
	
	static class Line {
		public Point p1, p2;
		public int slope_angle;
		public Line(Point p1, Point p2){
			this.p1 = p1;
			this.p2 = p2;
			slope_angle = (int) Math.round(Math.toDegrees((p2.y - p1.y) / (p2.x - p1.x)));
		}
		
	}	
	
	public EdgeDetection() { 
		original_image = new Mat();
		canny = new Mat();
		rs = new RoadSegmentation();
	}
	
	public EdgeDetection(Mat mat){
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
	
	private double checkIntersection(Line l1, double a1, double b1, Line l2, double a2, double b2){
		double x0 = -(b1-b2)/(a1-a2);
		if(Math.min(l1.p1.x, l1.p2.x) < x0
				&& x0 < Math.max(l1.p1.x, l1.p2.x)
				&& Math.min(l2.p1.x, l2.p2.x) < x0
				&& x0 < Math.max(l2.p1.x, l2.p2.x))
			return x0;
		else return -1;
	}
	
	public void findVP_drawLines(Mat img, List<Line> lleft, List<Line> lmid, List<Line> lright){
		double aL, bL, aR, bR, aM, bM;
		double xI = -1, yI = -1;
		Line dleft=null, dright=null, dmid=null;
		System.out.println(lleft.size()+ " "+lright.size()+" "+lmid.size());
		try {
			// Lines represented as y = a*x + b
			for (Line left : lleft) {
				aL = (left.p2.y-left.p1.y)/(left.p2.x-left.p1.x);
				bL = left.p1.y - aL * left.p1.x;
				dleft = left;
				System.out.println("Current left slope: "+aL);
				
				for (Line right : lright) {
					aR = (right.p2.y-right.p1.y)/(right.p2.x-right.p1.x);
					bR = right.p1.y - aR * right.p1.x;
					dright = right;
					System.out.println("Current right slope: "+aR);
					
					xI = checkIntersection(left, aL, bL, right, aR, bR);
					System.out.println("INTER "+xI);
					if( xI == -1)
						continue;
					
					// calculate the Y coordinate of the possible intersection point
					yI = aR * xI + bR;
					
					if(lmid.size() > 0){
						for (Line mid : lmid) {
							aM = (mid.p2.y-mid.p1.y)/(mid.p2.x-mid.p1.x);
							bM = mid.p1.y - aM * mid.p1.x;
							dmid = mid;
							System.out.println("Current mid slope: "+aM);
							
							// check if the middle line contains the point
							if((aM * xI + bM) == yI){
								// found the point, break all the iterations
								System.out.println("found point ("+xI+","+yI+")");
								throw new Exception();
							}
						}
					}
					else break;
				}
			}
		} catch (Exception e) {
			//Imgproc.circle(img, new Point(xI,yI), 1, new Scalar(0,0,255));
		}
		
		if( dleft != null) Imgproc.line(img, dleft.p1, dleft.p2, new Scalar(255,0,0), 2);
		if( dright != null) Imgproc.line(img, dright.p1, dright.p2, new Scalar(255,0,0), 2);
		if( dmid != null) Imgproc.line(img, dmid.p1, dmid.p2, new Scalar(255,0,0), 2);
	}
	
	public Mat getNormal(boolean segmentation){
		Mat img = new Mat();
		original_image.copyTo(img);
		
		Mat roi = new Mat(img, new Rect(roiLeftClipping, img.height() - (int) (img.height()*roiRatioClipping-roiTopClipping), img.width()-roiRightClipping, (int) (img.height()*roiRatioClipping-roiTopClipping-roiBottomClipping)));
		Mat thresholdImage = new Mat(roi.height() + roi.height() / 2, roi.width(), CvType.CV_8UC1);
	    Imgproc.cvtColor(roi, thresholdImage, Imgproc.COLOR_RGB2GRAY, 4);
	    Imgproc.GaussianBlur(thresholdImage, thresholdImage, new Size(gaussianBlurAmount,gaussianBlurAmount), 0);
	    Imgproc.threshold(thresholdImage, thresholdImage,0,255,Imgproc.THRESH_BINARY+Imgproc.THRESH_OTSU);
	    Imgproc.Canny(thresholdImage, thresholdImage, cannyThresholdLow, cannyThresholdHigh);
	    Imgproc.GaussianBlur(thresholdImage, thresholdImage, new Size(gaussianBlurAmount,gaussianBlurAmount), 0);
	    
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
	    
	    /*List<Line> lleft = new ArrayList<Line>(),
	    		lright = new ArrayList<Line>(),
	    		lmid = new ArrayList<Line>();*/
	    
	    // if segmentation is enable, paint it on the main image befora drawing the hough lines and VP
	    if(segmentation)
	    {
	    	Mat seg = new Mat();
			original_image.copyTo(seg);
	    	rs.findRoad(seg).copyTo(img);
	    }
	    
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
/*
	        //middle line
	        if( (theta>Math.toRadians(170) || theta<Math.toRadians(10))){
	        	lmid.add(new Line(pt1, pt2));
	        	//Imgproc.line(img, pt1, pt2, color, 2);
	        }
	        else if( (theta>Math.toRadians(115) && theta<Math.toRadians(125))){
	        	lright.add(new Line(pt1, pt2));
	        	//Imgproc.line(img, pt1, pt2, color, 2);
	        }
	        else if( (theta>Math.toRadians(65) && theta<Math.toRadians(75))){
	        	lleft.add(new Line(pt1, pt2));
	        	//Imgproc.line(img, pt1, pt2, color, 2);
	        }*/
	    }
	    //findVP_drawLines(img, lleft, lmid, lright);

	    thresholdImage.copyTo(canny);
	    return img;
	}
	
	public Mat getProbabilistic(boolean segmentation){
		Mat img = new Mat();
		original_image.copyTo(img);
		
		Mat roi = new Mat(img, new Rect(roiLeftClipping, img.height() - (int) (img.height()*roiRatioClipping-roiTopClipping), img.width()-roiRightClipping, (int) (img.height()*roiRatioClipping-roiTopClipping-roiBottomClipping)));
		Mat thresholdImage = new Mat(roi.height() + roi.height() / 2, roi.width(), CvType.CV_8UC1);
	    Imgproc.cvtColor(roi, thresholdImage, Imgproc.COLOR_RGB2GRAY, 4);
	    //show(thresholdImage);
	    Imgproc.GaussianBlur(thresholdImage, thresholdImage, new Size(gaussianBlurAmount,gaussianBlurAmount), 0);
	    //show(thresholdImage);
	    Imgproc.threshold(thresholdImage, thresholdImage,0,255,Imgproc.THRESH_BINARY+Imgproc.THRESH_OTSU);
	    //show(thresholdImage);
	    Imgproc.Canny(thresholdImage, thresholdImage, cannyThresholdLow, cannyThresholdHigh, 3, false);
	    //show(thresholdImage);
	    Imgproc.GaussianBlur(thresholdImage, thresholdImage, new Size(gaussianBlurAmount,gaussianBlurAmount), 0);
	    //show(thresholdImage);
	    Mat lines = new Mat();

	    Imgproc.HoughLinesP(thresholdImage, lines, 1, Math.PI/180, houghThreshold, minLineLength, maxLineGap);
	    
	    // if segmentation is enable, paint it on the main image befora drawing the hough lines and VP
	    if(segmentation)
	    {
	    	Mat seg = new Mat();
			original_image.copyTo(seg);
	    	rs.findRoad(seg).copyTo(img);
	    }
	    
	    for (int x = 0; x < lines.rows(); x++) 
	    {
	          double[] vec = lines.get(x, 0);
	          double x1 = vec[0], 
	                 y1 = vec[1],
	                 x2 = vec[2],
	                 y2 = vec[3];
	          Point start = new Point(x1+roiLeftClipping, y1 + img.height() *(1-roiRatioClipping)+roiTopClipping);
	          Point end = new Point(x2+roiLeftClipping, y2 +  img.height()*(1-roiRatioClipping)+roiTopClipping);

	          Imgproc.line(img, start, end, new Scalar(0,255,0), 2);
	    }
	    
	    thresholdImage.copyTo(canny);
	    return img;
	}

	public Mat getOriginal() {
		return original_image;
	}
	
	private void show(Mat m){
		Mat r = new Mat();
		m.copyTo(r);
		new Window("img", r).setVisible(true);
	}
	
}
