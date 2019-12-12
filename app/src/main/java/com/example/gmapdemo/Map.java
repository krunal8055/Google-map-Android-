package com.example.gmapdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class Map extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;
    private static final int LOCATION_REQUEST_CODE =101 ;
    Button Back_Home,Zoomin,Zoomout;
    //GoogleMap map;
    String location2;
    LatLng latLng;

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
        Zoomin = findViewById(R.id.zoomin);
        Zoomout = findViewById(R.id.zoomout);
        Back_Home.setOnClickListener( this);
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
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        LatLng C_latLng = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(C_latLng);
        googleMap.addMarker(markerOptions);
        googleMap.setMyLocationEnabled(true);
        Intent i = getIntent();

        List<Address> addresseslist = null;

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
            Toast.makeText(Map.this, location2, Toast.LENGTH_LONG).show();
        }
        googleMap.addPolyline(new PolylineOptions().add(
                C_latLng,
                latLng
        ).width(5).color(Color.BLUE));
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

}
