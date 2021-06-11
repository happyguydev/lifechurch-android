package org.chat21.android.core.contacts.synchronizers;

import java.util.ArrayList;

public class Contact {
    public String id;
    public String name;
    public String image;
    public ArrayList<ContactEmail> emails;
    public ArrayList<ContactPhone> numbers;

    public Contact(String id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.emails = new ArrayList<ContactEmail>();
        this.numbers = new ArrayList<ContactPhone>();
    }

    @Override
    public String toString() {
        String result = name;
        if (numbers.size() > 0) {
            ContactPhone number = numbers.get(0);
            result += " (" + number.number + " - " + number.type + ")";
        }
        if (emails.size() > 0) {
            ContactEmail email = emails.get(0);
            result += " [" + email.address + " - " + email.type + "]";
        }
        return result;
    }

    public String getallphone() {
        String result1 = "";


        for (int i = 0;i<numbers.size();i++){
            ContactPhone number = numbers.get(i);
            result1 += number.number + ",";
        }
        return result1;

//        ContactPhone number = numbers.get(0);
//        return number.number;
    }

    public void addEmail(String address, String type) {
        emails.add(new ContactEmail(address, type));
    }

    public void addNumber(String number, String type) {
        numbers.add(new ContactPhone(number, type));
    }
}
