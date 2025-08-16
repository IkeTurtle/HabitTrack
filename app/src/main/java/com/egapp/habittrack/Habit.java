package com.egapp.habittrack;

public class Habit {
    String id;
    String name;
    boolean completed;
    String habitDate;

    public Habit() {} // Wird für Firebase Anbindung gebraucht. Ohne leeren Konstruktor keine Realtime Datenanbindung möglich.

    public Habit(String id, String name, boolean completed, String habitDate) {
        this.id = id;
        this.name = name;
        this.completed = completed;
        this.habitDate = habitDate;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDate() {
        return habitDate;
    }
    public boolean isCompleted() {
        return completed;
    }



    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDate(String date) {
        this.habitDate = date;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
