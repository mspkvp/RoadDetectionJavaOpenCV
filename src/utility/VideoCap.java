package utility;

/**
 * Class VideoCap
 * @description Captures a frame from the specified Camera
 * Adapted from source: http://computervisionandjava.blogspot.pt/2013/10/java-opencv-webcam.html 
 */

import java.awt.image.BufferedImage;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class VideoCap {

    VideoCapture cap;
    Mat2Image mat2Img = new Mat2Image();

    public VideoCap(){
        cap = new VideoCapture();
        cap.open(0);
    } 
    
    public BufferedImage getOneFrame(boolean edge_detection) {
    	Mat capture = new Mat();
        cap.read(capture);
        return ConverterTools.Mat2Image(capture);
    }
    
    public Mat getOneFrameMat() {
    	Mat capture = new Mat();
        cap.read(capture);
        return capture;
    }
}