package utility;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.text.ChangedCharSetException;

public class Histogram {
	
	private final Scalar[] colorsRGB = new Scalar[] { new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255) };
    /*private final Scalar[] colorsHue = new Scalar[] {
            new Scalar(255, 0, 0, 255),   new Scalar(255, 60, 0, 255),  new Scalar(255, 120, 0, 255), new Scalar(255, 180, 0, 255), new Scalar(255, 240, 0, 255),
            new Scalar(215, 213, 0, 255), new Scalar(150, 255, 0, 255), new Scalar(85, 255, 0, 255),  new Scalar(20, 255, 0, 255),  new Scalar(0, 255, 30, 255),
            new Scalar(0, 255, 85, 255),  new Scalar(0, 255, 150, 255), new Scalar(0, 255, 215, 255), new Scalar(0, 234, 255, 255), new Scalar(0, 170, 255, 255),
            new Scalar(0, 120, 255, 255), new Scalar(0, 60, 255, 255),  new Scalar(0, 0, 255, 255),   new Scalar(64, 0, 255, 255),  new Scalar(120, 0, 255, 255),
            new Scalar(180, 0, 255, 255), new Scalar(255, 0, 255, 255), new Scalar(255, 0, 215, 255), new Scalar(255, 0, 85, 255),  new Scalar(255, 0, 0, 255)
    };*/
    //private final Scalar white = Scalar.all(255);
    private final int histSizeNum = 256;//25; // bins
    private final MatOfFloat histRanges = new MatOfFloat(0f, 256f);
    private final MatOfInt[] channels = new MatOfInt[] { new MatOfInt(0), new MatOfInt(1), new MatOfInt(2) };
    private final MatOfInt histSize = new MatOfInt(histSizeNum);
    private final int hist_w = 256;
    private final int hist_h = 200;
    private final long bin_w = Math.round((double) hist_w / histSizeNum);
    
    private Mat rgba;
    private Size sizeHistImg = new Size(hist_w, hist_h);
    private Mat r_hist, g_hist, b_hist;//, v_hist, h_hist;
    private Point p1, p2;
    private float[] buffer;
    private int thickness;
    private Mat mask;
    private List<Window> histWindows = new ArrayList<Window>();

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
        //v_hist = new Mat(sizeHistImg, CvType.CV_8UC3);
        //h_hist = new Mat(sizeHistImg, CvType.CV_8UC3);
        thickness = 2;
        
        Mat auxHist = new Mat();//, auxMat = new Mat();
        
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
        histWindows.add(new Window("Blue Histogram", b_hist));
        
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
        histWindows.add(new Window("Green Histogram", g_hist));
        
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
        histWindows.add(new Window("Red Histogram", r_hist));
        /*
        // ########### HSV
        Imgproc.cvtColor(rgba, auxMat, Imgproc.COLOR_RGB2HSV_FULL);
        // Value
        Imgproc.calcHist(Arrays.asList(auxMat), channels[2], mask, auxHist, histSize, histRanges);
        Core.normalize(auxHist, auxHist, sizeHistImg.height-10, 0, Core.NORM_INF);
        auxHist.get(0,0,buffer);
        for(int h=0; h<histSizeNum; h++){
        	p1.x = p2.x = h*bin_w;
        	p1.y = sizeHistImg.height-1;
        	p2.y = p1.y - 2 - (int)buffer[h];
        	Imgproc.line(v_hist, p1, p2, white, thickness);
        }
        histWindows.add(new Window("Value Histogram", v_hist));
     
        // Hue
        Imgproc.calcHist(Arrays.asList(auxMat), channels[0], mask, auxHist, histSize, histRanges);
        Core.normalize(auxHist, auxHist, sizeHistImg.height-10, 0, Core.NORM_INF);
        auxHist.get(0,0,buffer);
        for(int h=0; h<histSizeNum; h++){
        	p1.x = p2.x = h*bin_w;
        	p1.y = sizeHistImg.height-1;
        	p2.y = p1.y - 2 - (int)buffer[h];
        	Imgproc.line(v_hist, p1, p2, colorsHue[(int)(h/10.24)], thickness);
        	System.out.println((int)Math.round(h/10.24));
        }
        histWindows.add(new Window("Hue Histogram", v_hist));*/
        
	}
	
	public void show(){
        histWindows.forEach((Window) -> { Window.setVisible(true); });
	}

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String [] args){
        System.out.println(System.getProperty("user.dir"));
        Mat image = Imgcodecs.imread("profile.jpg");
        //new Window("", image).setVisible(true);
        Histogram hist = new Histogram(image);
        hist.show();
    }
}
