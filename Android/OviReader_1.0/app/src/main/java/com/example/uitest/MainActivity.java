package com.example.uitest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.uitest.databinding.ActivityMainBinding;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private Button connect_to_reader;
    public Reading reading;
    public ReadToArduino readToArduino;
    private BluetoothAdapter btAdapter;
    private BluetoothDevice btDevice;
    private BluetoothSocket btSocket;
    private InputStream inputStream;
    private UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //We declare a default UUID to create the global variable
    private ListView listView;
    private String arduinoAddress = "20:18:07:09:19:65";
    public static ArrayList<Animal> dataReceived ;
    public static Button save;
    private RadioGroup bcs1;
    private RadioGroup bcs2;
    static public TextView codCapo;
    private RadioButton bcsValue5;
    private ImageButton clear;
    private Switch comment;
    private CustomAdapter customAdapter ;
    private ModifyData modifyData = new ModifyData("binaryData",this);
    private DataMia oggi = new DataMia();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connect_to_reader = findViewById(R.id.connected_to_reader);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);

        //Declaration variables
        dataReceived = modifyData.readBinary();
        listView = findViewById(R.id.listView);
        customAdapter = new CustomAdapter(this,R.layout.list_item,dataReceived);
        listView.setAdapter(customAdapter);
        codCapo = findViewById(R.id.codCapoRead);
        save = findViewById(R.id.saveData);
        bcs1 = findViewById(R.id.Bcs1);
        bcs2 = findViewById(R.id.Bcs2);
        bcsValue5 = findViewById(R.id.radioButtonBcs5);
        clear = findViewById(R.id.clear);
        comment = findViewById(R.id.comment1);




        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery
        )
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        //Check if device supported bluetooth module
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(MainActivity.this, "Il dispositivo non supporta il bluetooth", Toast.LENGTH_SHORT).show();

        }

        bcs1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == bcsValue5.getId()){
                    for (int i = 0; i < bcs2.getChildCount(); i++) {
                        bcs2.getChildAt(i).setEnabled(false);
                    }
                }else{
                    for (int i = 0; i < bcs2.getChildCount(); i++) {
                        bcs2.getChildAt(i).setEnabled(true);
                    }
                }
                bcs2.clearCheck();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = String.valueOf(codCapo.getText());
                if(!text.isEmpty() && bcs1.getCheckedRadioButtonId()!=-1){
                    Float tempBcs = valueBcs();
                    Animal a = new Animal(text,tempBcs);
                    a.setComment(comment.isChecked());

                    if (! dataReceived.contains(a)) dataReceived.add(a);
                    else {
                        dataReceived.get(dataReceived.indexOf(a)).setComment(comment.isChecked());
                        dataReceived.get(dataReceived.indexOf(a)).setBcs(tempBcs);
                    }
                    customAdapter.notifyDataSetChanged();

                    modifyData.writeBinary(dataReceived);
                    resetAll();
                }

            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!codCapo.getText().toString().isEmpty()) {
                    resetAll();
                }
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.connected_to_reader) {
            // Perform the action corresponding to the menu item click
            // For example, you can show a toast message
            if(connectToArduino()){
                Toast.makeText(MainActivity.this,"Connessione eseguita", Toast.LENGTH_SHORT).show();
                reading = new Reading(inputStream,readToArduino);
                reading.start();
            }
            else Toast.makeText(MainActivity.this,"Connessione non eseguita",Toast.LENGTH_SHORT).show();
            return true;
        }

        if(id == R.id.save_txt_file){
            if(!dataReceived.isEmpty()){
                ModifyData.writeTxt("Dati_lettore_"+oggi.toString(),dataReceived);
                dataReceived.clear();
                customAdapter.notifyDataSetChanged();
                resetAll();
                if (modifyData.deleteBinaryFile()) Toast.makeText(MainActivity.this,"Dati salvati nella cartella Download",Toast.LENGTH_LONG).show();
                else Toast.makeText(MainActivity.this,"Binary file deleted failed",Toast.LENGTH_LONG).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    public boolean connectToArduino(){
        if (!btAdapter.isEnabled()) {
            return false;
        }
        btDevice = btAdapter.getRemoteDevice(arduinoAddress);
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Toast.makeText(MainActivity.this, "Permission problem",Toast.LENGTH_SHORT);
                return false;
            }
            btSocket = btDevice.createRfcommSocketToServiceRecord(uuid);
            btSocket.connect();
            inputStream = btSocket.getInputStream();
            readToArduino = new ReadToArduino(inputStream,MainActivity.this);
            return true;
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, e.getMessage(),Toast.LENGTH_SHORT);
            return false;

        }
    }

    private Float valueBcs(){
        float dec =Float.valueOf( ( (RadioButton)findViewById(bcs1.getCheckedRadioButtonId())).getText().toString());
        if(bcs2.getCheckedRadioButtonId() != -1) {
            dec +=  Float.valueOf( ( (RadioButton)findViewById(bcs2.getCheckedRadioButtonId())).getText().toString() );
        }
        return dec;
    }

    private void resetAll(){
        if(bcs1.getCheckedRadioButtonId() != (-1)) bcs1.clearCheck();
        if(bcs2.getCheckedRadioButtonId() != (-1)) bcs2.clearCheck();
        comment.setChecked(false);
        save.setText("Salva");
        codCapo.setText("");
    }


}