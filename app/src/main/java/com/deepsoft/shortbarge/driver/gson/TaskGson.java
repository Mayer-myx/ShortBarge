package com.deepsoft.shortbarge.driver.gson;

public class TaskGson extends ResultGson {

    //任务信息data

    //任务状态：1-待运输;2-到达起始点(装货);3-运输中;4-延迟;5-到达经停站;6-继续运输;7-到达目的地(卸货);8-完成;9-异常换车
    //花费时间：例如“1,20”，逗号前是小时，逗号后是分钟；上述为1小时20分

    private String transportTaskId;
    private Integer state;
    private String startTime;
    private String arrivalTime;
    private String duration;
    private String nextStation;
    private String nextStationEng;

    public String getTransportTaskId() {
        return transportTaskId;
    }

    public void setTransportTaskId(String transportTaskId) {
        this.transportTaskId = transportTaskId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getNextStation() {
        return nextStation;
    }

    public void setNextStation(String nextStation) {
        this.nextStation = nextStation;
    }

    public String getNextStationEng() {
        return nextStationEng;
    }

    public void setNextStationEng(String nextStationEng) {
        this.nextStationEng = nextStationEng;
    }
}
