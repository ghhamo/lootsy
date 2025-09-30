package hamo.job.dto;

public record LoginResponse(String token, long expiresIn) {
}
