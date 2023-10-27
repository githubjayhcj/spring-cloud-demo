package com.example.webService.entity;

/**
 * @ClassName: PendingEvent
 * @Author: hongchenjie-(Evins)
 * @Data: 2023/9/29 09:52
 * @Version: 1.0.0
 * @Description: TODO
 */

// 待处理事件（支付充值，激活，确认完成）
public class PendingEvent {
    private int id;
    private int socketChannelId;
    private String socketChannelName;
    private int status;
    private int type;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSocketChannelId() {
        return socketChannelId;
    }

    public void setSocketChannelId(int socketChannelId) {
        this.socketChannelId = socketChannelId;
    }

    public String getSocketChannelName() {
        return socketChannelName;
    }

    public void setSocketChannelName(String socketChannelName) {
        this.socketChannelName = socketChannelName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "PendingEvent{" +
                "id=" + id +
                ", socketChannelId=" + socketChannelId +
                ", socketChannelName='" + socketChannelName + '\'' +
                ", status=" + status +
                ", type='" + type + '\'' +
                '}';
    }
}
