package com.example.minicapapp;

public class RecordedDataItem {
    private long id; // The ID associated with the subject's profile.
    private String testName; // The test's name.
    private String timestamp; // The test's date.
    private String testType; // The type of test conducted (e.g., Compressive, Tensile, etc.)
    private String materialType; // The type of material that was tested, if known (e.g., Wood, Steel, etc.)

    public RecordedDataItem(long id, String testName, String timestamp, String testType, String materialType) {
        this.id = id;
        this.testName = testName;
        this.timestamp = timestamp;
        this.testType = testType;
        this.materialType = materialType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }
}