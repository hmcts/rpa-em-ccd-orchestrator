package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.Response;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OpenIdConnectScenarios extends BaseTest {

    public static final String API_STITCH_CCD_BUNDLES = "/api/stitch-ccd-bundles";

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void testValidAuthenticationAndAuthorisation() throws IOException {
        String wrappedJson = caseBundleJsonPayload();

        Response response =
                testUtil
                        .authRequest()
                        .baseUri(testUtil.getTestUrl())
                        .contentType(APPLICATION_JSON_VALUE)
                        .body(wrappedJson)
                        .post(API_STITCH_CCD_BUNDLES);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    // Invalid S2SAuth
    public void testInvalidS2SAuth() throws IOException {
        String wrappedJson = caseBundleJsonPayload();

        Response response =
                testUtil
                        .invalidS2SAuth()
                        .baseUri(testUtil.getTestUrl())
                        .contentType(APPLICATION_JSON_VALUE)
                        .body(wrappedJson)
                        .post(API_STITCH_CCD_BUNDLES);

        assertEquals(response.getStatusCode(), 401);
    }

    @Test
    // Invalid S2SAuth
    public void testWithInvalidIdamAuth() throws IOException {
        String wrappedJson = caseBundleJsonPayload();

        Response response =
                testUtil
                        .invalidS2SAuth()
                        .baseUri(testUtil.getTestUrl())
                        .contentType(APPLICATION_JSON_VALUE)
                        .body(wrappedJson)
                        .post(API_STITCH_CCD_BUNDLES);

        assertEquals(response.getStatusCode(), 401);
    }

    @Test
    // Empty IdamAuth and Valid S2S Auth
    public void testWithEmptyIdamAuthAndValidS2SAuth() throws IOException {
        String wrappedJson = caseBundleJsonPayload();

        assertThrows(NullPointerException.class, () -> testUtil.validS2SAuthWithEmptyIdamAuth()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .post("/api/stitch-ccd-bundles"));

    }

    @Test
    // Empty IdamAuth and Empty S2SAuth
    public void testIdamAuthAndS2SAuthAreEmpty() throws IOException {
        String wrappedJson = caseBundleJsonPayload();

        assertThrows(NullPointerException.class, () -> testUtil.emptyIdamAuthAndEmptyS2SAuth()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .post("/api/stitch-ccd-bundles"));

    }

    private String caseBundleJsonPayload() throws JsonProcessingException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        return String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);
    }

}

