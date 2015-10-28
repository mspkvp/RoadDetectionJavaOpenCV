package utility;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import org.opencv.core.Core;
import org.opencv.core.Mat;

public class Window extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -909513795392329876L;
	private BufferedImage image;
	private JFrame thisWindow = this;

	public Window(String title, Mat mat){
		setTitle(title);
		setBounds(100, 100, mat.cols()+15, mat.rows()+40);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		image = ConverterTools.Mat2Image(mat);
		addListeners();
	}
	
	public Window(String title, Mat mat, int posX, int posY, int width, int height){
		setTitle(title);
		setBounds(posX, posY, width+15, height+40);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		image = ConverterTools.Mat2Image(mat);
		addListeners();
	}
	
	public void paint(Graphics g){
		g = getContentPane().getGraphics();
		g.drawImage(image, 0, 0, this);
	}
	
	private void addListeners(){
		addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_C){
					//Imgcodecs.imwrite("RGB_capture.jpg", videoCap.getOneFrameMat());
				}
				else if(arg0.getKeyCode() == KeyEvent.VK_G){
					//Imgcodecs.imwrite("GRAYSCALE_capture.jpg", Mat2Image.cvtGrayscale(videoCap.getOneFrameMat()));
				}
				else if(arg0.getKeyCode() == KeyEvent.VK_ESCAPE){
					System.exit(0);
				}
				
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	public void setImage(BufferedImage image){
		this.image = image;
	}

	/*
	public static void main(String [] args) throws InterruptedException{
		// Webcam Viewer
		VideoCap vidcap = new VideoCap();
		Window wind = new Window("Test", vidcap.getOneFrameMat());
		UpdateViewThread updt = new UpdateViewThread(wind, vidcap);
		updt.start();
		wind.setVisible(true);
		
	}*/
	
	static {
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
}
