package com.dg.weather.mcp.server;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Component
public class WeatherClient {
    private static final String API_BASE_URL = "https://api.weather.gov";
    private final RestClient restClient;

    public WeatherClient() {
        this.restClient = RestClient.builder()
            .baseUrl(API_BASE_URL)
            .build();
    }

    public Points getPoints(double latitude, double longitude) {
        return restClient.get()
                .uri("/points/{latitude},{longitude}", latitude, longitude)
                .retrieve()
                .body(Points.class);
    }

    public Alert getAlert(String state) {
        return this.restClient.get()
                .uri("/alerts/active/area/%s".formatted(state))
                .retrieve()
                .body(Alert.class);
    }

    public Forecast getForecastByLocation(Points points) {
        return this.restClient.get()
            .uri(points.properties().forecast())
            .retrieve()
            .body(Forecast.class);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
	public record Points(Props properties) {
		@JsonIgnoreProperties(ignoreUnknown = true)
		public record Props(String forecast) {}
	}

    	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Forecast(Props properties) {
		@JsonIgnoreProperties(ignoreUnknown = true)
		public record Props(List<Period> periods) {
		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		public record Period(Integer number,String name,
				String startTime, String endTime,
				Boolean isDayTime, Integer temperature,
				String temperatureUnit,
				String temperatureTrend,
				Map probabilityOfPrecipitation,
				String windSpeed, String windDirection,
				String icon, String shortForecast,
				String detailedForecast) {
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Alert(List<Feature> features) {

		@JsonIgnoreProperties(ignoreUnknown = true)
		public record Feature(Properties properties) {
		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		public record Properties(String event, String areaDesc,
				String severity, String description,
				String instruction) {
		}
	}
    
}
