package uk.gov.hmcts.reform.em.orchestrator.functional;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.em.orchestrator.config.Constants;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBoolean;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class CcdStitchScenarios extends BaseTest {

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void testPostBundleStitch() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        Response response = postStitchCCDBundle(wrappedJson);

        JsonPath path = response.getBody().jsonPath();
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("Bundle title", path.getString("data.caseBundles[0].value.title"));
        Assert.assertNotNull(path.getString("data.caseBundles[0].value.stitchedDocument.document_url"));
    }

    @Test
    public void testPostBundleStitchWithWordDoc() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundleWithWordDoc();
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        Response response = postStitchCCDBundle(wrappedJson);

        JsonPath path = response.getBody().jsonPath();
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("Bundle title", path.getString("data.caseBundles[0].value.title"));
        Assert.assertNotNull(path.getString("data.caseBundles[0].value.stitchedDocument.document_url"));
    }

    @Test
    public void testSpecificFilename() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setFileName("my-file-name.pdf");

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        Response response = postStitchCCDBundle(wrappedJson);

        JsonPath path = response.getBody().jsonPath();
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("Bundle title", path.getString("data.caseBundles[0].value.title"));
        Assert.assertEquals("my-file-name.pdf", path.getString("data.caseBundles[0].value.fileName"));
        Assert.assertNotNull(path.getString("data.caseBundles[0].value.stitchedDocument.document_url"));
    }

    @Test
    public void testFilenameWithoutExtension() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setFileName("doc-file-name");

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        Response response = postStitchCCDBundle(wrappedJson);

        JsonPath path = response.getBody().jsonPath();
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("doc-file-name.pdf", path.getString("data.caseBundles[0].value.stitchedDocument.document_filename"));
        Assert.assertEquals("doc-file-name", path.getString("data.caseBundles[0].value.fileName"));
    }

    @Test
    public void testPostBundleStitchFileNameOneChar() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setFileName("a");

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        Response response = postStitchCCDBundle(wrappedJson);

        JsonPath path = response.getBody().jsonPath();
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertNotNull(path.getString("errors[0]"));
        Assert.assertEquals(Constants.STITCHED_FILE_NAME_FIELD_LENGTH_ERROR_MSG, path.getString("errors[0]"));
    }

    @Test
    public void testPostBundleStitchFileName51Char() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setFileName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        Response response = postStitchCCDBundle(wrappedJson);

        JsonPath path = response.getBody().jsonPath();
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertNotNull(path.getString("errors[0]"));
        Assert.assertEquals(Constants.STITCHED_FILE_NAME_FIELD_LENGTH_ERROR_MSG, path.getString("errors[0]"));
    }

    @Test
    public void testNoFileNameButBundleTitleOnly() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundleWithWordDoc();

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        Response response = postStitchCCDBundle(wrappedJson);

        JsonPath path = response.getBody().jsonPath();
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("Bundle title", path.getString("data.caseBundles[0].value.title"));
        Assert.assertEquals("Bundle title.pdf", path.getString("data.caseBundles[0].value.stitchedDocument.document_filename"));
    }

    @Test
    public void testFilenameErrors() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setFileName("1234567890123456789012345678901%.pdf");

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        Response response = postStitchCCDBundle(wrappedJson);

        JsonPath path = response.getBody().jsonPath();
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("Bundle title", path.getString("data.caseBundles[0].value.title"));
        Assert.assertEquals("1234567890123456789012345678901%.pdf", path.getString("data.caseBundles[0].value.fileName"));
        Assert.assertNotNull(path.getString("errors[0]"));
    }

    @Test
    public void testLongBundleDescriptionErrors() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();

        StringBuilder sample = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            sample.append("y");
        }
        bundle.setDescription(sample.toString());
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        Response response = postStitchCCDBundle(wrappedJson);

        JsonPath path = response.getBody().jsonPath();
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertNotNull(path.getString("errors[0]"));
    }

    @Test
    public void testWithoutCoversheets() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setHasCoversheets(CcdBoolean.No);

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        Response response = postStitchCCDBundle(wrappedJson);

        JsonPath path = response.getBody().jsonPath();
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("Bundle title", path.getString("data.caseBundles[0].value.title"));
        Assert.assertEquals("No", path.getString("data.caseBundles[0].value.hasCoversheets"));
        Assert.assertNotNull(path.getString("data.caseBundles[0].value.stitchedDocument.document_url"));
    }

    @Test
    public void testWithImageRendering() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundleWithImageRendered();
        bundle.setHasCoversheets(CcdBoolean.No);

        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        Response response = postStitchCCDBundle(wrappedJson);

        JsonPath path = response.getBody().jsonPath();
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("schmcts.png", path.getString("data.caseBundles[0].value.documentImage.docmosisAssetId"));
        Assert.assertEquals("firstPage", path.getString("data.caseBundles[0].value.documentImage.imageRenderingLocation"));
        Assert.assertEquals("translucent", path.getString("data.caseBundles[0].value.documentImage.imageRendering"));
        Assert.assertEquals(50, path.getInt("data.caseBundles[0].value.documentImage.coordinateX"));
        Assert.assertEquals(50, path.getInt("data.caseBundles[0].value.documentImage.coordinateY"));
    }

    private Response postStitchCCDBundle(String wrappedJson) {
        return testUtil
                .authRequest()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .post("/api/stitch-ccd-bundles");
    }
}
