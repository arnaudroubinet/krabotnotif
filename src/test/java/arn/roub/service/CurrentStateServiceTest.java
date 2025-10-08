package arn.roub.service;

import arn.roub.krabot.scrapper.CurrentState;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Test suite for CurrentStateService REST endpoint.
 * Tests the REST API that exposes application state.
 */
@QuarkusTest
@DisplayName("CurrentStateService REST Endpoint Tests")
class CurrentStateServiceTest {

    @InjectMock
    CurrentState currentState;

    @BeforeEach
    void setup() {
        // Setup default mock behavior
        when(currentState.getNbkramail()).thenReturn(0);
        when(currentState.getHasNotification()).thenReturn(false);
        when(currentState.getCurrentVersion()).thenReturn("v2.4.10");
        when(currentState.getLatestVersion()).thenReturn("v2.4.10");
    }

    @Test
    @DisplayName("Should return current state with HTTP 200")
    void shouldReturnCurrentState() {
        // Given
        when(currentState.getNbkramail()).thenReturn(3);
        when(currentState.getHasNotification()).thenReturn(true);
        when(currentState.getCurrentVersion()).thenReturn("v2.4.10");
        when(currentState.getLatestVersion()).thenReturn("v2.5.0");

        // When & Then
        given()
            .when()
                .get("/krabot/state")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("nbKramail", equalTo(3))
                .body("hasNotification", equalTo(true))
                .body("currentVersion", equalTo("v2.4.10"))
                .body("latestVersion", equalTo("v2.5.0"));
    }

    @Test
    @DisplayName("Should handle null kramail count")
    void shouldHandleNullKramailCount() {
        // Given
        when(currentState.getNbkramail()).thenReturn(null);

        // When & Then
        given()
            .when()
                .get("/krabot/state")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("nbKramail", equalTo(0));
    }

    @Test
    @DisplayName("Should handle null notification flag")
    void shouldHandleNullNotificationFlag() {
        // Given
        when(currentState.getHasNotification()).thenReturn(null);

        // When & Then
        given()
            .when()
                .get("/krabot/state")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("hasNotification", equalTo(false));
    }

    @Test
    @DisplayName("Should return zero kramails when none exist")
    void shouldReturnZeroKramails() {
        // When & Then
        given()
            .when()
                .get("/krabot/state")
            .then()
                .statusCode(200)
                .body("nbKramail", equalTo(0))
                .body("hasNotification", equalTo(false));
    }

    @Test
    @DisplayName("Should return correct content type")
    void shouldReturnJsonContentType() {
        // When & Then
        given()
            .when()
                .get("/krabot/state")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }
}
