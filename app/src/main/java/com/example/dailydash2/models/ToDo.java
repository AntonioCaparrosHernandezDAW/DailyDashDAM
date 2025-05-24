package com.example.dailydash2.models;

public class ToDo {
    private int idTarea;
    private String titulo;
    private String prioridad;
    private String fechaInicio;
    private String fechaFin;
    private boolean completada;

    public ToDo(int idTarea, String titulo, String prioridad, String fechaInicio, String fechaFin, boolean completada) {
        this.idTarea = idTarea;
        this.titulo = titulo;
        this.prioridad = prioridad;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.completada = completada;
    }

    //No es necesario un setter del ID porque no tengo pensado que se pueda modificar

    public int getIdTarea() {
        return idTarea;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(String fechaFin) {
        this.fechaFin = fechaFin;
    }

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }
}
