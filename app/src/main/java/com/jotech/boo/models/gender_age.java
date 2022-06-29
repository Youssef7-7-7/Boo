package com.jotech.boo.models;

public class gender_age {
    private String gender;
    private int age;

    public gender_age() {

    }

    public gender_age(String gender, int age) {
        this.gender = gender;
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) { this.gender = gender; }
}
