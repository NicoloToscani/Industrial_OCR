package advmobdev.unipr.it.ocr;

import android.widget.RadioButton;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;

// Rappresenta la pipiline di pre-elaborazioone passata alla camera live
public class Pipeline implements Serializable {

    // Binarizzazione immagine, applico sogli all'immagine in scala di grigio

    private boolean simpleThreshold, adaptiveThredshold, binaryThredshold, otsuThredshold, meanThredshold, gaussianTredshold;
    private double simpleThresholdValue, adaptiveThreasholdValue;
    private int adaptiveNeighborhoodSize;


    // Morfologia

    private boolean erosionRadioButton, dilatationRadioButton, openingRadioButton, closingRadioButton;
    private int erosionXValue, erosionYValue, dilatationXValue, dilatationYValue, openingXValue, openingYValue, closingXValue, closingYValue;

    // Equalizzazione istogramma
    private boolean simpleEqRadioButton, clacheEqRadioButton, disableEqRadioButton;
    private int tileSizeXValue, tileSizeYValue;
    private double limitValue;


    // Filtri di sharpening
    // Unsharp Masking
    int kSizeXUnmaskFilter, kSizeYUnmaskFilter;
    double scalarMaskUnmask, sigmaXUnmaskFilter, sigmaYUnmaskFilter;
    boolean unsharpMaskingEnable;

    // Lista campi etichetta da ricercare
    ArrayList<String> labelValues;



    // Costruttore
    public Pipeline(){

        // Inizializzo componenti per binarizzazione
        System.out.println("Allocato oggetto Pipeline");

        // Setto valori di default avvio applicazione
        // Binarizzazione
        setSimpleThresholdValue(127.0);
        setAdaptiveThreasholdValue(127.0);
        setAdaptiveNeighborhoodSize(5);

        // Morfologia
        setErosionXValue(2);
        setErosionYValue(2);
        setDilatationXValue(2);
        setDilatationYValue(2);
        setOpeningXValue(5);
        setOpeningYValue(5);
        setClosingXValue(5);
        setClosingYValue(5);

        // CLACHE istogram
        setTileSizeXValue(8);
        setTileSizeYValue(8);
        setLimitValue(5.0);

        // Smoothing Unsharp Masking
        setkSizeXUnmaskFilter(5);
        setkSizeYUnmaskFilter(5);
        setSigmaXUnmaskFilter(5.0);
        setSigmaYUnmaskFilter(0.0);
        setScalarMaskUnmask(25);






    }

    // Setter per binarizzazione
    public void setSimpleThreshold(boolean simpleThreshold) {
        this.simpleThreshold = simpleThreshold;
    }

    public void setAdaptiveThredshold(boolean adaptiveThredshold) {
        this.adaptiveThredshold = adaptiveThredshold;
    }

    public void setBinaryThredshold(boolean binaryThredshold) {
        this.binaryThredshold = binaryThredshold;
    }

    public void setOtsuThredshold(boolean otsuThredshold) {
        this.otsuThredshold = otsuThredshold;
    }

    public void setMeanThredshold(boolean meanThredshold) {
        this.meanThredshold = meanThredshold;
    }

    public void setGaussianTredshold(boolean gaussianTredshold) {
        this.gaussianTredshold = gaussianTredshold;
    }

    public void setSimpleThresholdValue(double simpleThresholdValue) {
        this.simpleThresholdValue = simpleThresholdValue;
    }

    public void setAdaptiveThreasholdValue(double adaptiveThreasholdValue) {
        this.adaptiveThreasholdValue = adaptiveThreasholdValue;
    }

    public void setAdaptiveNeighborhoodSize(int adaptiveNeighborhoodSize) {
        this.adaptiveNeighborhoodSize = adaptiveNeighborhoodSize;
    }

    public boolean isAdaptiveThredshold() {
        return adaptiveThredshold;
    }

    public boolean isBinaryThredshold() {
        return binaryThredshold;
    }

    public boolean isGaussianTredshold() {
        return gaussianTredshold;
    }

    public boolean isMeanThredshold() {
        return meanThredshold;
    }

    public boolean isOtsuThredshold() {
        return otsuThredshold;
    }

    public boolean isSimpleThreshold() {
        return simpleThreshold;
    }

    public double getAdaptiveThreasholdValue() {
        return adaptiveThreasholdValue;
    }

    public double getSimpleThresholdValue() {
        return simpleThresholdValue;
    }

    public int getAdaptiveNeighborhoodSize() {
        return adaptiveNeighborhoodSize;
    }

    public boolean isClosingRadioButton() {
        return closingRadioButton;
    }

    public boolean isDilatationRadioButton() {
        return dilatationRadioButton;
    }

    public boolean isErosionRadioButton() {
        return erosionRadioButton;
    }

    public boolean isOpeningRadioButton() {
        return openingRadioButton;
    }

    public int getDilatationXValue() {
        return dilatationXValue;
    }

