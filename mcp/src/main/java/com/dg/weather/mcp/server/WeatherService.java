package com.dg.weather.mcp.server;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import com.dg.weather.mcp.server.WeatherClient.Alert;
import com.dg.weather.mcp.server.WeatherClient.Forecast;

@Service
public class WeatherService {
    private final WeatherClient weatherClient;

    public WeatherService(WeatherClient wc) {
        this.weatherClient = wc;
    }

    @Tool(description = "Get weather alerts for a US state.")
    public String getAlerts(@ToolParam(description = "Two-letter US state code (e.g. CA, NY)") String state) {
        System.out.println("Calling getAlerts for " + state);
        var alerts = weatherClient.getAlert(state);
        
        return formatAlerts(alerts);
    }

    @Tool(description = "Get weather forecast for a location.")
    public String getForecast(
        @ToolParam(description = "Latitude of the location") double latitude,
        @ToolParam(description = "Longitude of the location") double longitude) {
        
        var points = weatherClient.getPoints(latitude, longitude);
        var forecast = weatherClient.getForecastByLocation(points);

        return formatForecast(forecast);
    }

    private String formatAlerts(Alert alerts) {
        return alerts.features().stream().map(feature -> {
                var properties = feature.properties();
                return 
                        """
                        Event: %s
                        Area: %s
                        Severity: %s
                        Description: %s
                        Instructions: %s
                        """.formatted(properties.event(), properties.areaDesc(),
                        properties.severity(), properties.description(), properties.instruction());
            }).collect(Collectors.joining("\n---\n"));
    }

    private String formatForecast(Forecast forecast) {
        return forecast.properties().periods().stream().map(period -> {

                return """
                    Temperature: %s°%s
                    Wind: %s %s
                    Forecast: %s
                    """.formatted(period.temperature(), period.temperatureUnit(),
                period.windSpeed(), period.windDirection(), period.detailedForecast());
            }).collect(Collectors.joining("\n---\n"));
    }

    public static void main(String[] args) {
        var weatherClient = new WeatherClient();
        var service = new WeatherService(weatherClient);
		System.out.println(service.getForecast(47.6062, -122.3321));
		System.out.println(service.getAlerts("FL"));
    }
}
