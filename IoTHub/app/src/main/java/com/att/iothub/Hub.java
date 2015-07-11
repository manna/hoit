package com.att.iothub;

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
            try {
                final Button b = new Button(getApplicationContext());
                b.setMaxEms(10);
                b.setText("Sensor");
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
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        if (id == R.id.new_output){
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i,MUSIC_PICKER_CODE);
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
