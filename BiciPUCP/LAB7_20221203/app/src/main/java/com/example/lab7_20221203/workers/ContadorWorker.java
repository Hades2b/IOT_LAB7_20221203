package com.example.lab7_20221203.workers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class ContadorWorker extends Worker {

    public static final String KEY_SEGUNDOS_RESTANTES = "segundos_restantes";
    public static final String KEY_INICIO = "inicio"; // timestamp de inicio (milis)

    public ContadorWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        long inicio = getInputData().getLong(KEY_INICIO, System.currentTimeMillis());
        long fin = inicio + 120_000;

        while (true) {
            long ahora = System.currentTimeMillis();
            int restantes = (int) ((fin - ahora) / 1000);

            if (restantes < 0) restantes = 0;

            Data progress = new Data.Builder()
                    .putInt(KEY_SEGUNDOS_RESTANTES, restantes)
                    .build();
            setProgressAsync(progress);

            if (restantes <= 0 || isStopped()) {
                break;
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
