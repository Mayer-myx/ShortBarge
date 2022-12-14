package com.deepsoft.shortbarge.driver.gson;

import java.util.List;

public class UserInfoGson {
    // 获取用户详情gson
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
        private Integer id;
        private String username;
        private List<Integer> roleIdList;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public List<Integer> getRoleIdList() {
            return roleIdList;
        }

        public void setRoleIdList(List<Integer> roleIdList) {
            this.roleIdList = roleIdList;
        }
    }
}
