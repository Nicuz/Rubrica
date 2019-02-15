package com.nicuz.rubrica.Model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.util.ArrayList;

public class Contact {

    private String firstName;
    private String lastName;
    private ArrayList<PhoneNumber> phoneNumbers;
    private String email;
    private String company;
    private String address;
    private String photo;
    private String businessCardPhoto;

    public Contact(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Contact(String firstName,
                   String lastName,
                   ArrayList<PhoneNumber> phoneNumbers,
                   String email,
                   String company,
                   String address,
                   String photo,
                   String businessCardPhoto) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumbers = phoneNumbers;
        this.email = email;
        this.company = company;
        this.address = address;
        this.photo = photo;
        this.businessCardPhoto = businessCardPhoto;
    }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPhoneNumbers(ArrayList<PhoneNumber> phoneNumbers) { this.phoneNumbers = phoneNumbers; }
    public void setEmail(String email) { this.email = email; }
    public void setCompany(String company) { this.company = company; }
    public void setAddress(String address) { this.address = address; }
    public void setPhoto(String photo) { this.photo = photo; }
    public void setBusinessCardPhoto(String businessCardPhoto) { this.businessCardPhoto = businessCardPhoto; }


    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public ArrayList<PhoneNumber> getPhoneNumbers() { return phoneNumbers; }
    public String getEmail() { return email; }
    public String getCompany() { return company; }
    public String getAddress() { return address; }
    public String getPhoto() { return photo; }
    public String getBusinessCardPhoto() { return businessCardPhoto; }

    public String getFullName() { return firstName + " " + lastName; }

    public Bitmap getDecodedPhoto(String photo) {
        byte[] byteArray = Base64.decode(photo, Base64.DEFAULT);
        Bitmap decodedPhoto;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        decodedPhoto = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
        return decodedPhoto;
    }

    public String getvCard(Contact contact) {
        String vCard = "BEGIN:VCARD\r\nVERSION:2.1\r\n";
        vCard += "N:" + contact.getLastName() + ";" + contact.getFirstName() + ";;;\r\n";
        vCard += "FN:" + contact.getFullName() + "\r\n";

        if (contact.getAddress() != null) {
            vCard += "ORG:" + contact.getCompany() + "\r\n";
        }

        for (PhoneNumber tel : contact.getPhoneNumbers()) {
            vCard += "TEL;TYPE=" + tel.getType() + ":" + tel.getNumber() + "\r\n";
        }

        if (contact.getAddress() != null) {
            vCard += "ADR:;;" + contact.getAddress() + ";;;;\r\n";
        }

        if (contact.getEmail() != null) {
            vCard += "EMAIL:" + contact.getEmail() + "\r\n";
        }

        vCard += "END:VCARD";

        return vCard;
    }
}