package com.data.analyser.dto;

public class ValueCountPair {
    String value;
    int count;

    public ValueCountPair(String value, int count){
        this.value = value;
        this.count=count;
    }

    public String getValue() {
        return value;
    }

    public int getCount() {
        return count;
    }
}