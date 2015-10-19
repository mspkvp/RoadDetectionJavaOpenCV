package utility;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Histogram {
	
	private final Scalar[] colorsRGB = new Scalar[] { new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255) };
    private final Scalar[] colorsHue = new Scalar[] {
            new Scalar(255, 0, 0, 255),   new Scalar(255, 60, 0, 255),  new Scalar(255, 120, 0, 255), new Scalar(255, 180, 0, 255), new Scalar(255, 240, 0, 255),
            new Scalar(215, 213, 0, 255), new Scalar(150, 255, 0, 255), new Scalar(85, 255, 0, 255),  new Scalar(20, 255, 0, 255),  new Scalar(0, 255, 30, 255),
            new Scalar(0, 255, 85, 255),  new Scalar(0, 255, 150, 255), new Scalar(0, 255, 215, 255), new Scalar(0, 234, 255, 255), new Scalar(0, 170, 255, 255),
            new Scalar(0, 120, 255, 255), new Scalar(0, 60, 255, 255),  new Scalar(0, 0, 255, 255),   new Scalar(64, 0, 255, 255),  new Scalar(120, 0, 255, 255),
            new Scalar(180, 0, 255, 255), new Scalar(255, 0, 255, 255), new Scalar(255, 0, 215, 255), new Scalar(255, 0, 85, 255),  new Scalar(255, 0, 0, 255)
    };
    private final Scalar white = Scalar.all(255);
    private final int histSizeNum = 25; // bins
    private final MatOfFloat histRanges = new MatOfFloat(0f, 256f);
    private final MatOfInt[] channels = new MatOfInt[] { new MatOfInt(0), new MatOfInt(1), new MatOfInt(2) };
    private final MatOfInt histSize = new MatOfInt(histSizeNum);
    private final int hist_w = 512;
    private final int hist_h = 400;
    private final long bin_w = Math.round((double) hist_w / histSizeNum);
    
    private Mat rgba;
    private Size sizeRgba;
    private Mat r_hist, g_hist, b_hist, a_hist, hsv_hist;
    private Point p1, p2;
    private float[] buffer;
    private int thickness;
    private Mat mask;
    private List<Window> histWindows = new ArrayList<Window>();

	/*public Histogram(Mat mat) throws NullPointerException {

        if(mat.empty()) throw new NullPointerException();

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2HSV);

        h_hist = new Mat();
        s_hist = new Mat();
        v_hist = new Mat();

        Core.split(mat, hsv_planes);

        Imgproc.calcHist(Arrays.asList(hsv_planes.get(0)), new MatOfInt(0), new Mat(), h_hist, histSize, histRange, accumulate);
        Imgproc.calcHist(Arrays.asList(hsv_planes.get(1)), new MatOfInt(0), new Mat(), s_hist, histSize, histRange, accumulate);
        Imgproc.calcHist(Arrays.asList(hsv_planes.get(2)), new MatOfInt(0), new Mat(), v_hist, histSize, histRange, accumulate);

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
	}*/
	
	public Histogram( Mat image ){
		int mHistSizeNum = 25;
		Mat mIntermediateMat = new Mat();
        MatOfInt[] mChannels = new MatOfInt[] { new MatOfInt(0), new MatOfInt(1), new MatOfInt(2) };
        float[] mBuff = new float[mHistSizeNum];
        MatOfInt mHistSize = new MatOfInt(mHistSizeNum);
        MatOfFloat mRanges = new MatOfFloat(0f, 256f);
        Mat mMat0  = new Mat();
        Scalar[] mColorsRGB = new Scalar[] { new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255) };
        Scalar[] mColorsHue = new Scalar[] {
                new Scalar(255, 0, 0, 255),   new Scalar(255, 60, 0, 255),  new Scalar(255, 120, 0, 255), new Scalar(255, 180, 0, 255), new Scalar(255, 240, 0, 255),
                new Scalar(215, 213, 0, 255), new Scalar(150, 255, 0, 255), new Scalar(85, 255, 0, 255),  new Scalar(20, 255, 0, 255),  new Scalar(0, 255, 30, 255),
                new Scalar(0, 255, 85, 255),  new Scalar(0, 255, 150, 255), new Scalar(0, 255, 215, 255), new Scalar(0, 234, 255, 255), new Scalar(0, 170, 255, 255),
                new Scalar(0, 120, 255, 255), new Scalar(0, 60, 255, 255),  new Scalar(0, 0, 255, 255),   new Scalar(64, 0, 255, 255),  new Scalar(120, 0, 255, 255),
                new Scalar(180, 0, 255, 255), new Scalar(255, 0, 255, 255), new Scalar(255, 0, 215, 255), new Scalar(255, 0, 85, 255),  new Scalar(255, 0, 0, 255)
        };
        Scalar mWhilte = Scalar.all(255);
        Point mP1 = new Point();
        Point mP2 = new Point();
        
        Mat rgba = new Mat(); 
        image.copyTo(rgba);
        
        Size sizeRgba = rgba.size();

        Mat hist = new Mat();
        int thikness = (int) (sizeRgba.width / (mHistSizeNum + 10) / 5);
        if(thikness > 5) thikness = 5;
        int offset = (int) ((sizeRgba.width - (5*mHistSizeNum + 4*10)*thikness)/2);
        // RGB
        for(int c=0; c<3; c++) {
            Imgproc.calcHist(Arrays.asList(rgba), mChannels[c], mMat0, hist, mHistSize, mRanges);
            Core.normalize(hist, hist, sizeRgba.height/2, 0, Core.NORM_INF);
            hist.get(0, 0, mBuff);
            for(int h=0; h<mHistSizeNum; h++) {
                mP1.x = mP2.x = offset + (c * (mHistSizeNum + 10) + h) * thikness;
                mP1.y = sizeRgba.height-1;
                mP2.y = mP1.y - 2 - (int)mBuff[h];
                Imgproc.line(rgba, mP1, mP2, mColorsRGB[c], thikness);
            }
        }
        // Value and Hue
        Imgproc.cvtColor(rgba, mIntermediateMat, Imgproc.COLOR_RGB2HSV_FULL);
        // Value
        Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[2], mMat0, hist, mHistSize, mRanges);
        Core.normalize(hist, hist, sizeRgba.height/2, 0, Core.NORM_INF);
        hist.get(0, 0, mBuff);
        for(int h=0; h<mHistSizeNum; h++) {
            mP1.x = mP2.x = offset + (3 * (mHistSizeNum + 10) + h) * thikness;
            mP1.y = sizeRgba.height-1;
            mP2.y = mP1.y - 2 - (int)mBuff[h];
            Imgproc.line(rgba, mP1, mP2, mWhilte, thikness);
        }
        // Hue
        Imgproc.calcHist(Arrays.asList(mIntermediateMat), mChannels[0], mMat0, hist, mHistSize, mRanges);
        Core.normalize(hist, hist, sizeRgba.height/2, 0, Core.NORM_INF);
        hist.get(0, 0, mBuff);
        for(int h=0; h<mHistSizeNum; h++) {
            mP1.x = mP2.x = offset + (4 * (mHistSizeNum + 10) + h) * thikness;
            mP1.y = sizeRgba.height-1;
            mP2.y = mP1.y - 2 - (int)mBuff[h];
            Imgproc.line(rgba, mP1, mP2, mColorsHue[h], thikness);
        }
        histWindows.add(new Window("Histogram", rgba));
	}

	public Histogram( Mat image, int num ){
        buffer = new float[histSizeNum];
        mask = new Mat();
        p1 = new Point();
        p2 = new Point();
        rgba = new Mat();
        
        image.copyTo(rgba);
        sizeRgba = rgba.size();
        thickness = (int) (sizeRgba.width / (histSizeNum + 10) / 5);
        if(thickness > 5) thickness = 5;
        
        
        Mat intermediateMat = new Mat();
        Mat hist = new Mat();
        Mat histImage  = new Mat( hist_h, hist_w, CvType.CV_8UC1);
        
        int offset = (int) ((sizeRgba.width - (5*histSizeNum + 4*10)*thickness)/2);
        
        generate(channels[0], colorsRGB[0]).copyTo(histImage);
        
        // RGB
        for(int c=0; c<3; c++) {
            Imgproc.calcHist(Arrays.asList(rgba), channels[c], mask, hist, histSize, histRanges);
            Core.normalize(hist, hist, sizeRgba.height/2, 0, Core.NORM_INF);
            hist.get(0, 0, buffer);
            for(int h=0; h<histSizeNum; h++) {
                p1.x = p2.x = offset + (c * (histSizeNum + 10) + h) * thickness;
                p1.y = sizeRgba.height-1;
                p2.y = p1.y - 2 - (int)buffer[h];
                Imgproc.line(rgba, p1, p2, colorsRGB[c], thickness);
            }
        }

        // Value and Hue
        Imgproc.cvtColor(rgba, intermediateMat, Imgproc.COLOR_RGB2HSV_FULL);
        // Value
        Imgproc.calcHist(Arrays.asList(intermediateMat), channels[2], mask, hist, histSize, histRanges);
        Core.normalize(hist, hist, sizeRgba.height/2, 0, Core.NORM_INF);
        hist.get(0, 0, buffer);
        for(int h=0; h<histSizeNum; h++) {
            p1.x = p2.x = offset + (3 * (histSizeNum + 10) + h) * thickness;
            p1.y = sizeRgba.height-1;
            p2.y = p1.y - 2 - (int)buffer[h];
            Imgproc.line(rgba, p1, p2, white, thickness);
        }
        // Hue
        Imgproc.calcHist(Arrays.asList(intermediateMat), channels[0], mask, hist, histSize, histRanges);
        Core.normalize(hist, hist, sizeRgba.height/2, 0, Core.NORM_INF);
        hist.get(0, 0, buffer);
        for(int h=0; h<histSizeNum; h++) {
            p1.x = p2.x = offset + (4 * (histSizeNum + 10) + h) * thickness;
            p1.y = sizeRgba.height-1;
            p2.y = p1.y - 2 - (int)buffer[h];
            Imgproc.line(rgba, p1, p2, colorsHue[h], thickness);
        }
        histWindows.add(new Window("Histogram", rgba));
        histWindows.add(new Window("Histogram 1", histImage));
	}

	private Mat generate(MatOfInt channel, Scalar colors) {
		Mat histogram = new Mat(hist_h, hist_w, CvType.CV_8UC3);
        Imgproc.calcHist(Arrays.asList(rgba), channel, mask, histogram, histSize, histRanges);
        Core.normalize(histogram, histogram, hist_h - 10, 0, Core.NORM_INF);
        /*histogram.get(0, 0, buffer);
        for(int h=0; h<histSizeNum; h++) {
            p1.x = p2.x = offset + (c * (histSizeNum + 10) + h) * thickness;
            p1.y = sizeRgba.height-1;
            p2.y = p1.y - 2 - (int)buffer[h];
            Imgproc.line(rgba, p1, p2, colors, thickness);
        }*/
        return histogram;
	}
	
	public void show(){
        histWindows.forEach((Window) -> { Window.setVisible(true); });
	}

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String [] args){
        System.out.println(System.getProperty("user.dir"));
        Mat image = Imgcodecs.imread("Horario.PNG");
        //new Window("", image).setVisible(true);
        Histogram hist = new Histogram(image);
        hist.show();
        Histogram hist2 = new Histogram(image, 1);
        hist2.show();
    }
}
