package com.byron.watchdogdev;

import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class StockListAdapter extends RecyclerView.Adapter<StockListAdapter.StockViewHolder> {
    // Include a variable for watchlist collected in input field to be passed to
    private final LinkedHashMap<String, ArrayList<Double>> symbols;

    // Constructor passes watchlist data to member variable symbols
    public StockListAdapter(LinkedHashMap<String, ArrayList<Double>> data) {
        symbols = data;
    }

    // Inflate the layout
    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use the XML to inflate the layout, idk what the args do though
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_item, parent, false);

        // TODO: Put onClick listener here

        StockViewHolder stockViewHolder = new StockViewHolder(view);
        return stockViewHolder;
    }

    // Binds the components in the view to actual data
    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        // Bind each element in the holder to a member variable
        TextView symbolText = holder.symbol;
        TextView priceText = holder.price;

        // Translate the keys of the watchlist
        String symbol = symbols.keySet().toArray()[position].toString();

        // Set each text field to appropriate text, this method acts like a for loop
        Log.v("Adapter", "Adding " + symbol);
        symbolText.setText(symbol);
        priceText.setText(symbols.get(symbol).get(0).toString());
    }

    // Self-explanatory
    @Override
    public int getItemCount() {
        return symbols.size();
    }

    // Holds the view for the recycler view and child components
    public class StockViewHolder extends RecyclerView.ViewHolder {
        // Declare the two components of the recycler view
        TextView symbol;
        TextView price;

        // Constructor attaches the XML elements to these declared components
        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            this.symbol = (TextView) itemView.findViewById(R.id.stockText);
            this.price = (TextView) itemView.findViewById(R.id.priceText);
        }
    }
}
