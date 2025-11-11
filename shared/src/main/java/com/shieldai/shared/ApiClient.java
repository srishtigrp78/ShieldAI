package com.shieldai.shared;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Hardened API client with retry logic and fallback mechanisms
 */
public class ApiClient {
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final int maxRetries;
    private final long retryDelayMs;
    
    public ApiClient(String baseUrl) {
        this(baseUrl, 3, 1000);
    }
    
    public ApiClient(String baseUrl, int maxRetries, long retryDelayMs) {
        this.baseUrl = baseUrl;
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Send detection report with retry logic
     */
    public CompletableFuture<Boolean> sendDetectionAsync(DetectionReport report) {
        return CompletableFuture.supplyAsync(() -> {
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    String json = objectMapper.writeValueAsString(report);
                    
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(baseUrl + "/api/detections"))
                            .header("Content-Type", "application/json")
                            .timeout(Duration.ofSeconds(30))
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();
                    
                    HttpResponse<String> response = httpClient.send(request, 
                            HttpResponse.BodyHandlers.ofString());
                    
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        System.out.println("✅ Detection sent successfully (attempt " + attempt + ")");
                        return true;
                    } else {
                        System.err.println("❌ Server error " + response.statusCode() + " (attempt " + attempt + ")");
                    }
                    
                } catch (ConnectException | SocketTimeoutException e) {
                    System.err.println("🔄 Connection failed (attempt " + attempt + "): " + e.getMessage());
                } catch (IOException | InterruptedException e) {
                    System.err.println("❌ Request failed (attempt " + attempt + "): " + e.getMessage());
                }
                
                // Wait before retry (exponential backoff)
                if (attempt < maxRetries) {
                    try {
                        long delay = retryDelayMs * (long) Math.pow(2, attempt - 1);
                        TimeUnit.MILLISECONDS.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            
            System.err.println("💾 All retry attempts failed - detection will be stored locally");
            return false;
        });
    }
    
    /**
     * Test connection to backend
     */
    public boolean testConnection() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/health"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
            
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}