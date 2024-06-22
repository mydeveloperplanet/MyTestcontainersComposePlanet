package com.mytestcontainerscomposeplanet;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Unit test for simple App.
 */
@Testcontainers
class TestcontainersComposeTest {

    @Container
    private static final DockerComposeContainer environment =
            new DockerComposeContainer(new File("src/test/resources/compose.yaml"))
                    .withExposedService("database", 5432)
                    .withExposedService("web",
                            8080,
                            Wait.forLogMessage(".*org.apache.catalina.startup.Catalina.start Server startup in.*\\n",
                                    1));

    @Test
    void testApp() throws Exception {

        try (HttpClient client = HttpClient.newHttpClient()) {
            String frostWebUrl = environment.getServiceHost("web", 8080) + ":" + environment.getServicePort("web", 8080);
            String baseUrl = "http://" + frostWebUrl + "/FROST-Server/v1.1/Things";
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
