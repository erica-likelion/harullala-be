package likelion.harullala.dto;

import lombok.Getter;

@Getter
public class ApiSuccess<T> {
    private final T data;
    private final String message;

    private ApiSuccess(T data, String message) {
        this.data = data;
        this.message = message;
    }

    public static <T> ApiSuccess<T> of(T data, String message) {
        return new ApiSuccess<>(data, message);
    }
}
