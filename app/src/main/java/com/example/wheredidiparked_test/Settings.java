package com.example.wheredidiparked_test;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;

public class Settings extends AppCompatActivity {

    private String carName;
    private String imageUri;
    private DatabaseHelper dbHelper;
    private Uri selectedImageUri;
    private TextView carNameSettings;
    private ImageView carImage;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        carNameSettings = findViewById(R.id.carNameSettings);
        carImage = findViewById(R.id.imageView3);
        carName = getIntent().getStringExtra("carName");
        imageUri = dbHelper.getCarImageUri(carName);
        carNameSettings.setText(carName);
        carImage.setImageURI(Uri.parse(imageUri));

        Button btnChangePhoto = findViewById(R.id.buttonChangeImage);
        Button btnDeleteHistory = findViewById(R.id.buttonDeleteHistory);
        Button btnDeleteVehicle = findViewById(R.id.buttonDeleteCar);

        btnChangePhoto.setOnClickListener(v -> openImagePicker());
        btnDeleteHistory.setOnClickListener(v -> deleteParkingHistory());
        btnDeleteVehicle.setOnClickListener(v -> confirmDeleteVehicle());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            String[] projection = {MediaStore.Images.Media.DATA};
            try (Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    imageUri = cursor.getString(columnIndex);
                    File imageFile = new File(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    carImage.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error occurred, cannot change image.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        dbHelper.updateCarPhoto(carName, imageUri);
    }

    private void openImagePicker() {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        }

    private void deleteParkingHistory() {
        String tableName = carName.replaceAll("\\s+", "_");
        dbHelper.deleteAllLocationsFromTable(tableName);
        Toast.makeText(this, "Parking history deleted successfully.", Toast.LENGTH_LONG).show();
    }

    private void confirmDeleteVehicle() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Vehicle")
                .setMessage("Are you sure you want to delete this vehicle?")
                .setPositiveButton("Yes", (dialog, which) -> deleteVehicle())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteVehicle() {
        String tableName = carName.replaceAll("\\s+", "_");
        dbHelper.deleteCar(carName, tableName);
        Toast.makeText(this, "Vehicle deleted successfully.", Toast.LENGTH_LONG).show();
        finish();
    }
}