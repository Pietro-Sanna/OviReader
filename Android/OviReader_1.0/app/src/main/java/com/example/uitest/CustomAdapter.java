package com.example.uitest;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends ArrayAdapter<Animal> {


    public CustomAdapter(@NonNull Context context, int resource, List<Animal> data) {
        super(context, resource,data);
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView =  inflater.inflate(R.layout.list_item, null);
        TextView textView = convertView.findViewById(R.id.text_view_id);
        Animal currentItem = getItem(position);
        int purple = Color.parseColor("#E6E6FA");
        if (currentItem != null) {
            textView.setText(currentItem.toString());
            if(currentItem.getComment()) textView.setBackgroundColor(purple);
        }

        return convertView;
    }

}
