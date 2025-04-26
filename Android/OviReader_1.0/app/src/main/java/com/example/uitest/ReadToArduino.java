package com.example.uitest;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ReadToArduino extends Thread {
    private final Object lock = new Object(); // Shared object for synchronization
    private InputStream inputStream;
    private boolean aBoolean;
    private Activity activity;
    private Handler handler;


    ReadToArduino(InputStream inputStream , Activity activity) {
        this.inputStream = inputStream;
        this.activity = activity;
        this.handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void run() {



        aBoolean = true;
        byte[] buffer = new byte[1024];
        int bytes = 0; // bytes returned from read()
        int numberOfReadings = 0; // number of readings from Arduino

        while (numberOfReadings < 1024 || aBoolean) {
            synchronized (lock) {
                try {
                    if (inputStream.available() > 0 && MainActivity.codCapo.getText().toString().equals("")) {
                        try {
                            buffer[bytes] = (byte) inputStream.read();

                            if (buffer[bytes] == '\r') {
                                String readMessage = new String(buffer, 0, bytes);

                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Animal a = new Animal(readMessage);
                                        MainActivity.codCapo.setText(readMessage);
                                        if(MainActivity.dataReceived.contains(a)) {
                                            MainActivity.save.setText("Modifica");
                                            MainActivity.codCapo.setTextColor(Color.RED);
                                        }
                                        else {
                                            MainActivity.save.setText("Salva");
                                            MainActivity.codCapo.setTextColor(Color.BLACK);
                                        }
                                    }
                                });
                                buffer[bytes] = (byte) inputStream.read();
                                bytes = 0;
                                numberOfReadings++;
                            }
                            else
                                bytes++;
                        } catch (IOException e) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void close() {
        aBoolean = false;
    }
    public Object getLock() {
        return lock;
    }

}

