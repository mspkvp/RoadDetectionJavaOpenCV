package utility;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Histogram {

    List<Mat> hsv_planes = new ArrayList<Mat>();
    MatOfInt histSize = new MatOfInt(256); // bins
    final MatOfFloat histRange = new MatOfFloat(0f, 256f); // HSV Ranges
    boolean accumulate = false;
    Mat h_hist, s_hist, v_hist;
    Window histWindow;

	public Histogram(Mat mat) throws NullPointerException {

        if(mat.empty()) throw new NullPointerException();

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2HSV);

        h_hist = new Mat();
        s_hist = new Mat();
        v_hist = new Mat();

        Core.split(mat, hsv_planes);

        List<Mat> auxList = new LinkedList<Mat>();
        auxList.add(hsv_planes.get(0));
        Imgproc.calcHist(auxList, new MatOfInt(0), new Mat(), h_hist, histSize, histRange, accumulate);
        auxList.clear(); auxList.add(hsv_planes.get(1));
        Imgproc.calcHist(auxList, new MatOfInt(0), new Mat(), s_hist, histSize, histRange, accumulate);
        auxList.clear(); auxList.add(hsv_planes.get(2));
        Imgproc.calcHist(auxList, new MatOfInt(0), new Mat(), v_hist, histSize, histRange, accumulate);

        // Plot Histogram window
        int hist_w = 512;
        int hist_h = 400;
        long bin_w = Math.round((double) hist_w / 256);

        Mat histImage  = new Mat( hist_h, hist_w, CvType.CV_8UC3, new Scalar(0,0,0));
        Core.normalize(h_hist, h_hist, 3, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
        Core.normalize(s_hist, s_hist, 3, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
        Core.normalize(v_hist, v_hist, 3, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());

        for (int i = 1; i < 256; i++) {
            Point p1 = new Point(bin_w * (i - 1), hist_h - Math.round(h_hist.get(i - 1, 0)[0]));
            Point p2 = new Point(bin_w * (i), hist_h - Math.round(h_hist.get(i, 0)[0]));
            Imgproc.line(histImage, p1, p2, new Scalar(255, 0, 0), 2, 8, 0);
            
            Point p3 = new Point(bin_w * (i - 1), hist_h - Math.round(s_hist.get(i - 1, 0)[0]));
            Point p4 = new Point(bin_w * (i), hist_h - Math.round(s_hist.get(i, 0)[0]));
            Imgproc.line(histImage, p3, p4, new Scalar(0, 255, 0), 2, 8, 0);

            Point p5 = new Point(bin_w * (i - 1), hist_h - Math.round(v_hist.get(i - 1, 0)[0]));
            Point p6 = new Point(bin_w * (i), hist_h - Math.round(v_hist.get(i, 0)[0]));
            Imgproc.line(histImage, p5, p6, new Scalar(0, 0, 255), 2, 8, 0);
            
        }

        histWindow = new Window("Histogram", histImage);
	}
	
	public void show(){
        histWindow.setVisible(true);
	}

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String [] args){
        System.out.println(System.getProperty("user.dir"));
        Mat image = Imgcodecs.imread("Histogram_Calculation_Original_Image.jpg");
        new Window("", image).setVisible(true);
        Histogram hist = new Histogram(image);
        hist.show();
    }
}
