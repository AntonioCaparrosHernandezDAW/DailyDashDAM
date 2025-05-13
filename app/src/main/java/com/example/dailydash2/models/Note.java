package com.example.dailydash2.models;

public class Note {
    public int idNote;
    public String title;
    public String text;
    public String date;

    public Note(int idNote, String title, String text, String date) {
        this.idNote = idNote;
        this.title = title;
        this.text = text;
        this.date = date;
    }
}

