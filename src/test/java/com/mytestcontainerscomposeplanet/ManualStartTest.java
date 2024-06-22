package com.mytestcontainerscomposeplanet;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
class ManualStartTest {

    @Test
    void testApp() throws Exception {

        try (HttpClient client = HttpClient.newHttpClient()) {
            String baseUrl = "http://localhost:8080/FROST-Server/v1.1/Things";
            URI uri = URI.create(baseUrl);

            // JSON payload to be sent in the request body
            String postPayload = """
                {
                  "name" : "Kitchen",
                  "description" : "The Kitchen in my house",
                  "properties" : {
                    "oven" : true,
                    "heatingPlates" : 4
                  }
                }
            """;

            // Post the request
            HttpRequest post = HttpRequest.newBuilder()
                    .uri(uri)
                    .method("POST", HttpRequest.BodyPublishers.ofString(postPayload))
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> response = client.send(post, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, response.statusCode());

            // Get the Thing
            URI getUri = URI.create(baseUrl + "(1)");
            HttpRequest get = HttpRequest.newBuilder()
                    .uri(getUri)
                    .GET()
                    .header("Content-Type", "application/json")
                    .build();
            String expected = "{\"@iot.selfLink\":\"http://localhost:8080/FROST-Server/v1.1/Things(1)\",\"@iot.id\":1,\"name\":\"Kitchen\",\"description\":\"The Kitchen in my house\",\"properties\":{\"heatingPlates\":4,\"oven\":true},\"HistoricalLocations@iot.navigationLink\":\"http://localhost:8080/FROST-Server/v1.1/Things(1)/HistoricalLocations\",\"Locations@iot.navigationLink\":\"http://localhost:8080/FROST-Server/v1.1/Things(1)/Locations\",\"Datastreams@iot.navigationLink\":\"http://localhost:8080/FROST-Server/v1.1/Things(1)/Datastreams\"}";
            response = client.send(get, HttpResponse.BodyHandlers.ofString());
            assertEquals(expected, response.body());
        }
    }

}
