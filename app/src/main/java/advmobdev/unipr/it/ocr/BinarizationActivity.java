package advmobdev.unipr.it.ocr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


// Activity per gestione metodi binarizzazione immagine
public class BinarizationActivity extends AppCompatActivity {

    // Widget
    RadioButton simpleRadioButton, adaptiveRadioButton, meanRadioButton, gaussianRadioButton, binaryRadioButton, otsuRadioButton;

    TextView simpleThresholdValue, adaptiveNeighborhoodSize, adaptiveThresholdValue;

    Button saveButton;

    Bundle bundle;

    Pipeline pipeline;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Associa un activity alla sua View
        setContentView(R.layout.activity_binarization);

        this.setTitle("Image segmentation");

        saveButton = (Button) findViewById(R.id.buttonSave);


        simpleRadioButton = (RadioButton) findViewById(R.id.radio_thr_simple);
        adaptiveRadioButton = (RadioButton) findViewById(R.id.radio_thr_adaptive);
        meanRadioButton = (RadioButton) findViewById(R.id.radio_thr_adaptive_meanc_c);
        gaussianRadioButton = (RadioButton)findViewById(R.id.radio_thr_adaptive_gaussian_c);
        binaryRadioButton = (RadioButton) findViewById(R.id.radio_thr_binary);
        otsuRadioButton = (RadioButton) findViewById(R.id.radio_thr_otsu);

        simpleThresholdValue = (TextView) findViewById(R.id.editSimpleThr);
        adaptiveNeighborhoodSize = (TextView) findViewById(R.id.editNeighSize);
        adaptiveThresholdValue = (TextView)  findViewById(R.id.editAdaptiveValue);


        // Recupero il bundle e lo utilizzo per settare i dati inseriti
        bundle = getIntent().getExtras();

        if (bundle != null) {

            pipeline = (Pipeline) bundle.getSerializable(OCR.STRINGA_BUNDLE);


            simpleThresholdValue.setText(Double.toString(pipeline.getSimpleThresholdValue()));
            adaptiveThresholdValue.setText(Double.toString(pipeline.getAdaptiveThreasholdValue()));
            adaptiveNeighborhoodSize.setText(Integer.toString(pipeline.getAdaptiveNeighborhoodSize()));

            // Set default radio
            simpleRadioButton.setChecked(pipeline.isSimpleThreshold());
            adaptiveRadioButton.setChecked(pipeline.isAdaptiveThredshold());
            binaryRadioButton.setChecked(pipeline.isBinaryThredshold());
            otsuRadioButton.setChecked(pipeline.isOtsuThredshold());
            meanRadioButton.setChecked(pipeline.isMeanThredshold());
            gaussianRadioButton.setChecked(pipeline.isGaussianTredshold());

        }

        ;


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Recupero il bundle e apro la live camera


                pipeline.setSimpleThresholdValue(Double.parseDouble((String) simpleThresholdValue.getText().toString()));
                pipeline.setAdaptiveNeighborhoodSize(Integer.parseInt((String) adaptiveNeighborhoodSize.getText().toString()));
                pipeline.setAdaptiveThreasholdValue(Double.parseDouble((String)adaptiveThresholdValue.getText().toString()));

                // Apro Live camera inoltrando il bundle

                Context context = getApplicationContext();

                Intent intent = new Intent(new Intent(context, SettingsListActivity.class));
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });

    }

    // Radio button per gestione tipo soglia
    public void onRadioThrButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_thr_simple:
                if (checked) {

                    // Visualizzo frame immagini a colori

                    simpleRadioButton.setChecked(true);
                    adaptiveRadioButton.setChecked(false);

                    pipeline.setSimpleThreshold(true);
                    pipeline.setAdaptiveThredshold(false);

                }

                break;
            case R.id.radio_thr_adaptive:
                if (checked) {

                    // Visualizzo frame immagini in scala di grigio
                    simpleRadioButton.setChecked(false);
                    adaptiveRadioButton.setChecked(true);

                    pipeline.setSimpleThreshold(false);
                    pipeline.setAdaptiveThredshold(true);
                }

                break;
        }
    }

    // Radio button per gestione soglia semplice
    public void onRadioSimpleThrButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_thr_binary:
                if (checked) {

                    // Visualizzo frame immagini a colori

                    binaryRadioButton.setChecked(true);
                    otsuRadioButton.setChecked(false);

                    pipeline.setBinaryThredshold(true);
                    pipeline.setOtsuThredshold(false);
                }

                break;
            case R.id.radio_thr_otsu:
                if (checked) {

                    // Visualizzo frame immagini in scala di grigio
                    binaryRadioButton.setChecked(false);
                    otsuRadioButton.setChecked(true);

                    pipeline.setBinaryThredshold(false);
                    pipeline.setOtsuThredshold(true);
                }

                break;
        }
    }


    // Radio button per gestione sogli adattiva
    public void onRadioAdaptiveThrButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_thr_adaptive_meanc_c:
                if (checked) {

                    // Visualizzo frame immagini a colori

                    meanRadioButton.setChecked(true);
                    gaussianRadioButton.setChecked(false);

                    pipeline.setMeanThredshold(true);
                    pipeline.setGaussianTredshold(false);
                }

                break;
            case R.id.radio_thr_adaptive_gaussian_c:
                if (checked) {

                    // Visualizzo frame immagini in scala di grigio
                    meanRadioButton.setChecked(false);
                    gaussianRadioButton.setChecked(true);

                    pipeline.setMeanThredshold(false);
                    pipeline.setGaussianTredshold(true);

                }

                break;

        }
    }

}
