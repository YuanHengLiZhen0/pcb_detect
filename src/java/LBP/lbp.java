package LBP;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import util.Util;

import java.util.ArrayList;
import java.util.List;


public class lbp {
   public static  byte ret0(char a,char b){
       return a>=b?(byte)1:(byte)0;
   }
    public static  byte ret1(char a,char b){
        return a>b?(byte)1:(byte)0;
    }
    public static  Mat getCircularLBPFeatureOptimization(Mat src,int radius,int neighbors){
        //圆形LBP特征计算，效率优化版本，声明时默认neighbors=8

        int h=src.rows();
        int w=src.cols();
        Mat dst=new Mat();

        //LBP特征图像的行数和列数的计算要准确
        dst.create(h-2*radius,w-2*radius,CvType.CV_8UC1);
        dst.setTo(new Scalar(0));
        byte[] dstbytes=new byte[(h - 2*radius)*(w-2*radius)];
        for(int k=0;k<neighbors;k++)
        {
            //计算采样点对于中心点坐标的偏移量rx，ry
            float rx = (float) (radius * Math.cos(2.0 * Math.PI * k / neighbors));
            float ry = (float)(radius * Math.sin(2.0 * Math.PI * k / neighbors));
            //为双线性插值做准备
            //对采样点偏移量分别进行上下取整
            int x1 = (int)(Math.floor(rx));
            int x2 = (int)(Math.ceil(rx));
            int y1 = (int)(Math.floor(ry));
            int y2 = (int)(Math.ceil(ry));
            //将坐标偏移量映射到0-1之间
            float tx = rx - x1;
            float ty = ry - y1;
            //根据0-1之间的x，y的权重计算公式计算权重，权重与坐标具体位置无关，与坐标间的差值有关
            float w1 = (1-tx) * (1-ty);
            float w2 =    tx  * (1-ty);
            float w3 = (1-tx) *    ty;
            float w4 =    tx  *    ty;
            //循环处理每个像素


            byte[] bytes=new byte[h*w];
            src.get(0,0,bytes);  //从坐标开始的所有 的像素值 构成的数组
            for(int i=radius;i<h-radius;i++)
            {
                for(int j=radius;j<w-radius;j++)
                {
                    //获得中心像素点的灰度值

                    char center=(char)bytes[i*w+j];
                    //根据双线性插值公式计算第k个采样点的灰度值
                    char neighbor = (char)((char)bytes[(i+x1)*w+j+y1] * w1 +(char) bytes[(i+x1)*w+j+y2] *w2
                    +(char) bytes[(i+x2)*w+j+y1]*w3+  (char)bytes[(i+x2)*w+j+y2] *w4);


                    //LBP特征图像的每个邻居的LBP值累加，累加通过与操作完成，对应的LBP值通过移位取得
                    dstbytes[(i-radius)*(w-2*radius)+(j-radius)] |= ret1(neighbor,center) <<(neighbors-k-1);

                }

            }


        }
        dst.put(0,0,dstbytes);
        //Util.print(dst);
        return dst;
    }



