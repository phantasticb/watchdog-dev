package com.byron.watchdogdev;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    // Alpha Vantage API key
    private final String ALPHA_VANTAGE_KEY = "7E10Z3LOK5J917QU";

    // Watchlist
    private LinkedHashMap<String, ArrayList<Double>> symbols = new LinkedHashMap<>();

    // Create a queue for API requests
    private RequestQueue requestQueue;

    // Declare views
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

        // Set click listener of button
        buttonAdd.setOnClickListener(new addButtonListener());

        mainDisplay.setLayoutManager(new LinearLayoutManager(this));
        stockListAdapter = new StockListAdapter(symbols);
        mainDisplay.setAdapter(stockListAdapter);

        // Request Queue
        requestQueue  = Volley.newRequestQueue(this);

    }

    // Method that sends API request for market data
    private void getPriceData(String symbol) {
        // Put the URL into
        String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" +
                symbol + "&apikey=" + ALPHA_VANTAGE_KEY;

        // Tag variable for debug log purposes
        final String REQUEST_TAG = "PRICE REQUEST";

        // Here we go, this is such a mess, create a request object and fill in args
        Request request = new JsonObjectRequest(Request.Method.GET, url, null,
                // Implement this abstract listener class
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Get the quote data and parse it into an object since data is
                        // contained within an inner object for Alpha Vantage
                        JSONObject quote = response.optJSONObject("Global Quote");

                        // Default ticker set to NULL, if it is still NULL we have a problem
                        String symbol = "NULL";

                        // optString tends to break for some reason so I shoved it into a try-catch
                        try {
                            // If it works, it sets the ticker to the actual symbol entered
                            symbol = quote.optString("01. symbol");
                        } catch (Exception e) {
                            // If not, it pushes this to the log
                            Log.v(REQUEST_TAG, "Failed to parse name.");
                        }

                        // If symbol successfully parsed from JSON, execute this
                        if(symbol != "NULL") {
                            // Create an ArrayList object to update the hashmap watchlist with
                            ArrayList<Double> data = new ArrayList<>(2);

                            // Updates price and change from JSON
                            data.add(quote.optDouble("05. price"));
                            data.add(quote.optDouble("09. change"));

                            // Update debug log
                            Log.v(REQUEST_TAG, "Successfully requested " + symbol + " data");
                            Log.v(REQUEST_TAG, "Price is $" + data.get(0));

                            // Add data to hashmap watchlist
                            symbols.put(symbol, data);

                            // Update debug log
                            Log.v(REQUEST_TAG, symbols.toString());

                            // Tell the adapter to update, possible redundancy here but idk
                            mainDisplay.getAdapter().notifyItemInserted(symbols.size());
                            mainDisplay.getAdapter().notifyDataSetChanged();
                        } else {
                            // If symbol failed to parse
                            Log.v(REQUEST_TAG, "Failed to update price");

                            // Show an error message
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Invalid symbol", Toast.LENGTH_LONG);

                            toast.show();
                        }
                    }
                }, new Response.ErrorListener() {
                    // Implement the required ErrorListener abstract class
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(REQUEST_TAG, "Request failed");

                        // Just show an error message if something broke on a deep level
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "API request failed", Toast.LENGTH_LONG);

                        toast.show();
                    }
                });

        // Add the request (the whole mess above) to the queue
        requestQueue.add(request);
    }

    // This is the listener for the add button
    private class addButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            // If the text entry field isn't empty
            if(symbolEntry.getText().toString().trim().length() != 0) {
                // Ensure text is capitalized
                String symbol = symbolEntry.getText().toString().toUpperCase();

                // Call the getPriceData function
                getPriceData(symbol);

                // Clear the entry field
                symbolEntry.setText("");

                // Update the log
                Log.v("Main Activity", symbols.toString());
            }
        }
    }
}
