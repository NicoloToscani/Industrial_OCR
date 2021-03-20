package advmobdev.unipr.it.ocr;

import java.util.ArrayList;
import java.util.Iterator;

// Classe che applica il controllo OCV
public class OCV {

    private  ArrayList<String> ocrValues;     // Valori ottenuti dopo applicazione OCR da Tesseract
    private  ArrayList<String> labelValues;   // Valori etichetta da controllare

    private double ocvAverage = 0.0;

/*
    public double applyOcv(ArrayList<String> ocr, ArrayList<String> labels){

        this.ocrValues = ocr;
        this.labelValues = labels;

        double countOk = 0;
        double labelIn = 0; // Numero di campi label compilati


        // Pop dei valori nel container OCR
        while(!(labelValues.isEmpty())){

            // Confronto stringhe
            String ocvValue = labelValues.remove(0);

            // Se la label da ricercare non è la stringa vuota
            if(ocvValue.compareToIgnoreCase("") != 0){

                labelIn = labelIn + 1;

                int numberOCr = 0;

                boolean find = false;

                // Match su tutte le stringhe OCR
                while(numberOCr != ocrValues.size() && find == false){


                    if(ocrValues.get(numberOCr).compareToIgnoreCase(ocvValue) == 0 ){

                        countOk = countOk +1;

                        find = true;

                    }

                    numberOCr = numberOCr +1;

                }

            }

        }

        ocvAverage = (countOk / labelIn) * 100.0;

        if(Double.isNaN(ocvAverage)){

            ocvAverage = 0.0;

        }

        return ocvAverage;
    }

 */


    public double applyOcv(ArrayList<String> ocr, ArrayList<String> labels){

        this.ocrValues = ocr;
        this.labelValues = labels;

        Iterator<String> iterator = labelValues.iterator();

        double countOk = 0;
        double labelIn = 0; // Numero di campi label compilati

        // Pop dei valori nel container OCR
        while(iterator.hasNext()){

            // Confronto stringhe
            String ocvValue = iterator.next();

            // Se la label da ricercare non è la stringa vuota
            if(ocvValue.compareToIgnoreCase("") != 0){

                labelIn = labelIn + 1;

                int numberOCr = 0;

                boolean find = false;

                // Match su tutte le stringhe OCR
                while(numberOCr != ocrValues.size() && find == false){


                    if(ocrValues.get(numberOCr) != null) {
                        if (ocrValues.get(numberOCr).compareToIgnoreCase(ocvValue) == 0) {

                            countOk = countOk + 1;

                            find = true;

                        }
                    }

                    numberOCr = numberOCr +1;

                }
            }
        }

        ocvAverage = (countOk / labelIn) * 100.0;

        if(Double.isNaN(ocvAverage)){

            ocvAverage = 0.0;
        }

        return ocvAverage;
    }



}
