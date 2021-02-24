package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

public class AutomatedBundlingWithCallbacks extends BaseTest {

    private static final int WAIT_SECONDS = 60;

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void testSuccessfulAsyncStitching() throws Exception {
        String uploadedUrl = testUtil.uploadDocument();
        String documentString = extendedCcdHelper.getCcdDocumentJson("my doc text", uploadedUrl, "mydoc.txt");
        String caseId = extendedCcdHelper.createCase(documentString).getId().toString();
        extendedCcdHelper.triggerEvent(caseId, "createBundle");
        int i = 0;
        while (i < WAIT_SECONDS) {
            JsonNode caseJson = extendedCcdHelper.getCase(caseId);
            if (!caseJson.findPath("stitchStatus").asText().equals("NEW")) {
                Assert.assertEquals("DONE", caseJson.findPath("stitchStatus").asText());
                Assert.assertEquals("null", caseJson.findPath("stitchingFailureMessage").asText());
                break;
            }
            Thread.sleep(1000);
            System.out.println("waiting");
            i++;
        }
        if (i >= WAIT_SECONDS) {
            Assert.fail("Status was not retrieved.");
        }
    }

    @Test
    public void testUnSuccessfulAsyncStitching() throws Exception {
        String uploadedUrl = testUtil.uploadDocument("dm-text.csv", "text/csv");
        String documentString = extendedCcdHelper.getCcdDocumentJson("my doc text", uploadedUrl, "mydoc.txt");
        String caseId = extendedCcdHelper.createCase(documentString).getId().toString();
        extendedCcdHelper.triggerEvent(caseId, "createBundle");
        int i = 0;
        while (i < WAIT_SECONDS) {
            JsonNode caseJson = extendedCcdHelper.getCase(caseId);
            System.out.println(String.format("Testing %s - %s", caseId, caseJson.toPrettyString()));
            if (!caseJson.findPath("stitchStatus").asText().equals("NEW")) {
                Assert.assertEquals("FAILED", caseJson.findPath("stitchStatus").asText());
                Assert.assertEquals("Unknown file type: text/csv", caseJson.findPath("stitchingFailureMessage").asText());
                break;
            }
            Thread.sleep(1000);
            i++;
            System.out.println("waiting");
        }
        if (i >= WAIT_SECONDS) {
            Assert.fail("Status was not retrieved.");
        }
    }
}
