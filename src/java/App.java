
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import train.AnnTrain;
import train.FeatureInterface;
import train.Features;
import util.Util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class App {
    static {
        //加载opencv动态链接库
     String path = "/usr/local/share/OpenCV/java/libopencv_java341.so";

     System.load(path);
    }

    public static void main(String[] args)  {


       // Mat src = Imgcodecs.imread("res/10.jpg", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE); //读取原始电路板图片
        AnnTrain ann=new AnnTrain("tmp/ann", "tmp/ann.xml");
        ann.train();


/*     ResLocate resLocate = new ResLocate();
        resLocate.resLocate(src);
       ResIdentify resIdentify = new ResIdentify();
        resIdentify.resIdentify(src);
        SVMTrain svm = new SVMTrain();
       svm.svmTrain(true, false);*/

     /*   CapLocate capLocate = new CapLocate();
        capLocate.capLocate(src);*/

  /* Mat detect = Imgcodecs.imread("res/img/capacity/debug_crop_19.jpg");
     PolarDetect polarDetect = new PolarDetect();
     polarDetect.detect(detect);*/
      //  Mat detect = Imgcodecs.imread("res/img/jibian1.jpg");
      //  Distortion distortion = new Distortion(detect);

/*
        MissParts missParts = new MissParts();
        missParts.subtract();*/


/*     RandomAccessFile file=new RandomAccessFile("/home/zydq/Image2/Texture/Train.txt","rw");
      file.seek(file.length()-1);

       for(int i=0;i<=95;i++){
           file.writeChars("/home/zydq/Image2/1/"+i+".jpg 1");
           file.writeChars("\n");

       }
        file.close();*/
    }

}

