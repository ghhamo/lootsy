package hamo.job.exception.exceptions.orderException;

public class OrderTotalMismatchException extends RuntimeException {
    public OrderTotalMismatchException(double calculatedTotal, double totalAmount) {
        super("Order total mismatch. Expected: " + calculatedTotal + ", Provided: " + totalAmount);
    }
}
