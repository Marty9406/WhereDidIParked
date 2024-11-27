package com.example.wheredidiparked_test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class ShowHistory extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gmap;
    private String carName;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_history);
        Intent intent = getIntent();
        carName = intent.getStringExtra("carName");
        dbHelper = new DatabaseHelper(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.id_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gmap = googleMap;

        List<Location> historyLocations = dbHelper.getLocationHistoryForCar(carName);
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        for (Location location : historyLocations) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            gmap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.orange_dot))
                    .title(carName +" was parked here."));
            boundsBuilder.include(latLng);
        }
        LatLngBounds bounds = boundsBuilder.build();
        gmap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        gmap.getUiSettings().setZoomGesturesEnabled(true);
    }
}