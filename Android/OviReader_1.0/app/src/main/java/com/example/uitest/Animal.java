package com.example.uitest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

public class Animal implements Serializable {
    private String codCapo;
    private float bcs;
    private boolean comment = false;
    Animal(String codiceCapo, float bcs){
        this.bcs = bcs;
        this.codCapo = codiceCapo;
    }
    Animal(String codiceCapo){
        this.codCapo = codiceCapo;
    }

    @NonNull
    @Override
    public String toString() {
        return codCapo + " " + Float.toString(bcs);
    }

    public String getCodCapo() {
        return codCapo;
    }

    public float getBcs() {
        return bcs;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if(!(obj instanceof Animal)) return false;
        return (((Animal) obj).getCodCapo().equals(this.codCapo));
    }

    public void setBcs(float bcs) {
        this.bcs = bcs;
    }
    public void setComment(boolean comment){
        this.comment = comment;
    }
    public boolean getComment(){return comment;}
}
