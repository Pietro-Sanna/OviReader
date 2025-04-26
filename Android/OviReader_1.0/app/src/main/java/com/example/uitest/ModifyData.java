package com.example.uitest;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ModifyData {
    private String binaryName;
    private ArrayList<Animal> dataRecived;
    private Activity activity;

    ModifyData(String binaryName,Activity activity){
        this.binaryName = binaryName;
        this.activity=activity;
    }

    public ArrayList<Animal> readBinary(){
        try{
            File directory = new File(activity.getFilesDir(),"ReaderDocuments");

            if (!directory.exists()) {
                boolean dirCreated = directory.mkdirs(); // Creates the directory if it doesn't exist
                if (!dirCreated) {
                    // Directory creation failed
                    Toast.makeText(activity, "Creation direcotry error",Toast.LENGTH_LONG).show();
                }
            }
            File file = new File(directory, binaryName);
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            dataRecived= (ArrayList<Animal>) ois.readObject();
            System.out.println("File binario caricato");
            return dataRecived;
        } catch (IOException | ClassNotFoundException e) {
            Toast.makeText(activity, "Nessun dato trovato in memoria", Toast.LENGTH_LONG).show();
            return new ArrayList<>();
        }

    }

    public boolean writeBinary(ArrayList<Animal> animals){
        try {

            File directory = new File(activity.getFilesDir(),"ReaderDocuments");
            File file = new File(directory, binaryName);
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(animals);
            oos.flush();
            oos.close();
            return true;
        } catch (IOException e){
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public static boolean writeTxt(String txtName,ArrayList<Animal> animals){

        try{
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), txtName + ".txt");


            FileWriter fileWriter = new FileWriter(file);
            for(Animal a : animals){
                String write = a.toString();
                if(a.getComment()) write+=" true";
                else write+=" false";
                fileWriter.write(write+"\n");
            }
            fileWriter.close();
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteBinaryFile(){
        File directory = new File(activity.getFilesDir(),"ReaderDocuments");

        if (!directory.exists()) {
            boolean dirCreated = directory.mkdirs(); // Creates the directory if it doesn't exist
            if (!dirCreated) {
                // Directory creation failed
                return false;
            }
        }
        File file = new File(directory, binaryName);
        if(file.exists()) {
            file.delete();
            return true;
        }else return true;
    }


}
