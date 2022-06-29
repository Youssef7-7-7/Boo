package com.jotech.boo.models;

public class User {
    private String Uid, name, profile, city, country;
    private int coins;

    public User(){
    }

    public User(String Uid, String name, String profile, String city, int coins, String country) {
        this.Uid = Uid;
        this.name = name;
        this.profile = profile;
        this.city = city;
        this.coins = coins;
        this.country = country;
    }

    public String getUid() { return Uid; }

    public void setUid(String Uid) {
        this.Uid = Uid;
    }

    public  String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) { this.city = city; }

    public int getCoins() { return coins; }

    public void setCoins(int coins) { this.coins = coins; }

    public String getCountry() { return country; }

    public void setCountry(String country) { this.country = country; }
}
