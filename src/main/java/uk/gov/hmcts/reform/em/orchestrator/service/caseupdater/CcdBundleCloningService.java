package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CcdBundleCloningService implements CcdCaseUpdater {

    private final ObjectMapper objectMapper;
    private final JavaType type;

    public CcdBundleCloningService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        type = objectMapper.getTypeFactory().constructParametricType(CcdValue.class, CcdBundleDTO.class);
    }

    @Override
    public JsonNode updateCase(CcdCallbackDto ccdCallbackDto) {

        Optional<ArrayNode> maybeBundles = ccdCallbackDto.findCaseProperty(ArrayNode.class);
        Optional<ArrayNode> processedMaybeBundles = maybeBundles.map(bundles -> {
            ArrayNode processedBundles = objectMapper.createArrayNode();
            try {
                for (JsonNode bundleJson : bundles) {
                    List<JsonNode> processedBundleOrBundles = processBundle(bundleJson);
                    processedBundles.addAll(processedBundleOrBundles);
                }
                return processedBundles;
            } catch (IOException e) {
                return bundles;
            }
        });

        if (maybeBundles.isPresent() && processedMaybeBundles.isPresent()) {
            maybeBundles.get().removeAll();
            maybeBundles.get().addAll(reorderBundles(processedMaybeBundles.get()));
        }

        return ccdCallbackDto.getCaseData();
    }

    private List<JsonNode> processBundle(JsonNode originalJson) throws IOException {
        List<JsonNode> returnList = new ArrayList<>();

        CcdBundleDTO originalBundle = bundleJsonToBundleDto(originalJson);
        if (originalBundle.getEligibleForCloningAsBoolean()) {
            JsonNode originalProcessedJson = bundleDtoToBundleJson(originalBundle);
            JsonNode clonedJson = cloneBundle(originalProcessedJson);

            returnList.add(originalProcessedJson);
            returnList.add(clonedJson);
        } else {
            returnList.add(originalJson);
        }

        return returnList;
    }

    private JsonNode cloneBundle(JsonNode originalJson) throws IOException {
        JsonNode unprocessedClonedJson = originalJson.deepCopy();
        CcdBundleDTO clonedBundle = bundleJsonToBundleDto(unprocessedClonedJson);
        clonedBundle.setTitle("CLONED_" + clonedBundle.getTitle());
        clonedBundle.setFileName("CLONED_" + clonedBundle.getFileName());
        return bundleDtoToBundleJson(clonedBundle);
    }

    private CcdBundleDTO bundleJsonToBundleDto(JsonNode jsonNode) throws IOException {
        CcdValue<CcdBundleDTO> ccdValue = objectMapper.readValue(objectMapper.treeAsTokens(jsonNode), type);
        return ccdValue.getValue();
    }

    private JsonNode bundleDtoToBundleJson(CcdBundleDTO ccdBundle) {
        CcdValue<CcdBundleDTO> ccdValue = new CcdValue<>();
        ccdValue.setValue(ccdBundle);
        return objectMapper.convertValue(ccdValue, JsonNode.class);
    }

    private ArrayNode reorderBundles(ArrayNode bundles) {
        ArrayNode reorderedBundles = objectMapper.createArrayNode();
        for (JsonNode bundle : bundles) {
            CcdValue<CcdBundleDTO> ccdBundleDTO = null;
            try {
                ccdBundleDTO = objectMapper.readValue(objectMapper.treeAsTokens(bundle), type);
                if (ccdBundleDTO.getValue().getEligibleForCloningAsBoolean()) {
                    ccdBundleDTO.getValue().setEligibleForCloningAsBoolean(false);
                    reorderedBundles.insert(0, objectMapper.convertValue(ccdBundleDTO, JsonNode.class));
                } else {
                    reorderedBundles.add(objectMapper.convertValue(ccdBundleDTO, JsonNode.class));
                }
            } catch (IOException e) {
                return bundles;
            }
        }
        return reorderedBundles;
    }
}
