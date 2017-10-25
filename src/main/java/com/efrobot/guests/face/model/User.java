package com.efrobot.guests.face.model;

/**
 * Created by mac on 16/7/4.
 */
public class User {
    private String user_id;
    private String personId;
    private String name;
    private String age;
    private String date;

    public String getServer_person_id() {
        return server_person_id;
    }

    public void setServer_person_id(String server_person_id) {
        this.server_person_id = server_person_id;
    }

    private String server_person_id = "server_person_id";
    private String gender;

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    private String score = "score";
    private String head;

    public User(String personId, String name, String age, String gender) {
        this.personId = personId;
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    public User() {

    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
