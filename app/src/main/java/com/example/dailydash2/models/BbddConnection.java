package com.example.dailydash2.models;

public class BbddConnection {
    public static final String BASE_URL = "http://192.168.0.102/ProyectoDAM/";

    public static String getUrl(String scriptName) {
        return BASE_URL + scriptName;
    }
}