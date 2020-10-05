package uk.gov.hmcts.reform.em.orchestrator.smoke;

import io.restassured.RestAssured;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.em.EmTestConfig;

@SpringBootTest(classes = {EmTestConfig.class})
@PropertySource(value = "classpath:application.yml")
@RunWith(SpringRunner.class)
public class SmokeTest {

    private static final String MESSAGE = "Welcome to EM Ccd Orchestrator API!";

    @Value("${test.url}")
    private String testUrl;

    @Test
    public void testHealthEndpoint() {

        RestAssured.useRelaxedHTTPSValidation();

        String response = RestAssured.given()
            .request("GET", testUrl + "/")
            .then()
            .statusCode(200).extract().body().asString();

        Assert.assertEquals(MESSAGE, response);


    }
}
