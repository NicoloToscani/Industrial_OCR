package advmobdev.unipr.it.ocr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import androidx.annotation.Dimension;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.imgproc.Imgproc;

import java.sql.Driver;

import static org.opencv.imgproc.Imgproc.getStructuringElement;

public class LiveCameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {


    private CameraBridgeViewBase mOpenCvCameraView;

    private boolean framesColor = false;

    RadioButton radioButtonColor, radioButtonGray;
    Button settings;

    Bundle bundle;

    Pipeline pipeline;


    // Immagine a colori in arrivo dalla videocamera
    Mat mColor, mGray = null, mGrayT = null, mGrayF = null, mColorT = null, mColorF = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Associa un activity alla sua View
        setContentView(R.layout.activity_live_camera);

        this.setTitle("Live OCR");

        System.out.println("Creata activity Live Camera");

        // Set up camera listener.
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.CameraView);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mOpenCvCameraView.enableView();

        // Ottengo dimensione del display
        Display displaySize = getWindowManager().getDefaultDisplay();
        android.graphics.Point size = new Point();
        displaySize.getSize(size);
        int width = size.x;
        int height = size.y;

        System.out.println("Dimensioni display: x = " + width + " y = " + height);

        // mOpenCvCameraView.setMaxFrameSize(width,height);


        radioButtonColor = (RadioButton)findViewById(R.id.radio_color);
        radioButtonGray = (RadioButton)findViewById(R.id.radio_gray);
        settings = (Button)findViewById(R.id.buttonSettings);



        radioButtonColor.setChecked(false);
        radioButtonGray.setChecked(true);

        // Recupero il bundle e lo utilizzo per settare i dati inseriti
        bundle = getIntent().getExtras();

        if (bundle != null) {

            pipeline = (Pipeline) bundle.getSerializable(SettingsListActivity.STRINGA_BUNDLE);
            System.out.println(pipeline.getSimpleThresholdValue());

        }


        // Ritorno ai settaggi e restituisco bundle modificato
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Apro pagina e ritorno bundle


                Context context = getApplicationContext();
                Intent intent = new Intent(new Intent(context, SettingsListActivity.class));

                intent.putExtras(bundle);
                startActivity(intent);

            }
        });



    }

    @Override
    public void onCameraViewStarted(int width, int height) {

        // Istanza delle Mat utilizzate ad ogni frame
        mGray = new Mat(height, width, CvType.CV_8UC1);
        mGrayT = new Mat(height, width, CvType.CV_8UC1);
        mGrayF = new Mat(height, width, CvType.CV_8UC1);

        mColor = new Mat(height, width, CvType.CV_8UC4);
        mColorT = new Mat(height, width, CvType.CV_8UC4);
        mColorF = new Mat(height, width, CvType.CV_8UC4);


    }

    @Override
    public void onCameraViewStopped() {

        mGray.release();
        mGrayT.release();
        mGrayF.release();

        mColor.release();
        mColorT.release();
        mColorF.release();

    }

    // Ad ogni frame che mi arriva dalla camera
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        /*
        Ruoto immagine a colori in arrivo dalla camera

        Core.transpose(mColor, mColorT);
        Imgproc.resize(mColorT, mColorF, mColorF.size(), 0,0, 0);
        Core.flip(mColorF, mColor, 1 );
        return mColor;
         */

        inputFrame.rgba().copyTo(mColor); // Copio i frame nella Mat a colori
        inputFrame.gray().copyTo(mGray);  // Copio i frame nella Mat livello di grigio

        Imgproc.cvtColor(mColor,mColor,Imgproc.COLOR_RGBA2RGB); //convert rgba mat to rgb


        /*
        // Ruoto immagine a livello di grigio su dispositivo fisico
        Core.transpose(mGray, mGrayT);
        Imgproc.resize(mGrayT, mGrayF, mGrayF.size(), 0,0, 0);
        Core.flip(mGrayF, mGray, 1 );

        */


        /* ------------------- PIPELINE PER MIGLIORAMENTO --------------------
         Ora faccio passare il flusso delle immagini in scala di grigio nella pipeline
         Pipiline statica ovvero è stata decisa priva dell'elaborazione tramite live camera
         effettuando delle prove in campo tramite una preelaborazione effettuata sulle immagini
         Un notevole migloramento sarebbe quello di poter tenere traccia di tutte le
         pre elaborazioni che vengono fatte sulla foto di test ed applicarle nell'ordine registrato
         durante il test sulla live camera

        - Conversione in scala di grigio: viene fatta dalla live camera
        - Binarizzazione: applicazione di una soglia per binarizzare l'immagine

        */


        if(framesColor == true){

            return mColor;
        }

        // Immagine in scala di grigio di partenza
        else if (framesColor == false){


            // Applico binarizzazione
            // Simple threshold
            if(pipeline.isSimpleThreshold()){

                System.out.println("Applico simple threshold su live camera");

                // Verifico tipo di soglia
                // Binaria
                if(pipeline.isBinaryThredshold()) {
                    Imgproc.threshold(mGray, mGray, pipeline.getSimpleThresholdValue(), 255, Imgproc.THRESH_BINARY);
                    System.out.println("Applicazione soglia binaria con soglia: " + pipeline.getSimpleThresholdValue());
                }
                // Otsu
                else if(pipeline.isOtsuThredshold()){
                    Imgproc.threshold(mGray, mGray, pipeline.getSimpleThresholdValue(), 255, Imgproc.THRESH_OTSU);
                    System.out.println("Applicazione soglia Otsu con soglia: " + pipeline.getSimpleThresholdValue());
                }

            }

            else if(pipeline.isAdaptiveThredshold()){

                System.out.println("Applico adaptive threshold su live camera");

                if(pipeline.isMeanThredshold()){

                    Imgproc.adaptiveThreshold(mGray, mGray, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, pipeline.getAdaptiveNeighborhoodSize(),pipeline.getAdaptiveThreasholdValue());

                    System.out.println("Applicazione soglia adattiva MEAN");
                }

                else if(pipeline.isGaussianTredshold()){

                    Imgproc.adaptiveThreshold(mGray, mGray, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, pipeline.getAdaptiveNeighborhoodSize(),pipeline.getAdaptiveThreasholdValue());

                    System.out.println("Applicazione soglia adattiva GAUSSIAN");
                }

            }

            // Applico morfologia dopo binarizzazione con maschera rettangolare

            // Erosione
            if(pipeline.isErosionRadioButton()){

                Size kernel = new Size(pipeline.getErosionXValue(),pipeline.getErosionYValue());
                Mat erodeElement = getStructuringElement(Imgproc.MORPH_RECT, kernel);
                Imgproc.erode(mGray,mGray, erodeElement);

                System.out.println("Erosione con maschera: " + pipeline.getErosionXValue() + " " + pipeline.getErosionYValue());
            }
            // Dilatazione
            else if(pipeline.isDilatationRadioButton()){

                Size kernel = new Size(pipeline.getDilatationXValue(),pipeline.getDilatationYValue());
                Mat dilateElement = getStructuringElement(Imgproc.MORPH_RECT, kernel);
                Imgproc.dilate(mGray,mGray, dilateElement);

                System.out.println("Dilatazione con maschera: " + pipeline.getDilatationXValue() + " " + pipeline.getDilatationYValue());

            }
            // Apertura
            else if(pipeline.isOpeningRadioButton()){

                Mat kernel = Mat.ones(pipeline.getOpeningXValue(),pipeline.getOpeningYValue(), CvType.CV_32F);
                Imgproc.morphologyEx(mGray, mGray, Imgproc.MORPH_OPEN, kernel );

                System.out.println("Apertura con apertura: " + pipeline.getOpeningXValue() + " " + pipeline.getOpeningYValue());

            }
            // Chiusura
            else if(pipeline.isClosingRadioButton()){

                Mat kernel = Mat.ones(pipeline.getClosingXValue(),pipeline.getClosingYValue(), CvType.CV_32F);
                Imgproc.morphologyEx(mGray, mGray, Imgproc.MORPH_CLOSE, kernel );

                System.out.println("Chiusura con maschera: " + pipeline.getClosingXValue() + " " + pipeline.getClosingYValue());

            }

            return mGray;
        }

        return null;
    }


    // Gestione radio buttom per il cambio del colore
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_color:
                if (checked) {

                    // Visualizzo frame immagini a colori
                    framesColor = true;
                    radioButtonGray.setChecked(false);
                }

                break;
            case R.id.radio_gray:
                if (checked) {

                    // Visualizzo frame immagini in scala di grigio
                    framesColor = false;
                    radioButtonColor.setChecked(false);
                }

                break;
        }
    }
}
