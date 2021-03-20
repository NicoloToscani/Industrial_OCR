package advmobdev.unipr.it.ocr;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

public class TessOCR  {

    private static final String TAG = "Log";
    private final TessBaseAPI mTess;
    private static String DATA_PATH = null;
    private static final String TESS_DATA = "/tesseract";
    private  String language = "ita";

    // Per caricamento da asset
    // private static final String data_path = Environment.getExternalStorageDirectory().toString() + "/Tess";
   // private static final String tess_data = "/tessdata";




    private ResultIterator textIterator = null;
    ;
    private Context context;

    // Costruttore
    public TessOCR() {


        mTess = new TessBaseAPI();

        // Ottengo path per il file di con i dati training in base alla lingua
        DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/tesseract";

        System.out.println("Patch OCR:" + DATA_PATH + TESS_DATA);

        mTess.init(DATA_PATH, language);
        // mTess.init(data_path,language);

        // Versione Tessearact: 3.05.00
        System.out.println("Versione Tessearact: " + mTess.getVersion());

        // Linguaggio utilizzato per la ricerca: ita
        System.out.println("Linguaggio OCR: " + mTess.getInitLanguagesAsString());

        // Imposto il tipo di segmentazione della pagina, uso quello di default
        // mTess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_ONLY);

    }

    // Ritorna il testo passando l'immagine
    public String getOCRResult(Bitmap bitmap) {

        try{

            String textOCR;

            // Fornire a Tesseract un immagine come Pix è piu efficente, quindi ci arriva come Bitmap
            // e la converto in Pix di Leptonica

            // Pix pixImage = ReadFile.readBitmap(bitmap); ??? Vedere se ho differenze

            mTess.setImage(bitmap);

            // Confidence: valutata
            System.out.println("Confidence: " + mTess.meanConfidence());

            // Oytengo il testo dell'immagine
            textOCR = mTess.getUTF8Text();

            printConfidence(mTess);

            // Ottengo iteratore sui risultati, in questo caso su ogni parola
            textIterator = mTess.getResultIterator();

            return textOCR;

        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        return "";
    }




    public void onDestroy(){

        if(mTess != null){
            mTess.end();
        }

    }



    // Ritorno iteratore sui risultati ottenuti
    public ResultIterator ocrIterator(){

        return textIterator;

    }


    public void printConfidence(TessBaseAPI mTess){


        ResultIterator ri = mTess.getResultIterator(); // Ottengo iteratore
        int level = TessBaseAPI.PageIteratorLevel.RIL_SYMBOL;


        do{
            String symbol = ri.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD);
            float confidence = ri.confidence(TessBaseAPI.PageIteratorLevel.RIL_WORD);

            ResultIterator rCahr = ri;

            if(symbol != null){





                // System.out.println("---------------------------------------------");
                // System.out.println("Parola " + symbol + ", Confidence: " + confidence + "\n");

                do {

                    if(rCahr != null){

                        // Per ogni carattere della parola riconosciuta ottengo quali sono le scelte
                        // che adotta Tesseract sul livello di confidenza per la scelta
                        List<Pair<String, Double>> choiches = rCahr.getChoicesAndConfidence(level);

                        for (int i = 0; i < choiches.size(); i++) {

                            System.out.println("             Carattere: " + choiches.get(i).first + " Confidenza: " + choiches.get(i).second);

                        }

                            System.out.println("             -----------------------------------------------------------------               ");

                    }

                    } while (rCahr.next(TessBaseAPI.PageIteratorLevel.RIL_SYMBOL));

            }

        } while (ri.next(TessBaseAPI.PageIteratorLevel.RIL_WORD));

    }






}
