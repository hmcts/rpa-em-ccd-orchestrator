package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

import java.io.File;
import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class CcdBundleCloningServiceTest {

    private CcdBundleCloningService ccdBundleCloningService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final File jsonOneEligible = new File(ClassLoader.getSystemResource("case-one-eligible.json").getPath());

    private final File jsonOneNotEligible = new File(ClassLoader.getSystemResource("case-one-not-eligible.json").getPath());

    private final File jsonOneEligibleOneNot = new File(ClassLoader.getSystemResource("case-one-eligible-one-not-eligible.json").getPath());

    private final File jsonTwoEligible = new File(ClassLoader.getSystemResource("case-two-eligible.json").getPath());

    private final JavaType type = objectMapper.getTypeFactory().constructParametricType(CcdValue.class, CcdBundleDTO.class);

    private CcdCallbackDto createCallbackDto(JsonNode node) {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ccdCallbackDto.setPropertyName(Optional.of("caseBundles"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt("jwt");
        return ccdCallbackDto;
    }

    private CcdBundleDTO getIthBundleDto(ArrayNode bundlesJson, int i) throws Exception {
        JsonNode ithBundleJson = bundlesJson.get(i);
        CcdValue<CcdBundleDTO> ccdValue = objectMapper.readValue(objectMapper.treeAsTokens(ithBundleJson), type);
        return ccdValue.getValue();
    }

    private void checkIthBundleTitle(ArrayNode list, int i, String title) throws Exception {
        CcdBundleDTO bundle = getIthBundleDto(list, i);
        Assert.assertEquals(title, bundle.getTitle());
    }

    @Before
    public void setup() {
        ccdBundleCloningService = new CcdBundleCloningService(objectMapper);
    }

    @Test
    public void testNameUpdated() throws Exception {
        JsonNode node = objectMapper.readTree(jsonOneEligible);
        CcdCallbackDto ccdCallbackDto = createCallbackDto(node);

        Assert.assertEquals(1, node.get("case_details").get("case_data").get("caseBundles").size());

        ccdBundleCloningService.updateCase(ccdCallbackDto);

        ArrayNode updatedBundles = (ArrayNode) node.path("case_details").path("case_data").path("caseBundles");
        CcdBundleDTO updatedFirstBundle = getIthBundleDto(updatedBundles, 0);
        CcdBundleDTO updatedSecondBundle = getIthBundleDto(updatedBundles, 1);

        Assert.assertEquals(2, node.get("case_details").get("case_data").get("caseBundles").size());
        Assert.assertEquals(updatedFirstBundle.getDescription(), updatedSecondBundle.getDescription());
        checkIthBundleTitle(updatedBundles, 0, "CLONED_First Bundle");
        checkIthBundleTitle(updatedBundles, 1, "First Bundle");
    }

    @Test
    public void testCloningSetsToFalse() throws Exception {
        JsonNode node = objectMapper.readTree(jsonOneEligible);
        CcdCallbackDto ccdCallbackDto = createCallbackDto(node);

        ArrayNode originalBundles = (ArrayNode) node.path("case_details").path("case_data").path("caseBundles");
        CcdBundleDTO originalFirstBundle = getIthBundleDto(originalBundles, 0);

        Assert.assertEquals("yes", originalFirstBundle.getEligibleForCloning());

        ccdBundleCloningService.updateCase(ccdCallbackDto);

        ArrayNode updatedBundles = (ArrayNode) node.path("case_details").path("case_data").path("caseBundles");

        CcdBundleDTO updatedFirstBundle = getIthBundleDto(updatedBundles, 0);
        CcdBundleDTO updatedSecondBundle = getIthBundleDto(updatedBundles, 1);

        Assert.assertFalse(updatedFirstBundle.getEligibleForCloningAsBoolean());
        Assert.assertFalse(updatedSecondBundle.getEligibleForCloningAsBoolean());
    }

    @Test
    public void testFalseBundlesNotCloned() throws Exception {
        JsonNode node = objectMapper.readTree(jsonOneNotEligible);
        CcdCallbackDto ccdCallbackDto = createCallbackDto(node);

        ArrayNode originalBundles = (ArrayNode) node.path("case_details").path("case_data").path("caseBundles");
        CcdBundleDTO originalBundle = getIthBundleDto(originalBundles, 0);

        Assert.assertEquals(1, node.get("case_details").get("case_data").get("caseBundles").size());
        Assert.assertEquals("no", originalBundle.getEligibleForCloning());

        ccdBundleCloningService.updateCase(ccdCallbackDto);

        ArrayNode updatedBundles = (ArrayNode) node.path("case_details").path("case_data").path("caseBundles");

        Assert.assertEquals(1, node.get("case_details").get("case_data").get("caseBundles").size());
        Assert.assertEquals(0, StringUtils.countMatches(updatedBundles.toString(), "CLONED"));
    }

    @Test
    public void testAllEligibleBundlesCloned() throws Exception {
        JsonNode node = objectMapper.readTree(jsonTwoEligible);
        CcdCallbackDto ccdCallbackDto = createCallbackDto(node);
        ArrayNode originalBundles = (ArrayNode) node.path("case_details").path("case_data").path("caseBundles");

        Assert.assertEquals(2, node.get("case_details").get("case_data").get("caseBundles").size());
        Assert.assertEquals(0, StringUtils.countMatches(originalBundles.toString(), "CLONED"));

        ccdBundleCloningService.updateCase(ccdCallbackDto);

        ArrayNode updatedBundles = (ArrayNode) node.path("case_details").path("case_data").path("caseBundles");

        Assert.assertEquals(4, node.get("case_details").get("case_data").get("caseBundles").size());
        checkIthBundleTitle(updatedBundles, 0, "CLONED_Second Bundle");
        checkIthBundleTitle(updatedBundles, 1, "Second Bundle");
        checkIthBundleTitle(updatedBundles, 2, "CLONED_First Bundle");
        checkIthBundleTitle(updatedBundles, 3, "First Bundle");
    }

    @Test
    public void testOnlyEligibleBundlesCloned() throws Exception {
        JsonNode node = objectMapper.readTree(jsonOneEligibleOneNot);
        CcdCallbackDto ccdCallbackDto = createCallbackDto(node);

        ArrayNode originalBundles = (ArrayNode) node.path("case_details").path("case_data").path("caseBundles");

        Assert.assertEquals(2, node.get("case_details").get("case_data").get("caseBundles").size());
        Assert.assertEquals(0, StringUtils.countMatches(originalBundles.toString(), "CLONED"));

        ccdBundleCloningService.updateCase(ccdCallbackDto);

        ArrayNode updatedBundles = (ArrayNode) node.path("case_details").path("case_data").path("caseBundles");

        Assert.assertEquals(3, node.get("case_details").get("case_data").get("caseBundles").size());
        checkIthBundleTitle(updatedBundles, 0, "CLONED_First Bundle");
        checkIthBundleTitle(updatedBundles, 1, "First Bundle");
        checkIthBundleTitle(updatedBundles, 2, "Second Bundle");
    }

    @Test
    public void testIfMaybeBundlesIsNotPresent() throws Exception {
        JsonNode node = objectMapper.readTree(jsonOneEligible);
        CcdCallbackDto ccdCallbackDto = createCallbackDto(node);
        Assert.assertEquals(1, node.get("case_details").get("case_data").get("caseBundles").size());

        ccdCallbackDto.setPropertyName(Optional.of("non-existent property"));
        ccdBundleCloningService.updateCase(ccdCallbackDto);

        Assert.assertEquals(1, node.get("case_details").get("case_data").get("caseBundles").size());
    }
}
