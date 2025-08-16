package com.egapp.habittrack;

import java.util.HashMap;
import java.util.Map;

public class Habit {
    String id;
    String name;
    boolean completed;
    Map<String, Boolean> completions;


    public Habit() {} // Wird für Firebase Anbindung gebraucht. Ohne leeren Konstruktor keine Realtime Datenanbindung möglich.

    public Habit(String id, String name, boolean completed, String habitDate) {
        this.id = id;
        this.name = name;

        this.completions = new HashMap<>();
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Map<String, Boolean> getCompletions() {
        return completions;
    }



    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    public void setCompletions(Map<String, Boolean> completions) {
        this.completions = completions;
    }
}
