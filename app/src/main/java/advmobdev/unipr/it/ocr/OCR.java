package advmobdev.unipr.it.ocr;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;


import java.io.BufferedInputStream;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.opencv.imgproc.Imgproc.MORPH_DILATE;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.rectangle;
import static org.opencv.imgproc.Imgproc.threshold;

public class OCR extends AppCompatActivity implements NumberPicker.OnValueChangeListener, View.OnTouchListener {
    private static final String TAG = "OCR";
    private static final int SELECT_PICTURE = 1;
    private String selectedImagePath;

    private Pipeline pipeline;
    public static String STRINGA_BUNDLE = "pipeline_bundle";

    // Per caricamento da asset
     @SuppressWarnings("deprecation")
     private static final String data_path = Environment.getExternalStorageDirectory().toString() + "/tesseract";
     private static final String tess_data = "/tessdata";

   // private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_PERMISSIONS = 1;

    boolean realDevice = false;

    Context context;
    Intent intent;

    // Flag di binarizzazione, resettato ad ogni nuovo caricamento immagine
    Boolean imageBin = false;

    // Immagine originale a colori caricata, applico gli algoritmi da questa in poi
    Mat sampledImage=null;

    // Immagine caricata dalla libreria
    Mat originalImage=null;

    // Immagine convertita a livello di grigio e utilizzata per tutta l'elaborazione
    Mat greyImage=null;

    // Immagine prepocessata da inviare a Tesseract
     Mat prepocessedImage = null;

    // Bit a 1 dopo che è stato applicato uno smoothing
    Boolean smoothImage = false;

    // Immagine finale con boundingbox
    Mat boundingImage = null;

    // Bitmap dell'immagine prepocessata da dare a tersseract
    Bitmap bitmapOCR = null;

    int mHistSizeNum = 25;
    int mHistGraySizeNum = 256;


    // Gestione della trasformazione geometrica
    ImageView iv;    // ImageView su cui acquisire touch dei punti
    private ArrayList<org.opencv.core.Point> mCorners=new ArrayList<org.opencv.core.Point>();

    // Sotto-rettangolo immagine da processare
    Boolean subRectangle = false;

    Rect subRect = null;

    // Iteratore risultato tesseract
    ResultIterator textIterator = null;

    // Lista dei valori ricercare
    ArrayList<String> labelValues;

    // Lista dei caratteri ottenuti da Tesseract da utilizzare nel controllo OCV
    ArrayList<String> ocrValues;

    // Barra navigazione cambio pagine
    BottomNavigationView bottomNavigationView;

    // Tesseract
    TessOCR mTessOCR;
    private  String language = "ita";

    // OCV control
    OCV ocv;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");

                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    NumberPicker np; // Number picker


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verifico esecuzione applicazione su emulatore
        if(buildModelContainsEmulatorHints(Build.MODEL)){

            realDevice = false;

        }
        // Altrimenti è in esecuzione sul dispositivo reale
        else if(! buildModelContainsEmulatorHints(Build.MODEL)){

            realDevice = true;

            prepareTessData();

        }

        context = getApplicationContext();

        pipeline = new Pipeline();

        labelValues = new ArrayList<String>(10);



        // Ottengo oggetto tesseract
        mTessOCR = new TessOCR();

        // Controllo OCV
        ocv = new OCV();

        // Associa un activity alla sua View
        setContentView(R.layout.activity_ocr);

        // Carico il number picker nel layout
        //   np = (NumberPicker) findViewById(R.id.nBin);

        iv = (ImageView) findViewById(R.id.OCRImageView);
        iv.setOnTouchListener(this);

