package com.att.iothub;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Hub extends ActionBarActivity {

    int MUSIC_PICKER_CODE = 1;

    MediaPlayer mp;

    HashMap<Button, Integer> inputColorMap = new HashMap<>();
    HashMap<Button, Set<Button>> inputOutputMap = new HashMap<>();
    HashMap<Button, Uri> outputMap = new HashMap<>();

    LinearLayout inputs, outputs;

    Button selectedInput = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub);

        inputs = (LinearLayout)findViewById(R.id.inputs);
        outputs = (LinearLayout)findViewById(R.id.outputs);

        findViewById(R.id.button).setVisibility(View.GONE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hub, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.new_input) {

            final CharSequence[] items = {"Accelerometer", "Temperature", "Ambient Light", "Humidity", "Barometer", "Gyroscope", "Compass", "Magnetometer"};
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);

            builder1.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        final Button b = new Button(getApplicationContext());
                        b.setMaxEms(10);
                        b.setText(items[i]);
                        Random rnd = new Random();
                        int color = Color.argb(255, rnd.nextInt(16)*16, rnd.nextInt(16)*16, rnd.nextInt(16)*16);
                        while (inputColorMap.containsValue(color)){
                            color = Color.argb(255, rnd.nextInt(16)*16, rnd.nextInt(16)*16, rnd.nextInt(16)*16);
                        }
                        b.setBackgroundColor(color);
                        inputColorMap.put(b, color);

                        b.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            if (selectedInput == b){
                                b.setEnabled(true);
                                selectedInput = null;
                            }
                            else {
                                b.setEnabled(false);
                                if (selectedInput != null) {
                                    selectedInput.setEnabled(true);
                                }
                                selectedInput = b;
                            }
                            }
                        });

                        inputs.addView(b);
                        inputOutputMap.put(b, new HashSet<Button>());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            AlertDialog alert11 = builder1.create();
            alert11.show();

            return true;
        }

        if (id == R.id.new_output){
            final CharSequence[] items = {"Speakers"};

            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);

            builder1.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (items[i].toString()){
                        case "Speakers":
                            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent,MUSIC_PICKER_CODE);
                            break;
                        default:
                            break;
                    }
                }
            });
            AlertDialog alert11 = builder1.create();
            alert11.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MUSIC_PICKER_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                final Button b = new Button(getApplicationContext());
                b.setMaxEms(10);
                b.setText("SPEAKER: " + uri.toString());
                outputMap.put(b, uri);

                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick( View view ) {
                        if (selectedInput != null ) {
                            selectedInput.setEnabled(true);
                            b.setBackgroundColor(inputColorMap.get(selectedInput));
                            selectedInput = null;
                        } else {
                            executeOutputAction(b);
                        }
                    }
                });

                outputs.addView(b);
            }
        }
    }


    private void executeOutputActionsOfInput( Button inputButton ){
        if(!inputOutputMap.containsKey(inputButton)) return;
        for (Button outputButton : inputOutputMap.get(inputButton)){
            executeOutputAction(outputButton);
        }
    }


    private void executeOutputAction(Button outputButton){
        mp = MediaPlayer.create(getApplicationContext(), outputMap.get(outputButton));
        mp.start();
    }
}
