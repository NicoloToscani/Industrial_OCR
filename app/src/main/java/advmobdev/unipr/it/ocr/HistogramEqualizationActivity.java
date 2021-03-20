package advmobdev.unipr.it.ocr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HistogramEqualizationActivity extends AppCompatActivity {

    RadioButton simpleEqRadioButton, clacheEqRadioButton, disableEqRadioButton;
    TextView tileSizeXValue, tileSizeYValue, limitValue;
    Button saveButton;

    Bundle bundle;

    Pipeline pipeline;





    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Associa un activity alla sua View
        setContentView(R.layout.activity_hist_equalization);

        this.setTitle("Histogram equalization");

        saveButton = (Button) findViewById(R.id.buttonSave);

        simpleEqRadioButton = (RadioButton)findViewById(R.id.radio_eq_simple);
        clacheEqRadioButton = (RadioButton)findViewById(R.id.radio_eq_clache);
        disableEqRadioButton = (RadioButton)findViewById(R.id.radio_eq_disable);

        tileSizeXValue = (TextView)findViewById(R.id.editXTileClache);
        tileSizeYValue = (TextView)findViewById(R.id.editYTileClache);
        limitValue = (TextView)findViewById(R.id.editLimitClache);

        // Recupero il bundle e lo utilizzo per settare i dati inseriti
        bundle = getIntent().getExtras();

        if (bundle != null) {

            pipeline = (Pipeline) bundle.getSerializable(OCR.STRINGA_BUNDLE);


            tileSizeXValue.setText(Integer.toString(pipeline.getTileSizeXValue()));
            tileSizeYValue.setText(Integer.toString(pipeline.getTileSizeYValue()));
            limitValue.setText(Double.toString(pipeline.getLimitValue()));


            // Set default radio
            simpleEqRadioButton.setChecked(pipeline.isSimpleEqRadioButton());
            clacheEqRadioButton.setChecked(pipeline.isClacheEqRadioButton());
            disableEqRadioButton.setChecked(pipeline.isDilatationRadioButton());


        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Recupero il bundle e apro la live camera

                pipeline.setTileSizeXValue(Integer.parseInt((String)tileSizeXValue.getText().toString()));
                pipeline.setTileSizeYValue(Integer.parseInt((String)tileSizeYValue.getText().toString()));
                pipeline.setLimitValue(Double.parseDouble((String)limitValue.getText().toString()));


                // Apro Live camera inoltrando il bundle

                Context context = getApplicationContext();

                Intent intent = new Intent(new Intent(context, SettingsListActivity.class));
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });


    }


    // Radio button per gestione tipo soglia
    public void onRadioEqButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_eq_simple:
                if (checked) {

                    simpleEqRadioButton.setChecked(true);
                    clacheEqRadioButton.setChecked(false);

                    pipeline.setSimpleEqRadioButton(true);
                    pipeline.setClacheEqRadioButton(false);

                }

                break;
            case R.id.radio_eq_clache:
                if (checked) {

                    simpleEqRadioButton.setChecked(false);
                    clacheEqRadioButton.setChecked(true);

                    pipeline.setSimpleEqRadioButton(false);
                    pipeline.setClacheEqRadioButton(true);
                }

                break;

            case R.id.radio_eq_disable:
                if(checked){
                    simpleEqRadioButton.setChecked(false);
                    clacheEqRadioButton.setChecked(false);
                    disableEqRadioButton.setChecked(true);

                    pipeline.setSimpleEqRadioButton(false);
                    pipeline.setClacheEqRadioButton(false);
                    pipeline.setDisableEqRadioButton(true);

                }
                break;
        }
    }


}