        // Aggancio UI
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Listener pressione tasti
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {

                Bundle bundle = new Bundle();
                // Pulsante Home
                switch (item.getItemId()){
                    /*
                    case R.id.page_1:
                        System.out.println("Avvio Activity impostazione parametri algoritmi");
                        // Creo intent per il cambio activity
                         context = getApplicationContext();
                         intent = new Intent(new Intent(context, PrepocessingActivity.class));
                        startActivity(intent);

                        return true;

                     */
                    case R.id.page_2:
                        System.out.println("Avvio Activity inserimento campi etichetta");


                        bundle.putStringArrayList(STRINGA_BUNDLE, labelValues);

                        // Creo intent per il cambio activity
                        context = getApplicationContext();
                        intent = new Intent(new Intent(context, LabelActivity.class));
                        intent.putExtras(bundle);
                        startActivity(intent);
                        return true;

                    case R.id.page_3:
                        System.out.println("Avvio Activity live camera");



                        // Apro l'activity principale passando il bundle con i dati inseriti dall'utente

                        bundle = new Bundle();
                        bundle.putSerializable(STRINGA_BUNDLE, pipeline);
                        Context context = getApplicationContext();

                        Intent intent = new Intent(new Intent(context, LiveCameraActivity.class));
                        intent.putExtras(bundle);
                        startActivity(intent);

                        System.out.println("Passato dati per analisi OCR");

                        return true;

                    /*
                    case R.id.page_4:
                        System.out.println("Avvio List View");

                        // Apro l'activity principale passando il bundle con i dati inseriti dall'utente

                        Bundle bundle = new Bundle();
                        bundle.putSerializable(STRINGA_BUNDLE, pipeline);
                        Context context = getApplicationContext();

                        Intent intent = new Intent(new Intent(context, SettingsListActivity.class));
                        intent.putExtras(bundle);
                        startActivity(intent);

                        System.out.println("Passato dati per analisi OCR");

                        return true;

                        */


                }


                return false;
            }
        });

        // Listener che si mette in ascolto sul cambio di valore
      //  np.setOnValueChangedListener(this);
      //  np.setMinValue(1);
      //  np.setMaxValue(256);
      //  np.setValue(25);     // Valore iniza  le
      //  np.setVisibility(View.INVISIBLE);  // Lo rendo visibile quando viene caricato istogramma


        // Se il bundle è presente setto la lista per OCR
        Bundle bundle = getIntent().getExtras();

        if(bundle != null){

            labelValues = bundle.getStringArrayList(LabelActivity.STRINGA_BUNDLE);

            System.out.println("Ottenuto i dati da controllare");
            for (int i = 0; i < labelValues.size(); i++){
                System.out.println(labelValues.get(i).toString());

            }

        }

        pipeline.setLabelValues(labelValues);

    }

    // Ogni volta che riassumo l'applicane ma anche quando rilanciata app se chiusa
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onResume(){
        super.onResume();

        // Verifico permesso per la fotocamera e scrittura(lettura) su storage
        if((checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            // Se ho il permesso posso eseguire questo codice
            Log.i(TAG, "Permesso attivo per la fotocamera");
            Log.i(TAG, "Permesso per lettura e scrittura su storage attivo");

            // Chiama l'inizializzazione asincrona e passa l'oggetto callback creato in precendenza
            // e sceglie quale versione di OpenCV caricare.
            // Serve anche a verificare che l'OpenCV manager installato support la versione che si sta
            // provando a caricare.
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        }

        else if (! (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {

            // Richiesta permessi camera - Deny or Allow
            // Quando l'utente risponde alla richiesta dei pribilegi vieni invocata onRequestPermissionsResult
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ocr, menu);
        return true;
    }

    // Chiamato quando l'utente cambia il picker e quando l'utente sceglie l'istogramma

    private boolean displayHist(Mat image) {
        if (sampledImage == null) {
            Context context = getApplicationContext();
            CharSequence text = "Bisogna prima caricare un'immagine!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return true;
        }
        Mat histImage = new Mat();
        image.copyTo(histImage);
        calcHist(histImage);  // Calcolo istogramma
        // calcHistGray(histImage);
        displayImage(histImage);
        return true;
    }

    private boolean displayHistGray(Mat image) {

        Mat histImage = new Mat();
        image.copyTo(histImage);

        calcHistGray(histImage);  // Calcolo istogramma livello di grigio
        displayImage(histImage);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_openGallery) {
            Intent intent = new Intent();
            // Definisco tipo di interazione a cui è soggetto l'intent
            intent.setType("image/*");
            // Definisco azione di selezione su immagine di qualunque tipo
            intent.setAction(Intent.ACTION_PICK);
            // Avvio Intent e quando conclusa aspetto un risultato di ritorno
            // Chooser per gestire la scelta dell'activity con cui gestire un Intent per definire
            // con quale app aprire l'immagine (galleria)
            startActivityForResult(Intent.createChooser(intent,
                    "Seleziona immagine"), SELECT_PICTURE);
            return true;
        }

        // Applico trasformazione prospettica
        else if(id == R.id.action_geometry){
            // Verifico corretto caricamento dell'immagine
            if(sampledImage==null)
            {
                Context context = getApplicationContext();
                CharSequence text = "Bisogna prima caricare un'immagine!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }

            // Controllo che l'utente abbia premuto almeno i 4 vertici
            if (mCorners.size() < 4) {
                Context context = getApplicationContext();
                CharSequence text = "Bisogna selezionare almeno 4 vertici!";
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }

            // Calcolo il centroide facendo la media delle corrdinate dei vertici
            org.opencv.core.Point centroid = new org.opencv.core.Point(0, 0);
            for (org.opencv.core.Point point : mCorners) {
                centroid.x += point.x;
                centroid.y += point.y;
            }

            centroid.x /= mCorners.size();
            centroid.y /= mCorners.size();
            sortCorners(centroid);

            // Ora calcolo trasformazione e la applico all'immagine
            Mat correctedImage=new Mat(sampledImage.rows(), sampledImage.cols(),sampledImage.type());
            Mat srcPoints= Converters.vector_Point2f_to_Mat(mCorners);
            Mat destPoints=Converters.vector_Point2f_to_Mat(
                    Arrays.asList(new org.opencv.core.Point[]{
                            new org.opencv.core.Point(correctedImage.cols(),
                                    correctedImage.rows()),
                            new org.opencv.core.Point(0, correctedImage.rows()),
                            new org.opencv.core.Point(0,0),
                            new org.opencv.core.Point(correctedImage.cols(),0)
                    }));
            Mat transformation=Imgproc.getPerspectiveTransform(srcPoints, destPoints);
            Imgproc.warpPerspective(sampledImage, correctedImage,transformation, correctedImage.size());

            // Copio l'immagine trasposta nell'immagine sorgente in modo da poterla utilizzare
            // per gli algoritmi successivi
            sampledImage = correctedImage;

            // Stampo immagine originale a cui è stata applicata la trasformazione prospettica
            displayImage(sampledImage);

            // Azzero tutti gli angoli per calcolo nuova trasformazione
            mCorners.clear();

        }

        // Visualizza Istogramma
        else if (id == R.id.action_Hist) {
            /* Inizio Variante 1 */
           // np.setVisibility(View.VISIBLE);
            // Visualizzo istogramma immagine a colori
           displayHist(sampledImage);

            // Visualizzo istogramma immagine a livelli di grigio
            // displayHist(greyImage);


        }
        // Converti a livello di grigio
        else if (id == R.id.action_togs) {
            if(sampledImage==null)
            {
                Context context = getApplicationContext();
                CharSequence text = "Bisogna prima caricare un'immagine!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            greyImage=new Mat();
            // Converto immagine a colori RGB a livelli di grigio
            Imgproc.cvtColor(sampledImage, greyImage, Imgproc.COLOR_RGB2GRAY);
            displayImage(greyImage);

            // Provo  a plottare istogramma livello di grigio, le componenti RGB presentano gli stessi
            // valori perchè nella conversione da scala di grigio a RGB vengono copiati i valori
            // dei livelli di grigio nelle 3 componenti
            Mat eqC = new Mat();
            Imgproc.cvtColor(greyImage, eqC, Imgproc.COLOR_GRAY2RGB);
           displayHistGray(eqC); // Metodo per visualizazione istogramma su livello di grigio
           // displayHist(eqC);


            return true;
        }

        // Filtri di Smoothing
        else if (id == R.id.action_average || id == R.id.action_gaussian || id == R.id.action_median
                || id == R.id.action_laplancian || id == R.id.action_unsharp_mask || id == R.id.action_gradient){
            blurImage(id);
        }

        // Equalizza a livello di grigio
        else if (id == R.id.action_egs) {
            // Devo prima convertire a grigio
            if(greyImage==null)
            {
                Context context = getApplicationContext();
                CharSequence text = "Bisogna prima convertire a livelli di grigio!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            Mat eqGS=new Mat();

            // Equalizza l'istogramma, cerco di migliorare il contrasto dell'immagine
            // Non è detto che nel caso dell'OCR miglioriamo perchè perdo l'informazione sul nero
            // delìi caratteri
            // L'equalizazione dell'immagine di OpenCV lavora su un singolo canale, quindi inscala di
            // grigio oppure nel caso RGB mi devo estrarre i diversi canali
            Imgproc.equalizeHist(greyImage, eqGS);
            displayImage(eqGS);
            Mat eqC = new Mat();
            Imgproc.cvtColor(eqGS, eqC, Imgproc.COLOR_GRAY2RGB);

             // Visualizzo istogramma dopo equalizazione del livello di grigio
             displayHist(eqC);

             // Assegno l'immagine equalizzata alla Mat grayImage in  modo da poter essere presa
            // in carica dall'algoritmo successivo

            greyImage = eqGS;

            return true;
        }

        // Binarizzo immagine
        // Opzione 1: sfondo bianco con caratteri neri
        // Opzione 2: sfondo nero con caratteri bianchi

        // Trasformo immagine da scala di grigio a binaria
        // La soglia dattiva puo essere:
        // - Gaussiana
        // - Media
        // - Otsu

        else if(id == R.id.action_binary){

            if(greyImage==null)
            {
                Context context = getApplicationContext();
                CharSequence text = "Bisogna prima convertire a livelli di grigio!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }

            Mat binaryImage = new Mat();

            // Applico algoritmo di binarizzazione sull'immagine in scala di grigio e gli riassegno
            // i valori

            // Applico la soglia di Otsu che tiene conto delle differenzae di variazione di luce
             //Imgproc.threshold(greyImage, greyImage, 20, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
            // Imgproc.threshold(greyImage, greyImage,0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

              Imgproc.threshold(greyImage, greyImage,0, 255, Imgproc.THRESH_OTSU);

          //  Imgproc.adaptiveThreshold(greyImage, greyImage, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 10,10);

            // greyImage = binaryImage;

            imageBin = true; // Ho binarizzato l'immagine e posso applicare algoritmi successivi

            // Vidualizzo immagine
            displayImage(greyImage);

        }

        // Equalizzazione dell'istogramma con illuminazione, CLACHE
        else if(id == R.id.action_clahe){

            if(greyImage==null)
            {
                Context context = getApplicationContext();
                CharSequence text = "Bisogna prima convertire a livelli di grigio!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }

            double clipLimits = 5.0;
            Size tileGridSize = new Size(8,8);

            Mat eqGS = new Mat();
            CLAHE clache = Imgproc.createCLAHE(clipLimits, tileGridSize);

            // Equilizo istogramma
            clache.apply(greyImage, eqGS);

            // Assegno l'immagine equalizzata alla Mat grayImage in  modo da poter essere presa
            // in carica dall'algoritmo successivo

            greyImage = eqGS;
             displayImage(greyImage);

        }

        // Erosione
        else if(id == R.id.action_erosion){
            // Verifico che l'immagine sia stata binarizzata
            System.out.println("Immagine binarizzata = " + imageBin.toString());

            if(imageBin==false)
            {
                Context context = getApplicationContext();
                CharSequence text = "L'immagine deve essere binarizzata!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }
            Size kernel = new Size(2.5,2.5);
            Mat erodeElement = getStructuringElement(Imgproc.MORPH_RECT, kernel);

            Imgproc.erode(greyImage,greyImage, erodeElement);

            displayImage(greyImage);

        }

        // Dilatazione
        else if (id == R.id.action_dilation){
            // Verifico che l'immagine sia stata binarizzata
            if(imageBin==false)
            {
                Context context = getApplicationContext();
                CharSequence text = "L'immagine deve essere binarizzata!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }

            Size kernel = new Size(2.5,2.5);
            Mat dilateElement = getStructuringElement(Imgproc.MORPH_RECT, kernel);
            Imgproc.dilate(greyImage,greyImage, dilateElement);

            displayImage(greyImage);

        }

        // Apertura: per rimouovere rumore
        // erosione seguita da dilatazione
        else if (id == R.id.action_opening){
            // Verifico che l'immagine sia stata binarizzata
            if(imageBin==false)
            {
                Context context = getApplicationContext();
                CharSequence text = "L'immagine deve essere binarizzata!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }

            Mat kernel = Mat.ones(5,5, CvType.CV_32F);
            Imgproc.morphologyEx(greyImage, greyImage, Imgproc.MORPH_OPEN, kernel );
            displayImage(greyImage);

        }

        // Chiusura: chiude piccoli fori
        // Dilatazione seguita da erosione
        else if (id == R.id.action_closing){
            // Verifico che l'immagine sia stata binarizzata
            if(imageBin==false)
            {
                Context context = getApplicationContext();
                CharSequence text = "L'immagine deve essere binarizzata!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
            }

            Mat kernel = Mat.ones(5,5, CvType.CV_32F);
            Imgproc.morphologyEx(greyImage, greyImage, Imgproc.MORPH_CLOSE, kernel );
            displayImage(greyImage);

        }

        // Eseguo scansione caratteri con Tesseract
        else if (id == R.id.action_ocr_apply){

            System.out.println("Ottengo caratteri con Tesseract");

            // Assegno l'immagine preprocessata inviata a Tesseract come immagine finale sulla quale
            // stampo i boundingbox
            boundingImage = sampledImage;

            // Aggiunto controlllo: potrei fornire a tesseserct l'immagine a colori con un
            // aumento del tempo di calcolo
            Mat tesseractMat = new Mat();

            if(greyImage == null){

                tesseractMat = sampledImage;

            }

            else if(greyImage != null){

                tesseractMat = greyImage;



            }

            // Converto la Mat prepocessata come Bitmap da dare a Tesseract
           // bitmapOCR = bitMapProcess(greyImage);
            bitmapOCR = bitMapProcess(tesseractMat);

            // Ottengo OCR
            doOCR(bitmapOCR);

            // Ottengo iteratore da Tesseract

            textIterator = mTessOCR.ocrIterator();

            // Ciclo su iteratori per ottenere bounding box sulle parole
            calculateBoundingBoxWorld(textIterator);

            // Stampo immagine originale con inserito i bounding box trovati
            displayImage(boundingImage);

            // Applico OCV - Optical Character Verification
            // Ottengo un riscontro tra i caratteri riconosciuti e quelli impostati sulla verifica.
            // Ora faccio una media delle confidence sulle parole riconosciute.
            // Valutare come verificano i sistemi di visione
             double ocvAverage = ocv.applyOcv(ocrValues, labelValues);

             System.out.print(" ----- Media delle parole riconosciute: " + ocvAverage + " % -------\n");

             // Toast OCV
            Context context = getApplicationContext();
            CharSequence text = "Campi riconosciuti: " + ocvAverage + " %";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return true;

        }


        /*
        // Avvio activity per inserimento caratteri etichetta
        else if (id == R.id.action_label){

            System.out.println("Avvio Activity inserimento campi etichetta");

            // Creo intent per il cambio activity
            Context context = getApplicationContext();
            Intent intent = new Intent(new Intent(context, LabelActivity.class));
            startActivity(intent);

        }
        */
        return super.onOptionsItemSelected(item);
    }

    // Ritorno del risultato del caricamento immagine da galleria
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                // Ottengo URI a immagine da galleria immagini
                Uri selectedImageUri = data.getData();

                Log.i(TAG, "uri: " + selectedImageUri);

                // Trasformo uri in un path per im_read di open cv
                selectedImagePath = getPath(selectedImageUri);
                Log.i(TAG, "selectedImagePath: " + selectedImagePath);
                try {
                    loadImage(selectedImagePath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                displayImage(sampledImage);
            }
        }
    }

    // Ottengo link a immagine in memoria da uri
    private String getPath(Uri uri) {
        if(uri == null ) {
            return null;
        }
        // prova a recuperare l'immagine prima dal Media Store
        // questo però funziona solo per immagini selezionate dalla galleria

        String[] projection = { MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(uri, projection,
                null, null, null);
        // Verifco che sia riuscito ad accedere alla tabella
        if(cursor != null ){
            int column_index = cursor.getColumnIndexOrThrow(
                    MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

    private double calculateSubSampleSize(Mat srcImage, int reqWidth, int reqHeight) {
// Recuperiamo l'altezza e larghezza dell'immagine sorgente
        int height = srcImage.height();
        int width = srcImage.width();
        double inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
// Calcoliamo i rapporti tra altezza e larghezza richiesti e quelli dell'immagine sorgente
            double heightRatio = (double) reqHeight / (double) height;
            double widthRatio = (double) reqWidth / (double) width;
// Scegliamo tra i due rapporti il minore
            inSampleSize = heightRatio<widthRatio ? heightRatio :widthRatio;
        }
        return inSampleSize;
    }

    private void loadImage(String path) throws FileNotFoundException {

        // Resetto il flag di binarizzazione
        imageBin = false;
        smoothImage = false;
        originalImage = new Mat();


            originalImage = Imgcodecs.imread(path);
            Mat rgbImage = new Mat();
            Imgproc.cvtColor(originalImage, rgbImage, Imgproc.COLOR_BGR2RGB);

            Display display = getWindowManager().getDefaultDisplay();
            // Qui va selezionato l'import della classe "android graphics Point" !
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;

            System.out.println("Dimensione immagine selezionata X(righe):  " + rgbImage.rows() + " Y(colonne): " + rgbImage.cols());
            sampledImage = new Mat();

            double downSampleRatio = calculateSubSampleSize(rgbImage, width, height);
            Imgproc.resize(rgbImage, sampledImage, new Size(), downSampleRatio, downSampleRatio, Imgproc.INTER_AREA);

            // Ridimensiono per Tesseract, da valutare se si può migliare
            //  Imgproc.resize(rgbImage, sampledImage, new Size(),1.2,1.2,Imgproc.INTER_CUBIC);




        try {
            ExifInterface exif = new ExifInterface(selectedImagePath);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 1);
            switch (orientation)
            {
                case ExifInterface.ORIENTATION_NORMAL:
                    // ottieni l'immagine specchiata
                    sampledImage=sampledImage.t();

                    // flip lungo l'asse y
                    Core.flip(sampledImage, sampledImage, 1);

                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    // ottieni l'immagine "sotto-sopra"
                    sampledImage=sampledImage.t();

                    // flip lungo l'asse x
                    Core.flip(sampledImage, sampledImage, 0);

                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
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

    // Calcolo istogramma dell'immagine a colori
    private void calcHist(Mat image)
    {
        MatOfInt mHistSize = new MatOfInt(mHistSizeNum);
        Mat hist = new Mat();
        float []mBuff = new float[mHistSizeNum];
        // MatOfFloat histogramRanges = new MatOfFloat(0f, 256f);
        MatOfFloat histogramRanges = new MatOfFloat(0f, 255f);

        Scalar mColorsRGB[] = new Scalar[] { new Scalar(200, 0, 0, 255),
                new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255) };
        org.opencv.core.Point mP1 = new org.opencv.core.Point();
        org.opencv.core.Point mP2 = new org.opencv.core.Point();
        int thickness = (int) (image.width() / (mHistSizeNum+10)/3);
        if(thickness> 3) thickness = 3;
        MatOfInt mChannels[] = new MatOfInt[] { new MatOfInt(0), new MatOfInt(1), new MatOfInt(2) };
        Size sizeRgba = image.size();
        int offset = (int) (sizeRgba.width - (3*(mHistSizeNum*thickness+30)));
        // RGB
        for(int c=0; c<3; c++) {
            Imgproc.calcHist(Arrays.asList(image), mChannels[c], new
                    Mat(), hist, mHistSize, histogramRanges);
            Core.normalize(hist, hist, sizeRgba.height/2, 0,
                    Core.NORM_INF);
            hist.get(0, 0, mBuff);
            for(int h=0; h<mHistSizeNum; h++) {
                mP1.x = mP2.x = offset + (c * (mHistSizeNum + 10) + h) * thickness;
                mP1.y = sizeRgba.height-1;
                mP2.y = mP1.y - (int)mBuff[h];
                Imgproc.line(image, mP1, mP2, mColorsRGB[c], 1);
            }
        }
    }


    // Calcolo histogramma in scala di grigio, distribuzione dei livelli di grigio all'interno dell'immagine
    // da 0 a 255
    // Asse x: intensità dei divresi livelli di grigio rk
    // Asse y: numero di pixel che rientrano i quella intensità  nk = h(rk)
    // Mi permette di capire come è distribuita l'immagine.
    // E' un immagine scura? chiara?
    private void calcHistGray(Mat image) {

        // Predisposizione per OpenCV

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
                mP1.x = mP2.x = ((mHistSizeNum) + h) * thickness;
                mP1.y = sizeRgba.height; // Fondo dell'immagine
                mP2.y = mP1.y - (int)mBuff[h]; // Valore dell'istogramma
                Imgproc.line(image, mP1, mP2, mColorsRGB[2], 1);
            }

    }

    // Al cambio di valore del picker ricalcolo istogramma
    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

        // Al cambio del valore del picker ricalcolo istogramma
        // Passandogli i bin selezionati
        mHistSizeNum=newVal;
         displayHist(sampledImage );
    }


    // Ritorno bitmap dopo elaborazione per edge detection
    private Bitmap bitMapProcess(Mat image){
        Bitmap bitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, bitmap);
        return bitmap;
    }

    // Visualizzo immagine in formato bitmap e non Mat utilizzata da Tesseract per l'elaborazione
    private void displayImageOCR(Bitmap image){

        ImageView iv = (ImageView) findViewById(R.id.OCRImageView);
        iv.setImageBitmap(image);

    }


    // Chiama il metodo getOCRResult
    private void doOCR(final Bitmap bitmap) {

        String srcText = mTessOCR.getOCRResult(bitmap);
        System.out.println("\n");
        System.out.println(srcText);

        // Stampo immagine in bianco e nero
        displayImageOCR(bitmap);

    }

    // Ordino gli angoli rispetto al centroide
    // Ordino i 4 vertici rispetto alla distanza dal centroide
    private void sortCorners(org.opencv.core.Point center)
    {
        ArrayList<org.opencv.core.Point> top=new ArrayList<org.opencv.core.Point>();
        ArrayList<org.opencv.core.Point> bottom=new ArrayList<org.opencv.core.Point>();

        // Capisco se i punti sono in alto o in basso
        for (int i = 0; i < mCorners.size(); i++) {
            if (mCorners.get(i).y < center.y)
                top.add(mCorners.get(i));
            else
                bottom.add(mCorners.get(i));
        }

        double topLeft=top.get(0).x;
        int topLeftIndex=0;
        for(int i=1;i<top.size();i++) {
            if (top.get(i).x<topLeft) {
                topLeft=top.get(i).x;
                topLeftIndex=i;
            }
        }

        double topRight=top.get(0).x;
        int topRightIndex=0;
        for(int i=0;i<top.size();i++) {
            if (top.get(i).x>topRight) {
                topRight=top.get(i).x;
                topRightIndex=i;
            }
        }

        double bottomLeft=bottom.get(0).x;
        int bottomLeftIndex=0;
        for(int i=1;i<bottom.size();i++) {
            if(bottom.get(i).x<bottomLeft) {
                bottomLeft=bottom.get(i).x;
                bottomLeftIndex=i;
            }
        }

        double bottomRight=bottom.get(0).x;
        int bottomRightIndex=0;
        for(int i=1;i<bottom.size();i++) {
            if(bottom.get(i).x>bottomRight) {
                bottomRight=bottom.get(i).x;
                bottomRightIndex=i;
            }
        }

        org.opencv.core.Point topLeftPoint = top.get(topLeftIndex);
        org.opencv.core.Point topRightPoint = top.get(topRightIndex);
        org.opencv.core.Point bottomLeftPoint = bottom.get(bottomLeftIndex);
        org.opencv.core.Point bottomRightPoint = bottom.get(bottomRightIndex);

        mCorners.clear();
        mCorners.add(bottomRightPoint);
        mCorners.add(bottomLeftPoint);
        mCorners.add(topLeftPoint);
        mCorners.add(topRightPoint);
    }

    // Generato dal touch sullo schermo
    // motionEvent = contiene informazioni sull'evento
    // Compongo la lista degli angoli da utilizzare nella trasformazione
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {


        Mat cornerMat = sampledImage;

        if(cornerMat != null) {

            // getX e getY ottiene le coordinate rispetto all'image view e non all'immagine
            // quindi devo ottere x e y rispetto all'immagine
            // Qui ho quindi le coordinate x e y rispetto all'immagine e non alla imageView
            int projectedX = (int) ((double) motionEvent.getX() * ((double) cornerMat.width() / (double) view.getWidth()));
            int projectedY = (int) ((double) motionEvent.getY() * ((double) cornerMat.height() / (double) view.getHeight()));
            // Punti per un angolo
            org.opencv.core.Point corner = new org.opencv.core.Point(projectedX, projectedY);

            // Aggiungo gli angoli
            mCorners.add(corner);

            // Disegno sull'immagine un cerchio per indicare sul display dove ho cliccato
            // I cerchi disegnati sull'immagine non devono essere passati all'immagine da processare
            // Disabilito il plot sull'immagine
            // Imgproc.circle(cornerMat, corner, (int) 5, new Scalar(0,0,255),2);

            displayImage(cornerMat);

            System.out.println("Inserito angolo e ricalcolato immagine");



        }

        return false;
    }

    // Pipeline: Rimozione del rumore e risalto contorni
    // Applicazione dei filtri di Smoothing e di Sharpening
    // Filtri di Smoothing: - media
    //                      - gaussiana
    //                      - mediana
    // Filtri di Sharpening: - 1° ordine: gradiente (derivata prima)
    //                       - 2° ordine: laplanciano (derivata seconda)
    //                       - Unsharp Masking
    // L'immagine deve essere in scala di grigio
    private void blurImage(int id)
    {
        // Un kernel maggiore sfuoca di piu l'immagine


        // Filtro di media
        if(id==R.id.action_average)
        {
            Mat blurredImage=new Mat();
            Size size=new Size(5,5);
            // Calcola automaticamenti i pesi del filtro in base alla dimensione size
            // Qui applico filtro di media 7x 7
            Imgproc.blur(greyImage, blurredImage, size);
            displayImage(blurredImage);

            smoothImage = true;
            greyImage = blurredImage;
        }
        // Filtro gaussiano
        else if(id==R.id.action_gaussian)
        {
            Mat blurredImage=new Mat();
            Size size=new Size(5,5);
            // Applico filtro gaussiano 7 x 7
            // la deviazione standard sulla X e sulla Y sulla base della grandezza del filtro
            // Indicando 0 le calcola in automatico
            // Maggiore è la varianza piu il filtro è potente
            Imgproc.GaussianBlur(greyImage, blurredImage, size, 0,0);
            displayImage(blurredImage);

            smoothImage = true;

            greyImage = blurredImage;

        }
        // Filtro mediano
        else if(id==R.id.action_median)
        {
            Mat blurredImage=new Mat();
            int kernelDim=5;
            // dimensione kerne = 11 (dispari), perchè se spari l'elementpo centrale non esiste.
            Imgproc.medianBlur(greyImage,blurredImage , kernelDim);
            // blurredImage.copyTo(greyImage);
            displayImage(blurredImage);

            smoothImage = true;

            greyImage = blurredImage;
        }

        else if(id == R.id.action_gradient){


        }

        // Filtro laplanciano per sharpering, 2° ordine
        else if(id==R.id.action_laplancian)
        {
            Mat sharpImage=new Mat();

            int kernel_size = 1;
            int scale = 1;
            int delta = 0;
            int ddepth = CvType.CV_16S;

            Mat abs_dst = new Mat();
            Imgproc.Laplacian( greyImage, sharpImage, ddepth, kernel_size, scale, delta, Core.BORDER_DEFAULT );

            // converting back to CV_8U
            Core.convertScaleAbs( sharpImage, abs_dst );

            displayImage(abs_dst);
            greyImage = abs_dst;

            System.out.println("Applicato filtro sharpening Laplanciano");
        }

        else if(id== R.id.action_unsharp_mask){

            // Applicare filtro di smoothing per sfocare immagine originale
            // Come immagine sfocata prendo una gray image gia preelaborata con un opportuno filtro
            // di smoothing per vedere i diversi risultati


            Mat blurredImage=new Mat();
            Size size=new Size(5,5);
            // Applico filtro gaussiano 7 x 7
            // la deviazione standard sulla X e sulla Y sulla base della grandezza del filtro
            // Indicando 0 le calcola in automatico
            // Maggiore è la varianza piu il filtro è potente
            Imgproc.GaussianBlur(greyImage, blurredImage, size, 5,0);

            // Sottrarre immagine sfocata tramite filtro di sharpering dall'originale

            // Calcolo la maschera
            Mat mask = new Mat();
            Core.subtract(greyImage, blurredImage, mask);

            // Aggiungere la maschera all'originale con un opportuno peso K
            // Se k = 1 allora è un Unsharp Mask
            // Se k > 1 allora è un filtraggio highboost

            // Scalar k = new Scalar(6.5);

            Scalar k = new Scalar(25);
            Mat temp = new Mat();
            Core.multiply(mask, k, temp);
            Mat sharpedImage = new Mat();

            Core.add(greyImage, temp, sharpedImage);

            greyImage = sharpedImage;

            displayImage(greyImage);

            }
        }

// Disegno bounding box parole ottenute da Tesseract
    private void calculateBoundingBoxWorld(ResultIterator iterator){

        ocrValues = new ArrayList<String>();
        String lastUTF8Text;
        float lastConfidence;
        int[] lastBoundingBox;
        int count = 0;
        iterator.begin();
        System.out.println("--- Parole OCR trovate ----");
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

            Imgproc.rectangle(boundingImage, rectP1, rectP2, new Scalar(0,255,0), 2);

            // Etichetta su bounding box parola rilevata
            String label = lastUTF8Text + ": " + lastConfidence;
            int[] baseLine = new int[1];
            Size labelSize = Imgproc.getTextSize(label, Core.FONT_HERSHEY_SIMPLEX, 0.5, 1, baseLine);

            // Inserisco stringa riconosciuta e livello di confidenza
            Imgproc.putText(boundingImage, label, new org.opencv.core.Point(x_1, y_1),
                    Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 0));

            System.out.println("Parola: " + lastUTF8Text + " Confidence: " + lastConfidence);

            count++;
        }
        while (iterator.next(TessBaseAPI.PageIteratorLevel.RIL_WORD));

    }

    // Prendo i file traineddata e li copio sulla SD card del dispositivo
    private void prepareTessData(){
        try{
            File dir = new File(data_path+tess_data);
            if(!dir.exists()){
                dir.mkdir();
            }
            String fileList[] = getAssets().list("");
            for(String fileName : fileList){
                String pathToDataFile = data_path + tess_data + "/" + fileName;
                if(!(new File(pathToDataFile)).exists()){
                    InputStream is = getAssets().open(fileName);
                    OutputStream os = new FileOutputStream(pathToDataFile);
                    byte [] buff = new byte[1024];
                    int len;
                    while((len = is.read(buff))>0){
                        os.write(buff,0,len);
                    }
                    is.close();
                    os.close();
                }
            }
        } catch (IOException e) {
            Log.w(TAG, e.getMessage());
        }
    }

    // Gestione permessi fotocamera
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        // Risposta dal permesso della fotocamera e scrittura su storage
        if (requestCode == REQUEST_PERMISSIONS) {
            // Verifico risposta dell'utente
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Conenso attivo, carico la fotocamera
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this, mLoaderCallback);
            }

            else {

                // Permessi non concesso
                Toast.makeText(this,"Permessi non concesso", Toast.LENGTH_SHORT).show();
            }

        }

        // other 'case' lines to check for other
        // permissions this app might request.

        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    // Verifico se sono su dispositivo simulato o fisico
    public boolean buildModelContainsEmulatorHints(String buildModel) {
        return buildModel.startsWith("sdk")
                || "google_sdk".equals(buildModel)
                || buildModel.contains("Emulator")
                || buildModel.contains("Android SDK");
    }

}


