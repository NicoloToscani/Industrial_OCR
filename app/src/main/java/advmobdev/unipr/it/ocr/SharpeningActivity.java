package advmobdev.unipr.it.ocr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SharpeningActivity extends AppCompatActivity {


    Switch enableUnsharpMask;
    TextView kSizeX, kSizeY, sigmaX, sigmaY, scalarK;

    Button saveButton;

    Bundle bundle;

    Pipeline pipeline;





    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Associa un activity alla sua View
        setContentView(R.layout.activity_sharpening_filters);

        this.setTitle("Sharpening");

        saveButton = (Button) findViewById(R.id.buttonSave);

        enableUnsharpMask = (Switch)findViewById(R.id.switchUnsharp);

       kSizeX = (TextView)findViewById(R.id.editXKsize);
       kSizeY = (TextView)findViewById(R.id.editYKsize);
       sigmaX = (TextView)findViewById(R.id.editSigmaX);
       sigmaY = (TextView)findViewById(R.id.editSigmaY);
       scalarK = (TextView)findViewById(R.id.editKscalar);

        // Recupero il bundle e lo utilizzo per settare i dati inseriti
        bundle = getIntent().getExtras();

        if (bundle != null) {

            pipeline = (Pipeline) bundle.getSerializable(OCR.STRINGA_BUNDLE);


            kSizeX.setText(Integer.toString(pipeline.getkSizeXUnmaskFilter()));
            kSizeY.setText(Integer.toString(pipeline.getkSizeXUnmaskFilter()));
            sigmaX.setText(Double.toString(pipeline.getSigmaXUnmaskFilter()));
            sigmaY.setText(Double.toString(pipeline.getSigmaYUnmaskFilter()));
            scalarK.setText(Double.toString(pipeline.getScalarMaskUnmask()));


            // Set default radio
            enableUnsharpMask.setChecked(pipeline.isUnsharpMaskingEnable());


        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Recupero il bundle e apro la live camera


                pipeline.setkSizeXUnmaskFilter(Integer.parseInt((String) kSizeX.getText().toString()));
                pipeline.setkSizeYUnmaskFilter(Integer.parseInt((String) kSizeY.getText().toString()));
                pipeline.setSigmaXUnmaskFilter(Double.parseDouble((String) sigmaX.getText().toString()));
                pipeline.setSigmaYUnmaskFilter(Double.parseDouble((String) sigmaY.getText().toString()));
                pipeline.setScalarMaskUnmask(Double.parseDouble((String) scalarK.getText().toString()));


                // Apro Live camera inoltrando il bundle

                Context context = getApplicationContext();

                Intent intent = new Intent(new Intent(context, SettingsListActivity.class));
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });




        enableUnsharpMask = (Switch) findViewById(R.id.switchUnsharp);
        enableUnsharpMask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){

                    System.out.println("Abilito unsharp masking");

                    pipeline.setUnsharpMaskingEnable(true);
                }

                else if(!isChecked){

                    System.out.println("Disabilito unsharpmasking");

                    pipeline.setUnsharpMaskingEnable(false);
                }

            }
        });

    }


}
