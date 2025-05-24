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

    public int getIdNote() {
        return idNote;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

