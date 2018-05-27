package com.example.unknow.bitafira.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class Evaluation implements Serializable {

    private String id;
    private String dateStart;
    private String timeEvaluation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDateStart() {
        return dateStart;
    }

    public void setDateStart(String dateStart) {
        this.dateStart = dateStart;
    }

    public String getTimeEvaluation() {
        return timeEvaluation;
    }

    public void setTimeEvaluation(String timeEvaluation) {
        this.timeEvaluation = timeEvaluation;
    }

}
