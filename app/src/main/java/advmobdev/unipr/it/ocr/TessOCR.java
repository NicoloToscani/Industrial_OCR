package advmobdev.unipr.it.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.ResultIterator;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.googlecode.leptonica.android.Scale;

import org.opencv.calib3d.StereoSGBM;

public class TessOCR {

    private final TessBaseAPI mTess;

    private static  String DATA_PATH = null;
    private static final String TESS_DATA = "/tessdata";

    private ResultIterator textIterator = null;;



    // Costruttore
    public TessOCR(Context context, String language) {

        mTess = new TessBaseAPI();

        // Ottengo path per il file di con i dati training in base alla lingua
        DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/tesseract";

        System.out.println("Patch OCR:" + DATA_PATH + TESS_DATA);

        mTess.init(DATA_PATH,language);

        // Versione Tessearact: 3.05.00
        System.out.println("Versione Tessearact: " + mTess.getVersion());

        // Linguaggio utilizzato per la ricerca: ita
        System.out.println("Linguaggio OCR: " + mTess.getInitLanguagesAsString());

        // Imposto il tipo di segmentazione della pagina
         // mTess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD);


    }

    // Ritorna il testo passando l'immagine
    public  String getOCRResult(Bitmap bitmap){

        String textOCR;

        // Fornire a Tesseract un immagine come Pix è piu efficente, quindi ci arriva come Bitmap
        // e la converto in Pix di Leptonica

       // Pix pixImage = ReadFile.readBitmap(bitmap); ??? Vedere se ho differenze

        mTess.setImage(bitmap);



        // Confidence: a cosa è riferita ?
        System.out.println("Confidence: " + mTess.meanConfidence());

        // Oytengo il testo dell'immagine
        textOCR = mTess.getUTF8Text();

        // Ottengo iteratore sui risultati, in questo caso su ogni parola
        textIterator = mTess.getResultIterator();

        return textOCR;

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

}
