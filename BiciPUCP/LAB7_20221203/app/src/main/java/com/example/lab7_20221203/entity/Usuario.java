package com.example.lab7_20221203.entity;

public class Usuario {
    private String uid;
    private String email;
    private String codigoPUCP;        // 8 dígitos
    private String pin;               // 4 dígitos
    private String iotAuthToken;
    private long timestampAprobacion;
    private String fotoUrl;
    public Usuario() {}

    public Usuario(String uid, String email, String codigoPUCP, String pin, String iotAuthToken, long timestampAprobacion, String fotoUrl) {
        this.uid = uid;
        this.email = email;
        this.codigoPUCP = codigoPUCP;
        this.pin = pin;
        this.iotAuthToken = iotAuthToken;
        this.timestampAprobacion = timestampAprobacion;
        this.fotoUrl = fotoUrl;
    }

    // Getters y setters
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodigoPUCP() {
        return codigoPUCP;
    }
    public void setCodigoPUCP(String codigoPUCP) {
        this.codigoPUCP = codigoPUCP;
    }

    public String getPin() {
        return pin;
    }
    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getIotAuthToken() {
        return iotAuthToken;
    }
    public void setIotAuthToken(String iotAuthToken) {
        this.iotAuthToken = iotAuthToken;
    }

    public long getTimestampAprobacion() {
        return timestampAprobacion;
    }
    public void setTimestampAprobacion(long timestampAprobacion) {
        this.timestampAprobacion = timestampAprobacion;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }
    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

}