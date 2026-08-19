package com.tiendagenerica.backend.excepcion;

import com.tiendagenerica.backend.dto.CargueProductosDto;
import com.tiendagenerica.backend.dto.RespuestaDto;
import com.tiendagenerica.backend.servicio.CargueProductosServicio.CargueInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.stream.Collectors;

/**
 * Centraliza el manejo de errores para que todos los modulos devuelvan
 * el mismo formato de mensaje al usuario, tal como lo exige el conjunto
 * de pruebas del documento.
 */
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<RespuestaDto> noEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RespuestaDto.error(ex.getMessage()));
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<RespuestaDto> datosInvalidos(DatosInvalidosException ex) {
        return ResponseEntity.badRequest().body(RespuestaDto.error(ex.getMessage()));
    }

    /** Errores de Bean Validation: se concatenan los mensajes de cada campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaDto> validacion(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining(". "));
        return ResponseEntity.badRequest()
                .body(RespuestaDto.error("Error en los datos suministrados: " + detalle));
    }

    /** Carga de CSV con errores de validacion: devuelve el detalle de cada error. */
    @ExceptionHandler(CargueInvalidoException.class)
    public ResponseEntity<CargueProductosDto> cargueInvalido(CargueInvalidoException ex) {
        return ResponseEntity.badRequest().body(ex.getDetalle());
    }

    @ExceptionHandler({ MissingServletRequestPartException.class, MissingServletRequestParameterException.class })
    public ResponseEntity<RespuestaDto> faltaParametro(Exception ex) {
        return ResponseEntity.badRequest()
                .body(RespuestaDto.error("No se selecciono archivo para cargar"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaDto> general(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RespuestaDto.error("Error interno del sistema: " + ex.getMessage()));
    }
}
