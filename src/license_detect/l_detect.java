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

public class l_detect {

	public static void main( String[] args){
		  
		 System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		 Imgcodecs imageCodecs = new Imgcodecs(); 
		 MatOfRect cars = new MatOfRect();
		 
		 String image_name = "car41.jpeg";
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
	  	
		 if(!licenseCascade.load(xmlFile)) 
		  {
		  		System.out.println("Error Loading XML File");
		  } 
		 else 
		  {
		  		System.out.println("Success Loading XML");
		  }
		  
		 licenseCascade.detectMultiScale(img_out, cars);
		 Rect[] carsArray = cars.toArray();
		 System.out.println(carsArray.length);
		
		 if(carsArray.length == 0)
		 {
			 licenseCascade16.detectMultiScale(img_out, cars);
			 carsArray = cars.toArray();
			 System.out.println(carsArray.length);
		 }
		
		 for (int i = 0; i < carsArray.length; i++)
			{
			   String val = Integer.toString(i);
			   //Imgproc.rectangle(matrix, carsArray[i].tl(), carsArray[i].br(), new Scalar(0, 255, 0), 10);
			   //Imgcodecs.imwrite("bounding_box_images/" + val + image_name, matrix);
			   //Mat plate = Imgcodecs.imread("./bounding_box_images/" + image_name + i);
			   Rect roi = new Rect(carsArray[i].tl(), carsArray[i].br());
			   Mat cropped = new Mat(matrix, roi);
			   
			   Mat cropped_grey = new Mat();
			   Imgproc.cvtColor(cropped, cropped_grey, Imgproc.COLOR_RGB2GRAY); 
			   //Imgproc.equalizeHist(cropped_grey, cropped_grey);
			   //Imgproc.GaussianBlur(cropped_grey, cropped_grey,new Size(45,45), 0);
			   Imgcodecs.imwrite("cropped/" + val + image_name,cropped_grey);
			   
			   Ocr.setUp(); // one time setup
			   Ocr ocr = new Ocr(); // create a new OCR engine
			   ocr.startEngine("eng", Ocr.SPEED_FASTEST,new Object[]{}); // English
			   String s = ocr.recognize(new File[] {new File("cropped/" + val + image_name)},
					   Ocr.RECOGNIZE_TYPE_TEXT, Ocr.OUTPUT_FORMAT_PLAINTEXT,new Object[]{});
			   System.out.println("Asprise Result:  " + s);
			   ocr.stopEngine();
			   
			   File imageFile = new File("cropped/" + val + image_name);
			   ITesseract instance = new Tesseract(); 
			  
			   File tessDataFolder = LoadLibs.extractTessResources("tessdata");
			   instance.setDatapath("/usr/share/tesseract-ocr/tessdata/");
			   
			   try {
		            String result = instance.doOCR(imageFile);
		            System.out.println("Tesseract :"+ result);
		            
		        } catch (TesseractException e) {
		            System.err.println(e.getMessage());
		        }
			   
			}
		   
	}
}
