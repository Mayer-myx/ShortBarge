package com.deepsoft.shortbarge.driver.bean;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class TaskGson extends ResultGson {
    //任务信息data

    //任务状态：1-待运输;2-到达起始点(装货);3-运输中;4-延迟;5-到达经停站;6-继续运输;7-到达目的地(卸货);8-完成;9-异常换车
    //花费时间：例如“1,20”，逗号前是小时，逗号后是分钟；上述为1小时20分

    private String transportTaskId;
    private Integer state;
    private boolean delay;
    private String startTime;
    private String arrivalTime;
    private String duration;
    private String stateDuration;
    private String nextStation;
    private String nextStationEng;
    private boolean stopOver;
    private boolean emergency;
    private String originLng;
    private String originLat;
    private String originFenceRange;
    private String originWarningRange;
    private String stopLng;
    private String stopLat;
    private String stopFenceRange;
    private String stopWarningRange;
    private String destinationLng;
    private String destinationLat;
    private String destinationFenceRange;
    private String destinationWarningRange;
    private String adminName;
    private String adminNameEng;
    private String adminPhone;


    public String getTaskState(String type){

        Map<Integer, String> res_zh = new HashMap<>();
        res_zh.put(1, "等待中");
        res_zh.put(2, "装卸中");
        res_zh.put(3, "运输中");

        res_zh.put(4, "延迟");
        res_zh.put(5, "到达经停站");
        res_zh.put(6, "继续运输");
        res_zh.put(7, "装卸中");
        res_zh.put(8, "完成");
        res_zh.put(9, "异常换车");

        Map<Integer, String> res_en = new HashMap<>();
        res_en.put(1, "Waiting");
        res_en.put(2, "Loading");
        res_en.put(3, "In Transit");

        res_en.put(4, "Delayed");
        res_en.put(5, "Arrive at the stop");
        res_en.put(6, "Continue shipping");
        res_en.put(7, "Loading");
        res_en.put(8, "Finish");
        res_en.put(9, "Abnormal car change");

        String res;
        if(type.equals("2")){
            res = res_zh.get(getState()) + "\t\t";
        } else {
            res = res_en.get(getState()) + "\t\t";
        }

        return res;
    }

    public String getTaskDura(String type){
        String str = getDuration();
        int h = 0, min = 0, sub = str.indexOf(',');
        if(sub != -1){
            if(sub == 0){
                h = 0;
            }else {
                h = Integer.parseInt(str.substring(0, sub));
            }
            min = Integer.parseInt(str.substring(sub+1));
        }
        if(type.equals("1")) return String.valueOf(h*60+min)+" "+"minutes";
        else return String.valueOf(h*60+min)+" "+"分钟";
    }

    public String getTransportTaskId() {
        BigDecimal bigDecimal = new BigDecimal(Double.parseDouble(transportTaskId));
        return bigDecimal.toPlainString();
    }

    public void setTransportTaskId(String transportTaskId) {
        this.transportTaskId = transportTaskId;
    }

    public Boolean getDelay() {
        return delay;
    }

    public void setDelay(Boolean delay) {
        this.delay = delay;
    }

    public String getNextStation() {
        String res = "null";
        if(nextStation != null){
            res = nextStation + "\t\t";
        }
        return res;
    }

    public void setNextStation(String nextStation) {
        this.nextStation = nextStation;
    }

    public String getNextStationEng() {
        String res = "null";
        if(nextStationEng != null){
            res = nextStationEng + "\t\t";
        }
        return res;
    }

    public String getTaskStateDuration(String type) {
        String str = getStateDuration();
        int h = 0, min = 0, sub = str.indexOf(',');
        if(sub != -1){
            if(sub == 0){
                h = 0;
            }else {
                h = Integer.parseInt(str.substring(0, sub));
            }
            min = Integer.parseInt(str.substring(sub+1));
        }
        if(type.equals("1")) return String.valueOf(h*60+min)+" "+"minutes";
        else return String.valueOf(h*60+min)+" "+"分钟";
    }

    public String getStateDuration() {
        return stateDuration;
    }

    public void setStateDuration(String stateDuration) {
        this.stateDuration = stateDuration;
    }

    public void setNextStationEng(String nextStationEng) {
        this.nextStationEng = nextStationEng;
    }

    public Boolean getStopOver() {
        return stopOver;
    }

    public void setStopOver(Boolean stopOver) {
        this.stopOver = stopOver;
    }

    public Boolean getEmergency() {
        return emergency;
    }

    public void setEmergency(Boolean emergency) {
        this.emergency = emergency;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public boolean isDelay() {
        return delay;
    }

    public void setDelay(boolean delay) {
        this.delay = delay;
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

    public boolean isStopOver() {
        return stopOver;
    }

    public void setStopOver(boolean stopOver) {
        this.stopOver = stopOver;
    }

    public boolean isEmergency() {
        return emergency;
    }

    public void setEmergency(boolean emergency) {
        this.emergency = emergency;
    }

    public String getOriginLng() {
        return originLng;
    }

    public void setOriginLng(String originLng) {
        this.originLng = originLng;
    }

    public String getOriginLat() {
        return originLat;
    }

    public void setOriginLat(String originLat) {
        this.originLat = originLat;
    }

    public String getOriginFenceRange() {
        return originFenceRange;
    }

    public void setOriginFenceRange(String originFenceRange) {
        this.originFenceRange = originFenceRange;
    }

    public String getOriginWarningRange() {
        return originWarningRange;
    }

    public void setOriginWarningRange(String originWarningRange) {
        this.originWarningRange = originWarningRange;
    }

    public String getStopLng() {
        return stopLng;
    }

    public void setStopLng(String stopLng) {
        this.stopLng = stopLng;
    }

    public String getStopLat() {
        return stopLat;
    }

    public void setStopLat(String stopLat) {
        this.stopLat = stopLat;
    }

    public String getStopFenceRange() {
        return stopFenceRange;
    }

    public void setStopFenceRange(String stopFenceRange) {
        this.stopFenceRange = stopFenceRange;
    }

    public String getStopWarningRange() {
        return stopWarningRange;
    }

    public void setStopWarningRange(String stopWarningRange) {
        this.stopWarningRange = stopWarningRange;
    }

    public String getDestinationLng() {
        return destinationLng;
    }

    public void setDestinationLng(String destinationLng) {
        this.destinationLng = destinationLng;
    }

    public String getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(String destinationLat) {
        this.destinationLat = destinationLat;
    }

    public String getDestinationFenceRange() {
        return destinationFenceRange;
    }

    public void setDestinationFenceRange(String destinationFenceRange) {
        this.destinationFenceRange = destinationFenceRange;
    }

    public String getDestinationWarningRange() {
        return destinationWarningRange;
    }

    public void setDestinationWarningRange(String destinationWarningRange) {
        this.destinationWarningRange = destinationWarningRange;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getAdminNameEng() {
        return adminNameEng;
    }

    public void setAdminNameEng(String adminNameEng) {
        this.adminNameEng = adminNameEng;
    }

    public String getAdminPhone() {
        return adminPhone;
    }

    public void setAdminPhone(String adminPhone) {
        this.adminPhone = adminPhone;
    }
}
