package com.example.lab7_20221203.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.lab7_20221203.dto.RespuestaDesbloqueo;
import com.example.lab7_20221203.dto.SolicitudDesbloqueo;

import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Response;


public class SolDesbloqueoWorker extends Worker {

    // Claves para los datos de entrada
    public static final String KEY_CODIGO = "codigo";
    public static final String KEY_PIN = "pin";

    // Claves para los datos de salida (éxito)
    public static final String KEY_RESULT = "result";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_EXPIRA = "expira";

    // Clave para los datos de salida (error)
    public static final String KEY_ERROR = "error";

    public SolDesbloqueoWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Obtener datos de entrada
        String codigo = getInputData().getString(KEY_CODIGO);
        String pin = getInputData().getString(KEY_PIN);

        // Validar que los datos no sean nulos
        if (codigo == null || pin == null) {
            Data output = new Data.Builder()
                    .putString(KEY_ERROR, "Faltan datos: código o PIN no proporcionados")
                    .build();
            return Result.failure(output);
        }

        // Crear la solicitud
        SolicitudDesbloqueo request = new SolicitudDesbloqueo(codigo, pin);
        ApiService service = ApiClient.getClient().create(ApiService.class);
        Call<RespuestaDesbloqueo> call = service.solicitarDesbloqueo(request);

        try {
            // Ejecutar llamada sincrónica (dentro del Worker, es seguro)
            Response<RespuestaDesbloqueo> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                // Éxito: devolver datos del desbloqueo
                RespuestaDesbloqueo data = response.body();
                Data output = new Data.Builder()
                        .putString(KEY_RESULT, "OK")
                        .putString(KEY_TOKEN, data.getIotAuthToken())
                        .putString(KEY_TIMESTAMP, data.getTimestampAprobacion())
                        .putInt(KEY_EXPIRA, data.getDesbloqueoExpiraEn())
                        .build();
                return Result.success(output);
            } else {
                // Error HTTP (400, 500, etc.)
                String errorMsg = extraerMensajeError(response);
                Data output = new Data.Builder()
                        .putString(KEY_ERROR, errorMsg)
                        .build();
                return Result.failure(output);
            }

        } catch (Exception e) {
            // Error de red, timeout, etc.
            String errorMsg = "Error de conexión: " + e.getMessage();
            Data output = new Data.Builder()
                    .putString(KEY_ERROR, errorMsg)
                    .build();
            return Result.failure(output);
        }
    }

    /**
     * Extrae el mensaje de error del cuerpo de la respuesta HTTP.
     * Se espera un JSON como: {"mensaje": "El código de alumno no existe"}
     */
    private String extraerMensajeError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String json = response.errorBody().string();
                JSONObject obj = new JSONObject(json);
                if (obj.has("mensaje")) {
                    return obj.getString("mensaje");
                }
                // Si no tiene "mensaje", devolver el código de estado
                return "Error " + response.code() + ": " + response.message();
            }
        } catch (Exception e) {
            // Si falla el parsing, devolver mensaje genérico
        }
        return "Error desconocido del servidor";
    }
}
