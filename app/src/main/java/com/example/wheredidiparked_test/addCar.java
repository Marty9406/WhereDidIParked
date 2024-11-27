package com.example.wheredidiparked_test;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;

public class addCar extends AppCompatActivity {

    private EditText carNameEditText;
    private Button selectImageButton;
    private Button saveCarButton;
    private ImageView selectedImageView;
    private Uri selectedImageUri;
    private String imagePath;
    DatabaseHelper dbHelper;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_car);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        carNameEditText = findViewById(R.id.carNameEditText);
        selectImageButton = findViewById(R.id.selectImageButton);
        saveCarButton = findViewById(R.id.saveCarButton);
        selectedImageView = findViewById(R.id.selectedImageView);
        selectedImageUri = null;
        dbHelper = new DatabaseHelper(this);

        selectImageButton.setOnClickListener(v -> openImagePicker());
        saveCarButton.setOnClickListener(v -> saveCar());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            String[] projection = { MediaStore.Images.Media.DATA };
            try (Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    imagePath = cursor.getString(columnIndex);
                    File imageFile = new File(imagePath);
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    selectedImageView.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error occurred. Can't choose picture", Toast.LENGTH_LONG).show();
                finish();
            }
            selectedImageView.setVisibility(View.VISIBLE);
        }
    }

    private void saveCar() {
        String carName = carNameEditText.getText().toString().trim();

        if (carName.isEmpty()) {
            Toast.makeText(this, "Please enter name of your car.", Toast.LENGTH_LONG).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please choose an image.", Toast.LENGTH_LONG).show();
            return;
        }

        long id = dbHelper.addCar(carName, imagePath);
        if (id > 0) {
            dbHelper.createLocationTableForCar(carName);
            Toast.makeText(this, "New car saved.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Error occurred. Please try again.", Toast.LENGTH_LONG).show();
        }
    }
}