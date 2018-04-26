package capacity;

import org.omg.PortableServer.POA;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 通过面积和圆度来定位电容
 *
 * @Author HustLrz
 * @Date Created in 11:47 2017/11/17
 */
public class CapLocate {

    private static final String PATH = "res/img/capacity/";
    private static int threshold = 184;           //二值化阈值
    private static int morphOpenSizeX = 20;     //开操作size
    private static int morphOpenSizeY = 20;     //开操作size
    private static int morphDilateSizeX = 17;   //膨胀size
    private static int morphDilateSizeY = 17;   //膨胀size




    private  final  static  int S_MIN=20000;  //面积最小值
    private  final  static  int S_MAX=30000;  //面积最大值
    private  final  static  double COUNTER_PARA=1.4;  //面积最大值



    public List<Mat> capLocate(Mat src) {
        List<Mat> resultList = new ArrayList<Mat>();
        Mat src_blur = new Mat();
        Mat src_gray = new Mat();

        //高斯模糊
        Imgproc.GaussianBlur(src, src_blur, new Size(5, 5), 0, 0, 4);


        //灰度图+HSV
        Mat HSV=new Mat();
        Mat GRAY=new Mat();
        Imgproc.cvtColor(src, GRAY, Imgproc.COLOR_BGR2GRAY);
        //Imgcodecs.imwrite(PATH + "GRAY"  + ".jpg", GRAY);




      /*  Imgproc.cvtColor(src, HSV, Imgproc.COLOR_HSV2RGB);
        //Imgcodecs.imwrite(PATH + "HSV"  + ".jpg", HSV);
        Imgproc.cvtColor(HSV, src_gray, Imgproc.COLOR_BGR2GRAY);
        //Imgcodecs.imwrite(PATH + "HSV_GRAY"  + ".jpg", src_gray);

        Mat test = new Mat();
        Core.add(src_gray,GRAY,test);

        //二值化
        Mat img_threshold = new Mat();
        Imgproc.threshold(test, img_threshold, threshold, 255, Imgproc.THRESH_OTSU);
        //Imgcodecs.imwrite(PATH + "img_threshold"  + ".jpg", img_threshold);



        //开操作消掉部分白色线条和白色斑点
        Mat open=new Mat();
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(morphOpenSizeX, morphOpenSizeY));
        //Imgproc.morphologyEx(img_threshold, img_threshold, Imgproc.MORPH_OPEN, element);
       // Imgproc.morphologyEx(img_threshold, open, Imgproc.MORPH_OPEN, element);
        Imgproc.morphologyEx(img_threshold, open, Imgproc.MORPH_OPEN, element,new Point(-1,-1),1);
        //Imgcodecs.imwrite(PATH + "open" +  ".jpg", open);


       //膨胀消除电阻间隔
        Mat close=new Mat();
        element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(morphDilateSizeX, morphDilateSizeY));
        Imgproc.morphologyEx(open, close, Imgproc.MORPH_DILATE, element);
        //Imgcodecs.imwrite(PATH + "close" +  ".jpg", close);


        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(close, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);


        //通过圆度和面积两项指标来获取电容
        int index_jpg=0;
        for (int i = 0; i < contours.size(); i++) {

            int S = (int) Imgproc.contourArea(contours.get(i));

            if (S < S_MIN || S > S_MAX) {
                continue;
            }
            MatOfPoint2f mtx = new MatOfPoint2f(contours.get(i).toArray());
            int L = (int) Imgproc.arcLength(mtx, true);

            //平滑边缘曲线
            double epsilon = 0.01 * L;
            MatOfPoint2f result = new MatOfPoint2f();
            Imgproc.approxPolyDP(mtx, result, epsilon, true);

            //重新计算周长
            L = (int) Imgproc.arcLength(result, true);

            //圆度
            double roundness = 4 * Math.PI * S / (L * L);

            if (roundness < 0.5) {
                continue;
            }
           // System.out.println(S+"   "+roundness+"    "+L);
           //x旋转矩形   质心  长宽  角度
            RotatedRect minRect = Imgproc.minAreaRect(mtx);

            double r = minRect.size.width / minRect.size.height;
            double angle = minRect.angle;
           // System.out.println(r+"  "+minRect.center+"    "+minRect.size+"    "+minRect.angle);

            Size size ;

          *//*  if(Math.abs(angle)<50)
                angle=0;
            else
                angle=-90;*//*
          *//*  if (r < 1) {

                size = new Size(minRect.size.width* COUNTER_PARA, minRect.size.height* COUNTER_PARA);
            }
            else
                size = new Size(minRect.size.width* COUNTER_PARA , minRect.size.height* COUNTER_PARA);*//*
          size=new Size(249,235);

            double j=angle;
            for(;j<360;j+=15) {
                angle = j;

                Mat rotMat = Imgproc.getRotationMatrix2D(minRect.center, angle, 1);  //图像选装
                Mat img_rotated = new Mat();
                Imgproc.warpAffine(src, img_rotated, rotMat, src.size());
                Mat resultMat = showResultMat(img_rotated, size, minRect.center, index_jpg++);
                resultList.add(resultMat);
            }


        }*/

        return resultList;
    }

    private Mat showResultMat(Mat src, Size rect_size, Point center, int index) {
        Mat img_crop = new Mat();
        Imgproc.getRectSubPix(src, rect_size, center, img_crop);
        Imgproc.cvtColor(img_crop, img_crop, Imgproc.COLOR_BGR2GRAY);
        Imgcodecs.imwrite(PATH + "" + index + ".jpg", img_crop);
        return img_crop;
    }
}
