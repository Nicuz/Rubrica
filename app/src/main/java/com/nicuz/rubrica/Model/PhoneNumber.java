package com.nicuz.rubrica.Model;

public class PhoneNumber {

    private String number;
    private String type;

    public PhoneNumber(String number, String type) {
        this.number = number;
        this.type = type;
    }

    public void setNumber(String number) { this.number = number; }
    public void setType(String type) { this.type = type; }

    public String getNumber() { return number; }
    public String getType() { return type; }
}