package com.rrgroup.climateconnect;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.toolbox.JsonObjectRequest;
import com.elyeproj.loaderviewlibrary.LoaderTextView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.jasvir.seekbar.SemiCircleArcProgressBar;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    boolean locationPermissionGranted = false;
    boolean fusedLocationIsOn = false;

    private RequestQueue mRequestQueue;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_CODE_SEARCH = 1;

    TextView myCity;

    HashMap<String, Integer> drawableMap;

    private String latitudeKey = "latitude";
    private String longitudeKey = "longitude";
    private String CitySaved = "CitySaved";
    private String CityName;
    private SharedPreferences sharedPreferences;
    private double latitude_s;
    private double longitude_s;
    boolean myLocationClicked=true;
    private  String myLocationClickedKey ="myLocationClickedKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //night mode disable
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myCity = findViewById(R.id.myCity);
        //for edditing location
        myCity.setOnLongClickListener(view->{
            vibrate();
            Intent searchIntent = new Intent(MainActivity.this, Search.class);
            startActivityForResult(searchIntent, REQUEST_CODE_SEARCH);
            return true;
        });

        //get resources
        getRes();

        //get previous saved data
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Retrieve the latitude and longitude values from SharedPreferences
        latitude_s = Double.parseDouble(sharedPreferences.getString(latitudeKey, "0"));
        longitude_s = Double.parseDouble(sharedPreferences.getString(longitudeKey, "0"));
        myLocationClicked = sharedPreferences.getBoolean(myLocationClickedKey, true);
        CityName = sharedPreferences.getString(CitySaved,"Your City");
        try {
            retrieveData(CityName);
        }catch (Exception e){
            e.printStackTrace();
        }

        //set black color for status bar icons
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            int flags = decorView.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(flags);
        }

        //volley initialisation
        mRequestQueue = Volley.newRequestQueue(this);

        //checking which one to use current location or other location previously searched
        if(myLocationClicked){
            //for location permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                startLocationUpdates();
            } else {
                // Request location permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else {
            parseWeatherData(String.valueOf(latitude_s),String.valueOf(longitude_s));
            getCityName(latitude_s, longitude_s);
            myCity.setText(CityName);
        }

        //today's date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
        String currentDate = dateFormat.format(new Date());
        TextView date = findViewById(R.id.date);
        date.setText(currentDate);

    }

    private void getRes() {
        drawableMap = new HashMap<>();
        drawableMap.put("day/113", R.drawable.d113);
        drawableMap.put("day/116.png", R.drawable.d116);
        drawableMap.put("day/119.png", R.drawable.d119);
        drawableMap.put("day/122.png", R.drawable.d122);
        drawableMap.put("day/143.png", R.drawable.d143);
        drawableMap.put("day/176.png", R.drawable.d176);
        drawableMap.put("day/179.png", R.drawable.d179);
        drawableMap.put("day/182.png", R.drawable.d182);
        drawableMap.put("day/185.png", R.drawable.d185);
        drawableMap.put("day/200.png", R.drawable.d200);
        drawableMap.put("day/227.png", R.drawable.d227);
        drawableMap.put("day/230.png", R.drawable.d230);
        drawableMap.put("day/248.png", R.drawable.d248);
        drawableMap.put("day/260.png", R.drawable.d260);
        drawableMap.put("day/263.png", R.drawable.d263);
        drawableMap.put("day/266.png", R.drawable.d266);
        drawableMap.put("day/284.png", R.drawable.d284);
        drawableMap.put("day/281.png", R.drawable.d281);
        drawableMap.put("day/293.png", R.drawable.d293);
        drawableMap.put("day/296.png", R.drawable.d296);
        drawableMap.put("day/299.png", R.drawable.d299);
        drawableMap.put("day/302.png", R.drawable.d302);
        drawableMap.put("day/305.png", R.drawable.d305);
        drawableMap.put("day/308.png", R.drawable.d308);
        drawableMap.put("day/311.png", R.drawable.d311);
        drawableMap.put("day/314.png", R.drawable.d314);
        drawableMap.put("day/317.png", R.drawable.d317);
        drawableMap.put("day/320.png", R.drawable.d320);
        drawableMap.put("day/323.png", R.drawable.d323);
        drawableMap.put("day/326.png", R.drawable.d326);
        drawableMap.put("day/329.png", R.drawable.d329);
        drawableMap.put("day/332.png", R.drawable.d332);
        drawableMap.put("day/335.png", R.drawable.d335);
        drawableMap.put("day/338.png", R.drawable.d338);
        drawableMap.put("day/350.png", R.drawable.d350);
        drawableMap.put("day/353.png", R.drawable.d353);
        drawableMap.put("day/359.png", R.drawable.d359);
        drawableMap.put("day/365.png", R.drawable.d365);
        drawableMap.put("day/356.png", R.drawable.d356);
        drawableMap.put("day/362.png", R.drawable.d362);
        drawableMap.put("day/368.png", R.drawable.d368);
        drawableMap.put("day/371.png", R.drawable.d371);
        drawableMap.put("day/377.png", R.drawable.d377);
        drawableMap.put("day/386.png", R.drawable.d386);
        drawableMap.put("day/389.png", R.drawable.d389);
        drawableMap.put("day/392.png", R.drawable.d392);
        drawableMap.put("day/395.png", R.drawable.d395);
        drawableMap.put("day/374.png", R.drawable.d374);

        drawableMap.put("night/113.png", R.drawable.n113);
        drawableMap.put("night/116.png", R.drawable.n116);
        drawableMap.put("night/119.png", R.drawable.n119);
        drawableMap.put("night/122.png", R.drawable.n122);
        drawableMap.put("night/143.png", R.drawable.n143);
        drawableMap.put("night/176.png", R.drawable.n176);
        drawableMap.put("night/179.png", R.drawable.n179);
        drawableMap.put("night/182.png", R.drawable.n182);
        drawableMap.put("night/185.png", R.drawable.n185);
        drawableMap.put("night/200.png", R.drawable.n200);
        drawableMap.put("night/227.png", R.drawable.n227);
        drawableMap.put("night/230.png", R.drawable.n230);
        drawableMap.put("night/248.png", R.drawable.n248);
        drawableMap.put("night/260.png", R.drawable.n260);
        drawableMap.put("night/263.png", R.drawable.n263);
        drawableMap.put("night/266.png", R.drawable.n266);
        drawableMap.put("night/284.png", R.drawable.n284);
        drawableMap.put("night/281.png", R.drawable.n281);
        drawableMap.put("night/293.png", R.drawable.n293);
        drawableMap.put("night/296.png", R.drawable.n296);
        drawableMap.put("night/299.png", R.drawable.n299);
        drawableMap.put("night/302.png", R.drawable.n302);
        drawableMap.put("night/305.png", R.drawable.n305);
        drawableMap.put("night/308.png", R.drawable.n308);
        drawableMap.put("night/311.png", R.drawable.n311);
        drawableMap.put("night/314.png", R.drawable.n314);
        drawableMap.put("night/317.png", R.drawable.n317);
        drawableMap.put("night/320.png", R.drawable.n320);
        drawableMap.put("night/323.png", R.drawable.n323);
        drawableMap.put("night/326.png", R.drawable.n326);
        drawableMap.put("night/329.png", R.drawable.n329);
        drawableMap.put("night/332.png", R.drawable.n332);
        drawableMap.put("night/335.png", R.drawable.n335);
        drawableMap.put("night/338.png", R.drawable.n338);
        drawableMap.put("night/350.png", R.drawable.n350);
        drawableMap.put("night/353.png", R.drawable.n353);
        drawableMap.put("night/359.png", R.drawable.n359);
        drawableMap.put("night/365.png", R.drawable.n365);
        drawableMap.put("night/356.png", R.drawable.n356);
        drawableMap.put("night/362.png", R.drawable.n362);
        drawableMap.put("night/368.png", R.drawable.n368);
        drawableMap.put("night/371.png", R.drawable.n371);
        drawableMap.put("night/377.png", R.drawable.n377);
        drawableMap.put("night/386.png", R.drawable.n386);
        drawableMap.put("night/389.png", R.drawable.n389);
        drawableMap.put("night/392.png", R.drawable.n392);
        drawableMap.put("night/395.png", R.drawable.n395);
        drawableMap.put("night/374.png", R.drawable.n374);
    }

    public void startLocationUpdates() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    fusedLocationIsOn = true;
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    parseWeatherData(String.valueOf(latitude), String.valueOf(longitude));
                    //here setting current location on opening true
                    saveLocationToSharedPreferences(String.valueOf(latitude), String.valueOf(longitude),true,getCityName(latitude, longitude));
                }
            }
        };

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // Update interval in milliseconds
        locationRequest.setFastestInterval(5000); // Fastest update interval in milliseconds
        locationRequest.setSmallestDisplacement(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void saveLocationToSharedPreferences(String latitude, String longitude, Boolean myLocationClickedTF,String City) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(latitudeKey, latitude);
        editor.putString(longitudeKey, longitude);
        editor.putBoolean(myLocationClickedKey, myLocationClickedTF);
        editor.putString(CitySaved,City);
        editor.apply();
    }

    public void parseWeatherData(String latitude, String longitude) {
        String url = "https://api.weatherapi.com/v1/forecast.json?key="+AppConfig.weatherAPIKey+"&q=" + latitude + "," + longitude + "&aqi=yes&days=3";
        System.out.println(url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response);
                        saveJsonResponseToFile(response);
                        parseJSON(response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mRequestQueue.add(request);
    }

    private void saveJsonResponseToFile(JSONObject json) {
        try {
            String jsonString = json.toString();
            FileOutputStream fileOutputStream = openFileOutput("response.json", Context.MODE_PRIVATE);
            fileOutputStream.write(jsonString.getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readJsonResponseFromFile() {
        String json = null;
        try {
            FileInputStream fileInputStream = openFileInput("response.json");
            byte[] data = new byte[fileInputStream.available()];
            fileInputStream.read(data);
            json = new String(data, StandardCharsets.UTF_8);
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("JSON : "+json);
        return json;
    }

    private void retrieveData(String city){
        myCity.setText(city);
        String jsonSavedData = readJsonResponseFromFile();
        if(jsonSavedData!=null){
            System.out.println("JSON : "+jsonSavedData);
            parseJSON(jsonSavedData);
        }
    }

    private void parseJSON(String jsonData){
        try {
            JSONObject response = new JSONObject(jsonData);
            List<String> hour_new = new ArrayList<>(); //houly list for 24h
            List<String> temp_c_list = new ArrayList<>(); //corresponding temp list for 24h
            List<Integer> weather_icons = new ArrayList<>();//corresponding icons
            List<String> temperature_daily = new ArrayList<>();
            List<Integer> daily_icons = new ArrayList<>();
            List<String> day_daily = new ArrayList<>();


            //current
            JSONObject current = response.getJSONObject("current");
            String tempC = current.getString("temp_c").split("\\.")[0];
            LoaderTextView temperature = findViewById(R.id.temperatute);
            temperature.setText(tempC + "°C");
            temp_c_list.add(tempC + "°C");
            Date currentTime = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.US);
            String formattedTime = sdf.format(currentTime); //Current Time - will use if required
            hour_new.add("Now");

            //big weather icon
            JSONObject condition_curr = current.getJSONObject("condition");
            String curr_icon = condition_curr.getString("icon");
            String imgId = curr_icon.split("/")[5]+"/"+curr_icon.split("/")[6];
            System.out.println(imgId);
            ImageView main_weather_icon = findViewById(R.id.main_weather_icon);
            Integer resourceId = drawableMap.get(imgId);
            if(resourceId!=null){
                main_weather_icon.setImageResource(resourceId);
                weather_icons.add(resourceId);
                Picasso.get()
                        .load(resourceId)
                        .resize(96, 96)
                        .centerCrop()
                        .into(main_weather_icon);
            }



            //wind speed
            int wind_km = current.getInt("wind_kph");
            TextView wind_kph = findViewById(R.id.wind_kph);
            wind_kph.setText(wind_km + "km/h");

            //cloud
            int cloud = current.getInt("cloud");
            TextView cld = findViewById(R.id.cloud);
            cld.setText(cloud + "%");

            //humidity
            int humidity = current.getInt("humidity");
            TextView humid = findViewById(R.id.humidity);
            humid.setText(humidity + "%");

            //air quality aqi
            JSONObject air_q = current.getJSONObject("air_quality");
            try{
                double air_quality = air_q.getDouble("pm2_5");
                double prog = air_quality * 1.5;
                String aq = String.valueOf(air_quality).split("\\.")[0];
                SemiCircleArcProgressBar progressBar = findViewById(R.id.arcProgress);
                if ((int) air_quality >= 60 && (int) air_quality < 100) {
                    progressBar.setProgressBarColor(Color.parseColor("#F8EA9F30"));
                } else if ((int) air_quality >= 100) {
                    progressBar.setProgressBarColor(Color.parseColor("#FF0000"));
                }else if((int) air_quality < 60){
                    progressBar.setProgressBarColor(Color.parseColor("#3c7acf"));
                }
                progressBar.setPercentWithAnimation((int) prog);
                TextView aqi = findViewById(R.id.aqi);
                aqi.setText(aq);
            }catch (Exception e){
                e.printStackTrace();
                TextView aqi = findViewById(R.id.aqi);
                aqi.setText("N/A");
                SemiCircleArcProgressBar progressBar = findViewById(R.id.arcProgress);
                progressBar.setPercentWithAnimation((150));
            }
            
            //feels like
            int feels_like = current.getInt("feelslike_c");
            TextView feelsLike = findViewById(R.id.feels_like);
            feelsLike.setText("Feels Like : " + String.valueOf(feels_like) + "°C");

            //uv value
            String uv = current.getString("uv");
            TextView uv_index = findViewById(R.id.uv);
            uv_index.setText("UV Index : " + uv);


            //3 days string
            Calendar calendar = Calendar.getInstance();
            //today
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            String today = getDayName(dayOfWeek); //if required, will use
            //tomorrow's day
            calendar.add(Calendar.DAY_OF_WEEK, 1);
            int tomorrowDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            String tomorrow = getDayName(tomorrowDayOfWeek);
            //day after tomorrow
            calendar.add(Calendar.DAY_OF_WEEK, 1);
            int dayAfterTomorrowDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            String dayAfterTomorrow = getDayName(dayAfterTomorrowDayOfWeek);
            day_daily.add("Today");
            day_daily.add("Tomorrow");
            day_daily.add(dayAfterTomorrow);


            JSONObject forecast = response.getJSONObject("forecast");
            JSONArray forecastday = forecast.getJSONArray("forecastday");


            //forcast data (daily + hourly)
            for (int i = 0; i < 3; i++) {
                JSONObject single_day = forecastday.getJSONObject(i);
                JSONObject detail = single_day.getJSONObject("day");

                //min max temp
                int maxtemp_c = detail.getInt("maxtemp_c");
                int mintemp_c = detail.getInt("mintemp_c");
                temperature_daily.add(mintemp_c + "°C/" + maxtemp_c + "°C");


                //daily image
                JSONObject daily_image = detail.getJSONObject("condition");
                String daily_image_url = daily_image.getString("icon");
                String daily_img_id = daily_image_url.split("/")[5]+"/"+daily_image_url.split("/")[6];
                Integer DailyresourceId = drawableMap.get(daily_img_id);
                if(DailyresourceId!=null){
                    daily_icons.add(DailyresourceId);
                }

                //hourly data
                JSONArray hour = single_day.getJSONArray("hour");
                for (int j = 0; j < hour.length(); j++) {
                    JSONObject single_hour = hour.getJSONObject(j);
                    String datetime = single_hour.getString("time");
                    int tem_p = single_hour.getInt("temp_c");
                    JSONObject condition = single_hour.getJSONObject("condition");
                    String weather_icon_url = condition.getString("icon");
                    String hourlyImgIds = weather_icon_url.split("/")[5]+"/"+weather_icon_url.split("/")[6];
                    Integer HourlyresourceIds = drawableMap.get(hourlyImgIds);
                    if(HourlyresourceIds!=null){
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                            LocalDateTime dateTime1 = LocalDateTime.parse(datetime, formatter);
                            LocalDateTime currentDateTime = LocalDateTime.now();
                            if (dateTime1.isAfter(currentDateTime) || dateTime1.equals(currentDateTime)) {
                                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
                                try {
                                    Date Onlydate = inputFormat.parse(datetime);
                                    SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.US);
                                    if(Onlydate!=null){
                                        String time = outputFormat.format(Onlydate);
                                        hour_new.add(time);
                                    }
                                    temp_c_list.add(tem_p + "°C");
                                    weather_icons.add(HourlyresourceIds);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    }


                }

            }

            //hourly recycler view
            RecyclerView recyclerView_hourly = findViewById(R.id.hourly_recycler);
            LinearLayoutManager layoutManager_h = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
            recyclerView_hourly.setLayoutManager(layoutManager_h);
            HourlyAdapter hourlyAdapter = new HourlyAdapter(temp_c_list.subList(0, 25), hour_new.subList(0, 25), weather_icons.subList(0, 25));
            recyclerView_hourly.setAdapter(hourlyAdapter);
            //loading animation for hourly recycler
            LoaderTextView loading = findViewById(R.id.loading);
            loading.setVisibility(View.GONE);
            recyclerView_hourly.setVisibility(View.VISIBLE);

            // daily recycler view
            RecyclerView recyclerView_daily = findViewById(R.id.daily_recycler);
            LinearLayoutManager layoutManager_d = new LinearLayoutManager(MainActivity.this);
            recyclerView_daily.setLayoutManager(layoutManager_d);
            //disable swipe animation in recyclerview
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(MainActivity.this, R.drawable.divider);
            recyclerView_daily.addItemDecoration(dividerItemDecoration);
            //continue setting
            DailyAdapter adapter = new DailyAdapter(temperature_daily, day_daily, daily_icons);
            recyclerView_daily.setAdapter(adapter);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getCityName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                String city = addresses.get(0).getLocality();
                String country = addresses.get(0).getCountryName();
                myCity.setText(city+", "+country);
                return (city+", "+country);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED ||
                    grantResults.length > 0 &&
                            grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Location permissions granted
                locationPermissionGranted = true;
                startLocationUpdates();
            } else {
                // Location permissions denied
                Toast.makeText(this, "Location permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static String getDayName(int dayOfWeek) {
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thurs", "Fri", "Sat"};
        return dayNames[dayOfWeek - 1];
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            // For devices running on older versions of Android
            vibrator.vibrate(100);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause location updates
        if(fusedLocationIsOn){
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SEARCH && resultCode == RESULT_OK && data != null) {
            // Retrieve the selected city from the Search activity
            String selectedCity = data.getStringExtra("selectedCity");
            String lat_long = data.getStringExtra("lat_long");

            if(selectedCity.equals("myLocation")){
                startLocationUpdates();
            }else{
                if (lat_long != null) {
                    String[] lati_longi =lat_long.split(",");
                    System.out.println(lat_long);
                    parseWeatherData(lati_longi[0],lati_longi[1]);
                    saveLocationToSharedPreferences(lati_longi[0],lati_longi[1],false,selectedCity);
                    myCity.setText(selectedCity);
                }
            }
        }
    }



}
