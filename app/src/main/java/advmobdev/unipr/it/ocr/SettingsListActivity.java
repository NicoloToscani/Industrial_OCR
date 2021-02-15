package advmobdev.unipr.it.ocr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SettingsListActivity extends AppCompatActivity {

    // Bottoni che rappresentano i menu
    // Come futura estensione far estendere all'Activity una

    ArrayList<String> items = new ArrayList<String>();

    ListView listView;

    ArrayAdapter<String > arrayAdapter;

    Context context;
    Intent intent;

    Pipeline pipeline;

    Bundle bundle;

    Button liveCamera;

    public static String STRINGA_BUNDLE = "pipeline_bundle";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Associa un activity alla sua View
        setContentView(R.layout.activiy_settings_list);

        this.setTitle("Image Enhance Settings");

        listView = (ListView) findViewById(R.id.list_view);

        System.out.println("Creata activity List View");

        items.add("Segmentation");
        items.add("Morphological Transformations");
        items.add("Riga 3");
        items.add("Riga 4");
        items.add("Riga 5");
        items.add("Riga 6");
        items.add("Riga 7");
        items.add("Riga 8");


        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);

        // Data injection
        listView.setAdapter(arrayAdapter);

        // Registro il listener per la prssione della riga
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Settaggio per binarizzazione immagine
                if (position == 0) {

                    context = getApplicationContext();

                    Intent intent = new Intent(new Intent(context, BinarizationActivity.class));
                    intent.putExtras(bundle);
                    startActivity(intent);

                }

                // Operazioni morfologiche
                if (position == 1) {

                    context = getApplicationContext();

                    Intent intent = new Intent(new Intent(context, MorphologicalActivity.class));
                    intent.putExtras(bundle);
                    startActivity(intent);

                }

            }
        });


        // Recupero il bundle
        bundle = getIntent().getExtras();

        if (bundle != null) {

            pipeline = (Pipeline) bundle.getSerializable(OCR.STRINGA_BUNDLE);
            System.out.println(pipeline.getSimpleThresholdValue());

        }

        liveCamera = (Button) findViewById(R.id.buttonSave);
        liveCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Passo il bundle alla live camera si cui lei applica gli algoritmi
                context = getApplicationContext();
                Intent intent = new Intent(new Intent(context, LiveCameraActivity.class));

                bundle.putSerializable(STRINGA_BUNDLE,pipeline);
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });

    }

}
