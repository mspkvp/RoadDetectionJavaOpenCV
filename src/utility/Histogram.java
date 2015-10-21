package utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Histogram {
	
	private final Scalar[] colorsRGB = new Scalar[] { new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255) };
    /*private final Scalar[] colorsHue = new Scalar[] {
            new Scalar(255, 0, 0, 255),   new Scalar(255, 60, 0, 255),  new Scalar(255, 120, 0, 255), new Scalar(255, 180, 0, 255), new Scalar(255, 240, 0, 255),
            new Scalar(215, 213, 0, 255), new Scalar(150, 255, 0, 255), new Scalar(85, 255, 0, 255),  new Scalar(20, 255, 0, 255),  new Scalar(0, 255, 30, 255),
            new Scalar(0, 255, 85, 255),  new Scalar(0, 255, 150, 255), new Scalar(0, 255, 215, 255), new Scalar(0, 234, 255, 255), new Scalar(0, 170, 255, 255),
            new Scalar(0, 120, 255, 255), new Scalar(0, 60, 255, 255),  new Scalar(0, 0, 255, 255),   new Scalar(64, 0, 255, 255),  new Scalar(120, 0, 255, 255),
            new Scalar(180, 0, 255, 255), new Scalar(255, 0, 255, 255), new Scalar(255, 0, 215, 255), new Scalar(255, 0, 85, 255),  new Scalar(255, 0, 0, 255)
    };*/
    private final Scalar white = Scalar.all(255);
    private final int histSizeNum = 256;//25; // bins
    private final MatOfFloat histRanges = new MatOfFloat(0f, 256f);
    private final MatOfInt[] channels = new MatOfInt[] { new MatOfInt(0), new MatOfInt(1), new MatOfInt(2) };
    private final MatOfInt histSize = new MatOfInt(histSizeNum);
    private final int hist_w = 256;
    private final int hist_h = 200;
    private final long bin_w = Math.round((double) hist_w / histSizeNum);
    
    private Mat rgba;
    private Size sizeHistImg = new Size(hist_w, hist_h);
    private Mat r_hist, g_hist, b_hist, v_hist;//, h_hist;
    private Point p1, p2;
    private float[] buffer;
    private int thickness;
    private Mat mask;
    private List<Window> histWindows = new ArrayList<Window>();
    private int numOfChannels;

	public Histogram( Mat image ){
		if(image.empty()) throw new NullPointerException();
		
		mask = new Mat();
        rgba = new Mat();
        image.copyTo(rgba);
        p1 = new Point();
        p2 = new Point();
        buffer = new float[histSizeNum];
        r_hist = new Mat(sizeHistImg, CvType.CV_8UC3);
        g_hist = new Mat(sizeHistImg, CvType.CV_8UC3);
        b_hist = new Mat(sizeHistImg, CvType.CV_8UC3);
        v_hist = new Mat(sizeHistImg, CvType.CV_8UC1);
        //h_hist = new Mat(sizeHistImg, CvType.CV_8UC3);
        thickness = 2;
        numOfChannels = rgba.channels();
	}
	
	public List<Mat> generate(){
		Mat auxHist = new Mat(), auxMat = new Mat();
        
		if(numOfChannels > 1){
	        //Blue channel
	        Imgproc.calcHist(Arrays.asList(rgba), channels[0], mask, auxHist, histSize, histRanges);
	        Core.normalize(auxHist, auxHist, sizeHistImg.height-10, 0, Core.NORM_INF);
	        auxHist.get(0,0,buffer);
	        for(int h=0; h<histSizeNum; h++){
	        	p1.x = p2.x = h*bin_w;
	        	p1.y = sizeHistImg.height-1;
	        	p2.y = p1.y - 2 - (int)buffer[h];
	        	Imgproc.line(b_hist, p1, p2, colorsRGB[0], thickness);
	        }
	        
	        //Green channel
	        Imgproc.calcHist(Arrays.asList(rgba), channels[1], mask, auxHist, histSize, histRanges);
	        Core.normalize(auxHist, auxHist, sizeHistImg.height-10, 0, Core.NORM_INF);
	        auxHist.get(0,0,buffer);
	        for(int h=0; h<histSizeNum; h++){
	        	p1.x = p2.x = h*bin_w;
	        	p1.y = sizeHistImg.height-1;
	        	p2.y = p1.y - 2 - (int)buffer[h];
	        	Imgproc.line(g_hist, p1, p2, colorsRGB[1], thickness);
	        }
	        
	        //Red channel
	        Imgproc.calcHist(Arrays.asList(rgba), channels[2], mask, auxHist, histSize, histRanges);
	        Core.normalize(auxHist, auxHist, sizeHistImg.height-10, 0, Core.NORM_INF);
	        auxHist.get(0,0,buffer);
	        for(int h=0; h<histSizeNum; h++){
	        	p1.x = p2.x = h*bin_w;
	        	p1.y = sizeHistImg.height-1;
	        	p2.y = p1.y - 2 - (int)buffer[h];
	        	Imgproc.line(r_hist, p1, p2, colorsRGB[2], thickness);
	        }
	        return Arrays.asList(b_hist, g_hist,r_hist);
		}
		else {
        
	        // ########### HSV
	        //Imgproc.cvtColor(rgba, auxMat, Imgproc.COLOR_gra);
	        // Value
	        Imgproc.calcHist(Arrays.asList(auxMat), channels[0], mask, auxHist, histSize, histRanges);
	        Core.normalize(auxHist, auxHist, sizeHistImg.height-10, 0, Core.NORM_INF);
	        auxHist.get(0,0,buffer);
	        for(int h=0; h<histSizeNum; h++){
	        	p1.x = p2.x = h*bin_w;
	        	p1.y = sizeHistImg.height-1;
	        	p2.y = p1.y - 2 - (int)buffer[h];
	        	Imgproc.line(v_hist, p1, p2, white, thickness);
	        }
	        return Arrays.asList(v_hist);
		}
	}
	
	public void show(){
		if(numOfChannels > 1){
			histWindows.add(new Window("Blue Histogram", b_hist));
			histWindows.add(new Window("Green Histogram", g_hist));
	        histWindows.add(new Window("Red Histogram", r_hist));
		}
		else histWindows.add(new Window("Value Histogram", v_hist));
        histWindows.forEach((Window) -> { Window.setVisible(true); });
	}

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /*public static void main(String [] args){
        System.out.println(System.getProperty("user.dir"));
        Mat image = Imgcodecs.imread("lowcontrast.jpg");
        //Histogram hist = new Histogram(image);
        //hist.show();
        Mat equalized = new Mat();
        
        Histogram hist = new Histogram(image);
        hist.generate();
        hist.show();
        
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY );
        image.copyTo(equalized);
        CLAHE cl = Imgproc.createCLAHE();
        cl.setClipLimit(16);
        cl.apply(equalized, equalized);
        Imgproc.cvtColor(equalized, equalized, Imgproc.COLOR_GRAY2BGR );
        
        Histogram histE = new Histogram(equalized);
        histE.generate();
        histE.show();

        new Window("Original", image).setVisible(true);
        new Window("Equalized", equalized).setVisible(true);
    }*/
}
