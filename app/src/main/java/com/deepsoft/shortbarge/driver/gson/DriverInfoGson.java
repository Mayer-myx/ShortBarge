package com.deepsoft.shortbarge.driver.gson;

public class DriverInfoGson {

    //获取司机信息gson
    private Integer code;
    private String msg;
    private DataDTO data;
    private Boolean success;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataDTO getData() {
        return data;
    }

    public void setData(DataDTO data) {
        this.data = data;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public static class DataDTO {
        private String licensePlate;
        private String emergencyContact;
        private String emergencyContactEng;
        private String emergencyPhone;

        public String getLicensePlate() {
            return licensePlate;
        }

        public void setLicensePlate(String licensePlate) {
            this.licensePlate = licensePlate;
        }

        public String getEmergencyContact() {
            return emergencyContact;
        }

        public void setEmergencyContact(String emergencyContact) {
            this.emergencyContact = emergencyContact;
        }

        public String getEmergencyContactEng() {
            return emergencyContactEng;
        }

        public void setEmergencyContactEng(String emergencyContactEng) {
            this.emergencyContactEng = emergencyContactEng;
        }

        public String getEmergencyPhone() {
            return emergencyPhone;
        }

        public void setEmergencyPhone(String emergencyPhone) {
            this.emergencyPhone = emergencyPhone;
        }
    }
}
