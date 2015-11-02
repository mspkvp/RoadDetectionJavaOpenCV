package app;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import utility.Window;

public class RoadSegmentation {
	
	public int pixelOffset = 45;
	public int pixelRange = 40;
	public int contourNumber = 1;
	
	public int roiTopClipping = 30;
	public int roiBottomClipping = 30;
	public int roiLeftClipping = 0;
	public int roiRightClipping = 0;
	public double roiRatioClipping = 0.6;
	
	public RoadSegmentation() {}
	
	public RoadSegmentation(int pixelOffset, int pixelRange, int contourNumber) {
		this.pixelOffset = pixelOffset;
		this.pixelRange = pixelRange;
		this.contourNumber = contourNumber;
	}
	
	/**
	 * Returns mat filtered with mean shift procedure.
	 * @param mat
	 * @return
	 */
	private Mat applyMeanShiftFiltering(Mat mat) {
		Mat ret = new Mat();
		Imgproc.pyrMeanShiftFiltering(mat, ret, 20, 40);
		return ret;
	}
	
	/**
	 * Returns the sample matrix of the road.
	 * @param mat
	 * @return
	 */
	private Mat getSampleMat(Mat mat) {
		//Rect rect = new Rect(mat.width()/2, mat.height()/2, mat.width()/5, (int)(mat.height()*0.4));
		Rect rect = new Rect(mat.width()/3, mat.height()/2, mat.width()/4, (int)(mat.height()*0.4));
		
		return new Mat(mat, rect);
	}
	
	/**
	 * Calculates the min and max pixel value from the sample mat
	 * @param matSample
	 * @return
	 */
	private List<Scalar> getAvgPixelVals(Mat matSample) {
		double[] min = new double[]{255,255,255};
		double[] max = new double[]{0,0,0};
		
		Mat matHSV = new Mat();
		matSample.convertTo(matHSV, Imgproc.COLOR_BGR2HSV);
		
		for (int i = 0; i < matSample.rows(); i++) {
			for (int j = 0; j < matSample.cols(); j++) {
				
				double[] pixel = matSample.get(i, j);
				double sat = matHSV.get(i, j)[0];
				//if pixel is not white and gray
				if (pixel[0] < 160 && pixel[0] >= (pixel[1]-pixelOffset) && pixel[0] <= (pixel[1]+pixelOffset) && 
						pixel[0] >= (pixel[2]-pixelOffset) && pixel[0] <= (pixel[2]+pixelOffset))  
					
					for (int x = 0; x < pixel.length; x++) {
						if (pixel[x] < min[x]) {
							min[x] = pixel[x];
						}
						if (pixel[x] > max[x]) {
							max[x] = pixel[x];
						}
					}				
			}
		}

		Scalar minRange = new Scalar((max[0]+min[0])/2-pixelRange, (max[1]+min[1])/2-pixelRange, (max[2]+min[2])/2-pixelRange);
		Scalar maxRange = new Scalar((max[0]+min[0])/2+pixelRange, (max[1]+min[1])/2+pixelRange, (max[2]+min[2])/2+pixelRange);
		
		//2nd option, less code and probably faster but if matSample contains any other pixels 
		//than the one from the road (e.g. line, background), 1st option is better.
		//Scalar mean = Core.mean(matSample);
		//minRange = new Scalar(mean.val[0]-diff, mean.val[1]-diff, mean.val[2]-diff);
		//maxRange = new Scalar(mean.val[0]+diff, mean.val[1]+diff, mean.val[2]+diff);
		
		return new ArrayList<Scalar>() {{
			add(minRange); add(maxRange);
		}};
	}
	/**
	 * Returns the index of the biggest contour.
	 * @param contours
	 * @param hierarchy
	 * @return
	 */
	private int getMaxContourIndex(List<MatOfPoint> contours, Mat hierarchy) {
		double maxContourH = 0;
        int maxContourIndex = -1;
        
		for (int i = 0; i < contours.size(); i++) {
        	if (contours.get(i).size().height > maxContourH) {
        		maxContourH = contours.get(i).size().height;
        		maxContourIndex = i;
        	}
        }
		return maxContourIndex;
	}
	
	/**
	 * Draws the biggest found contour on the original mat.
	 * @param matThresholded
	 * @param original
	 * @return
	 */
	private Mat drawContour(Mat matThresholded, Mat original) {
		List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Mat mat = new Mat(original, new Rect(roiLeftClipping, 
        		original.height() - (int) (original.height()*roiRatioClipping-roiTopClipping), 
        		original.width()-roiRightClipping, 
        		(int) (original.height()*roiRatioClipping-roiTopClipping-roiBottomClipping)));
		
        Imgproc.findContours(matThresholded, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
    	
        if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {
        	for (int i = 0; i < contourNumber; i++) {
        		int maxContourIndex = getMaxContourIndex(contours, hierarchy);
            	Imgproc.drawContours(mat, contours, maxContourIndex, new Scalar(255, 0, 0), -1);
            	contours.remove(maxContourIndex);
        	}
        }
        mat.copyTo(original.col(roiLeftClipping).row(original.height() - (int) (original.height()*roiRatioClipping-roiTopClipping)));
        return original;
	}
	
	/**
	 * Returns the provided matrix with marked road.
	 * @param mat
	 * @return
	 */
	public Mat findRoad(Mat mat) {
		Mat original = new Mat();
		mat.copyTo(original);
		//clip image to the region of interest
		Rect roiRect = new Rect(roiLeftClipping, 
				mat.height() - (int) (mat.height()*roiRatioClipping-roiTopClipping), 
				mat.width()-roiRightClipping, 
				(int) (mat.height()*roiRatioClipping-roiTopClipping-roiBottomClipping));
		mat = new Mat(mat, roiRect);		
		//apply mean-shift filter
		mat = applyMeanShiftFiltering(mat);		
		//get sample matrix of the road
		Mat matSample = getSampleMat(mat);
		//get min and max pixel values of the matrix
		List<Scalar> minMax = getAvgPixelVals(matSample);
		Mat matT = new Mat();
		//image sharpening
		Mat tmp = new Mat();
		Imgproc.GaussianBlur(mat, tmp, new Size(0,0), 4);
		Core.addWeighted(mat, 1.5, tmp, -0.5, 0, mat);
		//dilation
		int size = 5;
		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2*size + 1, 2*size+1));
        Imgproc.dilate(mat, mat, element);
        //create a road mask based on the previously extracted values
		Core.inRange(mat, minMax.get(0), minMax.get(1), matT);
		//draw road contour on the original image
		return drawContour(matT, original);
	}
	
	/**
	 * Displays image in a new window. For debugging purposes. 
	 * @param mat
	 */
	private void show(Mat mat, String title){
		Mat r = new Mat();
		mat.copyTo(r);
		new Window(title, r).setVisible(true);
	}
	
}
