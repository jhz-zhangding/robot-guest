package com.efrobot.guest.bean;

/**
 * Created by shuai on 2016/8/30.
 * 轮子bean
 */
public class Action_Wheel {
    //时间
    private String next_action_time;
    //方向
    private String  direction;
    //速度
    private String speed;
    //角度
    private String angle;
    public int position;

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getAngle() {
        return angle;
    }

    public void setAngle(String angle) {
        this.angle = angle;
    }

    public String getNext_action_time() {
        return next_action_time;
    }

    public void setNext_action_time(String next_action_time) {
        this.next_action_time = next_action_time;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
