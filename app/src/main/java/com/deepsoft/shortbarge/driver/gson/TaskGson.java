package com.deepsoft.shortbarge.driver.gson;

import java.util.List;

public class TaskGson {

    private String code;
    private String msg;
    private List<DataDTO> data;
    private Boolean success;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<DataDTO> getData() {
        return data;
    }

    public void setData(List<DataDTO> data) {
        this.data = data;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public static class DataDTO {
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
}
