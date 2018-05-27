package com.example.unknow.bitafira.model;

import java.io.Serializable;

public class EvaluationActive implements Serializable {
    private String id;
    private String idEvaluation;
    private String idPacient;
    private String start_time;
    private boolean isEvaluation;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdEvaluation() {
        return idEvaluation;
    }

    public void setIdEvaluation(String idEvaluation) {
        this.idEvaluation = idEvaluation;
    }

    public String getIdPacient() {
        return idPacient;
    }

    public void setIdPacient(String idPacient) {
        this.idPacient = idPacient;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public boolean isEvaluation() {
        return isEvaluation;
    }

    public void setEvaluation(boolean evaluation) {
        isEvaluation = evaluation;
    }
}
