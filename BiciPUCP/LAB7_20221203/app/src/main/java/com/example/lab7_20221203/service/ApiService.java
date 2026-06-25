package com.example.lab7_20221203.service;

import com.example.lab7_20221203.dto.RespuestaDesbloqueo;
import com.example.lab7_20221203.dto.SolicitudDesbloqueo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("bici/solicitar-desbloqueo")
    Call<RespuestaDesbloqueo> solicitarDesbloqueo(@Body SolicitudDesbloqueo solicitud);
}
