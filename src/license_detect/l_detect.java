package license_detect;

import org.opencv.core.Core;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.MatOfRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;

import net.sourceforge.tess4j.util.LoadLibs;
import net.sourceforge.tess4j.*;

import com.asprise.ocr.Ocr;
import com.lowagie.text.Image;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.*;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class l_detect {

	private static String filename;
	static JFrame frame = new JFrame("License Plate Recognition");
	static JPanel panel = new JPanel();


	public static void assign_filename(String iname) {
		filename = iname;
		ImageIcon image = new ImageIcon("images/" + filename);
		java.awt.Image imageicon = image.getImage(); // transform it 
		java.awt.Image newimg = imageicon.getScaledInstance(300, 200,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
		image = new ImageIcon(newimg);  // transform it back
		JLabel label = new JLabel("", image, JLabel.CENTER);
		label.setPreferredSize(new Dimension(500,200));
		panel.add(label);
		frame.add(panel);
		frame.revalidate();
	}

	public static void main(String[] args) {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		panel.setLayout(new FlowLayout());

		JLabel label = new JLabel("Enter Filename :");
		label.setBounds(100, 0, 100, 200);
		final JTextField tf = new JTextField("");
		tf.setBounds(100, 200, 200, 200);
		tf.setPreferredSize(new Dimension(200, 24));
		JButton button = new JButton();
		button.setText("Get Result!");
		panel.add(label);
		panel.add(tf);
		panel.add(button);
		frame.add(panel);
		frame.setSize(500, 500);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				assign_filename(tf.getText());
				get_results();
			
				
			}
		});

	}

	public static void get_results() {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		Imgcodecs imageCodecs = new Imgcodecs();
		MatOfRect cars = new MatOfRect();

		String image_name = filename;
		String image_src = "./images/" + image_name;
		Mat matrix = imageCodecs.imread(image_src);

		String file1 = "grey/" + image_name;

		Mat destination = new Mat();
		Imgproc.cvtColor(matrix, destination, Imgproc.COLOR_RGB2GRAY);
		Imgcodecs.imwrite(file1, destination);

		Mat img_out = Imgcodecs.imread(file1);

		String xmlFile16 = "haarcascade_licence_plate_rus_16stages.xml";
		CascadeClassifier licenseCascade16 = new CascadeClassifier(xmlFile16);

		String xmlFile = "haarcascade_russian_plate_number.xml";
		CascadeClassifier licenseCascade = new CascadeClassifier(xmlFile);

		if (!licenseCascade.load(xmlFile)) {
			System.out.println("Error Loading XML File");
		} else {
			System.out.println("Success Loading XML");
		}

		licenseCascade.detectMultiScale(img_out, cars);
		Rect[] carsArray = cars.toArray();
		System.out.println(carsArray.length);

		if (carsArray.length == 0) {
			licenseCascade16.detectMultiScale(img_out, cars);
			carsArray = cars.toArray();
			System.out.println(carsArray.length);
		}

		for (int i = 0; i < carsArray.length; i++) {
			String val = Integer.toString(i);
			// Imgproc.rectangle(matrix, carsArray[i].tl(), carsArray[i].br(),
			// new Scalar(0, 255, 0), 10);
			// Imgcodecs.imwrite("bounding_box_images/" + val + image_name,
			// matrix);
			// Mat plate = Imgcodecs.imread("./bounding_box_images/" +
			// image_name + i);
			Rect roi = new Rect(carsArray[i].tl(), carsArray[i].br());
			Mat cropped = new Mat(matrix, roi);

			Mat cropped_grey = new Mat();
			Imgproc.cvtColor(cropped, cropped_grey, Imgproc.COLOR_RGB2GRAY);
			// Imgproc.equalizeHist(cropped_grey, cropped_grey);
			// Imgproc.GaussianBlur(cropped_grey, cropped_grey,new Size(45,45),
			// 0);
			Imgcodecs.imwrite("cropped/" + val + image_name, cropped_grey);

			Ocr.setUp(); // one time setup
			Ocr ocr = new Ocr(); // create a new OCR engine
			ocr.startEngine("eng", Ocr.SPEED_FASTEST, new Object[] {}); // English
			String s = ocr.recognize(new File[] { new File("cropped/" + val
					+ image_name) }, Ocr.RECOGNIZE_TYPE_TEXT,
					Ocr.OUTPUT_FORMAT_PLAINTEXT, new Object[] {});
		//	System.out.println("Asprise Result:  " + s);
			
			if(i == carsArray.length - 1)
			{
				JLabel jl = new JLabel("Asprise Result :");
				jl.setBounds(100, 200, 100, 30);
				final JTextField tf = new JTextField(s);
				tf.setBounds(200, 150, 100, 30);
				tf.setPreferredSize(new Dimension(300, 30));
				panel.add(jl);
				panel.add(tf);
				frame.add(panel);
				frame.revalidate();
			}
			ocr.stopEngine();

			File imageFile = new File("cropped/" + val + image_name);
			ITesseract instance = new Tesseract();

			File tessDataFolder = LoadLibs.extractTessResources("tessdata");
			instance.setDatapath("/usr/share/tesseract-ocr/tessdata/");

			try {
				String result = instance.doOCR(imageFile);
				//System.out.println("Tesseract :" + result);
				if(i == carsArray.length - 1)
				{
					final JLabel jl_tes = new JLabel("Tesseract Result :");
					final JTextField tf_tes = new JTextField(result);
					tf_tes.setBounds(100, 180, 100, 30);
					tf_tes.setPreferredSize(new Dimension(300, 30));
					panel.add(jl_tes);
					panel.add(tf_tes);
					frame.add(panel);
					frame.revalidate();
				}

			} catch (TesseractException e) {
				System.err.println(e.getMessage());
			}

		}

	}
}
