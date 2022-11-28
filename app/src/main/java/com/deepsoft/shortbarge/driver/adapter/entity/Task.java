package com.deepsoft.shortbarge.driver.adapter.entity;

public class Task {
    private String start_time, arrival_time, destination, task_status;
    private boolean is_emergency;

    public Task(String start_time, String arrival_time, String destination, String task_status) {
        this.start_time = start_time;
        this.arrival_time = arrival_time;
        this.destination = destination;
        this.task_status = task_status;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getArrival_time() {
        return arrival_time;
    }

    public void setArrival_time(String arrival_time) {
        this.arrival_time = arrival_time;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getTask_status() {
        return task_status;
    }

    public void setTask_status(String task_status) {
        this.task_status = task_status;
    }

    public boolean getIs_emergency() {
        return is_emergency;
    }

    public void setIs_emergency(boolean is_emergency) {
        this.is_emergency = is_emergency;
    }
}
