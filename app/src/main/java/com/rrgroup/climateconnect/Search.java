package com.rrgroup.climateconnect;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Search extends AppCompatActivity {

    private MaterialAutoCompleteTextView autoCompleteTextView;
    private OkHttpClient client;
    private ArrayAdapter<String> adapter;
    private ArrayAdapter<String> adapter2;
    private CardView myLocation;
    private boolean myLocationClicked=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //night mode disable
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        //set black color for status bar icons
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            int flags = decorView.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(flags);
        }

        autoCompleteTextView = findViewById(R.id.autoComplete);
        myLocation=findViewById(R.id.myLocation);

        myLocation.setOnClickListener(view->{
            Intent intent = new Intent();
            intent.putExtra("selectedCity", "myLocation");
            setResult(RESULT_OK, intent);
            Toast.makeText(this, "Switched to Current Location", Toast.LENGTH_SHORT).show();
            finish();
        });

        client = new OkHttpClient();

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String input = s.toString();

                int lineCount = autoCompleteTextView.getLineCount();
                if (lineCount > 3) {
                    autoCompleteTextView.setText(input.substring(0, autoCompleteTextView.getSelectionStart() - 1));
                    autoCompleteTextView.setSelection(autoCompleteTextView.getText().length());
                }
                if (input.length() >= 3) {
                    requestCitySuggestions(input);

                }
            }
        });


        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCity = adapter.getItem(position);
            String lat_long = adapter2.getItem(position);
            System.out.println(lat_long);

            if (selectedCity != null) {
                // Pass the selected city back to MainActivity
                Intent intent = new Intent();
                intent.putExtra("selectedCity", selectedCity);
                intent.putExtra("lat_long", lat_long);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    private void requestCitySuggestions(String input) {
        String url = "http://api.geonames.org/searchJSON?name_startsWith="+input+"&maxRows=5&username="+AppConfig.GeonamesUsername;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    List<String> suggestions = parseCitySuggestions(responseData);
                    System.out.println(responseData);
                    runOnUiThread(() -> {
                        adapter = new ArrayAdapter<>(Search.this,
                                android.R.layout.simple_dropdown_item_1line, suggestions);
                        autoCompleteTextView.setAdapter(adapter);
                        autoCompleteTextView.showDropDown();
                    });
                }
            }
        });
    }

    private List<String> parseCitySuggestions(String responseData) {
        List<String> suggestions = new ArrayList<>();
        List<String> suggestions2 = new ArrayList<>();
        try {
            JSONObject jsonResponse = new JSONObject(responseData);
            JSONArray geonamesArray = jsonResponse.getJSONArray("geonames");
            for (int i = 0; i < geonamesArray.length(); i++) {
                JSONObject cityObject = geonamesArray.getJSONObject(i);
                String cityName = cityObject.getString("name");
                String countryName = cityObject.getString("countryName");
                String longitude = cityObject.getString("lng");
                String latitude = cityObject.getString("lat");
                String suggestion = cityName + ", " + countryName;
                suggestions.add(suggestion);
                suggestions2.add(latitude+","+longitude);
            }
            adapter2 = new ArrayAdapter<>(Search.this,
                    android.R.layout.simple_dropdown_item_1line, suggestions2);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return suggestions;
    }

}

