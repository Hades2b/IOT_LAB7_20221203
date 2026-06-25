package com.example.pucpvalidadorservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/validar")
public class ValidacionController {
    private static final Logger log = LoggerFactory.getLogger(ValidacionController.class);

    @GetMapping("/alumno/{codigo}")
    public boolean validarAlumno(@PathVariable("codigo") String codigo){
        log.info("🔍 Validando codigo: {}", codigo);
        if (codigo == null) return false;
        return codigo.matches("20\\d{6}");
    }

    @GetMapping("/candado/{pin}")
    public boolean validarCandado(@PathVariable String pin) {
        log.info("🔍 Validando pin: {}", pin);

        if (pin == null || !pin.matches("\\d{4}")) return false;

        for (int i = 0; i < pin.length() - 1; i++) {
            if (pin.charAt(i) == pin.charAt(i + 1)) {
                return false;
            }
        }
        return true;

    }

}
