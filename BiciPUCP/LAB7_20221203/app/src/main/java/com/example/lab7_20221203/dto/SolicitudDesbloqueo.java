package com.example.lab7_20221203.dto;

public class SolicitudDesbloqueo {

    private String codigo;
    private String pin;


    public SolicitudDesbloqueo(String codigo, String pin) {
        this.codigo = codigo;
        this.pin = pin;
    }

    public String getCodigo() {
        return codigo;
    }
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getPin() {
        return pin;
    }
    public void setPin(String pin) {
        this.pin = pin;
    }


}
