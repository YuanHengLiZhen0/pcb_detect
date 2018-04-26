package util;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class Creat_Data {

   public static Mat translateImg(Mat img, int offsetx, int offsety, int bk){
        Mat dst=new Mat();

        Mat trans_mat =Mat.zeros(2,3, CvType.CV_32F);
        trans_mat.put(0,0,1);
        trans_mat.put(0,2,offsetx);
        trans_mat.put(1,1,1);
        trans_mat.put(1,2,offsety);

        Imgproc. warpAffine(img, dst, trans_mat, img.size(), 1, 0,new Scalar(bk));
        return dst;
    }

    // rotate an image
    public static Mat rotateImg(Mat source, float angle, int bk){
        Point src_center=new Point(source.cols() / 2.0f, source.rows() / 2.0f);
        Mat rot_mat = Imgproc.getRotationMatrix2D(src_center, angle, 1.0);
        Mat dst=new Mat();
        Imgproc.warpAffine(source, dst, rot_mat, source.size(), 1, 0, new Scalar(bk));
        return dst;
    }

}
