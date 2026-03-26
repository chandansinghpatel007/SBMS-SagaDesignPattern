package com.sbms.busbookingsystem.exception;

import java.util.stream.Collectors;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import com.sbms.busbookingsystem.dto.ResponseMessagedto;
import com.sbms.busbookingsystem.utility.Constants;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ 1. VALIDATION EXCEPTION
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseMessagedto<Object>> handleValidation(MethodArgumentNotValidException ex) {

        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + " : " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseMessagedto.builder()
                        .statuscode(HttpStatus.BAD_REQUEST.value())
                        .status(Constants.FAILURE)
                        .message(errors)
                        .data(null)
                        .build());
    }

    // ✅ 2. RESOURCE NOT FOUND
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseMessagedto<Object>> handleNotFound(ResourceNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseMessagedto.builder()
                        .statuscode(HttpStatus.NOT_FOUND.value())
                        .status(Constants.FAILURE)
                        .message(ex.getMessage())
                        .data(null)
                        .build());
    }

    // ✅ 3. ILLEGAL ARGUMENT
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseMessagedto<Object>> handleIllegal(IllegalArgumentException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseMessagedto.builder()
                        .statuscode(HttpStatus.BAD_REQUEST.value())
                        .status(Constants.FAILURE)
                        .message(ex.getMessage())
                        .data(null)
                        .build());
    }

    // ✅ 4. RUNTIME EXCEPTION
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseMessagedto<Object>> handleRuntime(RuntimeException ex) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseMessagedto.builder()
                        .statuscode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .status(Constants.FAILURE)
                        .message("Runtime Error: " + ex.getMessage())
                        .data(null)
                        .build());
    }

    // ✅ 5. GLOBAL EXCEPTION (FALLBACK)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseMessagedto<Object>> handleException(Exception ex) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseMessagedto.builder()
                        .statuscode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .status(Constants.FAILURE)
                        .message("Internal Server Error")
                        .data(null)
                        .build());
    }
}