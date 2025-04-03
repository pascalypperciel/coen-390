package com.example.minicapapp;

import java.util.Date;

public class RecordedDataItem {
    private long sessionID; // The sessionID associated with the subject's profile.
    private String sessionName; // The test's name.
    private Date sessionTimestamp; // When the session was created.
    private float initialLength; // The initial length of the test sample
    private float initialArea; // The initial cross-sectional area of the test sample
    private float yieldStrain;
    private float yieldStress;

    public RecordedDataItem(long sessionID, String sessionName, Date sessionTimestamp, float initialLength, float initialArea, float yieldStrain, float yieldStress) {
        this.sessionID = sessionID;
        this.sessionName = sessionName;
        this.sessionTimestamp = sessionTimestamp;
        this.initialLength = initialLength;
        this.initialArea = initialArea;
        this.yieldStrain = yieldStrain;
        this.yieldStress = yieldStress;
    }

    public long getSessionID() {
        return sessionID;
    }

    public String getSessionName() {
        return sessionName;
    }

    public Date getSessionTimestamp() {
        return sessionTimestamp;
    }
}