package advmobdev.unipr.it.ocr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MorphologicalActivity extends AppCompatActivity {


    // Widget
    RadioButton erosionRadioButton, dilatationRadioButton, openingRadioButton, closingRadioButton;

    TextView erosionXValue, erosionYValue, dilatationXValue, dilatationYValue, openingXValue, openingYValue, closingXValue, closingYValue;

    Button saveButton;

    Bundle bundle;

    Pipeline pipeline;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Associa un activity alla sua View
        setContentView(R.layout.activity_morphology);

        this.setTitle("Morphological transformations");

        saveButton = (Button) findViewById(R.id.buttonSave);


        erosionRadioButton = (RadioButton) findViewById(R.id.radio_erosion);
        dilatationRadioButton = (RadioButton) findViewById(R.id.radio_dilatation);
        openingRadioButton = (RadioButton) findViewById(R.id.radio_opening);
        closingRadioButton = (RadioButton)findViewById(R.id.radio_closing);


        erosionXValue = (TextView) findViewById(R.id.editKernelXErosion);
        erosionYValue = (TextView) findViewById(R.id.editKernelYErosion);
        dilatationXValue = (TextView) findViewById(R.id.editKernelXDilatation);
        dilatationYValue = (TextView) findViewById(R.id.editKernelYDilatation);
        openingXValue = (TextView) findViewById(R.id.editKernelXOpening);
        openingYValue = (TextView) findViewById(R.id.editKernelYOpening);
        closingXValue = (TextView) findViewById(R.id.editKernelXClosing);
        closingYValue = (TextView) findViewById(R.id.editKernelYClosing);











        // Recupero il bundle e lo utilizzo per settare i dati inseriti
        bundle = getIntent().getExtras();

        if (bundle != null) {

            pipeline = (Pipeline) bundle.getSerializable(OCR.STRINGA_BUNDLE);



            // Set default erosion
            erosionRadioButton.setChecked(pipeline.isErosionRadioButton());
            dilatationRadioButton.setChecked(pipeline.isDilatationRadioButton());
            openingRadioButton.setChecked(pipeline.isOpeningRadioButton());
            closingRadioButton.setChecked(pipeline.isClosingRadioButton());

            erosionXValue.setText(Integer.toString(pipeline.getErosionXValue()));
            erosionYValue.setText(Integer.toString(pipeline.getErosionYValue()));
            dilatationXValue.setText(Integer.toString(pipeline.getDilatationXValue()));
            dilatationYValue.setText(Integer.toString(pipeline.getDilatationYValue()));
            openingXValue.setText(Integer.toString(pipeline.getOpeningXValue()));
            openingYValue.setText(Integer.toString(pipeline.getOpeningYValue()));
            closingXValue.setText(Integer.toString(pipeline.getClosingXValue()));
            closingYValue.setText(Integer.toString(pipeline.getClosingYValue()));


        }




        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Recupero il bundle e apro la live camera


                pipeline.setErosionXValue(Integer.parseInt((String) erosionXValue.getText().toString()));
                pipeline.setErosionYValue(Integer.parseInt((String) erosionYValue.getText().toString()));
                pipeline.setDilatationXValue(Integer.parseInt((String) dilatationXValue.getText().toString()));
                pipeline.setDilatationYValue(Integer.parseInt((String) dilatationYValue.getText().toString()));
                pipeline.setOpeningXValue(Integer.parseInt((String) openingXValue.getText().toString()));
                pipeline.setOpeningYValue(Integer.parseInt((String) openingYValue.getText().toString()));
                pipeline.setClosingXValue(Integer.parseInt((String) closingXValue.getText().toString()));
                pipeline.setClosingYValue(Integer.parseInt((String) closingYValue.getText().toString()));


                // Apro Live camera inoltrando il bundle

                Context context = getApplicationContext();

                Intent intent = new Intent(new Intent(context, SettingsListActivity.class));
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });

    }


    // Radio button per gestione tipo soglia
    public void onRadioMorphoButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_erosion:
                if (checked) {

                    // Erosione

                    erosionRadioButton.setChecked(true);
                    dilatationRadioButton.setChecked(false);
                    openingRadioButton.setChecked(false);
                    closingRadioButton.setChecked(false);

                    pipeline.setErosionRadioButton(true);
                    pipeline.setDilatationRadioButton(false);
                    pipeline.setOpeningRadioButton(false);
                    pipeline.setClosingRadioButton(false);

                }

                break;
                // Dilatazione
            case R.id.radio_dilatation:
                if (checked) {

                    // Dilatazione

                    erosionRadioButton.setChecked(false);
                    dilatationRadioButton.setChecked(true);
                    openingRadioButton.setChecked(false);
                    closingRadioButton.setChecked(false);

                    pipeline.setErosionRadioButton(false);
                    pipeline.setDilatationRadioButton(true);
                    pipeline.setOpeningRadioButton(false);
                    pipeline.setClosingRadioButton(false);
                }

                break;

                // Apertura
            case R.id.radio_opening:
                if (checked) {

                    // Dilatazione

                    erosionRadioButton.setChecked(false);
                    dilatationRadioButton.setChecked(false);
                    openingRadioButton.setChecked(true);
                    closingRadioButton.setChecked(false);

                    pipeline.setErosionRadioButton(false);
                    pipeline.setDilatationRadioButton(false);
                    pipeline.setOpeningRadioButton(true);
                    pipeline.setClosingRadioButton(false);
                }

                break;


            // Chiusura
            case R.id.radio_closing:
                if (checked) {

                    // Dilatazione

                    erosionRadioButton.setChecked(false);
                    dilatationRadioButton.setChecked(false);
                    openingRadioButton.setChecked(false);
                    closingRadioButton.setChecked(true);

                    pipeline.setErosionRadioButton(false);
                    pipeline.setDilatationRadioButton(false);
                    pipeline.setOpeningRadioButton(false);
                    pipeline.setClosingRadioButton(true);
                }

                break;
        }
    }

}
