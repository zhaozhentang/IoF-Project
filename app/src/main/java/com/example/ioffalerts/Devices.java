package com.example.ioffalerts;

import android.bluetooth.BluetoothClass;

/**
 * Created by dknig on 5/10/2016.
 */
public class Devices {

    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    @com.google.gson.annotations.SerializedName("deviceName")
    private String deviceName;





    @com.google.gson.annotations.SerializedName("deviceInstructLocation")
    private String InstructLocation;

    @com.google.gson.annotations.SerializedName("deviceDescription")
    private String description;

    @com.google.gson.annotations.SerializedName("deviceMapLocation")
    private String mapLocation;


    @com.google.gson.annotations.SerializedName("deviceUniversityName")
    private String universityName;


    public Devices() {

    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeviceName() {
        return deviceName;
    }



    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getInstructLocation() {
        return InstructLocation;
    }

    public void setInstructLocation(String instructLocation) {
        InstructLocation = instructLocation;
    }

    public String getMapLocation() {
        return mapLocation;
    }

    public void setMapLocation(String mapLocation) {
        this.mapLocation = mapLocation;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getUniversityName() {
        return universityName;
    }

    public void setUniversityName(String universityName) {
        this.universityName = universityName;
    }
}
