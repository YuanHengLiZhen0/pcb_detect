package train;

import LBP.lbp;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


/**
 * @Author HustLrz
 * @Date Created in 15:12 2017/11/6
 */
public class Features implements FeatureInterface {
    enum Direction {
        VERTICAL, HORIZONTAL
    }


    public  Mat getHistogramFeatures(Mat image) {
        Mat grayImage=new Mat();
        Imgproc.cvtColor(image, grayImage,Imgproc.COLOR_BGR2GRAY);


        Mat img_threshold=new Mat();;
        Imgproc.threshold(grayImage, img_threshold, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);


        return getHistogram(img_threshold);
    }

    public Mat getGrayPlusProject(Mat grayChar) {
        return null;
    }

    public Mat getSIFTFeatures(Mat image) {
        return null;
    }

    public Mat getHOGFeatures(Mat image) {
        return null;
    }

    public Mat getHSVHistFeatures(Mat image) {
        return null;
    }

    public Mat getLBPFeatures(Mat image) {

        Mat grayImage=image;
        //cvtColor(image, grayImage, CV_RGB2GRAY);

        Mat lbpimage;
        lbpimage = lbp.getCircularLBPFeatureOptimization(grayImage,4,8);   //lbp算子 生成图像


        Mat lbp_hist = lbp.spatial_histogram(lbpimage, 32, 4, 4,false);//直方图

        return lbp_hist;
    }

    public Mat getColorFeatures(Mat image) {
        return null;
    }

    public Mat getHistomPlusColoFeatures(Mat image) {
        return null;
    }

    public Mat getLBPplusHistFeatures(Mat image) {
        return null;
    }

    public Mat getGrayCharFeatures(Mat grayChar) {
        return null;
    }

    public Mat getGrayPlusLBP(Mat grayChar) {
        return null;
    }




    /**
     * 获取垂直或水平方向直方图
     *
     * @param img
     * @param direction
     * @return
     */
    private static float[] projectedHistogram(Mat img, Direction direction) {
        int sz = 0;
        switch (direction) {
            case HORIZONTAL:
                sz = img.rows();
                break;

            case VERTICAL:
                sz = img.cols();
                break;

            default:
                break;
        }

        // 统计这一行或一列中，非零元素的个数，并保存到nonZeroMat中
        float[] nonZeroMat = new float[sz];
        Core.extractChannel(img, img, 0);
        for (int j = 0; j < sz; j++) {
            Mat data = (direction == Direction.HORIZONTAL) ? img.row(j) : img.col(j);
            int count = Core.countNonZero(data);
            nonZeroMat[j] = count;
        }

        // Normalize histogram
        float max = 0;
        for (int j = 0; j < nonZeroMat.length; ++j) {
            max = Math.max(max, nonZeroMat[j]);
        }

        if (max > 0) {
            for (int j = 0; j < nonZeroMat.length; ++j) {
                nonZeroMat[j] /= max;
            }
        }

        return nonZeroMat;
    }


    private static Mat getHistogram1(Mat in, int sizeData) {
        float[] vhist = projectedHistogram(in, Direction.VERTICAL);
        float[] hhist = projectedHistogram(in, Direction.HORIZONTAL);

        Mat lowData = new Mat();
        if (sizeData > 0) {
            Imgproc.resize(in, lowData, new Size(sizeData, sizeData));
        }






        int numCols = vhist.length + hhist.length + lowData.cols() * lowData.rows();
        Mat out = new Mat();
        out.create(1, numCols, CvType.CV_32F);

        int j = 0;
        for (int i = 0; i < vhist.length; i++, j++) {
            out.put(0, j, vhist[i]);
        }

        for (int i = 0; i < hhist.length; i++, j++) {
            out.put(0, j, hhist[i]);
        }

        for (int x = 0; x < lowData.rows(); x++) {
            for (int y = 0; y < lowData.cols(); y++, j++) {
                float[] arr = new float[1];
                lowData.get(x, y, arr);
                out.put(0, j, arr[0]);
            }
        }
        return out;
    }




    private static Mat getHistogram(Mat in) {
        float[] vhist = projectedHistogram(in, Direction.VERTICAL);
        float[] hhist = projectedHistogram(in, Direction.HORIZONTAL);


        int numCols = vhist.length + hhist.length ;
        Mat out = new Mat();
        out.create(1, numCols, CvType.CV_32F);

        int j = 0;
        for (int i = 0; i < vhist.length; i++, j++) {
            out.put(0, j, vhist[i]);
        }

        for (int i = 0; i < hhist.length; i++, j++) {
            out.put(0, j, hhist[i]);
        }

        return out;
    }

}
