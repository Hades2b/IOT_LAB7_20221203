package com.example.lab7_20221203.service;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.lab7_20221203.dto.RespuestaDesbloqueo;
import com.example.lab7_20221203.workers.SolDesbloqueoWorker;

public class BiciService {

    private final Context context;
    private final WorkManager workManager;

    public BiciService(Context context) {
        this.context = context.getApplicationContext();
        this.workManager = WorkManager.getInstance(this.context);
    }

    /**
     * Solicita el desbloqueo al orquestador.
     * @param codigo Código PUCP (8 dígitos)
     * @param pin PIN del candado (4 dígitos)
     * @param callback Callback con el resultado (éxito o error)
     */
    public void solicitarDesbloqueo(String codigo, String pin, BiciCallback callback) {
        // Crear datos de entrada
        Data inputData = new Data.Builder()
                .putString(SolDesbloqueoWorker.KEY_CODIGO, codigo)
                .putString(SolDesbloqueoWorker.KEY_PIN, pin)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SolDesbloqueoWorker.class)
                .setInputData(inputData)
                .build();

        // Observar el resultado
        workManager.getWorkInfoByIdLiveData(workRequest.getId())
                .observeForever(workInfo -> {
                    if (workInfo == null) return;

                    if (workInfo.getState().isFinished()) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Data output = workInfo.getOutputData();
                            String token = output.getString(SolDesbloqueoWorker.KEY_TOKEN);
                            String timestamp = output.getString(SolDesbloqueoWorker.KEY_TIMESTAMP);
                            int expira = output.getInt(SolDesbloqueoWorker.KEY_EXPIRA, 120);

                            RespuestaDesbloqueo respuesta = new RespuestaDesbloqueo();
                            respuesta.setStatus("APROBADO");
                            respuesta.setIotAuthToken(token);
                            respuesta.setTimestampAprobacion(timestamp);
                            respuesta.setDesbloqueoExpiraEn(expira);

                            callback.onSuccess(respuesta);
                        } else {
                            // Fallo
                            Data output = workInfo.getOutputData();
                            String errorMsg = output.getString(SolDesbloqueoWorker.KEY_ERROR);
                            if (errorMsg == null) errorMsg = "Error desconocido del servidor";
                            callback.onError(errorMsg);
                        }
                    }
                });

        // Encolar el trabajo
        workManager.enqueue(workRequest);
    }

    /**
     * Versión con LiveData (más moderna, sin callbacks manuales).
     */
    public LiveData<ResultadoDesbloqueo> solicitarDesbloqueoLive(String codigo, String pin) {
        MutableLiveData<ResultadoDesbloqueo> liveData = new MutableLiveData<>();

        Data inputData = new Data.Builder()
                .putString(SolDesbloqueoWorker.KEY_CODIGO, codigo)
                .putString(SolDesbloqueoWorker.KEY_PIN, pin)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SolDesbloqueoWorker.class)
                .setInputData(inputData)
                .build();

        workManager.getWorkInfoByIdLiveData(workRequest.getId())
                .observeForever(workInfo -> {
                    if (workInfo == null) return;
                    if (workInfo.getState().isFinished()) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            Data output = workInfo.getOutputData();
                            String token = output.getString(SolDesbloqueoWorker.KEY_TOKEN);
                            String timestamp = output.getString(SolDesbloqueoWorker.KEY_TIMESTAMP);
                            int expira = output.getInt(SolDesbloqueoWorker.KEY_EXPIRA, 120);

                            RespuestaDesbloqueo respuesta = new RespuestaDesbloqueo();
                            respuesta.setStatus("APROBADO");
                            respuesta.setIotAuthToken(token);
                            respuesta.setTimestampAprobacion(timestamp);
                            respuesta.setDesbloqueoExpiraEn(expira);

                            liveData.setValue(new ResultadoDesbloqueo(respuesta, null));
                        } else {
                            Data output = workInfo.getOutputData();
                            String errorMsg = output.getString(SolDesbloqueoWorker.KEY_ERROR);
                            if (errorMsg == null) errorMsg = "Error desconocido del servidor";
                            liveData.setValue(new ResultadoDesbloqueo(null, errorMsg));
                        }
                    }
                });

        workManager.enqueue(workRequest);
        return liveData;
    }

    // ============== CALLBACKS Y CLASES AUXILIARES ==============

    public interface BiciCallback {
        void onSuccess(RespuestaDesbloqueo respuesta);
        void onError(String error);
    }

    public static class ResultadoDesbloqueo {
        public final RespuestaDesbloqueo respuesta;
        public final String error;

        public ResultadoDesbloqueo(RespuestaDesbloqueo respuesta, String error) {
            this.respuesta = respuesta;
            this.error = error;
        }

        public boolean isSuccess() {
            return respuesta != null && error == null;
        }
    }
}