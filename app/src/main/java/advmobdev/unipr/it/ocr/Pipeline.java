package advmobdev.unipr.it.ocr;

import android.widget.RadioButton;
import android.widget.TextView;

import java.io.Serializable;

// Rappresenta la pipiline di pre-elaborazioone passata alla camera live
public class Pipeline implements Serializable {

    // Binarizzazione immagine, applico sogli all'immagine in scala di grigio

    private boolean simpleThreshold, adaptiveThredshold, binaryThredshold, otsuThredshold, meanThredshold, gaussianTredshold;
    private double simpleThresholdValue, adaptiveThreasholdValue;
    private int adaptiveNeighborhoodSize;


    // Morfologia

    private boolean erosionRadioButton, dilatationRadioButton, openingRadioButton, closingRadioButton;
    private int erosionXValue, erosionYValue, dilatationXValue, dilatationYValue, openingXValue, openingYValue, closingXValue, closingYValue;


    // Costruttore
    public Pipeline(){

        // Inizializzo componenti per binarizzazione
        System.out.println("Allocato oggetto Pipeline");

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
}
