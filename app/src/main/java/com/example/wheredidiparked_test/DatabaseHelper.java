package com.example.wheredidiparked_test;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "cars.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_CARS = "cars";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_IMAGE = "image_path";

    private static final String CREATE_TABLE_CARS =
            "CREATE TABLE " + TABLE_CARS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_IMAGE + " TEXT);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CARS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARS);
        onCreate(db);
    }

    public void createLocationTableForCar(String carName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String tableName = carName.replaceAll("\\s+", "_");
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "latitude REAL NOT NULL, " +
                "longitude REAL NOT NULL, " +
                "is_current INTEGER NOT NULL DEFAULT 0" +
                ");";
        db.execSQL(createTableQuery);
    }

    public void saveLocation(String carName, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        String tableName = carName.replaceAll("\\s+", "_");

        String resetCurrentQuery = "UPDATE " + tableName + " SET is_current = 0;";
        db.execSQL(resetCurrentQuery);

        ContentValues values = new ContentValues();
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("is_current", 1);
        db.insert(tableName, null, values);
    }

    public void invalidateLocation(String carName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String tableName = carName.replaceAll("\\s+", "_");

        String resetCurrentQuery = "UPDATE " + tableName + " SET is_current = 0;";
        db.execSQL(resetCurrentQuery);
    }

    public Cursor getCurrentLocation(String carName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String tableName = carName.replaceAll("\\s+", "_");
        String query = "SELECT latitude, longitude FROM " + tableName + " WHERE is_current = 1;";
        return db.rawQuery(query, null);
    }

    @SuppressLint("Range")
    public List<Location> getLocationHistoryForCar(String carName) {
        List<Location> locations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String tableName = carName.replaceAll("\\s+", "_");

        Cursor cursor = db.rawQuery("SELECT latitude, longitude FROM " + tableName, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                double latitude = Double.longBitsToDouble(cursor.getLong(cursor.getColumnIndex("latitude")));
                double longitude = Double.longBitsToDouble(cursor.getLong(cursor.getColumnIndex("longitude")));
                locations.add(new Location(latitude, longitude));
            }
            cursor.close();
        }

        db.close();
        return locations;
    }

    public void updateCarPhoto(String carName, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_IMAGE, imagePath);

        db.update(TABLE_CARS, values, COLUMN_NAME + " = ?", new String[]{carName});

        db.close();
    }

    public void deleteAllLocationsFromTable(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tableName, null, null);
        db.close();
    }

    public void deleteCar(String carName, String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
        db.delete("cars", "name=?", new String[]{carName});
        db.close();
    }
    public String getCarImageUri(String carName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String imageUri = null;

        String[] columns = {COLUMN_IMAGE};
        String selection = COLUMN_NAME + " = ?";
        String[] selectionArgs = {carName};

        Cursor cursor = db.query(TABLE_CARS, columns, selection, selectionArgs,null,null,null);

        if (cursor != null && cursor.moveToFirst()) {
            imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE));
            cursor.close();
        }

        db.close();
        return imageUri;
    }

    public long addCar(String name, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_IMAGE, imagePath);
        long id = db.insert(TABLE_CARS, null, values);
        db.close();
        return id;
    }

    public Cursor getAllCars() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CARS, null, null, null, null, null, null);
    }
}