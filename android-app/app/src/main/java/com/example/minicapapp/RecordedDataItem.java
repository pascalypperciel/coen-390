package com.example.minicapapp;

public class RecordedDataItem {
    private long sessionID; // The sessionID associated with the subject's profile.
    private String sessionName; // The test's name.
    private String sessionTimestamp; // The test's date.
    private float initialLength; // The initial length of the test sample
    private float initialArea; // The initial cross-sectional area of the test sample

    public RecordedDataItem(long sessionID, String sessionName, float initialLength, float initialArea) {
        this.sessionID = sessionID;
        this.sessionName = sessionName;
        this.initialLength = initialLength;
        this.initialArea = initialArea;
    }

    public long getSessionID() {
        return sessionID;
    }

    public void setSessionID(long sessionID) {
        this.sessionID = sessionID;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getSessionTimestamp() {
        return sessionTimestamp;
    }

    public void setSessionTimestamp(String sessionTimestamp) {
        this.sessionTimestamp = sessionTimestamp;
    }

    public float getInitialLength() {
        return initialLength;
    }

    public void setInitialLength(float initialLength) {
        this.initialLength = initialLength;
    }

    public float getInitialArea() {
        return initialArea;
    }

    public void setInitialArea(float initialArea) {
        this.initialArea = initialArea;
    }
}