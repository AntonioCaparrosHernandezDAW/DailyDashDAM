package com.example.dailydash2.models;

//Objeto que gestiona cada nota en NoteFragment
public class Note {
    public int idNote;
    public String title;
    public String text;
    public String date;

    //Constructor
    public Note(int idNote, String title, String text, String date) {
        this.idNote = idNote;
        this.title = title;
        this.text = text;
        this.date = date;
    }

    //No es necesario un setter del ID porque no tengo pensado que se pueda modificar

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

