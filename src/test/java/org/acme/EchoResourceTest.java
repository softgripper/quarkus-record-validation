package org.acme;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestHTTPEndpoint(EchoResource.class)
class EchoResourceTest {
    @ParameterizedTest
    @ValueSource(strings = {"/echo-raw", "/echo-valid", "/echo-from-service"})
    void echo_returns200(String path) {
        var req = Map.of("message", "hello", "postcode", "1234");
        postThen(req, path).statusCode(200).body("$", is(req));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/echo-raw", "/echo-valid", "/echo-from-service"})
    void echo_invalidPayload_withViolationReport(String path) {
        var req = Map.of("message", "", "postcode", "12");
        postThen(req, path)
                .statusCode(400)
                .body("title", is("Constraint Violation"))
                .body("status", is(400))
                // We don't assert exact 'field' names to avoid brittleness.
                .body("violations.message", hasItems(
                        is("must be 4 digits"),
                        anyOf(is("must not be blank"), is("must not be empty"))
                ));
    }

    private static <T> ValidatableResponse postThen(T req, String path) {
        //@formatter:off
        return given()
                .contentType(ContentType.JSON)
                .body(req)
            .when()
                .post(path)
            .then();
        //@formatter:on
    }
}