package com.deepsoft.shortbarge.driver.gson;

import java.util.List;

public class UserInfoGson extends ResultGson {
    // 获取用户详情data
    private String id;
    private String username;
    private List<Integer> roleIdList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
