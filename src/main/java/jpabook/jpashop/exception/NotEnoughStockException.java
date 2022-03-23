package jpabook.jpashop.exception;

/**
 * 1. RuntimeException : 실행시 발생하는 예외 -> 발생할지 안할지 불분명할 경우 (Unchecked)
 * 2. Exception : 컴파일시 발생하는 예외 -> 예외가 이미 예측 가능할 경우 (Checked)
 */
public class NotEnoughStockException extends RuntimeException {

    public NotEnoughStockException() {
        super();
    }

    public NotEnoughStockException(String message) {
        super(message);
    }

    public NotEnoughStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotEnoughStockException(Throwable cause) {
        super(cause);
    }
}