    public int getClosingXValue() {
        return closingXValue;
    }

    public int getClosingYValue() {
        return closingYValue;
    }

    public int getDilatationYValue() {
        return dilatationYValue;
    }

    public int getErosionXValue() {
        return erosionXValue;
    }

    public int getErosionYValue() {
        return erosionYValue;
    }

    public int getOpeningXValue() {
        return openingXValue;
    }

    public int getOpeningYValue() {
        return openingYValue;
    }

    public void setClosingRadioButton(boolean closingRadioButton) {
        this.closingRadioButton = closingRadioButton;
    }

    public void setClosingXValue(int closingXValue) {
        this.closingXValue = closingXValue;
    }

    public void setDilatationRadioButton(boolean dilatationRadioButton) {
        this.dilatationRadioButton = dilatationRadioButton;
    }

    public void setClosingYValue(int closingYValue) {
        this.closingYValue = closingYValue;
    }

    public void setDilatationXValue(int dilatationXValue) {
        this.dilatationXValue = dilatationXValue;
    }

    public void setDilatationYValue(int dilatationYValue) {
        this.dilatationYValue = dilatationYValue;
    }

    public void setErosionRadioButton(boolean erosionRadioButton) {
        this.erosionRadioButton = erosionRadioButton;
    }

    public void setErosionXValue(int erosionXValue) {
        this.erosionXValue = erosionXValue;
    }

    public void setErosionYValue(int erosionYValue) {
        this.erosionYValue = erosionYValue;
    }

    public void setOpeningRadioButton(boolean openingRadioButton) {
        this.openingRadioButton = openingRadioButton;
    }

    public void setOpeningXValue(int openingXValue) {
        this.openingXValue = openingXValue;
    }

    public void setOpeningYValue(int openingYValue) {
        this.openingYValue = openingYValue;
    }

    public void setClacheEqRadioButton(boolean clacheEqRadioButton) {
        this.clacheEqRadioButton = clacheEqRadioButton;
    }

    public boolean isSimpleEqRadioButton() {
        return simpleEqRadioButton;
    }

    public boolean isClacheEqRadioButton() {
        return clacheEqRadioButton;
    }

    public int getTileSizeXValue() {
        return tileSizeXValue;
    }

    public double getLimitValue() {
        return limitValue;
    }

    public int getTileSizeYValue() {
        return tileSizeYValue;
    }

    public void setLimitValue(double limitValue) {
        this.limitValue = limitValue;
    }

    public void setSimpleEqRadioButton(boolean simpleEqRadioButton) {
        this.simpleEqRadioButton = simpleEqRadioButton;
    }

    public void setTileSizeXValue(int tileSizeXValue) {
        this.tileSizeXValue = tileSizeXValue;
    }

    public void setTileSizeYValue(int tileSizeYValue) {
        this.tileSizeYValue = tileSizeYValue;
    }

    public boolean isDisableEqRadioButton() {
        return disableEqRadioButton;
    }

    public void setDisableEqRadioButton(boolean disableEqRadioButton) {
        this.disableEqRadioButton = disableEqRadioButton;
    }

    public boolean isUnsharpMaskingEnable() {
        return unsharpMaskingEnable;
    }

    public double getScalarMaskUnmask() {
        return scalarMaskUnmask;
    }

    public double getSigmaYUnmaskFilter() {
        return sigmaYUnmaskFilter;
    }

    public double getSigmaXUnmaskFilter() {
        return sigmaXUnmaskFilter;
    }

    public int getkSizeXUnmaskFilter() {
        return kSizeXUnmaskFilter;
    }

    public int getkSizeYUnmaskFilter() {
        return kSizeYUnmaskFilter;
    }

    public void setkSizeXUnmaskFilter(int kSizeXUnmaskFilter) {
        this.kSizeXUnmaskFilter = kSizeXUnmaskFilter;
    }

    public void setkSizeYUnmaskFilter(int kSizeYUnmaskFilter) {
        this.kSizeYUnmaskFilter = kSizeYUnmaskFilter;
    }

    public void setScalarMaskUnmask(double scalarMaskUnmask) {
        this.scalarMaskUnmask = scalarMaskUnmask;
    }

    public void setSigmaYUnmaskFilter(double sigmaUnmaskFilter) {
        this.sigmaYUnmaskFilter = sigmaUnmaskFilter;
    }

    public void setSigmaXUnmaskFilter(double sigmaXUnmaskFilter) {
        this.sigmaXUnmaskFilter = sigmaXUnmaskFilter;
    }

    public void setUnsharpMaskingEnable(boolean unsharpMaskingEnable) {
        this.unsharpMaskingEnable = unsharpMaskingEnable;
    }

    public ArrayList<String> getLabelValues() {
        return labelValues;
    }

    public void setLabelValues(ArrayList<String> labelValues) {
        this.labelValues = labelValues;
    }
}
