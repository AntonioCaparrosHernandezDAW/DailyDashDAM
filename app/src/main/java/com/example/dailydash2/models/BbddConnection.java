package com.example.dailydash2.models;

//Clase que gestióna las llamadas a la base de datos (CAMBIAR AQUÍ LA IP SI SE QUIERE USAR EN LOCAL y luego en /res/xml/network_security_config.xml)
public class BbddConnection {
    public static final String BASE_URL = "http://192.168.0.100/ProyectoDAM/";

    public static String getUrl(String scriptName) {
        return BASE_URL + scriptName;
    }
}