   public  static Mat olbp(Mat src) {

        int h=src.rows();
        int w=src.cols();


        // get matrices
        Mat dst=new Mat();

        // allocate memory for result
        dst.create(h - 2, w - 2, CvType.CV_8UC1);
        // zero the result matrix

        dst.setTo(new Scalar(0));  //置0

        // calculate patterns
       byte[] bytes=new byte[h*w];
       byte[] dstbytes=new byte[(h - 2)*(w-2)];

       src.get(0,0,bytes);  //从坐标开始的所有 的像素值 构成的数组

        for (int i = 1; i<h - 1; i++) {


            for (int j = 1; j<w - 1; j++) {
               char center=(char)bytes[i*w+j];

                byte code = 0;
                code |=ret0((char)bytes[(i-1)*w+(j-1)],center)<<7;
                code |= ret0((char)bytes[(i-1)*w+(j)],center)<<6;
                code |= ret0((char)bytes[(i-1)*w+(j+1)],center)<<5;
                code |= ret0((char)bytes[(i)*w+(j+1)],center)<<4;
                code |= ret0((char)bytes[(i+1)*w+(j+1)],center)<<3;
                code |= ret0((char)bytes[(i+1)*w+(j)],center)<<2;
                code |= ret0((char)bytes[(i+1)*w+(j-1)],center)<<1;
                code |= ret0((char)bytes[(i)*w+(j-1)],center)<<0;
               // dst.put(i - 1, j - 1,code);


                dstbytes[(i-1)*(w-2)+(j-1)]=code;
            }
        }

      /*  for(int i=0;i<h-2;i++){
            for(int j=0;j<w-2;j++)
                System.out.format("%4d",dstbytes[i*w+j]);
            System.out.println();
        }*/
        dst.put(0,0,dstbytes);

        return dst;
    }


    public static Mat histc_(List<Mat> src, int minVal, int maxVal, boolean normed) {
        Mat result=new Mat();
        // Establish the number of bins.
        int histSize = maxVal-minVal+1;

        // Set the ranges.
        float range[] = {(float)minVal, (float)(maxVal + 1)} ;
        float[] histRange =  range.clone() ;
        // calc histogram

        Imgproc.calcHist(src, new MatOfInt(0), new Mat(), result, new MatOfInt(histSize),new MatOfFloat(histRange), false);
        // normalize

        int r=result.rows();
        int c=result.cols();
        float []doubles=new float[r*c];
        result.get(0,0,doubles);
        if(normed) {
           long k= src.get(0).total();//返回数组元素的个数
            for(int i=0;i<r;i++){
                for(int j=0;j<c;j++){
                    doubles[i*c+j]/=k;

                }
            }
            result.put(0,0,doubles);
        }

        return result.reshape(1,1);
    }

    public static Mat spatial_histogram(Mat src, int numPatterns, int grid_x, int grid_y, boolean normed) {
        // calculate LBP patch size
        int width = src.cols() / grid_x;
        int height = src.rows() / grid_y;
        // allocate memory for the spatial histogram
        Mat result = Mat.zeros(grid_x * grid_y, numPatterns, CvType.CV_32FC1);
        // return matrix with zeros if no data was given
        if (src.empty())
            return result.reshape(1, 1);
        // initial result_row
        int resultRowIdx = 0;
        // iterate through grid

        for (int i = 0; i < grid_y; i++) {
            for (int j = 0; j < grid_x; j++) {
                Mat src_cell =new Mat(src, new Range(i*height, (i + 1)*height), new Range(j*width, (j + 1)*width));
                Mat cell_hist = histc(src_cell, 0, (numPatterns - 1), true);
                // copy to the result matrix
                Mat result_row = result.row(resultRowIdx);
                cell_hist.reshape(1, 1).convertTo(result_row, CvType.CV_32FC1);
                // increase row count in result matrix
                resultRowIdx++;
            }
        }
        // return result as reshaped feature vector
        return result.reshape(1, 1);
    }



   public static  Mat histc(Mat src, int minVal, int maxVal, boolean normed) {
        List<Mat> list=new ArrayList<Mat>();
        list.add(src);
        switch (src.type()) {
            case 1: //CvType.CV_8SC1
                return histc_(list, minVal, maxVal, normed);

            case 0://CvType.CV_8UC1
                return histc_(list, minVal, maxVal, normed);

            case 3://CV_16SC1: 3
                return histc_(list, minVal, maxVal, normed);

            case 2://CV_16UC1:2
                return histc_(list, minVal, maxVal, normed);

            case 4://CV_32SC1:4
                return histc_(list, minVal, maxVal, normed);

            case 5://CV_32FC1:5
                return histc_(list, minVal, maxVal, normed);

            default:


        }
        return new Mat();

    }





}
