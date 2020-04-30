package com.byron.watchdogdev;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    // Alpha Vantage API key
    private final String ALPHA_VANTAGE_KEY = "7E10Z3LOK5J917QU";

    // TODO: Find a way to URL encode the refresh key within Java
    private String TD_REFRESH_KEY;
    
    private final String TD_APP_ID = "RMXVQLMDQ1VKXJ23EUWBXMNRFMSNHSOJ";
    private String TD_ACCESS_KEY = "null";

    // Watchlist
    private ArrayList<ArrayList<String>> symbols_list = new ArrayList<>();

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

        // Read TD Code from file
        try {
          InputStream inputStream = getAssets().open("refresh_code.txt");
          int size = inputStream.available();
          byte[] buffer = new byte [size];
          inputStream.read(buffer);
          inputStream.close();
          TD_REFRESH_KEY = new String(buffer);

          Log.v("CODE READ", "Received code " + TD_REFRESH_KEY.substring(0, 6) + "...");
        } catch (IOException e) {
            e.printStackTrace();
            Log.v("CODE READ", "Could not read code");
        }

        // Views
        buttonAdd = (Button) findViewById(R.id.buttonAdd);
        symbolEntry = (TextView) findViewById(R.id.symbolEntry);
        mainDisplay = (RecyclerView) findViewById(R.id.mainDisplay);

        // Set click listener of button
        buttonAdd.setOnClickListener(new addButtonListener());

        mainDisplay.setLayoutManager(new LinearLayoutManager(this));
        stockListAdapter = new StockListAdapter(this, symbols_list);
        mainDisplay.setAdapter(stockListAdapter);

        // Request Queue
        requestQueue  = Volley.newRequestQueue(this);

        // FIXME: Put me in an async task, I will expire fast
        getTDAccessCode();

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {

                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                symbols_list.remove(viewHolder.getAdapterPosition());

                Log.v("SWIPE LISTEN", symbols_list.toString());

                stockListAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());

            }
        });

        helper.attachToRecyclerView(mainDisplay);

    }

    @Deprecated // Method that sends API request for Alpha Vantage data
    private void getDataAV(String symbol) {
        // Put the URL into
        String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" +
                symbol + "&apikey=" + ALPHA_VANTAGE_KEY;

        // Tag variable for debug log purposes
        final String REQUEST_TAG = "AV PRICE REQUEST";

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
                            ArrayList<String> data = new ArrayList<>(2);

                            // Updates price and change from JSON
                            data.add(symbol);
                            data.add(Double.toString(quote.optDouble("05. price")));
                            data.add(Double.toString(quote.optDouble("09. change")));

                            // Update debug log
                            Log.v(REQUEST_TAG, "Successfully requested " + symbol + " data");
                            Log.v(REQUEST_TAG, "Price is $" + data.get(0));

                            // Add data to hashmap watchlist

                            boolean alreadyExists = false;

                            for(ArrayList i : symbols_list) {
                                if(symbol.equals(i.get(0))) {
                                    alreadyExists = true;
                                }
                            }

                            if(!alreadyExists) {
                                symbols_list.add(data);

                                // Tell the adapter to update, possible redundancy here but idk
                                mainDisplay.getAdapter().notifyItemInserted(symbols_list.size());
                                mainDisplay.getAdapter().notifyDataSetChanged();
                            } else {
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        "Symbol already added", Toast.LENGTH_LONG);

                                toast.show();
                            }

                            // Update debug log
                            Log.v(REQUEST_TAG, symbols_list.toString());

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

    // [ASYNC] Retrieves access key from TD API
    private void getTDAccessCode() {
        // URL to connect to
        String url = "https://api.tdameritrade.com/v1/oauth2/token";

        // New request
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject data = new JSONObject();
                        String access_key = "null";

                        try {
                            data = new JSONObject(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            access_key = data.getString("access_token");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.v("RETRIEVE SUCCESS", access_key);
                        Log.v("RETRIEVE SUCCESS", "Setting new access key");
                        TD_ACCESS_KEY = access_key;
                        Log.v("NEW ACCESS KEY", TD_ACCESS_KEY);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("RETRIEVE FAILURE", "Could not retrieve access key");
                    }
                }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                String postBody = "grant_type=refresh_token&refresh_token=" + TD_REFRESH_KEY
                        + "&client_id=" + TD_APP_ID;

                return postBody.getBytes();
            }
        };

        requestQueue.add(postRequest);
    }

    // [ASYNC] Gets live TD data using access key
    private void getDataTD(String input_symbol) {
        // Make a final variable so this can be accessed
        final String symbol = input_symbol;

        // Set TD url with code
        String url = "https://api.tdameritrade.com/v1/marketdata/" + symbol
                + "/quotes?apikey=" + TD_APP_ID;

        // New request
        Request request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // Print raw data feed so I can see if its delayed
                Log.v("RAW DATA", response.toString());

                try {
                    // Parse JSON object
                    JSONObject dataset = response.getJSONObject(symbol);

                    String price = dataset.getString("lastPrice");
                    Log.v("QUOTE RETRIEVE", price);
                    String chg = dataset.getString("netChange");

                    // Make sure ticker does not already exist
                    boolean alreadyExists = false;

                    for(ArrayList<String> i : symbols_list) {
                        if(symbol.equals(i.get(0))) {
                            alreadyExists = true;
                        }
                    }

                    // Add the ticker to the list if it's not there already
                    if(!alreadyExists) {
                        ArrayList<String> data = new ArrayList<>();
                        data.add(symbol);
                        data.add(price);
                        data.add(chg);

                        symbols_list.add(data);

                        mainDisplay.getAdapter().notifyItemInserted(symbols_list.size());
                        mainDisplay.getAdapter().notifyDataSetChanged();
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Symbol already added", Toast.LENGTH_LONG);

                        toast.show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Invalid symbol", Toast.LENGTH_LONG);

                    toast.show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Could not connect to API", Toast.LENGTH_LONG);

                toast.show();
            }
        }){
            // Specify authorization header parameters
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                Log.v("ACCESS KEY CHECK", TD_ACCESS_KEY);
                params.put("Authorization", "Bearer " + TD_ACCESS_KEY);
                return params;
            }

            // Possibly redundant, too scared to remove for now
            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        // Add request to queue
        requestQueue.add(request);
    }

    // TODO: Aux function that gets new access codes, wrap in async task
    private void addCard() {

    }

    private class addButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            // If the text entry field isn't empty
            if(symbolEntry.getText().toString().trim().length() != 0) {
                // Ensure text is capitalized
                String symbol = symbolEntry.getText().toString().toUpperCase();

                getDataTD(symbol);

                // Clear the entry field
                symbolEntry.setText("");

                // Update the log
                Log.v("Main Activity", symbols_list.toString());
            }
        }
    }
}
