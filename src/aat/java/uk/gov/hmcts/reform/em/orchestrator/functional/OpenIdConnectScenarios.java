package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.Response;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

import static org.junit.Assert.assertEquals;

public class OpenIdConnectScenarios extends BaseTest {

    public static final String API_STITCH_CCD_BUNDLES = "/api/stitch-ccd-bundles";

    @Rule
    public ExpectedException exceptionThrown = ExpectedException.none();

    @Test
    public void testValidAuthenticationAndAuthorisation() throws IOException {
        String wrappedJson = caseBundleJsonPayload();

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .request("POST",testUtil.getTestUrl() + API_STITCH_CCD_BUNDLES);

        assertEquals(200,response.getStatusCode());

    }

    @Test
    // Invalid S2SAuth
    public void testInvalidS2SAuth() throws IOException, InterruptedException {
        String wrappedJson = caseBundleJsonPayload();

        Response response = testUtil.invalidS2SAuth()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .request("POST",testUtil.getTestUrl() + API_STITCH_CCD_BUNDLES);

        assertEquals(response.getStatusCode(),401);
    }

    @Test
    // Invalid S2SAuth
    public void testWithInvalidIdamAuth() throws IOException, InterruptedException {
        String wrappedJson = caseBundleJsonPayload();

        Response response = testUtil.invalidS2SAuth()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .request("POST", testUtil.getTestUrl() + API_STITCH_CCD_BUNDLES);

        assertEquals(response.getStatusCode(),401);
    }

    @Test
    // Empty IdamAuth and Valid S2S Auth
    public void testWithEmptyIdamAuthAndValidS2SAuth() throws IOException, InterruptedException {
        String wrappedJson = caseBundleJsonPayload();

        exceptionThrown.expect(IllegalArgumentException.class);

        Response response = testUtil.validS2SAuthWithEmptyIdamAuth()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .request("POST", testUtil.getTestUrl() + "/api/stitch-ccd-bundles");

    }

    @Test
    // Empty IdamAuth and Empty S2SAuth
    public void testIdamAuthAndS2SAuthAreEmpty() throws IOException, InterruptedException {
        String wrappedJson = caseBundleJsonPayload();

        exceptionThrown.expect(IllegalArgumentException.class);

        Response response = testUtil.emptyIdamAuthAndEmptyS2SAuth()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .request("POST",testUtil.getTestUrl() + "/api/stitch-ccd-bundles");

    }


    private String caseBundleJsonPayload() throws JsonProcessingException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        return String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);
    }

}
