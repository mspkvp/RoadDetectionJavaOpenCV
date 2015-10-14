package utility;

import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import org.opencv.core.Mat;

public class Window extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -909513795392329876L;
	private BufferedImage image, originalImage;
	private JFrame thisWindow = this;

	public Window(String title, Mat mat){
		setTitle(title);
		setBounds(100, 100, mat.cols(), mat.rows());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		originalImage = image = ConverterTools.Mat2Image(mat);
		addListeners();
	}
	
	public Window(String title, Mat mat, int posX, int posY, int width, int height){
		setTitle(title);
		setBounds(posX, posY, width, height);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		originalImage = image = ConverterTools.Mat2Image(mat);
		addListeners();
	}
	
	public void paint(Graphics g){
		g = getContentPane().getGraphics();
		g.drawImage(image, 0, 0, this);
	}
	
	private void addListeners(){
		// resize image upon window resizing
		getRootPane().addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
            	// Calculate proportional measures
            	resize();
            	thisWindow.repaint();
            }
        });
		
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

	public void resize() {
		int finalw = 1, finalh = 1; 
		
		if (originalImage.getWidth() > thisWindow.getWidth()) {
		    //scale width to fit
		    finalw = thisWindow.getWidth();
		    //scale height to maintain aspect ratio
		    finalh = (finalw * originalImage.getHeight()) / originalImage.getWidth();
		}

		// then check if we need to scale even with the new height
		if (originalImage.getHeight() > thisWindow.getHeight()) {
		    //scale height to fit instead
		    finalh = thisWindow.getHeight();
		    //scale width to maintain aspect ratio
		    finalw = (finalh * originalImage.getWidth()) / originalImage.getHeight();
		}
		
		BufferedImage newImg = new BufferedImage(thisWindow.getWidth(), thisWindow.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = newImg.createGraphics();
		g.drawImage(originalImage, 0, 0, finalw, finalh, null);
		g.dispose();
		image = newImg;
	}
	
	public static void main(String [] args) throws InterruptedException{
		// Webcam Viewer
		VideoCap vidcap = new VideoCap();
		Window wind = new Window("Test", vidcap.getOneFrameMat());
		UpdateViewThread updt = new UpdateViewThread(wind, vidcap);
		updt.start();
		wind.setVisible(true);
	}	
}
