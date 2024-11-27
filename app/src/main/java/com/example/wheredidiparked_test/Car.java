package com.example.wheredidiparked_test;

public class Car {
    private long id;
    private String name;
    private String imagePath;

    public Car(long id, String name, String imagePath) {
        this.id = id;
        this.name = name;
        this.imagePath = imagePath;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImagePath() {
        return imagePath;
    }
}