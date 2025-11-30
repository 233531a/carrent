package com.example.carrent.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений для REST API контроллеров.
 *
 * Централизованно обрабатывает ошибки, возникающие в @RestController,
 * и возвращает единообразные JSON-ответы с информацией об ошибке.
 *
 * Преимущества:
 * - Единый формат ответов при ошибках для всех API endpoints
 * - Не нужно дублировать try-catch в каждом контроллере
 * - Скрывает внутренние детали реализации от клиентов
 * - Упрощает отладку (стандартная структура ответа)
 *
 * Формат ответа при ошибке:
 * {
 *   "timestamp": "YYYY-MM-DDTHH:mm:ss",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Подробное описание ошибки"
 * }
 *
 * @author Система аренды автомобилей
 */
@RestControllerAdvice
public class RestExceptionHandler {

    /**
     * Обработка ошибок валидации (@Valid в контроллерах).
     *
     * Возвращает 400 Bad Request с детальным списком полей, которые не прошли валидацию.
     *
 * Пример ответа:
 * {
 *   "timestamp": "YYYY-MM-DDTHH:mm:ss",
 *   "status": 400,
 *   "errors": {
 *     "make": "не должно быть пустым",
 *     "year": "должно быть больше или равно 1980"
 *   }
 * }
     *
     * @param ex исключение с деталями ошибок валидации
     * @return ResponseEntity с кодом 400 и списком ошибок
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("errors", errors);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Обработка IllegalArgumentException.
     *
     * Обычно выбрасывается при:
     * - Невалидных датах бронирования (в прошлом, конец < начала)
     * - Попытке забронировать занятую машину
     * - Других логических ошибках в параметрах
     *
     * Возвращает 400 Bad Request.
     *
     * @param ex исключение с описанием ошибки
     * @return ResponseEntity с кодом 400 и сообщением об ошибке
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put("message", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Обработка AccessDeniedException.
     *
     * Выбрасывается когда пользователь пытается выполнить операцию,
     * на которую у него нет прав (например, клиент пытается отменить чужую бронь).
     *
     * Возвращает 403 Forbidden.
     *
     * @param ex исключение с описанием нарушения прав
     * @return ResponseEntity с кодом 403 и сообщением
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.FORBIDDEN.value());
        response.put("error", "Forbidden");
        response.put("message", "У вас нет прав для выполнения этой операции: " + ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Обработка RuntimeException (общие ошибки).
     *
     * Ловит непредвиденные ошибки, такие как:
     * - "Ресурс не найден"
     * - Ошибки работы с БД
     * - Другие внутренние ошибки
     *
     * Возвращает 500 Internal Server Error.
     *
     * ВАЖНО: В production лучше не отправлять клиенту полный текст ошибки
     * (может содержать внутренние детали), а логировать его на сервере.
     *
     * @param ex исключение
     * @return ResponseEntity с кодом 500 и сообщением об ошибке
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Обработка всех остальных исключений (fallback).
     *
     * Ловит любые необработанные исключения.
     * Возвращает 500 Internal Server Error с общим сообщением.
     *
     * @param ex любое исключение
     * @return ResponseEntity с кодом 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", "Произошла внутренняя ошибка сервера. Пожалуйста, попробуйте позже.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
