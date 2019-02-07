package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.BundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.testutil.Env;
import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;

import java.io.IOException;

public class CcdStitchScenarios {

    private final TestUtil testUtil = new TestUtil();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testPostBundleStitch() throws IOException {
        BundleDTO bundle = testUtil.getTestBundle();
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .request("POST", Env.getTestUrl() + "/api/stitch-cdd-bundles");

        Assert.assertEquals(200, response.getStatusCode());
        response.getBody().prettyPrint();
        JsonPath path = response.getBody().jsonPath();
        Assert.assertEquals("Bundle title", path.getString("data.caseBundles[0].value.bundleTitle"));
        Assert.assertNotNull(path.getString("data.caseBundles[0].value.stitchedDocumentURI"));
    }

    @Test
    public void testPostBundleStitchWithWordDoc() throws IOException {
        BundleDTO bundle = testUtil.getTestBundleWithWordDoc();
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        Response response = testUtil.authRequest()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .body(wrappedJson)
            .request("POST", Env.getTestUrl() + "/api/stitch-cdd-bundles");

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("Bundle title", response.getBody().jsonPath().getString("data.caseBundles[0].value.bundleTitle"));
        Assert.assertNotNull(response.getBody().jsonPath().getString("data.caseBundles[0].value.stitchedDocumentURI"));
    }

}
