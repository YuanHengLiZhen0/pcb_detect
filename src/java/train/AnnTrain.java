package train;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.ANN_MLP;
import org.opencv.ml.TrainData;
import util.Creat_Data;
import util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class AnnTrain {

     private final int  ROW_SAMPLE=0;
     private final int COL_SAMPLE=1;

     private final  static int CV_TERMCRIT_ITER=1;
     private final  String []kChars={"0","1","2","3","4","5","6","7","8","9","10"};

     private FeatureInterface feature=new Features();
     private ANN_MLP ann_=null;
     private String folderPath;
     private String xmlPath;


    public AnnTrain(String folderPath,String xmlPath){
        ANN_MLP ann= ANN_MLP.create();
        this.ann_=ann;
        this.folderPath=folderPath;
        this.xmlPath=xmlPath;

    }
    public void train(){

            int classNumber = 5;//元件种类

            Mat layers=new Mat();

            int input_number = 0;
            int hidden_number = 40;
            int output_number = 0;


            int N = input_number;
            int m = output_number;
           // int first_hidden_neurons = int(std::sqrt((m + 2) * N) + 2 * std::sqrt(N / (m + 2)));
          //  int second_hidden_neurons = int(m * std::sqrt(N / (m + 2)));

            boolean useTLFN = false;


                layers.create(1, 3, CvType.CV_32SC1);
                int layernum[]={512,hidden_number,classNumber};
                layers.put(0,0,layernum);


/*

                layers.create(1, 4, CV_32SC1);
                layers.at<int>(0) = input_number;
                layers.at<int>(1) = first_hidden_neurons;
                layers.at<int>(2) = second_hidden_neurons;
                layers.at<int>(3) = output_number;*/

//ann参数

            ann_.setLayerSizes(layers);
            ann_.setActivationFunction(ANN_MLP.SIGMOID_SYM, 1, 1);
            ann_.setTrainMethod(ANN_MLP.BACKPROP);
            ann_.setTermCriteria(new TermCriteria(CV_TERMCRIT_ITER, 30000, 0.0001));
            ann_.setBackpropWeightScale(0.1);
            ann_.setBackpropMomentumScale(0.1);


//读取目录下所有文件
             List<String> files=new ArrayList<String>();
             Util.getFiles(folderPath,files);

            if (files.size() == 0) {
                fprintf( "No file found in the train folder!\n");
                fprintf( "You should create a folder named \"tmp\" in EasyPR main folder.\n");
                fprintf( "Copy train data folder(like \"ann\") under \"tmp\". \n");
                return;
            }


            //获取数据
            //using raw data or raw + synthic data.
            TrainData traindata = sdata(350,5); //第二哥参数为元件种类

            System.out.println("Training ANN model, please wait...") ;
            long start = System.currentTimeMillis();
            ann_.train(traindata);
            long end = System.currentTimeMillis();
            ann_.save(xmlPath);


        System.out.println("Your ANN Model was saved to "+xmlPath);
        System.out.println("Training done. Time elapse: " + (double) (end - start) / (1000) +"second" );



        start = System.currentTimeMillis();
        // test();
        end = System.currentTimeMillis();
        System.out.println( "Predict done. Time elapse: " +(double) (end - start) / (1000) + "second" );
        }

//获取数据 ,每个元件的数据在相应的文件中
    private TrainData sdata(int number_for_count, int classNumber) {
       // assert(chars_folder_);

        Mat samples=new Mat();
        Vector<Integer> labels=new Vector<Integer>();

        //srand((unsigned) time(0));
        for (int i = 0; i < classNumber; ++i) {  //扫描所有文件夹.每个文件夹对应一个元件种类

            // auto char_key = kChars[i + kCharsTotalNumber - classNumber];
            String char_key = kChars[i];
            String sub_folder= folderPath+"/"+char_key+"/train";

            System.out.println(sub_folder);
            System.out.println(">> Training characters  "+char_key+" in" + sub_folder );

            List<String> chars_files =new ArrayList<String>();
            Util.getFiles(sub_folder.toString(),chars_files);


            int char_size = chars_files.size();
            System.out.println( ">> Characters count:"+char_size);  //文件夹下文件数量


            //读取样本,保存到mat中
            List<Mat> matVec=new ArrayList<Mat>(number_for_count);
           // matVec.reserve(number_for_count);    //样本数量
            for (String file : chars_files) {
                Mat img = Imgcodecs.imread(file, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);  // a grayscale image
                matVec.add(img);
            }


            //自动生成样本数据
            for (int t = 0; t <  number_for_count -  char_size; t++) {
                int rand_range = char_size + t;
                int ran_num = (int)Math.random() % rand_range;
                Mat img = matVec.get(ran_num);
                Mat simg = getSyntheticImage(img);
                matVec.add(simg);
                Imgcodecs.imwrite(sub_folder +"/" + i + "_" + t +"_" + ran_num + ".jpg", simg);

            }

            System.out.println( ">> Characters count:"+matVec.size());


            for (Mat img : matVec) {
                Mat fps = feature.getLBPFeatures(img);

                samples.push_back(fps);
                labels.add(i);
            }
        }

        Mat samples_=new Mat();
        samples.convertTo(samples_, CvType.CV_32F);
        Mat train_classes =
                Mat.zeros((int) labels.size(), classNumber, CvType.CV_32F);

        for (int i = 0; i < train_classes.rows(); ++i) {
            train_classes.put(i, labels.get(i),1.f) ;
        }
//System.out.println(samples_.rows()+"  "+train_classes.rows());
        return TrainData.create(samples_, 0, train_classes);


    }



    private Mat getSyntheticImage( Mat image) {
        Random random=new Random();
        int rand_type =  random.nextInt(100);


        Mat result = image.clone();

        if (rand_type % 2 == 0) { //上下移动
            int ran_x = random.nextInt(10) % 5 - 2;
            int ran_y = random.nextInt(10) % 5 - 2;
            result = Creat_Data.translateImg(result, ran_x, ran_y,255);
        } else if (rand_type % 2 != 0) { //旋转
            float angle = (float)(random.nextInt(360) % 360 - 5);

            result = Creat_Data.rotateImg(result, angle,255 );
        }

        return result;
    }
        private void fprintf(String str){
               System.out.println(str);
        }




}
