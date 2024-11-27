package com.example.wheredidiparked_test;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private TextView carNameTextView;
    private Button leftArrowButton, rightArrowButton;
    private List<Car> cars;
    private int currentCarIndex = -1;
    private Button buttonParked;
    private Button buttonFindCar;
    private Button buttonAbout;
    private Button buttonHistory;
    private Button buttonSettings;
    private Button addCarButton;
    private LocationManager locationManager;
    private SharedPreferences sharedPreferences;
    private ImageView carImage;
    private DatabaseHelper dbHelper;
    private Location currentLocation;

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        buttonParked = findViewById(R.id.buttonParked);
        buttonFindCar = findViewById(R.id.buttonFind);
        buttonAbout = findViewById(R.id.buttonAbout);
        carNameTextView = findViewById(R.id.carNameTextView);
        leftArrowButton = findViewById(R.id.leftArrowButton);
        rightArrowButton = findViewById(R.id.rightArrowButton);
        buttonHistory = findViewById(R.id.buttonHistory);
        buttonSettings = findViewById(R.id.buttonSettings);
        buttonAbout = findViewById(R.id.buttonAbout);
        addCarButton = findViewById(R.id.buttonAddCar);
        carImage = findViewById(R.id.imageView);

        dbHelper = new DatabaseHelper(this);
        sharedPreferences = getSharedPreferences("CarAppPreferences", MODE_PRIVATE);
        currentCarIndex = sharedPreferences.getInt("lastViewedCarIndex", 0);

        leftArrowButton.setOnClickListener(v -> showPreviousCar());
        rightArrowButton.setOnClickListener(v -> showNextCar());
        addCarButton.setOnClickListener(v -> openAddCarActivity());
        buttonHistory.setOnClickListener(v-> showHistory());
        buttonSettings.setOnClickListener(v -> showSettings());
        buttonFindCar.setOnClickListener(v -> findCar());
        buttonAbout.setOnClickListener(v -> showAbout());
        buttonParked.setOnClickListener(v -> saveLocation());

        if(ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES
            }, 100);
        }

        startLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCars();
        if (cars.isEmpty()) {
            showAddCarState();
        } else {
            try {
                showCar(currentCarIndex);
            } catch (Exception e) {
                showCar(cars.size() - 1);
            }

            addCarButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getSharedPreferences("CarAppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("lastViewedCarIndex", currentCarIndex);
        editor.apply();
    }

    private void openAddCarActivity() {
        Intent intent = new Intent(this, addCar.class);
        startActivity(intent);
    }

    @SuppressLint("Range")
    private void findCar() {
        Cursor cursor = dbHelper.getCurrentLocation(carNameTextView.getText().toString());
        if (cursor.moveToFirst()) {
            long latitude = cursor.getLong(cursor.getColumnIndex("latitude"));
            long longitude = cursor.getLong(cursor.getColumnIndex("longitude"));

            Intent intent = new Intent(MainActivity.this, Find.class);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            intent.putExtra("carName", carNameTextView.getText().toString());
            startActivity(intent);
        } else {
            Toast.makeText(MainActivity.this, "No current location of your car is saved.", Toast.LENGTH_LONG).show();
        }
    }

    private void showHistory() {
        List<com.example.wheredidiparked_test.Location> locations = dbHelper.getLocationHistoryForCar(carNameTextView.getText().toString());
        if(locations.isEmpty()) {
            Toast.makeText(MainActivity.this, "Parking history of this car is empty.", Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(MainActivity.this, ShowHistory.class);
            intent.putExtra("carName", carNameTextView.getText().toString());
            startActivity(intent);
        }
    }

    private void showSettings() {
        Intent intent = new Intent(MainActivity.this, Settings.class);
        intent.putExtra("carName", carNameTextView.getText().toString());
        startActivity(intent);
    }

    private void showAbout() {
        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(intent);
    }

    @SuppressLint("Range")
    private void loadCars() {
        cars = new ArrayList<>();
        Cursor cursor = dbHelper.getAllCars();
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME));
                String imagePath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_IMAGE));
                cars.add(new Car(id, name, imagePath));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void showCar(int index) {
        currentCarIndex = index;
        Car car = cars.get(index);
        carNameTextView.setText(car.getName());
        leftArrowButton.setEnabled(index > 0);
        rightArrowButton.setEnabled(true);
        carImage.setImageURI(Uri.parse(car.getImagePath()));
        setButtonsEnabled(true);
    }

    private void showAddCarState() {
        carNameTextView.setText(R.string.add_new_car);
        setButtonsEnabled(false);
        addCarButton.setVisibility(View.VISIBLE);
        leftArrowButton.setEnabled(!cars.isEmpty());
        rightArrowButton.setEnabled(false);
    }

    private void setButtonsEnabled(boolean enabled) {
        buttonParked.setEnabled(enabled);
        buttonFindCar.setEnabled(enabled);
        buttonHistory.setEnabled(enabled);
        buttonSettings.setEnabled(enabled);
    }

    private void showPreviousCar() {
        if (currentCarIndex > 0) {
            if(addCarButton.getVisibility() == View.GONE) {
                showCar(currentCarIndex - 1);
            }
            addCarButton.setVisibility(View.GONE);
            showCar(currentCarIndex);
        } else {
            showCar(currentCarIndex);
            addCarButton.setVisibility(View.GONE);
        }
    }

    private void showNextCar() {
        if (currentCarIndex < cars.size() - 1) {
            showCar(currentCarIndex + 1);
        } else {
            showAddCarState();
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, MainActivity.this);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "GPS location unavailable at this moment, please try again shortly.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLocation = location;
    }

    @SuppressLint("MissingPermission")
    private void saveLocation() {
        if (currentLocation != null) {
            dbHelper.saveLocation(carNameTextView.getText().toString(),
                    Double.doubleToRawLongBits(currentLocation.getLatitude()),
                    Double.doubleToRawLongBits(currentLocation.getLongitude()));
            Toast.makeText(MainActivity.this, "Location saved.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, "Unable to save location. No GPS location available yet.", Toast.LENGTH_LONG).show();
        }
    }
}