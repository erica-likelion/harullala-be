package likelion.harullala.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import likelion.harullala.dto.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiResponse<Void>> handle(ApiException e) {
        return ResponseEntity.status(e.getStatus())
                .body(new ApiResponse<>(e.getStatus().value(), e.getMessage(), null));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleValidation() {
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(400, "Validation error", null));
    }
}