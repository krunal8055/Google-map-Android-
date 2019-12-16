package com.example.gmapdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Map extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;
    private static final int LOCATION_REQUEST_CODE =101 ;
    Button Back_Home,Navigate,Zoomin,Zoomout;
    //GoogleMap map;
    String location2;
    LatLng latLng;
    GoogleMap googleMap;
    String sourceLatitude,sourceLongitude,destinationLatitude,destinationLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(Map.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Map.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }
        fetchCurrentLocation();

        Back_Home = findViewById(R.id.back_home_btn);
        Navigate = findViewById(R.id.back_Nav_btn);
        Zoomin = findViewById(R.id.zoomin);
        Zoomout = findViewById(R.id.zoomout);
        Back_Home.setOnClickListener( this);
        Navigate.setOnClickListener(this);
        Zoomin.setOnClickListener(this);
        Zoomout.setOnClickListener(this);
    }

    private void fetchCurrentLocation() {
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    Toast.makeText(Map.this,currentLocation.getLatitude()+" "+currentLocation.getLongitude(),Toast.LENGTH_SHORT).show();
                    SupportMapFragment supportMapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
                    supportMapFragment.getMapAsync(Map.this);
                }else{
                    Toast.makeText(Map.this,"No Location recorded",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResult) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchCurrentLocation();
                } else {
                    Toast.makeText(Map.this,"Location permission missing",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    @Override
    public void onClick(View v) {
        if(v == Back_Home)
        {
            Intent i = new Intent(this,MainActivity.class);
            startActivity(i);
            finish();
        }
        else if(v == Navigate)
        {

            String uri = "http://maps.google.com/maps?saddr=" + sourceLatitude + "," + sourceLongitude + "&daddr=" + destinationLatitude + "," + destinationLongitude;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        List<Address> addresseslist = null;

        //Source CO ORDINATION
        sourceLatitude = String.valueOf(currentLocation.getLatitude());
        sourceLongitude = String.valueOf(currentLocation.getLongitude());

        LatLng C_latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(C_latLng);
        googleMap.addMarker(markerOptions);
        googleMap.setMyLocationEnabled(true);
        Intent i = getIntent();

        if(!i.getStringExtra("L2").isEmpty())
        {
            location2 = i.getStringExtra("L2");
            Geocoder geocoder = new Geocoder(this);
            try{
                addresseslist = geocoder.getFromLocationName(location2,1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addresseslist.get(0);
            latLng = new LatLng(address.getLatitude(),address.getLongitude());
            googleMap.addMarker( new MarkerOptions().position(latLng).title("Destination"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,18));
            String url = getRequesturl(C_latLng,latLng);

            TaskRequestDirection taskRequestDirection = new TaskRequestDirection();
            taskRequestDirection.execute(url);
            //destination CO ORDINATION
            destinationLatitude = String.valueOf(address.getLatitude());
            destinationLongitude = String.valueOf(address.getLongitude());

            //Toast.makeText(Map.this, location2, Toast.LENGTH_LONG).show();
        }

        //Polylione Direct from Current location to Destination

       /* googleMap.addPolyline(new PolylineOptions()
                .add(C_latLng,
                latLng)
                .width(5)
                .color(Color.BLUE));*/

        addresseslist.clear();

        Zoomin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMap.moveCamera(CameraUpdateFactory.zoomIn());
            }
        });
        Zoomout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMap.moveCamera(CameraUpdateFactory.zoomOut());
            }
        });



    }

    private String getRequesturl(LatLng c_latLng, LatLng latLng) {
        String str_org = "origin="+c_latLng.latitude+","+c_latLng.longitude;
        Log.i("Origin:",str_org);

        String str_dest = "destination="+latLng.latitude+","+latLng.longitude;
        Log.i("Destination:",str_dest);

        String sensor = "sensor=false";

        String mode = "mode=driving";

        String param = str_org+"&"+str_dest+"&"+sensor+"&"+mode;
        Log.i("Param:",param);

        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+param+"&key="+"AIzaSyAzsaYsOeEnry_dTlo3p8EIWgRIG4Lgeg0";
        Log.i("FINAL URL:",url);
        return url;
    }

    public String requestDirection(String requrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try{
            URL url = new URL(requrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";

            while ((line = bufferedReader.readLine()) != null)
            {
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();



        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(inputStream!= null)
            {
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return responseString;
    }

    public class TaskRequestDirection extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            TaskParse taskParse = new TaskParse();
            taskParse.execute(s);
        }
    }

    public class TaskParse extends AsyncTask< String,Void,List<List<HashMap<String,String>>> >
    {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String,String>>> routes = null;
            try{
                jsonObject = new JSONObject(strings[0]);
                DirectionParse directionParse = new DirectionParse();
                routes = directionParse.parse(jsonObject);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            super.onPostExecute(lists);
            ArrayList points = null;
            PolylineOptions polylineOptions = null;
            for (List<HashMap<String,String>> paths:lists)
            {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                for(HashMap<String,String> point : paths)
                {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat,lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }
                googleMap.addPolyline(polylineOptions);
        }
    }

}
