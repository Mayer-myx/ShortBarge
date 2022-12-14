package com.deepsoft.shortbarge.driver.adapter.entity;

public class Task {
    private int state;
    private String id, start_time, arrival_time, duration, next_station, next_station_eng;

    public Task(String id, int state, String start_time, String arrival_time, String duration, String next_station, String next_station_eng) {
        this.id = id;
        this.state = state;
        this.start_time = start_time;
        this.arrival_time = arrival_time;
        this.duration = duration;
        this.next_station = next_station;
        this.next_station_eng = next_station_eng;
    }

    public String getId() {
        return id;
    }

    public int getState() {
        return state;
    }

    public String getStart_time() {
        return start_time;
    }

    public String getArrival_time() {
        return arrival_time;
    }

    public String getDuration() {
        return duration;
    }

    public String getNext_station() {
        return next_station;
    }

    public String getNext_station_eng() {
        return next_station_eng;
    }
}
