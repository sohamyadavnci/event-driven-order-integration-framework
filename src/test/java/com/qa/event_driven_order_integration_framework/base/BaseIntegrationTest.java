package com.qa.event_driven_order_integration_framework.base;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class BaseIntegrationTest {

    private static final KafkaContainer KAFKA_CONTAINER =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    protected static final WireMockServer WIREMOCK_SERVER =
            new WireMockServer(options().dynamicPort());

    static {
        KAFKA_CONTAINER.start();
        WIREMOCK_SERVER.start();
    }

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
        registry.add("payment.service.base-url", WIREMOCK_SERVER::baseUrl);
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        WIREMOCK_SERVER.resetAll();

        WIREMOCK_SERVER.stubFor(post(urlEqualTo("/payments/authorize"))
                .willReturn(okJson("""
                        {
                          "paymentStatus": "APPROVED",
                          "message": "Payment approved"
                        }
                        """)));
    }

    protected static String getKafkaBootstrapServers() {
        return KAFKA_CONTAINER.getBootstrapServers();
    }
}