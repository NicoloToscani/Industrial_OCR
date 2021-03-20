package advmobdev.unipr.it.ocr;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Dimension;
import androidx.appcompat.app.AppCompatActivity;

import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

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
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;

import static org.opencv.imgproc.Imgproc.getStructuringElement;

public class LiveCameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    // Per caricamento da asset
    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean framesColor = false;

    RadioButton radioButtonColor, radioButtonGray;
    Switch switchHistogram, switchOCR, switchOcv;
    TextView ocvAverage;


    Button settings;
    Bundle bundle;
    Pipeline pipeline;

    boolean activeHist;
    boolean activeOcr;
    boolean activeOcv;

    // Tesseract
    TessOCR mTessOCR;

    OCV ocv;

    // Iteratore risultato tesseract
    ResultIterator textIterator = null;

    // Bitmap dell'immagine prepocessata da dare a tersseract
    Bitmap bitmapOCR = null;

    // Immagine finale con boundingbox
    Mat boundingImage = null;

    // Immagine a colori in arrivo dalla videocamera
    Mat mColor, mGray = null, mGrayT = null, mGrayF = null, mColorT = null, mColorF = null;

    // Lista parola da ricercare
    ArrayList<String> labelValues;
    ArrayList<String> labelCopy;
    ArrayList<String> ocrValues;

    boolean realDevice = false;

    String buildModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Associa un activity alla sua View
        setContentView(R.layout.activity_live_camera);

        this.setTitle("Live OCR");

        System.out.println("Creata activity Live Camera");

        mTessOCR = new TessOCR();

        ocv = new OCV();

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

        switchHistogram = (Switch) findViewById(R.id.switch2);
        switchHistogram.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){

                    System.out.println("Abilito istogramma");

                    activeHist = true;
                }

                else if(!isChecked){

                    System.out.println("Disabilito istogramma");

                    activeHist = false;
                }

            }
        });

        switchOCR =  (Switch) findViewById(R.id.switchOCR);
        switchOCR.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){

                    System.out.println("Abilito OCR");

                     activeOcr = true;


                }

                else if(!isChecked){

                    System.out.println("Disabilito OCR");

                    activeOcr = false;

                }

            }
        });


        radioButtonColor = (RadioButton)findViewById(R.id.radio_color);
        radioButtonGray = (RadioButton)findViewById(R.id.radio_gray);
        settings = (Button)findViewById(R.id.buttonSettings);



        radioButtonColor.setChecked(false);
        radioButtonGray.setChecked(true);
        switchHistogram.setChecked(false);

        ocvAverage = (TextView)findViewById(R.id.textViewOcv);



        switchOcv = (Switch)findViewById(R.id.switchOCV);
        switchOcv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){

                    System.out.println("Abilito OCV");

                    activeOcv = true;

                }

                else if(!isChecked){

                    System.out.println("Disabilito OCV");

                    activeOcv = false;

                }

            }
        });

        // Recupero il bundle e lo utilizzo per settare i dati inseriti
        bundle = getIntent().getExtras();

        if (bundle != null) {

            pipeline = (Pipeline) bundle.getSerializable(SettingsListActivity.STRINGA_BUNDLE);
            System.out.println(pipeline.getSimpleThresholdValue());

        }

        labelValues = pipeline.getLabelValues();

        // Shallow copy pechè la classe OCV modifica labelValues ad ogni iterazione
        labelCopy = new ArrayList<>(labelValues);


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

        // Verifico esecuzione applicazione su emulatore
        if(buildModelContainsEmulatorHints(Build.MODEL)){

            realDevice = false;

        }
        // Altrimenti è in esecuzione sul dispositivo reale
        else if(! buildModelContainsEmulatorHints(Build.MODEL)){

            realDevice = true;

        }

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

        // Dealloco quando fermo l'acquisizione
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

        // Ottengo frame dalla camera
        inputFrame.rgba().copyTo(mColor); // Copio i frame nella Mat a colori
        inputFrame.gray().copyTo(mGray);  // Copio i frame nella Mat livello di grigio
        Imgproc.cvtColor(mColor,mColor,Imgproc.COLOR_RGBA2RGB); //convert rgba mat to rgb

        // In base a come ricevo dalla telecamera applico rotazione e traslazione corrispettiva
        // Differrenzio i casi di: Camera Back, Camera Front e emulatore (back e front)

        // Caso camera back su dispositivo reale applica una rotazione a sinistra di 90°
        // Caso camera front su dispositivo reale applica una rotazione a destra di 90°
        // Caso camera su emulatore effettua solo il mirror (flip immagine)

        // Il resize fa lo scaling, cambia la dimensione dell'immagine

        // Verifico orientamento del dispositivo e se sono su dispositivo reale ruoto frame
        // Ruoto immagine a livello di grigio su dispositivo fisico
        if(realDevice == true){

            // Camera back (rotated left 90°)
            // Ruoto immagine scala di grigio in arrivo dalla camera
            Core.transpose(mGray, mGrayT);
            Imgproc.resize(mGrayT, mGrayF, mGrayF.size(), 0,0, 0);
            Core.flip(mGrayF, mGray, 1 );

            // Ruoto immagine a colori in arrivo dalla camera su dispositivo fisico
            Core.transpose(mColor, mColorT);
            Imgproc.resize(mColorT, mColorF, mColorF.size(), 0,0, 0);
            Core.flip(mColorF, mColor, 1 );

            /*
            // Camera front (rotated right 90°) real device:

            // Ruoto immagine a scala di grigio in arrivo dalla camera
            Core.flip(mGray, mGrayF, 1);
            Core.transpose(mGrayF, mGrayT);
            Core.flip(mGrayT,mGrayF,1);
            Imgproc.resize(mGrayF, mGray, mGray.size(), 0,0, 0);

            // Ruoto immagine a colori in arrivo dalla camera
            Core.flip(mColor, mColorF, 1);
            Core.transpose(mColorF, mColorT);
            Core.flip(mColorT,mColorF,1);
            Imgproc.resize(mColorF, mColor, mColor.size(), 0,0, 0);

            */

        }

        /* ------------------- PIPELINE PER MIGLIORAMENTO --------------------
         Ora faccio passare il flusso delle immagini in scala di grigio nella pipeline
         Pipeline statica ovvero è stata decisa priva dell'elaborazione tramite live camera
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


            // Se presente applico equalizazzione istogramma
            // Equalizazione standard
            if(pipeline.isSimpleEqRadioButton()){

                Imgproc.equalizeHist(mGray,mGray);
                System.out.println("Applicata equalizazione standard");

            }
            // Equalizazione CLACHE - Contrast Limited Adaptive Histogram Equalization
            else if(pipeline.isClacheEqRadioButton()){

                Size size = new Size(pipeline.getTileSizeXValue(), pipeline.getTileSizeYValue());
                CLAHE clache = Imgproc.createCLAHE(pipeline.getLimitValue(), size);

                clache.apply(mGray, mGray);

                System.out.println("Applicato equalizazione CLACHE");

            }

            // Se attiva applico  filtro di sharpening

            if(pipeline.isUnsharpMaskingEnable()){


                // Applicare filtro di smoothing per sfocare immagine originale
                // Come immagine sfocata prendo una gray image gia preelaborata con un opportuno filtro
                // di smoothing per vedere i diversi risultati


                Mat blurredImage=new Mat();
                Size size=new Size(pipeline.getkSizeXUnmaskFilter(),pipeline.getkSizeYUnmaskFilter());
                // Applico filtro gaussiano
                // la deviazione standard sulla X e sulla Y sulla base della grandezza del filtro
                // Indicando 0 le calcola in automatico
                // Maggiore è la varianza piu il filtro è potente
                Imgproc.GaussianBlur(mGray, blurredImage, size, pipeline.getSigmaXUnmaskFilter(),pipeline.getSigmaYUnmaskFilter());

                // Sottrarre immagine sfocata tramite filtro di sharpering dall'originale

                // Calcolo la maschera
                Mat mask = new Mat();
                Core.subtract(mGray, blurredImage, mask);

                // Aggiungere la maschera all'originale con un opportuno peso K
                // Se k = 1 allora è un Unsharp Mask
                // Se k > 1 allora è un filtraggio highboost

                // Scalar k = new Scalar(6.5);

                Scalar k = new Scalar(pipeline.getScalarMaskUnmask());
                Mat temp = new Mat();
                Core.multiply(mask, k, temp);
                Mat sharpedImage = new Mat();

                Core.add(mGray, temp, sharpedImage);

                mGray = sharpedImage;

            }


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
                   // Imgproc.threshold(mGray, mGray, pipeline.getSimpleThresholdValue(), 255, Imgproc.THRESH_OTSU);
                    Imgproc.threshold(mGray, mGray, 0, 255, Imgproc.THRESH_OTSU);
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

            else if(activeHist == true){

                 displayHistGray(mGray);
                System.out.println("Abilitato istogramma");

            }


            if(activeOcr == true){

                bitmapOCR = bitMapProcess(mGray);

                // Ottengo OCR
                doOCR(bitmapOCR);

                // Ottengo iteratore da Tesseract
                textIterator = mTessOCR.ocrIterator();

                boundingImage = mGray;

                calculateBoundingBoxWorld(textIterator, boundingImage);

            }


            if((activeOcv == true) && (activeOcr == true)){

                 if(ocrValues != null) {

                     Double average = ocv.applyOcv(ocrValues, labelCopy);

                     if(!average.isNaN()){

                         String avg = Double.toString(average);
                         String ocvAvg = avg + " %";

                         setText(ocvAverage, ocvAvg);

                         System.out.print(average);
                     }

                 }




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


    /*
    // Gestione radio buttom per il cambio del colore
    public void onRadioHistogramButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_histogram:
                if (!checked) {

                    activeHist = false;
                    radioHistogram.setChecked(false);
                }

                else if(checked){
                    radioHistogram.setChecked(true);
                    // Disabilito il bottone di selezione
                    System.out.println("Attivato visualizzazione istogramma");
                    // Visualizzo l'istogramma sull'immagine
                    activeHist = true;

                }

                break;
        }


    }
    */
    // Visualizzazione e calcolo dell'istogramma in scala di grigio per valutare come applicare
    // la soglia per binarizzazione

    private boolean displayHistGray(Mat image) {

        Mat histImage = new Mat();
        image.copyTo(histImage);

        calcHistGray(histImage);  // Calcolo istogramma livello di grigio
         //displayImage(histImage);
        histImage.copyTo(mGray);

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

        // Punti per riferimento asse X
        org.opencv.core.Point mPx0 = new org.opencv.core.Point(); // x_min
        org.opencv.core.Point mPy0 = new org.opencv.core.Point(); // y_min
        org.opencv.core.Point mPx1 = new org.opencv.core.Point(); // x_max
        org.opencv.core.Point mPy1 = new org.opencv.core.Point(); // y_max
        org.opencv.core.Point mPxm = new org.opencv.core.Point(); // x_med
        org.opencv.core.Point mPym = new org.opencv.core.Point(); // y_med

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
            Imgproc.line(image, mP1, mP2, mColorsRGB[0], 1);
        }

        // Disegno le coordinate di riferimento per l'istogramma utili per identificare il punto
        // di sogliatura
        // mPx0 = 0
        // mPx1 = 255
        // mPxm = 127

        mPx0.x = mPy0.x = 25 * thickness;
        mPx0.y = sizeRgba.height;
        mPy0.y = mPx0.y - 20;
        Imgproc.line(image, mPx0, mPy0, mColorsRGB[0], 3);

        mPx1.x = mPy1.x = (25 + 255) * thickness;
        mPx1.y = sizeRgba.height;
        mPy1.y = mPx0.y - 20;
        Imgproc.line(image, mPx1, mPy1, mColorsRGB[0], 3);

        mPxm.x = mPym.x = (25 + 127) * thickness;
        mPxm.y = sizeRgba.height;
        mPym.y = mPxm.y - 20;
        Imgproc.line(image, mPxm, mPym, mColorsRGB[0], 3);

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

    // Ritorno bitmap dopo elaborazione per edge detection
    private Bitmap bitMapProcess(Mat image){
        Bitmap bitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, bitmap);
        return bitmap;
    }



    // Chiama il metodo getOCRResult
    private void doOCR(final Bitmap bitmap) {

        String srcText = mTessOCR.getOCRResult(bitmap);
        System.out.println("\n");
        System.out.println(srcText);

    }

    // Disegno bounding box parole ottenute da Tesseract
    private void calculateBoundingBoxWorld(ResultIterator iterator, Mat image){

        String lastUTF8Text;
        float lastConfidence;
        int[] lastBoundingBox;
        int count = 0;
        iterator.begin();
        System.out.println("--- Parole OCR trovate ----");

        ocrValues = new ArrayList<String>();
        do {

            // TessBaseAPI.PageIteratorLevel.RIL_WORD - Itero su una parola
            lastUTF8Text = iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD);

            lastConfidence = iterator.confidence(TessBaseAPI.PageIteratorLevel.RIL_WORD);

            ocrValues.add(lastUTF8Text);

            // Bounding box dell'ultima lettera trovata
            lastBoundingBox = iterator.getBoundingBox(TessBaseAPI.PageIteratorLevel.RIL_WORD);

            // Coordinate P1: left-top
            int x_1 = (lastBoundingBox[0]);
            int y_1= (lastBoundingBox[1]);

            // Coordinate P2: righ-bottom
            int x_2 = (lastBoundingBox[2]);
            int y_2= (lastBoundingBox[3]);

            // Punti per rettangolo
            org.opencv.core.Point rectP1 = new org.opencv.core.Point(x_1, y_1);
            org.opencv.core.Point rectP2 = new org.opencv.core.Point(x_2, y_2);

            Imgproc.rectangle(image, rectP1, rectP2, new Scalar(0,255,0), 2);

            // Etichetta su bounding box parola rilevata
            String label = lastUTF8Text + ": " + lastConfidence;
            int[] baseLine = new int[1];
            // Size labelSize = Imgproc.getTextSize(label, Core.FONT_HERSHEY_SIMPLEX, 0.5, 1, baseLine);

            // Inserisco stringa riconosciuta e livello di confidenza
            Imgproc.putText(image, label, new org.opencv.core.Point(x_1, y_1),
                    Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 0));


            System.out.println("Parola: " + lastUTF8Text + " Confidence: " + lastConfidence);

            count++;
        }
        while (iterator.next(TessBaseAPI.PageIteratorLevel.RIL_WORD)); // Ogni parola

    }


    // Verifico se l'applicazione è in esecuzione sull'emulatore o sul dispositivo reale
    // Utilizzo per modificare l'orientamento del frame della camera live
    public boolean buildModelContainsEmulatorHints(String buildModel) {
        return buildModel.startsWith("sdk")
                || "google_sdk".equals(buildModel)
                || buildModel.contains("Emulator")
                || buildModel.contains("Android SDK");
    }


    // Aggiorno media su interfaccia grafica
    private void setText(final TextView text,final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }






}
