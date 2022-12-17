package com.deepsoft.shortbarge.driver.adapter.entity;

public class Task {
    //任务状态：1-待运输;2-到达起始点(装货);3-运输中;4-延迟;5-到达经停站;6-继续运输;7-到达目的地(卸货);8-完成;9-异常换车
    //花费时间：例如“1,20”，逗号前是小时，逗号后是分钟；上述为1小时20分
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
