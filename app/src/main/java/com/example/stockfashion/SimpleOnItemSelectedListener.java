package com.example.stockfashion;

import android.view.View;
import android.widget.AdapterView;

public class SimpleOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

    private final Runnable onItemSelectedAction;

    // Constructor that takes a Runnable (like a lambda)
    public SimpleOnItemSelectedListener(Runnable onItemSelectedAction) {
        this.onItemSelectedAction = onItemSelectedAction;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // When an item is selected, execute the action
        if (onItemSelectedAction != null) {
            onItemSelectedAction.run();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing, as we only care about the selection event
    }
}
