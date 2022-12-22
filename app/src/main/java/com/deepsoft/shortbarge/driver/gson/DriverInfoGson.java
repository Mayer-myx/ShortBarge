package com.deepsoft.shortbarge.driver.gson;

public class DriverInfoGson extends ResultGson {

    // 司机信息data
    private String driverId;
    private String truckId;
    private String licensePlate;
    private String emergencyContact;
    private String emergencyContactEng;
    private String emergencyPhone;

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getTruckId() {
        return truckId;
    }

    public void setTruckId(String truckId) {
        this.truckId = truckId;
    }

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
