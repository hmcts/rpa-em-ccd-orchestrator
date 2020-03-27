package uk.gov.hmcts.reform.em.orchestrator.functional;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CcdCloneScenarios extends BaseTest {

    @Test
    public void testSingleBundleClone() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setEligibleForCloningAsBoolean(true);
        List<CcdValue<CcdBundleDTO>> list = new ArrayList<>();
        list.add(new CcdValue<>(bundle));
        String json = mapper.writeValueAsString(list);
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\": %s } } }", json);

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .request("POST", testUtil.getTestUrl() + "/api/clone-ccd-bundles");

        JsonPath path = response.getBody().jsonPath();

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("CLONED_Bundle title", path.getString("data.caseBundles[0].value.title"));
        Assert.assertEquals("no", path.getString("data.caseBundles[0].value.eligibleForCloning"));
        Assert.assertEquals("Bundle title", path.getString("data.caseBundles[1].value.title"));
        Assert.assertEquals("no", path.getString("data.caseBundles[1].value.eligibleForCloning"));
    }

    @Test
    public void testMultipleBundlesClone() throws IOException {
        CcdBundleDTO bundle1 = testUtil.getTestBundle();
        bundle1.setTitle("Bundle 1");

        CcdBundleDTO bundle2 = testUtil.getTestBundle();
        bundle2.setTitle("Bundle 2");
        bundle2.setFileName("FilenameBundle2");
        bundle2.setEligibleForCloningAsBoolean(true);

        List<CcdValue<CcdBundleDTO>> list = new ArrayList<>();
        list.add(new CcdValue<>(bundle1));
        list.add(new CcdValue<>(bundle2));

        String jsonList = mapper.writeValueAsString(list);
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\": %s  } } }", jsonList);

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .request("POST", testUtil.getTestUrl() + "/api/clone-ccd-bundles");

        JsonPath path = response.getBody().jsonPath();

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("CLONED_Bundle 2", path.getString("data.caseBundles[0].value.title"));
        Assert.assertEquals("Bundle 2", path.getString("data.caseBundles[1].value.title"));
        Assert.assertEquals("Bundle 1", path.getString("data.caseBundles[2].value.title"));
        Assert.assertEquals("no", path.getString("data.caseBundles[1].value.eligibleForCloning"));
        Assert.assertEquals("CLONED_FilenameBundle2", path.getString("data.caseBundles[0].value.fileName"));
    }

}
