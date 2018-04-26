package capacity;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;

public class Distortion {


    private static String FilePath = "res/img/distortion/";
    private ArrayList<Point> center=new ArrayList<Point>();   //圆心




    public Distortion(Mat src){
        Mat src_blur = new Mat();
        Mat src_gray = new Mat();
        Mat template_gray = new Mat();

        //高斯模糊
        Imgproc.GaussianBlur(src, src_blur, new Size(5, 5), 0, 0, 4);


  /*      //灰度图+HSV
        Mat HSV=new Mat();
        Mat GRAY=new Mat();
        Imgproc.cvtColor(src, GRAY, Imgproc.COLOR_BGR2GRAY);
        Imgcodecs.imwrite(FilePath + "GRAY"  + ".jpg", src_gray);


        Imgproc.cvtColor(src, HSV, Imgproc.COLOR_HSV2RGB);*/
        Imgproc.cvtColor(src, src_gray, Imgproc.COLOR_BGR2GRAY);
        Imgcodecs.imwrite(FilePath + "src_gray"  + ".jpg", src_gray);



        //二值化
        Mat img_threshold = new Mat();
        Imgproc.threshold(src_gray, img_threshold, 0, 255, Imgproc.THRESH_OTSU);





        //开操作
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(20, 20));
        Imgproc.morphologyEx(img_threshold, img_threshold, Imgproc.MORPH_OPEN, element);
        Imgcodecs.imwrite(FilePath+"img_threshold.jpg", img_threshold);

        //膨胀消除电阻间隔
        Mat close=new Mat();
        element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(17, 17));
        Imgproc.morphologyEx(img_threshold, close, Imgproc.MORPH_DILATE, element);
        Imgcodecs.imwrite(FilePath + "close" +  ".jpg", close);


        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(close, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        //求最大轮廓
        double maxArea = 0;
        int index = -1;
        for (int k = 0; k < contours.size(); k++) {
            double R=0;
            double area = Imgproc.contourArea(contours.get(k));

            if (area <50000 )  continue;


     /*   //最大轮廓质心
        Moments m = Imgproc.moments(contours.get(index));
        int x = (int) (m.get_m10() / m.get_m00());
        int y = (int) (m.get_m01() / m.get_m00());
        gravity_center = new Point(x, y);
*/
            //最大内切圆圆心
            MatOfPoint2f mtx = new MatOfPoint2f(contours.get(k).toArray());
            double dist = 0;
            int maxi=0,maxj=0;
            for (int i = 0; i < src.cols(); i++) {
                for (int j = 0; j < src.rows(); j++) {
                    dist = Imgproc.pointPolygonTest(mtx, new Point(i, j), true);
                    if (dist > R) {
                        R = dist;
                        maxi=i;
                        maxj=j;
                    }
                }
            }
            center.add(new Point(maxi,maxj));
            Imgproc.circle(src, new Point(maxi,maxj), (int) R, new Scalar(0, 255, 0));
        }
        Point v1=center.get(0);
        Point v2=center.get(1);
        Imgproc.line(src, v1, v2, new Scalar(0, 0, 255),2);

        Imgcodecs.imwrite(FilePath+"result.jpg", src);
         double distance=(v1.x-v2.x)*(v1.x-v2.x)+(v1.y-v2.y)*(v1.y-v2.y);
         double tan=(v1.y-v2.y)/(v1.x-v2.x);
         System.out.format("影像仪距离为:  %.3f;角度为:   %.3f",Math.sqrt(distance),Math.atan(tan)/Math.PI*180);

    }
}
