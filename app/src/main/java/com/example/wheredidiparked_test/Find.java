package com.example.wheredidiparked_test;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Find extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gmap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private String carName;
    private LatLng carLocation;
    private FloatingActionButton button;
    private DatabaseHelper DBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_find);

        button = findViewById(R.id.floatingActionButton);
        button.setOnClickListener(v -> carFound());

        Intent intent = getIntent();
        double carLatitude = Double.longBitsToDouble(intent.getLongExtra("latitude", 0));
        double carLongitude = Double.longBitsToDouble(intent.getLongExtra("longitude", 0));
        carName = intent.getStringExtra("carName");

        carLocation = new LatLng(carLatitude, carLongitude);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    LatLng currentLocation = new LatLng(locationResult.getLastLocation().getLatitude(),
                            locationResult.getLastLocation().getLongitude());
                    updateMapWithCurrentLocation(currentLocation);
                }
            }
        };

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.id_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gmap = googleMap;

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(carLocation, 12));
        googleMap.getUiSettings().setZoomGesturesEnabled(true);

        startLocationUpdates();
    }

    private void updateMapWithCurrentLocation(LatLng currentLocation) {
        gmap.clear();
        gmap.addMarker(new MarkerOptions()
                .position(carLocation)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.front_car))
                .title(carName + " is parked here."));
        gmap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.blue_dot))
                .title("You are here."));
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    100);
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void carFound() {
        DBHelper = new DatabaseHelper(this);
        DBHelper.invalidateLocation(carName);
        Toast.makeText(Find.this, "Car found. Current location of your car invalidated.", Toast.LENGTH_LONG).show();
        finish();

    }
}