package uk.gov.hmcts.reform.em.orchestrator.smoke;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.em.EmTestConfig;

@SpringBootTest(classes = {EmTestConfig.class})
@TestPropertySource(value = "classpath:application.yml")
@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Smoke")})
public class SmokeTest {

    private static final String MESSAGE = "Welcome to EM Ccd Orchestrator API!";

    @Value("${test.url}")
    private String testUrl;

    @Test
    public void testHealthEndpoint() {

        SerenityRest.useRelaxedHTTPSValidation();

        String response =
                SerenityRest
                        .given()
                        .baseUri(testUrl)
                        .get("/")
                        .then()
                        .statusCode(200).extract().body().asString();

        Assert.assertEquals(MESSAGE, response);


    }
}
