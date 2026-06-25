package com.example.orquestadorservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "pucp-validador-service")
public interface ValidarCandadoClient {

//    @GetMapping("/validar/alumno/{codigo}")
//    Boolean validarAlumno(@PathVariable("codigo") String codigo);

    @GetMapping("/validar/candado/{pin}")
    Boolean validarCandado(@PathVariable("pin") String pin);
}
