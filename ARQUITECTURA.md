# ARQUITECTURA - BiciPUCP

## 1. Patrón arquitectónico global
El patron de diseño de microservicios aplicado es **Patrón de registro de servicios**.

    - Se utiliza eureka-server como **Service Registry** para los otros 2 proyectos.
    - Los 2 microservicios registrados pueden comunicarse y llevar a cabo sus tareas a traves de RestClient y FeignClient sin necesidad de IPs fijas.

## 2. Restricción "Stateless" 
El proyecto orquestador pucp-validador-service cumple esta restricion del estandar Restful.

    - No utiliza sesión, cookies ni identificadores para procesar y ejecutar la petición.
    - Los endpoint únicamente reciben variables como @PathVariable en la Url, y el resultado depende solo de estas variables.