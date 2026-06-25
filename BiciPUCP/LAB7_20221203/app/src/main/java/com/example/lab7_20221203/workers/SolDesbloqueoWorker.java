package com.example.lab7_20221203.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.lab7_20221203.dto.RespuestaDesbloqueo;
import com.example.lab7_20221203.dto.SolicitudDesbloqueo;
import com.example.lab7_20221203.service.ApiClient;
import com.example.lab7_20221203.service.ApiService;

import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Response;

public class SolDesbloqueoWorker extends Worker {

    public static final String KEY_CODIGO = "codigo";
    public static final String KEY_PIN = "pin";
    public static final String KEY_RESULT = "result";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_EXPIRA = "expira";
    public static final String KEY_ERROR = "error";

    public SolDesbloqueoWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String codigo = getInputData().getString(KEY_CODIGO);
        String pin = getInputData().getString(KEY_PIN);

        if (codigo == null || pin == null) {
            Data output = new Data.Builder()
                    .putString(KEY_ERROR, "Faltan datos: código o PIN no proporcionados")
                    .build();
            return Result.failure(output);
        }

        SolicitudDesbloqueo request = new SolicitudDesbloqueo(codigo, pin);
        ApiService service = ApiClient.getClient().create(ApiService.class);
        Call<RespuestaDesbloqueo> call = service.solicitarDesbloqueo(request);

        try {
            Response<RespuestaDesbloqueo> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                RespuestaDesbloqueo data = response.body();
                Data output = new Data.Builder()
                        .putString(KEY_RESULT, "OK")
                        .putString(KEY_TOKEN, data.getIotAuthToken())
                        .putString(KEY_TIMESTAMP, data.getTimestampAprobacion())
                        .putInt(KEY_EXPIRA, data.getDesbloqueoExpiraEn())
                        .build();
                return Result.success(output);
            } else {
                String errorMsg = extraerMensajeError(response);
                Data output = new Data.Builder()
                        .putString(KEY_ERROR, errorMsg)
                        .build();
                return Result.failure(output);
            }
        } catch (Exception e) {
            Data output = new Data.Builder()
                    .putString(KEY_ERROR, "Error de conexión: " + e.getMessage())
                    .build();
            return Result.failure(output);
        }
    }

    private String extraerMensajeError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String json = response.errorBody().string();
                JSONObject obj = new JSONObject(json);
                if (obj.has("mensaje")) {
                    return obj.getString("mensaje");
                }
                return "Error " + response.code() + ": " + response.message();
            }
        } catch (Exception e) {
        }
        return "Error desconocido del servidor";
    }
}
