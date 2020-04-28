package com.byron.watchdogdev;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // Watchlist
    private LinkedHashMap<String, ArrayList<Float>> symbols = new LinkedHashMap<>();

    // Views
    private Button buttonAdd;
    private TextView symbolEntry;
    private RecyclerView mainDisplay;
    private StockListAdapter stockListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views
        buttonAdd = (Button) findViewById(R.id.buttonAdd);
        symbolEntry = (TextView) findViewById(R.id.symbolEntry);
        mainDisplay = (RecyclerView) findViewById(R.id.mainDisplay);

        buttonAdd.setOnClickListener(new addButtonListener());

        mainDisplay.setLayoutManager(new LinearLayoutManager(this));
        stockListAdapter = new StockListAdapter(symbols);
        mainDisplay.setAdapter(stockListAdapter);

    }

    private class addButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if(symbolEntry.getText().toString().trim().length() != 0) {
                // Add the symbol to the list with a placeholder price data
                ArrayList<Float> price_data = new ArrayList<>(2);
                price_data.add(0F); price_data.add(0F);
                symbols.put(symbolEntry.getText().toString().toUpperCase(), price_data);

                mainDisplay.getAdapter().notifyItemInserted(symbols.size());

                symbolEntry.setText("");
                Log.v("Main Activity", symbols.toString());
            }
        }
    }
}
