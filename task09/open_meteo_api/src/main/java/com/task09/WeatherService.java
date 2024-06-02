package com.task09;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherService {
    private static final String API_URL = "https://api.open-meteo.com/v1/forecast?latitude=40.1792&longitude=44.4991&hourly=temperature_2m";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String getWeatherForecast() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new IOException("Error retrieving weather forecast: " + response.statusCode());
        }
    }
}
