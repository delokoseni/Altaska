package com.example.Altaska.dto;

public class Change {
    private String field;
    private Object oldValue;
    private Object newValue;

    public Change() {}

    public Change(String field, Object oldValue, Object newValue) {
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getOld() {
        return oldValue;
    }

    public void setOld(Object oldValue) {
        this.oldValue = oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }
}
