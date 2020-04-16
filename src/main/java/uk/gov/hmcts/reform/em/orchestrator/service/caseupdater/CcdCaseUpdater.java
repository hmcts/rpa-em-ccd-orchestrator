package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface CcdCaseUpdater {

    JsonNode updateCase(CcdCallbackDto ccdCallbackDto);

    static List<JsonNode> reorderBundles(List<JsonNode> bundles, ObjectMapper objectMapper, JavaType type) {
        List<JsonNode> reorderedBundles = new ArrayList<>();
        for (JsonNode bundle : bundles) {
            CcdValue<CcdBundleDTO> ccdBundleDTO;
            try {
                ccdBundleDTO = objectMapper.readValue(objectMapper.treeAsTokens(bundle), type);
                if (ccdBundleDTO.getValue().getEligibleForStitchingAsBoolean()) {
                    ccdBundleDTO.getValue().setEligibleForStitchingAsBoolean(false);
                    reorderedBundles.add(0, objectMapper.convertValue(ccdBundleDTO, JsonNode.class));
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
