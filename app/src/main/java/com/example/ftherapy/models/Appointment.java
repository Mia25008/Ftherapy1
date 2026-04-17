package com.example.ftherapy.models;

public class Appointment {
    private String appointmentId;
    private String userId;
    private String userName;
    private String treatmentType;
    private String date;
    private String time;

    public Appointment() {}

    public Appointment(String appointmentId, String userId, String userName, String treatmentType, String date, String time) {
        this.appointmentId = appointmentId;
        this.userId = userId;
        this.userName = userName;
        this.treatmentType = treatmentType;
        this.date = date;
        this.time = time;
    }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getTreatmentType() { return treatmentType; }
    public void setTreatmentType(String treatmentType) { this.treatmentType = treatmentType; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}