package com.example.orquestadorservice;

import com.example.orquestadorservice.dto.RespuestaDesbloqueo;
import com.example.orquestadorservice.dto.SolicitudDesbloqueo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class DesbloquearController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ValidarCandadoClient validarClient;

    @PostMapping("/bici/solicitar-desbloqueo")
    public ResponseEntity<?> solicitarDesbloqueo(@RequestBody SolicitudDesbloqueo request) {
        String codigo=request.getCodigo();
        String pin=request.getPin();

        String urlRest = "http://pucp-validador-service/validar/alumno/" + codigo;
        Boolean codigoValido=restTemplate.getForObject(urlRest, Boolean.class);

        Boolean candadoValido=validarClient.validarCandado(pin);

        if (Boolean.TRUE.equals(candadoValido) && Boolean.TRUE.equals(codigoValido)) {
            String token = "PUCP-BIKE-" + UUID.randomUUID().toString();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            RespuestaDesbloqueo respuesta = new RespuestaDesbloqueo("APROBADO", token, 120, timestamp);
            return ResponseEntity.ok(respuesta);
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            if (Boolean.FALSE.equals(codigoValido)) {
                errorResponse.put("mensaje", "Código PUCP inválido.");
            } else if (Boolean.FALSE.equals(candadoValido)) {
                errorResponse.put("mensaje", "PIN de tu candado incorrecto.");
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

    }

}
