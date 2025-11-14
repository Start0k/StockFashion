package com.example.stockfashion;

import android.view.View;
import android.widget.AdapterView;

public class SimpleOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

    private final Runnable onItemSelectedAction;


    public SimpleOnItemSelectedListener(Runnable onItemSelectedAction) {
        this.onItemSelectedAction = onItemSelectedAction;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (onItemSelectedAction != null) {
            onItemSelectedAction.run();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
