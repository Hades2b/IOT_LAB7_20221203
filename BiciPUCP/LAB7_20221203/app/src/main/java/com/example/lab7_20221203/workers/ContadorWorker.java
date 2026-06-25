package com.example.lab7_20221203.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ContadorWorker extends Worker {

    public static final String KEY_SEGUNDOS_RESTANTES = "segundos_restantes";
    public static final String KEY_INICIO = "inicio"; // timestamp de inicio

    public ContadorWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        long inicio = getInputData().getLong(KEY_INICIO, System.currentTimeMillis());
        int segundosTotales = 120; // o tomarlo del input

        for (int i = segundosTotales; i >= 0; i--) {
            // Enviar progreso
            Data progress = new Data.Builder()
                    .putInt(KEY_SEGUNDOS_RESTANTES, i)
                    .build();
            setProgressAsync(progress);

            // Si se cancela el worker, salir
            if (isStopped()) {
                return Result.failure();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return Result.failure();
            }
        }

        return Result.success();
    }
}

