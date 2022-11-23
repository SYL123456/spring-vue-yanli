package com.example.demo.entity;

import java.io.Serializable;

public class AttitudeResp implements Serializable {

    private static final long serialVersionUID = 437620243132784926L;

    // 2
    private String task;

    // 0.4231323
    private String score;

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}