package org.acme;

import io.quarkus.logging.Log;
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
    @ValueSource(strings = {"/echo-raw", "/echo-valid", "/echo-from-service", "/echo-internal"})
    void echo_returns200(String path) {
        var request = Map.of("message", "hello", "postcode", "1234");
        var response = postThen(request, path).statusCode(200).body("$", is(request));
        Log.info(response.extract().body().asString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/echo-raw", "/echo-valid", "/echo-from-service", "/echo-internal"})
    void echo_invalidPayload_withViolationReport(String path) {
        var request = Map.of("message", "", "postcode", "");
        var response = postThen(request, path)
                .statusCode(400)
                .body("title", is("Constraint Violation"))
                .body("status", is(400))
                // We don't assert exact 'field' names to avoid brittleness.
                .body("violations.message", hasItems(
                        is("must be 4 digits"),
                        anyOf(is("must not be blank"), is("must not be empty"))
                ));
        Log.info(response.extract().body().asString());
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