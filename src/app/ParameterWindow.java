package app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

import org.opencv.core.Mat;

import net.miginfocom.swing.MigLayout;
import utility.ConverterTools;
import utility.HoughLines;
import utility.VideoCap;
import utility.Window;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.Component;

public class ParameterWindow extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8786031329336998086L;
	private static final JFileChooser fc = new JFileChooser();
	private final JFrame thisWindow = this;
	
	private JTextField cannyThresholdHigh;
	private JTextField cannyThresholdLow;
	private JTextField houghTreshold;
	private JTextField houghPminLineLength;
	private JTextField houghPmaxLineGap;
	private JTextField roiTop;
	private JTextField roiBot;
	private JTextField roiLeft;
	private JTextField roiRight;
	private JTextField roiClipRatio;
	private JTextField gaussianBlurAmount;
	private JTextField claheClipping;
	private JTextPane imageURI;
	
	private JButton btnOriginal;
	private JButton btnCanny;
	private JButton btnHough;
	private JButton btnHoughP;
	private JButton btnApply;
	
	private HoughLines hl;
	private JButton btnCamera;
	
	public ParameterWindow() {
		setResizable(false);
		hl = new HoughLines();
		buildWindow();
		setVariables();
	}
	
	private void buildWindow() {
		setSize(new Dimension(500, 430));
		setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 12));
		getContentPane().setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		getContentPane().setBackground(SystemColor.desktop);
		setBackground(SystemColor.desktop);
		setTitle("Settings");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel_openImage = new JPanel();
		getContentPane().add(panel_openImage, BorderLayout.NORTH);
		panel_openImage.setBackground(SystemColor.desktop);
		
		JButton btnOpen = new JButton("Open");
		btnOpen.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int returnVal = fc.showOpenDialog(thisWindow);

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            loadImage(file);
		        } else {
		        }
			}
		});
		btnOpen.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		
		imageURI = new JTextPane();
		imageURI.setEnabled(false);
		imageURI.setText("Choose an image to process...");
		imageURI.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		imageURI.setToolTipText("Choose an image to process...");
		imageURI.setEditable(false);
		
		btnCamera = new JButton("Use Camera");
		btnCamera.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				getImageFromCamera();
			}
		});
		btnCamera.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		GroupLayout gl_panel_openImage = new GroupLayout(panel_openImage);
		gl_panel_openImage.setHorizontalGroup(
			gl_panel_openImage.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_openImage.createSequentialGroup()
					.addGap(4)
					.addComponent(btnOpen)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(imageURI, GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnCamera, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		gl_panel_openImage.setVerticalGroup(
			gl_panel_openImage.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_openImage.createSequentialGroup()
					.addGroup(gl_panel_openImage.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_openImage.createSequentialGroup()
							.addGap(12)
							.addComponent(btnCamera, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addGroup(gl_panel_openImage.createSequentialGroup()
							.addGap(12)
							.addGroup(gl_panel_openImage.createParallelGroup(Alignment.TRAILING)
								.addComponent(btnOpen, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(imageURI, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE))))
					.addGap(13))
		);
		panel_openImage.setLayout(gl_panel_openImage);
		
		JPanel panel_vars = new JPanel();
		panel_vars.setForeground(SystemColor.text);
		panel_vars.setBackground(SystemColor.desktop);
		getContentPane().add(panel_vars, BorderLayout.WEST);
		
		JPanel Variables = new JPanel();
		Variables.setForeground(SystemColor.text);
		Variables.setBackground(SystemColor.desktop);
		Variables.setBorder(new TitledBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(255, 255, 255)), "Variables", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(255, 255, 255)));
		panel_vars.add(Variables);
		Variables.setLayout(new MigLayout("", "[80.00px][100.00,grow]", "[24px][24px][24px][24px][24px][][][][][][]"));
		
		JLabel lblCThresholdH = new JLabel("Canny Threshold High");
		lblCThresholdH.setForeground(SystemColor.text);
		lblCThresholdH.setFont(new Font("Lucida Sans Unicode", Font.BOLD, 11));
		Variables.add(lblCThresholdH, "cell 0 0,alignx trailing,aligny center");
		
		cannyThresholdHigh = new JTextField();
		cannyThresholdHigh.setHorizontalAlignment(SwingConstants.RIGHT);
		cannyThresholdHigh.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		Variables.add(cannyThresholdHigh, "cell 1 0,alignx center,growy");
		cannyThresholdHigh.setColumns(10);
		
		JLabel lblCThresholdL = new JLabel("Canny Threshold Low");
		lblCThresholdL.setForeground(Color.WHITE);
		lblCThresholdL.setFont(new Font("Lucida Sans Unicode", Font.BOLD, 11));
		Variables.add(lblCThresholdL, "cell 0 1,alignx trailing");
		
		cannyThresholdLow = new JTextField();
		cannyThresholdLow.setHorizontalAlignment(SwingConstants.RIGHT);
		cannyThresholdLow.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		cannyThresholdLow.setColumns(10);
		Variables.add(cannyThresholdLow, "cell 1 1,alignx center,growy");
		
		JLabel lblHoughThreshold = new JLabel("Hough Threshold");
		lblHoughThreshold.setForeground(Color.WHITE);
		lblHoughThreshold.setFont(new Font("Lucida Sans Unicode", Font.BOLD, 11));
		Variables.add(lblHoughThreshold, "cell 0 2,alignx trailing");
		
		houghTreshold = new JTextField();
		houghTreshold.setHorizontalAlignment(SwingConstants.RIGHT);
		houghTreshold.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		houghTreshold.setColumns(10);
		Variables.add(houghTreshold, "cell 1 2,alignx center,growy");
		
		JLabel lblHoughp = new JLabel("HoughP Min Line Len");
		lblHoughp.setForeground(Color.WHITE);
		lblHoughp.setFont(new Font("Lucida Sans Unicode", Font.BOLD, 11));
		Variables.add(lblHoughp, "cell 0 3,alignx trailing");
		
		houghPminLineLength = new JTextField();
		houghPminLineLength.setHorizontalAlignment(SwingConstants.RIGHT);
		houghPminLineLength.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		houghPminLineLength.setColumns(10);
		Variables.add(houghPminLineLength, "cell 1 3,alignx center,growy");
		
		JLabel lblHoughpMaxLine = new JLabel("HoughP Max Line Gap");
		lblHoughpMaxLine.setForeground(Color.WHITE);
		lblHoughpMaxLine.setFont(new Font("Lucida Sans Unicode", Font.BOLD, 11));
		Variables.add(lblHoughpMaxLine, "cell 0 4,alignx trailing");
		
		houghPmaxLineGap = new JTextField();
		houghPmaxLineGap.setHorizontalAlignment(SwingConstants.RIGHT);
		houghPmaxLineGap.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		houghPmaxLineGap.setColumns(10);
		Variables.add(houghPmaxLineGap, "cell 1 4,alignx center,growy");
		
		JLabel lblRoiClipping = new JLabel("ROI Clipping [T,B,L,R]");
		lblRoiClipping.setFont(new Font("Lucida Sans Unicode", Font.BOLD, 11));
		lblRoiClipping.setForeground(SystemColor.text);
		Variables.add(lblRoiClipping, "cell 0 5,alignx trailing");
		
		roiTop = new JTextField();
		roiTop.setHorizontalAlignment(SwingConstants.RIGHT);
		roiTop.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		Variables.add(roiTop, "flowx,cell 1 5,alignx center,growy");
		roiTop.setColumns(10);
		
		roiBot = new JTextField();
		roiBot.setHorizontalAlignment(SwingConstants.RIGHT);
		roiBot.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		Variables.add(roiBot, "cell 1 5");
		roiBot.setColumns(10);
		
		roiLeft = new JTextField();
		roiLeft.setHorizontalAlignment(SwingConstants.RIGHT);
		roiLeft.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		Variables.add(roiLeft, "cell 1 5");
		roiLeft.setColumns(10);
		
		roiRight = new JTextField();
		roiRight.setHorizontalAlignment(SwingConstants.RIGHT);
		roiRight.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		Variables.add(roiRight, "cell 1 5");
		roiRight.setColumns(10);
		
		JLabel lblRoiTopClip = new JLabel("ROI Top Clip Ratio");
		lblRoiTopClip.setForeground(SystemColor.text);
		lblRoiTopClip.setFont(new Font("Lucida Sans Unicode", Font.BOLD, 11));
		Variables.add(lblRoiTopClip, "cell 0 6,alignx trailing,growy");
		
		roiClipRatio = new JTextField();
		roiClipRatio.setHorizontalAlignment(SwingConstants.RIGHT);
		roiClipRatio.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		Variables.add(roiClipRatio, "cell 1 6,alignx center");
		roiClipRatio.setColumns(10);
		
		JLabel lblGaussianBlurAmount = new JLabel("Gaussian Blur Amount");
		lblGaussianBlurAmount.setForeground(SystemColor.text);
		lblGaussianBlurAmount.setFont(new Font("Lucida Sans Unicode", Font.BOLD, 11));
		Variables.add(lblGaussianBlurAmount, "cell 0 7,alignx trailing");
		
		gaussianBlurAmount = new JTextField();
		gaussianBlurAmount.setHorizontalAlignment(SwingConstants.RIGHT);
		gaussianBlurAmount.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		Variables.add(gaussianBlurAmount, "cell 1 7,alignx center");
		gaussianBlurAmount.setColumns(10);
		
		JLabel lblClahe = new JLabel("CLAHE Clip Limit");
		lblClahe.setForeground(SystemColor.text);
		lblClahe.setFont(new Font("Lucida Sans Unicode", Font.BOLD, 11));
		Variables.add(lblClahe, "cell 0 8,alignx trailing");
		
		claheClipping = new JTextField();
		claheClipping.setEditable(false);
		claheClipping.setEnabled(false);
		claheClipping.setHorizontalAlignment(SwingConstants.RIGHT);
		claheClipping.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		Variables.add(claheClipping, "cell 1 8,alignx center");
		claheClipping.setColumns(10);
		
		btnApply = new JButton("Apply");
		btnApply.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				updateVariables();
			}
		});
		Variables.add(btnApply, "cell 1 10,growx");
		btnApply.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		
		JPanel panel_actionBtns = new JPanel();
		panel_actionBtns.setForeground(SystemColor.text);
		panel_actionBtns.setBackground(SystemColor.desktop);
		getContentPane().add(panel_actionBtns, BorderLayout.EAST);
		panel_actionBtns.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JPanel Actions = new JPanel();
		Actions.setBorder(new TitledBorder(new MatteBorder(1, 1, 1, 1, (Color) new Color(255, 255, 255)), "Actions", TitledBorder.LEFT, TitledBorder.TOP, null, new Color(255, 255, 255)));
		Actions.setForeground(SystemColor.text);
		Actions.setBackground(SystemColor.desktop);
		panel_actionBtns.add(Actions);
		Actions.setLayout(new MigLayout("", "[131.00]", "[][][][]"));
		
		btnOriginal = new JButton("Original");
		btnOriginal.setEnabled(false);
		btnOriginal.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				showWindow("Original", hl.getOriginal());
			}
		});
		btnOriginal.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		Actions.add(btnOriginal, "cell 0 0,grow");
		
		btnCanny = new JButton("Canny");
		btnCanny.setEnabled(false);
		btnCanny.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				showWindow("Canny", hl.getCanny());
			}
		});
		btnCanny.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		Actions.add(btnCanny, "cell 0 1,grow");
		
		btnHough = new JButton("Hough Lines");
		btnHough.setEnabled(false);
		btnHough.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				doHoughLinesNormal();
			}
		});
		btnHough.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		Actions.add(btnHough, "cell 0 2,growx");
		
		btnHoughP = new JButton("Hough Lines Prob");
		btnHoughP.setEnabled(false);
		btnHoughP.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				doHoughLinesProbabilistic();
			}
		});
		btnHoughP.setFont(new Font("Lucida Sans Unicode", Font.PLAIN, 11));
		Actions.add(btnHoughP, "cell 0 3,grow");
	}
	
	private void setVariables(){
		cannyThresholdHigh.setText(Integer.toString(hl.cannyThresholdHigh));
		cannyThresholdLow.setText(Integer.toString(hl.cannyThresholdLow));
		houghTreshold.setText(Integer.toString(hl.houghThreshold));
		houghPminLineLength.setText(Integer.toString(hl.minLineLength));
		houghPmaxLineGap.setText(Integer.toString(hl.maxLineGap));
		roiTop.setText(Integer.toString(hl.roiTopClipping));
		roiBot.setText(Integer.toString(hl.roiBottomClipping));
		roiLeft.setText(Integer.toString(hl.roiLeftClipping));
		roiRight.setText(Integer.toString(hl.roiRightClipping));
		roiClipRatio.setText(Double.toString(hl.roiRatioClipping));
		gaussianBlurAmount.setText(Integer.toString(hl.gaussianBlurAmount));
		//claheClipping.setText(Integer.toString(hl.claheClipping));
	}
	
	private void updateVariables(){
		hl.cannyThresholdHigh = Integer.parseInt(cannyThresholdHigh.getText());
		hl.cannyThresholdLow = Integer.parseInt(cannyThresholdLow.getText());
		hl.houghThreshold = Integer.parseInt(houghTreshold.getText());
		hl.minLineLength = Integer.parseInt(houghPminLineLength.getText());
		hl.maxLineGap = Integer.parseInt(houghPmaxLineGap.getText());
		hl.roiTopClipping = Integer.parseInt(roiTop.getText());
		hl.roiBottomClipping = Integer.parseInt(roiBot.getText());
		hl.roiLeftClipping = Integer.parseInt(roiLeft.getText());
		hl.roiRightClipping = Integer.parseInt(roiRight.getText());
		hl.roiRatioClipping = Double.parseDouble(roiClipRatio.getText());
		hl.gaussianBlurAmount = Integer.parseInt(gaussianBlurAmount.getText());
		//hl.claheClipping = Integer.parseInt(claheClipping.getText());
		setVariables();
	}
	
	private void showWindow(String title, Mat mat){
		new Window(title, mat).setVisible(true);
	}
	
	private void getImageFromCamera(){
		VideoCap vidcap = new VideoCap();
		vidcap.openDevice(0);
		Window wind = new Window("Test", vidcap.getOneFrameMat());
		//UpdateViewThread updt;// = new UpdateViewThread(wind, vidcap);
		//updt.start();
		wind.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                vidcap.close();
            }
        } );
		wind.setVisible(true);
	}
	
	private void loadImage(File file){
		BufferedImage img = null;
		try {
		    img = ImageIO.read(file);
		    Mat mat = ConverterTools.Image2Mat(img);
		    hl.setImage(mat);
		    imageURI.setText(file.getAbsolutePath());
		    Window w = new Window(file.getName(), mat);
		    w.setVisible(true);
		    enableButtons();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void enableButtons(){
		btnOriginal.setEnabled(true);
		btnHough.setEnabled(true);
		btnHoughP.setEnabled(true);
		repaint();
	}
	
	private void enableCannyButton(){
		if(!btnCanny.isEnabled()){ 
			btnCanny.setEnabled(true);
			repaint();
		}
	}
	
	private void doHoughLinesNormal(){
		showWindow("Hough Lines Normal", hl.getNormal());
		enableCannyButton();
	}
	
	private void doHoughLinesProbabilistic(){
		showWindow("Hough Lines Probabilistic", hl.getProbabilistic());
		enableCannyButton();
	}
		
}
