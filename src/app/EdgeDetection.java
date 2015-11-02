package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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

	/*
	 * Represents a line
	 */
	public class Line {
		public Point p1, p2;
		public int slope_angle;
		public Line(Point p1, Point p2){
			this.p1 = p1;
			this.p2 = p2;
			slope_angle = (int) Math.round(Math.toDegrees((p2.y - p1.y) / (p2.x - p1.x)));
		}

		public String toString(){
			return new String("P1("+p1.x+","+p1.y+") P2("+p2.x+","+p2.y+")");
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

	/*
	 * Check if 2 lines intersect and if so, return the x coordinate of the intersection point
	 */
	private double checkIntersection(Line l1, double a1, double b1, Line l2, double a2, double b2){
		double x0 = -(b1-b2)/(a1-a2);
		if(Math.min(l1.p1.x, l1.p2.x) < x0
				&& x0 < Math.max(l1.p1.x, l1.p2.x)
				&& Math.min(l2.p1.x, l2.p2.x) < x0
				&& x0 < Math.max(l2.p1.x, l2.p2.x))
			return x0;
		else return -1;
	}
	
/*
 * Finds the medium point between 3 points
 */
	private Point calcMediumPoint(Point a, Point b, Point c){
		Point i = new Point();
		i.x =(	a.x*a.x*(-b.y) + a.x*a.x*c.y - a.y*a.y*b.y + a.y*a.y*c.y + a.y*b.x*b.x + a.y*b.y*b.y - a.y*c.x*c.x - a.y*c.y*c.y - b.x*b.x*c.y - b.y*b.y*c.y + b.y*c.x*c.x + b.y*c.y*c.y )/
				( 2*( -a.x*b.y + a.x*c.y + a.y*b.x - a.y*c.x - b.x*c.y + b.y*c.x ) );
		i.y = -( a.x*a.x*b.x - a.x*a.x*c.x - a.x*b.x*b.x - a.x*b.y*b.y + a.x*c.x*c.x + a.x*c.y*c.y + a.y*a.y*b.x - a.y*a.y*c.x + b.x*b.x*c.x - b.x*c.x*c.x - b.x*c.y*c.y + b.y*b.y*c.x )/
				( 2*( a.x*b.y - a.x*c.y - a.y*b.x + a.y*c.x + b.x*c.y - b.y*c.x) );
		return i;
	}
	
	@SuppressWarnings("unused")
	public void findVP(Mat img, List<Line> lleft, List<Line> lmid, List<Line> lright){
		double aL, bL, aR, bR, aM, bM;
		double xLR = -1, yLR = -1,
				xML = -1, yML = -1,
				xMR = -1, yMR = -1,
				xVP = -1, yVP = -1;
		Line dleft=null, dright=null, dmid=null;
		double area = Double.MAX_VALUE;
		System.out.println("L"+lleft.size()+ " R"+lright.size()+" M"+lmid.size());
	
		/*
		 * 2 Approaches for finding the vanishing point:
		 * + best angle selection from the group
		 * + 3 lines converging on a point
		 */
		if( lmid.size() < 0 || lleft.size() < 0 || lright.size() < 0){
			double leftAngleAVG = 0,
					rightAngleAVG = 0,
					midAngleAVG = 0;
			double slopeDiff = Double.MAX_VALUE;
			
			// calculate the average of the slopes
			for (int i=0; i<lleft.size(); i++) {
				leftAngleAVG += lleft.get(i).slope_angle;
			}
			leftAngleAVG = leftAngleAVG / lleft.size();
			
			for (int i=0; i<lright.size(); i++) {
				rightAngleAVG += lright.get(i).slope_angle;
			}
			rightAngleAVG = rightAngleAVG / lleft.size();
			
			for (int i=0; i<lmid.size(); i++) {
				midAngleAVG += lmid.get(i).slope_angle;
			}
			midAngleAVG = midAngleAVG / lmid.size();
			
			
			//Choose the Lines to draw based on the minimum slope difference from the average
			for (int i=0; i<lleft.size(); i++) {
				if( Math.abs(lleft.get(i).slope_angle - leftAngleAVG) < slopeDiff ){
					dleft = lleft.get(i);
					slopeDiff = Math.abs(lleft.get(i).slope_angle - leftAngleAVG);
				}
			}
			
			slopeDiff = Double.MAX_VALUE;
			for (int i=0; i<lright.size(); i++) {
				if( Math.abs(lright.get(i).slope_angle - rightAngleAVG) < slopeDiff ){
					dright = lright.get(i);
					slopeDiff = Math.abs(lright.get(i).slope_angle - rightAngleAVG);
				}
			}
			
			slopeDiff = Double.MAX_VALUE;
			for (int i=0; i<lmid.size(); i++) {
				if( Math.abs(lmid.get(i).slope_angle - midAngleAVG) < slopeDiff ){
					dmid = lmid.get(i);
					slopeDiff = Math.abs(lmid.get(i).slope_angle - midAngleAVG);
				}
			}
			
			// with 2 lines we can speculate a vanishing point
			if( lmid != null && lright != null){
				aR = (dright.p2.y-dright.p1.y)/(dright.p2.x-dright.p1.x);
				bR = dright.p1.y - aR * dright.p1.x;
				aM = (dmid.p2.y-dmid.p1.y)/(dmid.p2.x-dmid.p1.x);
				bM = dmid.p1.y - aM * dmid.p1.x;
				
				xVP = checkIntersection(dright, aR, bR, dmid, aM, bM);
				if( xVP != -1)
					yVP = aM * xML + bM;
			} else if ( lright != null && lleft != null) {
				aR = (dright.p2.y-dright.p1.y)/(dright.p2.x-dright.p1.x);
				bR = dright.p1.y - aR * dright.p1.x;
				aL = (dleft.p2.y-dleft.p1.y)/(dleft.p2.x-dleft.p1.x);
				bL = dleft.p1.y - aL * dleft.p1.x;
				
				xVP = checkIntersection(dright, aR, bR, dleft, aL, bL);
				if( xVP != -1)
					yVP = aL * xML + bL;
			} else if ( lmid == null && lleft != null ) {
				aL = (dleft.p2.y-dleft.p1.y)/(dleft.p2.x-dleft.p1.x);
				bL = dleft.p1.y - aL * dleft.p1.x;
				aM = (dmid.p2.y-dmid.p1.y)/(dmid.p2.x-dmid.p1.x);
				bM = dmid.p1.y - aM * dmid.p1.x;
				
				xVP = checkIntersection(dleft, aL, bL, dmid, aM, bM);
				if( xVP != -1)
					yVP = aM * xML + bM;
			}
			
		}
		else {
			try {
				// Lines represented as y = a*x + b
				for (int i=0; i<lleft.size(); i++) {
					Line left = lleft.get(i);
					aL = (left.p2.y-left.p1.y)/(left.p2.x-left.p1.x);
					bL = left.p1.y - aL * left.p1.x;

					for (int j=0; j<lright.size(); j++) {
						Line right = lright.get(j);
						aR = (right.p2.y-right.p1.y)/(right.p2.x-right.p1.x);
						bR = right.p1.y - aR * right.p1.x;

						xLR = checkIntersection(left, aL, bL, right, aR, bR);

						// no intersection
						if( xLR == -1)
							continue;

						// calculate the Y coordinate of the possible intersection point
						yLR = aR * xLR + bR;

						for (int k=0; k<lmid.size(); k++) {
							Line mid = lmid.get(k);
							aM = (mid.p2.y-mid.p1.y)/(mid.p2.x-mid.p1.x);
							bM = mid.p1.y - aM * mid.p1.x;

							// check if the middle line contains the point
							if((aM * xLR + bM) == yLR){
								// found the point, break all the iterations
								System.out.println("Found Perfect Intersection Point ("+xLR+","+yLR+")");
								throw new Exception();
							}

							// ---- calculate intersection points the the other 2 lines

							//IP Mid and Left Line
							xML = checkIntersection(mid, aM, bM, left, aL, bL);
							if( xML == -1)
								continue;
							yML = aM * xML + bM;

							//IP Mid and Right Line
							xMR = checkIntersection(mid, aM, bM, right, aR, bR);
							if( xMR == -1)
								continue;
							yMR = aM * xMR + bM;

							// ---- calculate area formed by the 3 points
							// A(xLR,yLR) B(xML,yML) C(xMR,yMR)
							double new_area = Math.abs((xLR*(yML-yMR)+xML*(yMR-yLR)+xMR*(yLR-yML))/2.0);
							if(new_area < area){
								dright = right;
								dleft = left;
								dmid = mid;
								area = new_area;
								Point midP = calcMediumPoint(
										new Point((xLR+xMR)/2,(yLR+yMR)/2),
										new Point((xMR+xML)/2,(yMR+yML)/2),
										new Point((xML+xLR)/2,(yML+yLR)/2)
										);
								xVP = midP.x;
								yVP = midP.y;
							}
						}
					}
				}				
			} catch (Exception e) {
				Imgproc.circle(img, new Point(xLR,yLR), 1, new Scalar(255,255,255));
			}
		}
		if( dleft != null){
			Imgproc.clipLine(
					new Rect(roiLeftClipping, img.height() - (int) (img.height()*roiRatioClipping-roiTopClipping), img.width()-roiRightClipping, (int) (img.height()*roiRatioClipping-roiTopClipping-roiBottomClipping)),
					dleft.p1, dleft.p2);
			Imgproc.line(img, dleft.p1, dleft.p2, new Scalar(0,0,255), 2);
		}
		if( dright != null){
			Imgproc.clipLine(
					new Rect(roiLeftClipping, img.height() - (int) (img.height()*roiRatioClipping-roiTopClipping), img.width()-roiRightClipping, (int) (img.height()*roiRatioClipping-roiTopClipping-roiBottomClipping)),
					dright.p1, dright.p2);
			Imgproc.line(img, dright.p1, dright.p2, new Scalar(0,0,255), 2);
		}
		if( dmid != null){
			Imgproc.clipLine(
					new Rect(roiLeftClipping, img.height() - (int) (img.height()*roiRatioClipping-roiTopClipping), img.width()-roiRightClipping, (int) (img.height()*roiRatioClipping-roiTopClipping-roiBottomClipping)),
					dmid.p1, dmid.p2);
			Imgproc.line(img, dmid.p1, dmid.p2, new Scalar(0,0,255), 2);
		}
		if( xVP != -1 && yVP != -1) Imgproc.circle(img, new Point(xVP,yVP), 1, new Scalar(0,255,0), 2);
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
		double a, b;
		double x0, y0;
		int imageSize = img.width() > img.height() ? img.width() : img.height();

		Vector<Line> lleft = new Vector<Line>();  
		Vector<Line> lright = new Vector<Line>();
		Vector<Line> lmid = new Vector<Line>();

		// if segmentation is enable, paint it on the main image before drawing the hough lines and VP
		if(segmentation)
		{
			Mat seg = new Mat();
			original_image.copyTo(seg);
			show(seg);
			rs.findRoad(seg).copyTo(img);
		}

		for (int x = 0; x < lines.rows(); x++) 
		{
			Point pt1 = new Point();
			Point pt2 = new Point();
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
			//Imgproc.line(img, pt1, pt2, color, 2);

			//middle line
			if( (theta>Math.toRadians(170) || theta<Math.toRadians(10))){
				Line l = new Line(pt1, pt2);
				lmid.add(l);
			}
			//right line
			else if( (theta>Math.toRadians(115) && theta<Math.toRadians(125))){
				lright.add(new Line(pt1, pt2));
			}
			//left line
			else if( (theta>Math.toRadians(65) && theta<Math.toRadians(75))){
				lleft.add(new Line(pt1, pt2));
			}
			//Imgproc.line(img, pt1, pt2, new Scalar(128,255,128), 2);
		}
		findVP(img, lleft, lmid, lright);

		thresholdImage.copyTo(canny);
		return img;
	}

	public Mat getProbabilistic(boolean segmentation){
		Mat img = new Mat();
		original_image.copyTo(img);

		Mat roi = new Mat(img, new Rect(roiLeftClipping, img.height() - (int) (img.height()*roiRatioClipping-roiTopClipping), img.width()-roiRightClipping, (int) (img.height()*roiRatioClipping-roiTopClipping-roiBottomClipping)));
		Mat thresholdImage = new Mat(roi.height() + roi.height() / 2, roi.width(), CvType.CV_8UC1);
		Imgproc.cvtColor(roi, thresholdImage, Imgproc.COLOR_RGB2GRAY, 4);
		Imgproc.GaussianBlur(thresholdImage, thresholdImage, new Size(gaussianBlurAmount,gaussianBlurAmount), 0);
		Imgproc.threshold(thresholdImage, thresholdImage,0,255,Imgproc.THRESH_BINARY+Imgproc.THRESH_OTSU);
		Imgproc.Canny(thresholdImage, thresholdImage, cannyThresholdLow, cannyThresholdHigh, 3, false);
		Imgproc.GaussianBlur(thresholdImage, thresholdImage, new Size(gaussianBlurAmount,gaussianBlurAmount), 0);
		Mat lines = new Mat();

		Imgproc.HoughLinesP(thresholdImage, lines, 1, Math.PI/180, houghThreshold, minLineLength, maxLineGap);

		// if segmentation is enabled, paint it on the main image before drawing the hough lines and VP
		if(segmentation)
		{
			Mat seg = new Mat();
			original_image.copyTo(seg);
			rs.findRoad(seg).copyTo(img);
		}
		
		Vector<Line> lleft = new Vector<Line>();  
		Vector<Line> lright = new Vector<Line>();
		Vector<Line> lmid = new Vector<Line>();

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
