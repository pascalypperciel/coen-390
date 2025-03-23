package com.example.minicapapp;

public class RecordedDataItem {
    private long session_id; // The session_id associated with the subject's profile.
    private String test_name; // The test's name.
    private String timestamp; // The test's date.
    private String test_type; // The type of test conducted (e.g., Compressive, Tensile, etc.)
    private String material_type; // The type of material that was tested, if known (e.g., Wood, Steel, etc.)

    public RecordedDataItem(long session_id, String test_name, String test_type, String material_type, String initial_length, String intial_area) {
        this.session_id = session_id;
        this.test_name = test_name;
        this.test_type = test_type;
        this.material_type = material_type;
    }

    public long getSessionID() {
        return session_id;
    }

    public void setSessionId(long session_id) {
        this.session_id = session_id;
    }

    public String getTestName() {
        return test_name;
    }

    public void setTestName(String test_name) {
        this.test_name = test_name;
    }

    public String getTestType() {
        return test_type;
    }

    public void setTestType(String test_type) {
        this.test_type = test_type;
    }

    public String getMaterialType() {
        return material_type;
    }

    public void setMaterialType(String material_type) {
        this.material_type = material_type;
    }
}