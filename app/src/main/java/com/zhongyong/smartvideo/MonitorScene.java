package com.zhongyong.smartvideo;

import java.io.Serializable;

/**
 * Created by fyc on 2018/2/8.
 */

public class MonitorScene implements Serializable {
    private String name;
    private String address;

    public MonitorScene(String name, String address, boolean isAdd) {
        this.name = name;
        this.address = address;
        this.isAdd = isAdd;
    }

    public boolean isAdd() {
        return isAdd;
    }

    public void setAdd(boolean add) {
        isAdd = add;
    }

    private boolean isAdd;

    public MonitorScene(String name, String address) {
        this.name = name;
        this.address = address;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
