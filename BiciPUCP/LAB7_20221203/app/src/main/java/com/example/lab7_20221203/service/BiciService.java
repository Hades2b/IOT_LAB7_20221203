package com.example.lab7_20221203.service;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
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

    public LiveData<ResultadoDesbloqueo> solicitarDesbloqueo(String codigo, String pin) {
        MutableLiveData<ResultadoDesbloqueo> liveData = new MutableLiveData<>();

        Data inputData = new Data.Builder()
                .putString(SolDesbloqueoWorker.KEY_CODIGO, codigo)
                .putString(SolDesbloqueoWorker.KEY_PIN, pin)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SolDesbloqueoWorker.class)
                .setInputData(inputData)
                .build();

        LiveData<WorkInfo> workInfoLiveData = workManager.getWorkInfoByIdLiveData(workRequest.getId());
        
        Observer<WorkInfo> observer = new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if (workInfo == null) return;
                
                if (workInfo.getState().isFinished()) {
                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                        Data output = workInfo.getOutputData();
                        RespuestaDesbloqueo respuesta = new RespuestaDesbloqueo();
                        respuesta.setStatus("APROBADO");
                        respuesta.setIotAuthToken(output.getString(SolDesbloqueoWorker.KEY_TOKEN));
                        respuesta.setTimestampAprobacion(output.getString(SolDesbloqueoWorker.KEY_TIMESTAMP));
                        respuesta.setDesbloqueoExpiraEn(output.getInt(SolDesbloqueoWorker.KEY_EXPIRA, 120));

                        liveData.setValue(new ResultadoDesbloqueo(respuesta, null));
                    } else {
                        String errorMsg = workInfo.getOutputData().getString(SolDesbloqueoWorker.KEY_ERROR);
                        if (errorMsg == null) errorMsg = "Error en la validación del candado";
                        liveData.setValue(new ResultadoDesbloqueo(null, errorMsg));
                    }
                    workInfoLiveData.removeObserver(this);
                }
            }
        };

        workInfoLiveData.observeForever(observer);
        workManager.enqueue(workRequest);

        return liveData;
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
