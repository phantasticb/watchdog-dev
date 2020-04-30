package com.byron.watchdogdev;

import android.content.Context;
import android.graphics.Color;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class StockListAdapter extends RecyclerView.Adapter<StockListAdapter.StockViewHolder> {
    // Include a variable for watchlist collected in input field to be passed to
    private final LinkedHashMap<String, ArrayList<Double>> symbols;
    private Context context;

    // Enum stock categories
    private enum categories {
        TECH,
        INDUSTRY,
        FINANCE,
        AUTO,
        AIR,
        SHOP,
        FOOD,
        MEDIA,
        SOCIAL,
        SPACE,
        NULL
    }

    // Constructor passes watchlist data to member variable symbols
    public StockListAdapter(Context activity, LinkedHashMap<String, ArrayList<Double>> data) {
        symbols = data;
        context = activity;
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
        ImageView image = holder.image;

        // Translate the keys of the watchlist
        String symbol = symbols.keySet().toArray()[position].toString();

        // Money format
        DecimalFormat df = new DecimalFormat("0.00");

        // Set each text field to appropriate text, this method acts like a for loop
        Log.v("Adapter", "Adding " + symbol);
        symbolText.setText(symbol);
        priceText.setText(df.format(symbols.get(symbol).get(0)));

        if(symbols.get(symbol).get(1) > 0) {
            priceText.setTextColor(0xFF00DD00);
        } else if(symbols.get(symbol).get(1) < 0) {
            priceText.setTextColor(0xFFBB0000);
        } else {
            priceText.setTextColor(Color.GRAY);
        }

        // Set the proper image
        Log.v("IMAGE FETCH", fetchImage(symbol).toString());

        switch(fetchImage(symbol)){
            case NULL:
                Log.v("IMAGE SET", "Category failed to parse");
                image.setImageResource(R.color.darkElement);
                break;
            case TECH:
                image.setImageResource(R.drawable.tech);
                Log.v("IMAGE SET", "Setting image tech");
                break;
            case MEDIA:
                image.setImageResource(R.drawable.media);
                Log.v("IMAGE SET", "Setting image tech");
                break;
            case INDUSTRY:
                image.setImageResource(R.drawable.industry);
                Log.v("IMAGE SET", "Setting image tech");
                break;
            case AIR:
                image.setImageResource(R.drawable.air);
                Log.v("IMAGE SET", "Setting image tech");
                break;
            case SPACE:
                image.setImageResource(R.drawable.space);
                Log.v("IMAGE SET", "Setting image tech");
                break;
            case FOOD:
                image.setImageResource(R.drawable.food);
                Log.v("IMAGE SET", "Setting image tech");
                break;
            case SHOP:
                image.setImageResource(R.drawable.shop);
                Log.v("IMAGE SET", "Setting image tech");
                break;
            case FINANCE:
                image.setImageResource(R.drawable.finance);
                Log.v("IMAGE SET", "Setting image tech");
                break;
            case SOCIAL:
                image.setImageResource(R.drawable.social);
                Log.v("IMAGE SET", "Setting image tech");
                break;
            case AUTO:
                image.setImageResource(R.drawable.auto);
                Log.v("IMAGE SET", "Setting image tech");
                break;
            default:
                image.setImageResource(R.color.darkElement);
                Log.v("IMAGE SET", "Setting image default");
                break;
        }
    }

    private categories fetchImage(String symbol) {
        String jsonString;
        try {
            InputStream is = context.getAssets().open("categories.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return categories.NULL;
        }



        try{
            JSONObject jsonData = new JSONObject(jsonString);
            JSONArray tech = jsonData.getJSONArray("tech");
            JSONArray media = jsonData.getJSONArray("media");
            JSONArray space = jsonData.getJSONArray("space");
            JSONArray industry = jsonData.getJSONArray("industry");
            JSONArray food = jsonData.getJSONArray("food");
            JSONArray shop = jsonData.getJSONArray("shop");
            JSONArray social = jsonData.getJSONArray("social");
            JSONArray air = jsonData.getJSONArray("air");
            JSONArray auto = jsonData.getJSONArray("auto");
            JSONArray finance = jsonData.getJSONArray("finance");

            Log.v("JSON PARSE", tech.toString());

            // This is terrible

            if(jsonArrayContains(tech, symbol)) {
                return categories.TECH;
            } else if(jsonArrayContains(media, symbol)) {
                return categories.MEDIA;
            } else if(jsonArrayContains(finance, symbol)) {
                return categories.FINANCE;
            } else if(jsonArrayContains(air, symbol)) {
                return categories.AIR;
            } else if(jsonArrayContains(auto, symbol)) {
                return categories.AUTO;
            } else if(jsonArrayContains(food, symbol)) {
                return categories.FOOD;
            } else if(jsonArrayContains(shop, symbol)) {
                return categories.SHOP;
            } else if(jsonArrayContains(social, symbol)) {
                return categories.SOCIAL;
            } else if(jsonArrayContains(space, symbol)) {
                return categories.SPACE;
            } else if(jsonArrayContains(industry, symbol)) {
                return categories.INDUSTRY;
            } else {
                return categories.NULL;
            }

        } catch (Exception e) {
            return categories.NULL;
        }
    }

    private boolean jsonArrayContains(JSONArray jsonArray, String element) throws JSONException {
        Log.v("JSON ARRAY READ", Boolean.toString(jsonArray.toString().contains(element)));

        ArrayList<String> list = new ArrayList<String>();
        for(int i = 0; i < jsonArray.length(); i++) {
            list.add((String) jsonArray.get(i));
        }

        return list.contains(element);
    }

    // Self-explanatory
    @Override
    public int getItemCount() {
        return symbols.size();
    }

    // Holds the view for the recycler view and child components
    public class StockViewHolder extends RecyclerView.ViewHolder {
        // Declare the three components of the recycler view
        ImageView image;
        TextView symbol;
        TextView price;

        // Constructor attaches the XML elements to these declared components
        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            this.symbol = (TextView) itemView.findViewById(R.id.stockText);
            this.price = (TextView) itemView.findViewById(R.id.priceText);
            this.image = (ImageView) itemView.findViewById(R.id.logoImage);
        }
    }
}
