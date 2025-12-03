package com.example.smsdemo;

import android.annotation.SuppressLint;
import android.provider.Telephony;

import java.text.SimpleDateFormat;

public class SmsInfo {

    private int _id; // 主键
    private String address; // 发送地址
    private long date; // 发送时间
    private int type; // 类型
    private String body; // 内容
    private int id;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss");

    //构造方法
    public SmsInfo(int _id, String address, String body, int type, long date) {
        this._id = _id;
        this.address = address;
        this.body = body;
        this.type = type;
        this.date = date;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFormatType() {
        switch (type) {
            case Telephony.Sms.MESSAGE_TYPE_INBOX:
                return "接收的短信";
            case Telephony.Sms.MESSAGE_TYPE_SENT:
                return "发送的短信";
            default:
                return "其他";
        }
    }

    public String getFormatDate() {
        return dateFormat.format(date);
    }

    @Override
    public String toString() {
        return "SmsInfo{" +
                "_id=" + _id +
                ", address='" + address + '\'' +
                ", date=" + date +
                ", type=" + type +
                ", body='" + body + '\'' +
                ", id=" + id +
                '}';
    }
}