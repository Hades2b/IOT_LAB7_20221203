package com.example.lab7_20221203.viewModel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.lab7_20221203.workers.ContadorWorker;

import java.util.UUID;

public class ContadorViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> segundosRestantes = new MutableLiveData<>(120);
    private final MutableLiveData<Boolean> expirado = new MutableLiveData<>(false);
    private UUID workerId;
    private boolean contadorActivo = false;

    private LiveData<WorkInfo> contadorLiveData;
    private Observer<WorkInfo> contadorObserver;

    public ContadorViewModel(@NonNull Application application) {
        super(application);
    }

    public void iniciarContador(long timestampAprobacion) {
        if (contadorActivo) return;

        long ahora = System.currentTimeMillis();
        long diff = (timestampAprobacion + 120_000) - ahora;
        int segundos = (int) Math.max(0, diff / 1000);

        if (segundos <= 0) {
            expirado.setValue(true);
            segundosRestantes.setValue(0);
            return;
        }

        contadorActivo = true;
        expirado.setValue(false);
        segundosRestantes.setValue(segundos);

        Data inputData = new Data.Builder()
                .putLong(ContadorWorker.KEY_INICIO, timestampAprobacion)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ContadorWorker.class)
                .setInputData(inputData)
                .addTag("contador_tag")
                .build();

        workerId = workRequest.getId();

        contadorObserver = workInfo -> {
            if (workInfo == null) return;
            if (workInfo.getState() == WorkInfo.State.RUNNING) {
                Data progress = workInfo.getProgress();
                int seg = progress.getInt(ContadorWorker.KEY_SEGUNDOS_RESTANTES, -1);
                if (seg != -1) {
                    segundosRestantes.setValue(seg);
                    if (seg <= 0) {
                        expirado.setValue(true);
                        contadorActivo = false;
                        limpiarObserver(); // Ya no necesitamos el observer
                    }
                }
            } else if (workInfo.getState().isFinished()) {
                contadorActivo = false;
                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                    expirado.setValue(true);
                    segundosRestantes.setValue(0);
                }
                limpiarObserver();
            }
        };

        contadorLiveData = WorkManager.getInstance(getApplication())
                .getWorkInfoByIdLiveData(workerId);
        contadorLiveData.observeForever(contadorObserver);

        WorkManager.getInstance(getApplication()).enqueue(workRequest);
    }

    public void reiniciarContador(long nuevoTimestamp) {
        limpiarObserver();
        WorkManager.getInstance(getApplication()).cancelAllWorkByTag("contador_tag");
        contadorActivo = false;
        iniciarContador(nuevoTimestamp);
    }

    private void limpiarObserver() {
        if (contadorLiveData != null && contadorObserver != null) {
            contadorLiveData.removeObserver(contadorObserver);
            contadorLiveData = null;
            contadorObserver = null;
        }
    }

    public void setExpiradoManual(boolean value) {
        expirado.setValue(value);
        if (value) segundosRestantes.setValue(0);
    }

    public LiveData<Integer> getSegundosRestantes() {
        return segundosRestantes;
    }

    public LiveData<Boolean> getExpirado() {
        return expirado;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        limpiarObserver();
    }
}
