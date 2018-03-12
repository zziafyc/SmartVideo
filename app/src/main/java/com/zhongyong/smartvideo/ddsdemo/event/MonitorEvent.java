package com.zhongyong.smartvideo.ddsdemo.event;

import java.io.Serializable;

/**
 * Created by fyc on 2018/3/9.
 */

public class MonitorEvent implements Serializable {
    private String name;
    private String address;

    public MonitorEvent(String name, String address) {
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
