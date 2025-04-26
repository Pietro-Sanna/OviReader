package com.example.uitest;

import java.io.IOException;
import java.io.InputStream;

public class Reading extends Thread {
    private ReadToArduino readToArduino;
    private InputStream inputStream;
    private boolean aBoolean = true;

    Reading(InputStream inputStream, ReadToArduino readToArduino) {
        this.readToArduino = readToArduino;
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        int control = 0;

        while (aBoolean) {
            if (control == 0) {
                readToArduino.start();
                control++; // Increment control to avoid multiple start calls
            }
            try {
                synchronized (readToArduino.getLock() ) {
                    if (inputStream.available() > 0 && MainActivity.codCapo.getText().toString().equals("")) {
                        // Notify the ReadToArduino thread when data is available
                        readToArduino.getLock().notify();
                    } else {
                        // Wait for notification from ReadToArduino
                        readToArduino.getLock().wait();
                    }
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isOpen() {
        return aBoolean;
    }

    public void close() {
        readToArduino.close();
        aBoolean = false;
    }
}

