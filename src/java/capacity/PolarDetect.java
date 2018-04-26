package capacity;


import com.sun.javafx.geom.Vec2d;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;

/**
 * 检测电容极性是否错误,输出角度
 *
 * @Author HustLrz
 * @Date Created in 8:51 2017/11/21
 */
public class PolarDetect {

    private static String templatePath = "res/img/template_1.png";  //加号模板
    private static String FilePath = "res/img/polarity/";  //加号模板
    private Point circle_center;   //圆心
     private Point gravity_center;   //质心
    private double R;
    Point markPoint;            //加号



    /**
     * 通过求圆心-质心与圆心-加号的角度来检测极性与偏移
     * @param src
     */
    public void detect(Mat src) {






        findCenter(src);

        System.out.println("markpoint: " + markPoint.x + " " + markPoint.y);
        System.out.println("circle: " + circle_center.x + " " + circle_center.y);
        System.out.println("gravity: " + gravity_center.x + " " + gravity_center.y);
        double tan1 = (gravity_center.y - circle_center.y) / (circle_center.x - gravity_center.x);
        double angle1 = Math.atan(tan1) / Math.PI * 180;
        System.out.println(tan1 + " " + angle1);
        double tan2 = (markPoint.y - circle_center.y) / (circle_center.x - markPoint.x);
        double angle2 = Math.atan(tan2) / Math.PI * 180;
        System.out.println(tan2 + " " + angle2);

        Vec2d v1=new Vec2d(gravity_center.x-circle_center.x,gravity_center.y-circle_center.y);
        Vec2d v2=new Vec2d(markPoint.x-circle_center.x,markPoint.y-circle_center.y);
        double cos=Math.abs((v1.x*v2.x)+(v1.y*v2.y))/(Math.sqrt(v1.x*v1.x+v1.y*v1.y)*Math.sqrt(v2.x*v2.x+v2.y*v2.y));
        double angle3= Math.acos(cos)*180/Math.PI;;

        System.out.format("角度为 %.3f",angle3);

        double  tan=Math.atan2((float) v2.y,(float)v2.x);
       // System.out.println(Math.cos(tan)+"Math.sin(tan)    "+Math.sin(tan));


        Point test_point=new Point();
        test_point.x=circle_center.x-R*Math.cos(tan);
        test_point.y=circle_center.y-R*Math.sin(tan);
       // System.out.println(circle_center  +"     "+markPoint);

        Imgproc.circle(src, test_point, 2, new Scalar(0, 0, 255));
        Imgproc.line(src,test_point,markPoint,new Scalar(255, 0, 0));


        double angle = 180 + angle1 - angle2;
        if (angle > 180) {
            angle = 360 - angle;
        }

       Imgproc.circle(src, markPoint, 2, new Scalar(0, 0, 255));
        Imgproc.circle(src, gravity_center, 2, new Scalar(0, 0, 255));
        Imgproc.circle(src, circle_center, 2, new Scalar(0, 0, 255));

       Imgcodecs.imwrite(FilePath+"result"+".jpg", src);
    }

    private void findCenter(Mat src) {
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



        Mat template = Imgcodecs.imread(templatePath);
        Imgproc.cvtColor(template, template_gray, Imgproc.COLOR_BGR2GRAY);
        Imgcodecs.imwrite(FilePath + "template"  + ".jpg", template_gray);


        Mat outputImage=new Mat();
        int machMethod=Imgproc.TM_CCORR_NORMED;
        //Template matching method
        Imgproc.matchTemplate(src_gray, template_gray, outputImage, machMethod);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(outputImage);

        Point matchLoc=mmr.maxLoc;
        //Draw rectangle on result image
        Imgproc.rectangle(src, matchLoc, new Point(matchLoc.x + template.cols(),
                matchLoc.y + template.rows()), new Scalar(0, 0, 255));
        Imgcodecs.imwrite(FilePath+"symbol.jpg", src);

       markPoint=new Point(matchLoc.x+template.cols()/2,matchLoc.y + template.rows()/2);





        //开操作
       Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(20, 20));
        Imgproc.morphologyEx(img_threshold, img_threshold, Imgproc.MORPH_OPEN, element);
        Imgcodecs.imwrite(FilePath+"img_threshold.jpg", img_threshold);

        Mat hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(img_threshold, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        //求最大轮廓
        double maxArea = 0;
        int index = -1;
        for (int i = 0; i < contours.size(); i++) {
            double area = Imgproc.contourArea(contours.get(i));
            if (area > maxArea) {
                index = i;
                maxArea = area;
            }
        }

        //最大轮廓质心
       Moments m = Imgproc.moments(contours.get(index));
        int x = (int) (m.get_m10() / m.get_m00());
        int y = (int) (m.get_m01() / m.get_m00());
        gravity_center = new Point(x, y);

        //最大内切圆圆心
        MatOfPoint2f mtx = new MatOfPoint2f(contours.get(index).toArray());
        double dist = 0;

        for (int i = 0; i < src.cols(); i++) {
            for (int j = 0; j < src.rows(); j++) {
                dist = Imgproc.pointPolygonTest(mtx, new Point(i, j), true);
                if (dist > R) {
                    R = dist;
                    circle_center = new Point(i, j);
                }
            }
        }
        Imgproc.circle(src, circle_center, (int)R, new Scalar(0, 255, 0));


    }
}
