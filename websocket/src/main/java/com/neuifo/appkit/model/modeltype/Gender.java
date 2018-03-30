package com.neuifo.appkit.model.modeltype;

/**
 * Created by neuifo on 2017/8/4.
 */

public enum Gender  {

    MALE(1, "fale"), FEMALE(2, "male");

    private int id;
    private String name;

    Gender(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static Gender transFer(int id) {
        for (Gender gender : values()) {
            if (id == gender.getId()) {
                return gender;
            }
        }
        return MALE;
    }

    public static Gender transFer(String value) {
        for (Gender gender : values()) {
            if (gender.getName().equals(value)) {
                return gender;
            }
        }
        return MALE;
    }
}
