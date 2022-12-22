package com.deepsoft.shortbarge.driver.gson;

import java.util.ArrayList;
import java.util.List;

public class TaskList extends ResultGson {

    private List<TaskGson> taskGsons = new ArrayList<>();

    public List<TaskGson> getTaskGsons() {
        return taskGsons;
    }

    public void setTaskGsons(List<TaskGson> taskGsons){
        this.taskGsons = taskGsons;
    }
}
