package advmobdev.unipr.it.ocr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;

import androidx.annotation.Dimension;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.imgproc.Imgproc;

import java.sql.Driver;
import java.util.Arrays;

import static org.opencv.imgproc.Imgproc.getStructuringElement;

public class LiveCameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {


    private CameraBridgeViewBase mOpenCvCameraView;

    private boolean framesColor = false;

    RadioButton radioButtonColor, radioButtonGray, radioHistogram;
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


        radioHistogram = (RadioButton)findViewById(R.id.radio_histogram);
        radioButtonColor = (RadioButton)findViewById(R.id.radio_color);
        radioButtonGray = (RadioButton)findViewById(R.id.radio_gray);
        settings = (Button)findViewById(R.id.buttonSettings);



        radioButtonColor.setChecked(false);
        radioButtonGray.setChecked(true);
        radioHistogram.setChecked(false);

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


    // Gestione radio buttom per il cambio del colore
    public void onRadioHistogramButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_histogram:
                if (!checked) {

                    radioHistogram.setChecked(true);
                    System.out.println("Attivato visualizzazione istogramma");
                    // Visualizzo l'istogramma sull'immagine
                    displayHistGray(mGray);

                }

                else if(checked){
                    radioHistogram.setChecked(false);
                    // Disabilito il bottone di selezione
                    System.out.println("Disabilitato visualizzazione istogramma");

                }

                break;
        }


    }

    // Visualizzazione e calcolo dell'istogramma in scala di grigio per valutare come applicare
    // la soglia per binarizzazione

    private boolean displayHistGray(Mat image) {

        Mat histImage = new Mat();
        image.copyTo(histImage);

        calcHistGray(histImage);  // Calcolo istogramma livello di grigio
         displayImage(histImage);

        return true;
    }

    // Calcolo histogramma in scala di grigio, distribuzione dei livelli di grigio all'interno dell'immagine
    // da 0 a 255
    // Asse x: intensità dei divresi livelli di grigio rk
    // Asse y: numero di pixel che rientrano i quella intensità  nk = h(rk)
    // Mi permette di capire come è distribuita l'immagine.
    // E' un immagine scura? chiara?
    private void calcHistGray(Mat image) {

        // Predisposizione per OpenCV

        int mHistGraySizeNum = 256;

        // Number of bins of the histogram
        // Intervallo che rappresenta la larghezza di una singola barra dell'istogramma lungo l'asse X
        // 256 perchè voglio tutti i valori sulla scala di grigio 0- 255
        // Bins = suddivisisoni intervallo

        // MatOfInt 1x1x1 che contiene il numbero di bin
        // 1 riga
        // 1 colonna
        // 1 canale
        MatOfInt mHistSize = new MatOfInt(mHistGraySizeNum);

        // Valori dell'istogramma, il metodo inserisce i valori dell'istogrammi calcolati
        Mat hist = new Mat();

        // Buffer in cui vado a memorizzare i valori dell'istogramma
        float []mBuff = new float[mHistGraySizeNum];

        // Limiti dell'istogramma, voglio calcolare i valori da 0 a 255
        // MatOfFloat 1x1x2
        // 1 riga
        // 1 colonna
        // 2 canali, 0 e 255
        MatOfFloat histogramRanges = new MatOfFloat(0f, 255f);

        // Predisposzione per disegnare l'istogramma sull'immagine

        // Utilizzato perplottare le linee dell'istogramma, memorizzo un insieme di scalari
        // Colore RED per istogramma rosso
        // Colore GREEN per istogramma verde
        // Colore BLUE per istogramma blue
        // Devo adattarlo al livello di grigio
        Scalar mColorsRGB[] = new Scalar[] { new Scalar(200, 0, 0, 255),
                new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255) };

        // Devo disegnare una line dell'immagine che corrsiponde ad un bin dell'istogramma
        // Se ho 256 bin avrò 256 righe
        // La linea verrà disegnata tra due punti mP1(x) e mP2(y)
        // I valori di questi punti li decideremo in base ai bin che vogliamo
        org.opencv.core.Point mP1 = new org.opencv.core.Point();
        org.opencv.core.Point mP2 = new org.opencv.core.Point();

        // Spessore delle linee
        // Se i bin sono pochi lo spessore avrà un certo numero
        // Se ho pochi bin posso fare le linee piu larghe per riempire lo schermo
        // Se ho 256 bin devo diminuire lo spessore, dipende anche dalla grandezza dell'immagine
        // La larghezza dell'immagine viene divisa per 3 perchè ho 3 immagini per quelle a colori
        int thickness = (int) (image.width() / (mHistGraySizeNum+10));

        // Controllo il massimo valore sullo spessore della linea
        if(thickness> 3) thickness = 3;

        // Indice dei canali
        MatOfInt mChannels[] = new MatOfInt[] { new MatOfInt(0), new MatOfInt(1), new MatOfInt(2) };

        // Calcolo le coordinate x e y sull'immagine di mP1 e mP2 per ciascun bin
        // Offset  da cui partire per iniziare a disegnare l'istogramma
        Size sizeRgba = image.size();

        // Da dove parto rispetto alla x
        int offset = (int) (sizeRgba.width - (3*(mHistGraySizeNum*thickness+30)));

        // Imgproc.calcHist(Arrays.asList(image), mChannels[0], new Mat(), hist, mHistSize, histRange );
        // RGB, per il coore cicla 3 volte su R G e B, qui i 3 canali sono uguali
        Imgproc.calcHist(Arrays.asList(image), mChannels[0], new
                Mat(), hist, mHistSize, histogramRanges);


        // Devo normalizzare l'istogramma perchè sulla y potrei ottenere dei valori molto alti di pixel
        // che non rientrano nello schermo
        // Normalizzo rispetto al valore massimo
        // Alpha: valore massimo di normalizzazione, il valore massimo voglio che copra metà dell'immagine
        Core.normalize(hist, hist, sizeRgba.height/2, 0,
                Core.NORM_INF);

        // Recupero i valori dell'istogramma
        // I valori che vado a prelevare i valori dell'istogramma sui bin calcolati
        hist.get(0, 0, mBuff);

        // Verifico i valori dell'istogramma calcolato
        for(int i = 0; i < mHistGraySizeNum; i++){
            System.out.println("Valore " + i + ":" + (int)mBuff[i]);
        }

        // Disegno istogramma sull'immagine
        for(int h=0; h<mHistGraySizeNum; h++) {
            mP1.x = mP2.x = ((25) + h) * thickness;
            mP1.y = sizeRgba.height; // Fondo dell'immagine
            mP2.y = mP1.y - (int)mBuff[h]; // Valore dell'istogramma
            Imgproc.line(image, mP1, mP2, mColorsRGB[2], 1);
        }

    }


    // Visualizzo immagine nel widget ImageView
    private void displayImage(Mat image)
    {
        // Creiamo una Bitmap
        Bitmap bitMap = Bitmap.createBitmap(image.cols(), image.rows(),Bitmap.Config.RGB_565);
        // Convertiamo l'immagine di tipo Mat in una Bitmap
        Utils.matToBitmap(image, bitMap);
        // Collego la ImageView e gli assegno la BitMap
        ImageView iv = (ImageView) findViewById(R.id.OCRImageView);
        iv.setImageBitmap(bitMap);

    }

}
