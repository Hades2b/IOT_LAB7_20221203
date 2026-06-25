package com.example.lab7_20221203.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ContadorViewModel extends ViewModel {




    public MutableLiveData<Integer> segundosRestantes = new MutableLiveData<>();
    public MutableLiveData<Boolean> expirado = new MutableLiveData<>();

    public MutableLiveData<Integer> getSegundosRestantes() { return segundosRestantes; }
    public MutableLiveData<Boolean> getExpirado() { return expirado; }


    private final MutableLiveData<Integer> contador = new MutableLiveData<>();

    public MutableLiveData<Integer> getContador() {
        return contador;
    }


}
