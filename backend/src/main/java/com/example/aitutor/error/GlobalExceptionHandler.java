package com.example.aitutor.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
  Map<String,Object> invalid(MethodArgumentNotValidException ex){ return Map.of("error","validation","details",ex.getBindingResult().toString()); }
  @ExceptionHandler(java.util.NoSuchElementException.class) @ResponseStatus(HttpStatus.NOT_FOUND)
  Map<String,Object> notFound(){ return Map.of("error","not_found"); }
  @ExceptionHandler(Exception.class) @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  Map<String,Object> boom(Exception e){ return Map.of("error","server","message",e.getMessage()); }
}
