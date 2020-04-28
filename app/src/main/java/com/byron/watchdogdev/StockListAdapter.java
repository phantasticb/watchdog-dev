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
    private final LinkedHashMap<String, ArrayList<Float>> symbols;

    public StockListAdapter(LinkedHashMap<String, ArrayList<Float>> data) {
        symbols = data;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_item, parent, false);

        // TODO: Put onClick listener here

        StockViewHolder stockViewHolder = new StockViewHolder(view);
        return stockViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        TextView symbol = holder.symbol;

        Log.v("Adapter", "Adding " + symbols.keySet().toArray()[position].toString());
        symbol.setText(symbols.keySet().toArray()[position].toString());
    }

    @Override
    public int getItemCount() {
        return symbols.size();
    }

    public class StockViewHolder extends RecyclerView.ViewHolder {
        TextView symbol;

        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            this.symbol = (TextView) itemView.findViewById(R.id.stockText);
        }
    }
}
