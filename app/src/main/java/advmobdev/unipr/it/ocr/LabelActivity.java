package advmobdev.unipr.it.ocr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Classe che gestisce l'inserimento dei caratteri da cercare tramite OCR
public class LabelActivity extends AppCompatActivity {

    // Salvo i valori inseriti e apro la MainActicity
    Button saveButton;
    Button pipelineButton;

    EditText editText1;
    EditText editText2;
    EditText editText3;
    EditText editText4;
    EditText editText5;
    EditText editText6;
    EditText editText7;
    EditText editText8;
    EditText editText9;
    EditText editText10;
    EditText editText11;

    Bundle bundle;



    // Lista utenti da
    ArrayList<String> labelValues;

  public static String STRINGA_BUNDLE = "lista_valori_etichetta";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Associa un activity alla sua View
        setContentView(R.layout.activity_label);

       this.setTitle("Label settings");

        labelValues = new ArrayList<String>(10);
        for(int i = 0; i < labelValues.size(); i++){

            labelValues.get(i);

        }

        saveButton = (Button)findViewById(R.id.buttonSave);
        pipelineButton = (Button)findViewById(R.id.buttonPipeline);

        editText1 = (EditText)findViewById(R.id.editTextTextField1);
        editText2 = (EditText)findViewById(R.id.editTextTextField2);
        editText3 = (EditText)findViewById(R.id.editTextTextField3);
        editText4 = (EditText)findViewById(R.id.editTextTextField4);
        editText5 = (EditText)findViewById(R.id.editTextTextField5);
        editText6 = (EditText)findViewById(R.id.editTextTextField6);
        editText7 = (EditText)findViewById(R.id.editTextTextField7);
        editText8 = (EditText)findViewById(R.id.editTextTextField8);
        editText9 = (EditText)findViewById(R.id.editTextTextField9);
        editText10 = (EditText)findViewById(R.id.editTextTextField10);

        // Recupero il bundle e lo utilizzo per settare i dati inseriti
        bundle = getIntent().getExtras();
        if (bundle != null) {

            labelValues = (ArrayList<String>) bundle.getSerializable(OCR.STRINGA_BUNDLE);
            if(labelValues.size() != 0) {
                editText1.setText(labelValues.get(0));
                editText2.setText(labelValues.get(1));
                editText3.setText(labelValues.get(2));
                editText4.setText(labelValues.get(3));
                editText5.setText(labelValues.get(4));
                editText6.setText(labelValues.get(5));
                editText7.setText(labelValues.get(6));
                editText8.setText(labelValues.get(7));
                editText9.setText(labelValues.get(8));
                editText10.setText(labelValues.get(9));
            }


        }



        pipelineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Apro l'activity principale passando il bundle con i dati inseriti dall'utente

                Bundle bundle = new Bundle();
                bundle.putStringArrayList(STRINGA_BUNDLE, labelValues);
                Context context = getApplicationContext();

                Intent intent = new Intent(new Intent(context, OCR.class));
                intent.putExtras(bundle);
                startActivity(intent);

                System.out.println("Passato dati per analisi OCR");
            }
        });

        // Ottengo i dati inseriti e li salvo internamente
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Ottengo i dati inseriti
                labelValues = new ArrayList<String>();

                labelValues.add(editText1.getText().toString());
                labelValues.add(editText2.getText().toString());
                labelValues.add(editText3.getText().toString());
                labelValues.add(editText4.getText().toString());
                labelValues.add(editText5.getText().toString());
                labelValues.add(editText6.getText().toString());
                labelValues.add(editText7.getText().toString());
                labelValues.add(editText8.getText().toString());
                labelValues.add(editText9.getText().toString());
                labelValues.add(editText10.getText().toString());

                System.out.println("Dati salvati correttamente");

            }
        });


    }

}
