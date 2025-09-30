package hamo.job.dto;

public record UserStatsDTO(int totalOrders, double totalSpent) {
    
    public UserStatsDTO {
        if (totalOrders < 0) {
            throw new IllegalArgumentException("Total orders cannot be negative");
        }
        if (totalSpent < 0) {
            throw new IllegalArgumentException("Total spent cannot be negative");
        }
    }
}
