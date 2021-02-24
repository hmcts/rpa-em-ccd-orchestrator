package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class AutomatedBundlingScenarios extends BaseTest {

    private static JsonNode validJson;
    private static JsonNode invalidJson;
    private static JsonNode filenameJson;
    private static JsonNode invalidConfigJson;

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Before
    public void setup() throws Exception {
        validJson = extendedCcdHelper.loadCaseFromFile("automated-case.json");
        invalidJson = extendedCcdHelper.loadCaseFromFile("invalid-automated-case.json");
        filenameJson = extendedCcdHelper.loadCaseFromFile("filename-case.json");
        invalidConfigJson = extendedCcdHelper.loadCaseFromFile("automated-case-invalid-configuration.json");
    }

    @Test
    public void testCreateBundle() {
        Response response = postNewBundle(validJson);

        assertEquals(200, response.getStatusCode());
        assertEquals("New bundle", response.getBody().jsonPath().getString("data.caseBundles[0].value.title"));
        assertEquals("Folder 1", response.getBody().jsonPath().getString("data.caseBundles[0].value.folders[0].value.name"));
        assertEquals("Folder 1.a", response.getBody().jsonPath().getString("data.caseBundles[0].value.folders[0].value.folders[0].value.name"));
        assertEquals("Folder 1.b", response.getBody().jsonPath().getString("data.caseBundles[0].value.folders[0].value.folders[1].value.name"));
        assertEquals("Folder 2", response.getBody().jsonPath().getString("data.caseBundles[0].value.folders[1].value.name"));
        assertEquals("stitched.pdf", response.getBody().jsonPath().getString("data.caseBundles[0].value.fileName"));
    }

    @Test
    public void testInvalidConfig() {
        Response response = postNewBundle(invalidJson);

        assertEquals(200, response.getStatusCode());
        assertEquals("Invalid configuration file entry in: does-not-exist.yaml" + "; Configuration file parameter(s) and/or parameter value(s)",
                response.getBody().jsonPath().getString("errors[0]"));
    }

    @Test
    public void testCorruptConfig() {
        Response response = postNewBundle(invalidConfigJson);

        assertEquals(200, response.getStatusCode());
        assertEquals("Invalid configuration file entry in: example-incorrect-key.yaml" + "; Configuration file parameter(s) and/or parameter value(s)",
                response.getBody().jsonPath().getString("errors[0]"));
    }

    @Test
    public void testFilename() {
        Response response = postNewBundle(filenameJson);

        assertEquals(200, response.getStatusCode());
        assertEquals("Bundle with filename", response.getBody().jsonPath().getString("data.caseBundles[0].value.title"));
        assertEquals("bundle.pdf", response.getBody().jsonPath().getString("data.caseBundles[0].value.fileName"));
    }

    @Test
    public void testTableOfContentsAndCoversheet() {
        Response response = postNewBundle(validJson);

        assertEquals(200, response.getStatusCode());
        assertEquals("Yes", response.getBody().jsonPath().getString("data.caseBundles[0].value.hasCoversheets"));
        assertEquals("Yes", response.getBody().jsonPath().getString("data.caseBundles[0].value.hasTableOfContents"));
        assertEquals("No", response.getBody().jsonPath().getString("data.caseBundles[0].value.hasFolderCoversheets"));
    }

    @Test
    public void testFolderCoversheets() {
        Response response = postNewBundle(filenameJson);

        assertEquals(200, response.getStatusCode());
        assertEquals("No", response.getBody().jsonPath().getString("data.caseBundles[0].value.hasCoversheets"));
        assertEquals("No", response.getBody().jsonPath().getString("data.caseBundles[0].value.hasTableOfContents"));
        assertEquals("Yes", response.getBody().jsonPath().getString("data.caseBundles[0].value.hasFolderCoversheets"));
    }

    @Test
    public void testSubSubfolders() {
        Response response = postNewBundle(validJson);

        assertEquals(200, response.getStatusCode());
        Assert.assertNull(response.getBody().jsonPath().getString("data.caseBundles[0].value.folders[0].value.folders[0].value.folders"));
        Assert.assertNotNull(response.getBody().jsonPath().getString("data.caseBundles[0].value.folders[0].value.folders[0].value.documents"));
    }

    @Test
    public void testAddFlatDocuments() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-1-flat-docs.yaml");

        Response response = postNewBundle(json);

        JsonPath responsePath = response.jsonPath();

        System.out.println(response.getBody().prettyPrint());
        assertEquals(200, response.getStatusCode());
        assertEquals(4, responsePath.getList("data.caseBundles[0].value.documents").size());
        assertEquals("Prosecution doc 1", responsePath.getString("data.caseBundles[0].value.documents[0].value.name"));
        assertEquals("Prosecution doc 2", responsePath.getString("data.caseBundles[0].value.documents[1].value.name"));
        assertEquals("Evidence doc", responsePath.getString("data.caseBundles[0].value.documents[2].value.name"));
        assertEquals("Defendant doc 1", responsePath.getString("data.caseBundles[0].value.documents[3].value.name"));
    }

    @Test
    public void testAddFlatFilteredDocuments() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-2-filter-flat-docs.yaml");

        Response response = postNewBundle(json);

        JsonPath responsePath = response.jsonPath();

        assertEquals(200, response.getStatusCode());
        assertEquals(2, responsePath.getList("data.caseBundles[0].value.documents").size());
        assertEquals("Prosecution doc 1", responsePath.getString("data.caseBundles[0].value.documents[0].value.name"));
        assertEquals("Prosecution doc 2", responsePath.getString("data.caseBundles[0].value.documents[1].value.name"));
    }

    @Test
    public void testAddFolderedDocuments() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-3-foldered-docs.yaml");

        Response response = postNewBundle(json);

        JsonPath responsePath = response.jsonPath();

        assertEquals(200, response.getStatusCode());
        assertEquals(2, responsePath.getList("data.caseBundles[0].value.folders").size());
        assertEquals(4, responsePath.getList("data.caseBundles[0].value.folders[0].value.documents").size());
        assertEquals("Prosecution doc 1", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[0].value.name"));
        assertEquals("Prosecution doc 2", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[1].value.name"));
        assertEquals("Defendant doc 1", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[2].value.name"));
        assertEquals("Evidence doc", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[3].value.name"));
        assertEquals(1, responsePath.getList("data.caseBundles[0].value.folders[1].value.documents").size());
        assertEquals("Single doc 1", responsePath.getString("data.caseBundles[0].value.folders[1].value.documents[0].value.name"));
    }

    @Test
    public void testAddFilteredFolderedDocuments() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-4-filtered-foldered-docs.yaml");

        Response response = postNewBundle(json);

        JsonPath responsePath = response.jsonPath();

        assertEquals(200, response.getStatusCode());
        assertEquals(2, responsePath.getList("data.caseBundles[0].value.folders").size());
        assertEquals(2, responsePath.getList("data.caseBundles[0].value.folders[0].value.documents").size());
        assertEquals("Prosecution doc 1", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[0].value.name"));
        assertEquals("Prosecution doc 2", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[1].value.name"));
        assertEquals(1, responsePath.getList("data.caseBundles[0].value.folders[1].value.documents").size());
        assertEquals("Single doc 1", responsePath.getString("data.caseBundles[0].value.folders[1].value.documents[0].value.name"));
    }

    @Test
    public void testTypoInConfigurationFile() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-6-has-typo.yaml");

        Response response = postNewBundle(json);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testDefaultFallBackConfigurationFile() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");

        Response response = postNewBundle(json);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testDocumentNotPresent() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-12-invalid-document-property.yaml");

        Response response = postNewBundle(json);

        JsonPath responsePath = response.jsonPath();
        assertEquals(200, response.getStatusCode());
        assertEquals(2, responsePath.getList("data.caseBundles[0].value.folders[0].value.documents").size());
    }

    @Test
    public void testDocumentPropertyIsAnArray() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-7-not-a-single-doc.yaml");

        Response response = postNewBundle(json);

        assertTrue(response.prettyPrint().contains("Element is an array: /caseDocuments"));
    }

    @Test
    public void testDocumentSetPropertyIsNotAnArray() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-8-not-an-array.yaml");

        Response response = postNewBundle(json);

        assertTrue(response.prettyPrint().contains("Element is not an array: /singleDocument"));
    }

    @Test
    public void testDocumentStructureCorrupted() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("document_url", "incorrect_property_name");
        json = json.replaceAll("configurationFile", "f-tests-5-invalid-url.yaml");

        Response response = postNewBundle(json);

        assertTrue(response.prettyPrint().contains("Could not find the property /documentLink/document_url in the node"));
    }

    @Test
    public void testConfigurationFileDoesNotExist() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "nonexistent.yaml");

        Response response = postNewBundle(json);

        assertTrue(response.getBody().print().contains("Invalid configuration file entry in: nonexistent.yaml"));
    }

    @Test
    public void testMultipleFilters() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-9-multiple-filters.yaml");

        Response response = postNewBundle(json);

        JsonPath responsePath = response.jsonPath();

        assertEquals(200, response.getStatusCode());
        assertEquals(1, responsePath.getList("data.caseBundles[0].value.folders").size());
        assertEquals(3, responsePath.getList("data.caseBundles[0].value.folders[0].value.documents").size());
        assertEquals("Prosecution doc 1", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[0].value.name"));
        assertEquals("Prosecution doc 2", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[1].value.name"));
        assertEquals("Evidence doc", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[2].value.name"));
    }

    @Test
    public void testSortDocumentsAscending() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-10-sorting.yaml");

        Response response = postNewBundle(json);

        JsonPath responsePath = response.jsonPath();

        assertEquals(200, response.getStatusCode());
        assertEquals(2, responsePath.getList("data.caseBundles[0].value.folders").size());
        assertEquals(4, responsePath.getList("data.caseBundles[0].value.folders[0].value.documents").size());
        assertEquals("Prosecution doc 1", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[0].value.name"));
        assertEquals("Prosecution doc 2", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[1].value.name"));
        assertEquals("Evidence doc", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[2].value.name"));
        assertEquals("Defendant doc 1", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[3].value.name"));
        assertEquals(1, responsePath.getList("data.caseBundles[0].value.folders[1].value.documents").size());
        assertEquals("Single doc 1", responsePath.getString("data.caseBundles[0].value.folders[1].value.documents[0].value.name"));
    }

    @Test
    public void testSortDocumentsDescending() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-11-sorting.yaml");

        Response response = postNewBundle(json);

        JsonPath responsePath = response.jsonPath();

        assertEquals(200, response.getStatusCode());
        assertEquals(2, responsePath.getList("data.caseBundles[0].value.folders").size());
        assertEquals(4, responsePath.getList("data.caseBundles[0].value.folders[0].value.documents").size());
        assertEquals("Defendant doc 1", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[0].value.name"));
        assertEquals("Evidence doc", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[1].value.name"));
        assertEquals("Prosecution doc 2", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[2].value.name"));
        assertEquals("Prosecution doc 1", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[3].value.name"));
        assertEquals(1, responsePath.getList("data.caseBundles[0].value.folders[1].value.documents").size());
        assertEquals("Single doc 1", responsePath.getString("data.caseBundles[0].value.folders[1].value.documents[0].value.name"));
    }

    @Test
    public void testEnableEmailNotificationIsNull() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");

        Response response = postNewBundle(validJson);

        JsonPath responsePath = response.jsonPath();

        assertEquals(200, response.getStatusCode());
        assertNull(responsePath.getString("data.caseBundles[0].value.enableEmailNotification"));

    }

    @Test
    public void testRenderImageInStitchedDocument() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-13-render-image-flat-docs.yaml");

        Response response = postNewBundle(json);

        JsonPath responsePath = response.jsonPath();

        assertEquals(200, response.getStatusCode());
        Assert.assertEquals("hmcts.png", responsePath.getString("data.caseBundles[0].value.documentImage.docmosisAssetId"));
        Assert.assertEquals("allPages", responsePath.getString("data.caseBundles[0].value.documentImage.imageRenderingLocation"));
        Assert.assertEquals("opaque", responsePath.getString("data.caseBundles[0].value.documentImage.imageRendering"));
        Assert.assertEquals(50, responsePath.getInt("data.caseBundles[0].value.documentImage.coordinateX"));
        Assert.assertEquals(50, responsePath.getInt("data.caseBundles[0].value.documentImage.coordinateY"));
    }

    private Response postNewBundle(Object requestBody) {
        return testUtil
                .authRequest()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE)
                .body(requestBody)
                .post("/api/new-bundle");
    }
}
