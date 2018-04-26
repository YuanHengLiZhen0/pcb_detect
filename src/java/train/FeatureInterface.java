package train;

import org.opencv.core.Mat;

/**
 * @Author HustLrz
 * @Date Created in 15:09 2017/11/6
 */
public interface FeatureInterface {

    /**
     * 获取垂直和水平的直方图图值
     * @param image
     * @return
     */
    Mat getHistogramFeatures(Mat image);

    //! gray and project feature
    Mat getGrayPlusProject(Mat grayChar);


    //! 本函数是获取垂直和水平的直方图图值




    //! 本函数是获取SIFT特征子
    Mat getSIFTFeatures(Mat image);

    //! 本函数是获取HOG特征子
    Mat getHOGFeatures(Mat image);

    //! 本函数是获取HSV空间量化的直方图特征子
    Mat getHSVHistFeatures(Mat image);

    //!LBP
    public  Mat  getLBPFeatures(Mat image);


    //! color feature
    Mat getColorFeatures(Mat image);

    //! color feature and histom
    Mat getHistomPlusColoFeatures(Mat image);


    //! LBP feature + Histom feature
    Mat getLBPplusHistFeatures(Mat image);

    //! grayChar feauter
    Mat getGrayCharFeatures(Mat grayChar);

    Mat getGrayPlusLBP( Mat grayChar);
}
