package advmobdev.unipr.it.ocr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

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


    // Lista utenti da
    ArrayList<String> labelValues;

  public static String STRINGA_BUNDLE = "lista_valori_etichetta";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Associa un activity alla sua View
        setContentView(R.layout.activity_label);

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
