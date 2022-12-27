package com.deepsoft.shortbarge.driver.gson;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

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

    public String getTaskState(String type){
        Map<Integer, String> res_zh = new HashMap<>();
        res_zh.put(1, "待运输");
        res_zh.put(2, "到达起始点(装货)");
        res_zh.put(3, "运输中");
        res_zh.put(4, "延迟");
        res_zh.put(5, "到达经停站");
        res_zh.put(6, "继续运输");
        res_zh.put(7, "到达目的地(卸货)");
        res_zh.put(8, "完成");
        res_zh.put(9, "异常换车");

        Map<Integer, String> res_en = new HashMap<>();
        res_en.put(1, "To be transported");
        res_en.put(2, "Arrival at the starting point (loading)");
        res_en.put(3, "In transit");
        res_en.put(4, "Delay");
        res_en.put(5, "Arrive at the stop");
        res_en.put(6, "Continue shipping");
        res_en.put(7, "Arrival at destination (unloading)");
        res_en.put(8, "Finish");
        res_en.put(9, "Abnormal car change");

        if(type.equals("2")) return res_zh.get(getState());
        else return res_en.get(getState());
    }

    public String getTaskDura(String type){
        String str = getDuration();
        int h = 0, min = 0, sub = str.indexOf(',');
        if(sub != -1){
            h = Integer.parseInt(str.substring(0, sub));
            min = Integer.parseInt(str.substring(sub));;
        }
        if(type.equals("1")) return String.valueOf(h*60+min)+" "+"minutes";
        else return String.valueOf(h*60+min)+" "+"分钟";
    }

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
