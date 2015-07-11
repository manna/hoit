package com.att.iothub;

import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.example.ti.ble.common.BluetoothLeService;
import com.example.ti.ble.sensortag.BarometerCalibrationCoefficients;
import com.example.ti.ble.sensortag.R;
import com.example.ti.ble.sensortag.SensorTagGatt;

public class Hub extends FragmentActivity {

    int MUSIC_PICKER_CODE = 1;

    MediaPlayer mp;

    HashMap<Button, Integer> inputColorMap = new HashMap<>();
    HashMap<Button, Set<Button>> inputOutputMap = new HashMap<>();
    HashMap<Button, Uri> outputMap = new HashMap<>();
    Button testBtn;

    LinearLayout inputs, outputs;

    Button selectedInput = null;
    private String mFwRev;
    private boolean mIsSensorTag2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub);

        inputs = (LinearLayout)findViewById(R.id.inputs);
        outputs = (LinearLayout)findViewById(R.id.outputs);
        testBtn = (Button) findViewById(R.id.button);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mIsSensorTag2 = extras.getBoolean("mIsSensorTag2");
        }
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

    public void testing(View v){
        Toast.makeText(v.getContext(), "OAD not available on this Android device", Toast.LENGTH_LONG).show();
        // Data read
        // String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
        // byte[] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
        //onCharacteristicsRead(SensorTagGatt.UUID_IRT_DATA.toString(), value, BluetoothGatt.GATT_SUCCESS);
    }

    private void onCharacteristicsRead(String uuidStr, byte[] value, int status) {
        // Log.i(TAG, "onCharacteristicsRead: " + uuidStr);

        if (uuidStr.equals(SensorTagGatt.UUID_DEVINFO_FWREV.toString())) {
            mFwRev = new String(value, 0, 3);
            Toast.makeText(this, "Firmware revision: " + mFwRev,Toast.LENGTH_LONG).show();
        }

        if (mIsSensorTag2)
            return;

        if (uuidStr.equals(SensorTagGatt.UUID_BAR_CALI.toString())) {
            // Sanity check
            if (value.length != 16)
                return;

            // Barometer calibration values are read.
            List<Integer> cal = new ArrayList<Integer>();
            for (int offset = 0; offset < 8; offset += 2) {
                Integer lowerByte = (int) value[offset] & 0xFF;
                Integer upperByte = (int) value[offset + 1] & 0xFF;
                cal.add((upperByte << 8) + lowerByte);
            }

            for (int offset = 8; offset < 16; offset += 2) {
                Integer lowerByte = (int) value[offset] & 0xFF;
                Integer upperByte = (int) value[offset + 1];
                cal.add((upperByte << 8) + lowerByte);
            }

            BarometerCalibrationCoefficients.INSTANCE.barometerCalibrationCoefficients = cal;
        }
    }
}
