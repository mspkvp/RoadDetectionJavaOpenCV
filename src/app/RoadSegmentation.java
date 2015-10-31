package app;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class RoadSegmentation {
	
	private int pixelOffset;
	private int pixelRange;
	
	public RoadSegmentation() {
		pixelOffset = 45;
		pixelRange = 40;
	}
	
	public RoadSegmentation(int pixelOffset, int pixelRange) {
		this.pixelOffset = pixelOffset;
		this.pixelRange = pixelRange;
	}
	
	/**
	 * Returns mat filtered with mean shift procedure.
	 * @param mat
	 * @return
	 */
	private Mat applyMeanShiftFiltering(Mat mat) {
		Imgproc.pyrMeanShiftFiltering(mat, mat, 20, 40);
		return mat;
	}
	
	/**
	 * Returns the sample mat of the road.
	 * @param mat
	 * @return
	 */
	private Mat getSampleMat(Mat mat) {
		Rect rect = new Rect(mat.width()/2, mat.height()/2, mat.width()/5, (int)(mat.height()*0.4));
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
		
		for (int i = 0; i < matSample.rows(); i++) {
			for (int j = 0; j < matSample.cols(); j++) {
				
				double[] pixel = matSample.get(i, j);
				
				//if pixel is grayish and not white
				if (pixel[0] < 250 && pixel[0] >= (pixel[1]-pixelOffset) && pixel[0] <= (pixel[1]+pixelOffset) && 
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
	 * Draws the biggest found contour on the original mat.
	 * @param matThresholded
	 * @param original
	 * @return
	 */
	private Mat drawContour(Mat matThresholded, Mat original) {
		List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        
        Imgproc.findContours(matThresholded, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
    	//Imgproc.findContours(matL, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0,0));

        double maxContourH = 0;
        int maxContourIndex = -1;
        if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {
            for (int i = 0; i >= 0; i = (int) hierarchy.get(0, i)[0]) {
            	if (contours.get(i).size().height > maxContourH) {
            		maxContourH = contours.get(i).size().height;
            		maxContourIndex = i;
            	}
            }
            Imgproc.drawContours(original, contours, maxContourIndex, new Scalar(255, 0, 0), -1);
            
        }
        return original;
	}
	
	/**
	 * Returns the provided mat with marked road.
	 * @param mat
	 * @return
	 */
	public Mat findRoad(Mat mat) {
		Mat original = new Mat();
		mat.copyTo(original);
		
		mat = applyMeanShiftFiltering(mat);		
		Mat matSample = getSampleMat(mat);
		List<Scalar> minMax = getAvgPixelVals(matSample);
		
		Mat matT = new Mat();
		Core.inRange(mat, minMax.get(0), minMax.get(1), matT);
		
		return drawContour(matT, original);
	}
	
}
