package com.example.lab7_20221203.dto;

public class RespuestaDesbloqueo {

    private String status;
    private String iotAuthToken;
    private int desbloqueoExpiraEn;
    private String timestampAprobacion;

    public RespuestaDesbloqueo() {
    }

    public RespuestaDesbloqueo(String status, String iotAuthToken, int desbloqueoExpiraEn, String timestampAprobacion) {
        this.status = status;
        this.iotAuthToken = iotAuthToken;
        this.desbloqueoExpiraEn = desbloqueoExpiraEn;
        this.timestampAprobacion = timestampAprobacion;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public int getDesbloqueoExpiraEn() {
        return desbloqueoExpiraEn;
    }
    public void setDesbloqueoExpiraEn(int desbloqueoExpiraEn) {
        this.desbloqueoExpiraEn = desbloqueoExpiraEn;
    }

    public String getIotAuthToken() {
        return iotAuthToken;
    }
    public void setIotAuthToken(String iotAuthToken) {
        this.iotAuthToken = iotAuthToken;
    }

    public String getTimestampAprobacion() {
        return timestampAprobacion;
    }
    public void setTimestampAprobacion(String timestampAprobacion) {
        this.timestampAprobacion = timestampAprobacion;
    }
}
