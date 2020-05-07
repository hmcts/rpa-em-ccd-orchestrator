package uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.TaskState;

import java.util.stream.StreamSupport;

import static uk.gov.hmcts.reform.em.orchestrator.util.StringUtilities.ensurePdfExtension;

@Service
public class CcdCallbackBundleUpdater {

    private final Logger log = LoggerFactory.getLogger(CcdCallbackBundleUpdater.class);

    private final ObjectMapper objectMapper;

    public CcdCallbackBundleUpdater(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    void updateBundle(CcdCallbackDto ccdCallbackDto, StitchingCompleteCallbackDto stitchingCompleteCallbackDto) {

        ArrayNode bundles = ccdCallbackDto
                .findCaseProperty(ArrayNode.class)
                .orElseThrow(() -> new CallbackException(400, null, "Bundle collection could not be found"));

        JsonNode ccdBundle = StreamSupport.stream(bundles.spliterator(), false)
                .map(jsonNode -> jsonNode.get("value"))
                .filter(ccdBundleJson ->
                        ccdBundleJson.get("id").asText()
                            .equals(stitchingCompleteCallbackDto.getCcdBundleId().toString()))
                .findFirst()
                .map(ccdBundleJson -> this.updateCcdBundle(ccdBundleJson, stitchingCompleteCallbackDto))
                .orElseThrow(() -> new CallbackException(400, null,
                        String.format("Bundle#%s could not be found",
                                stitchingCompleteCallbackDto.getCcdBundleId().toString())));

        log.debug(String.format("Updated ccdBundle: %s", ccdBundle.toString()));
    }

    private JsonNode updateCcdBundle(JsonNode ccdBundle, StitchingCompleteCallbackDto stitchingCompleteCallbackDto) {
        try {
            CcdBundleDTO ccdBundleDTO = this.objectMapper.treeToValue(ccdBundle, CcdBundleDTO.class);
            log.info(String.format("Updating bundle#%s with %s",
                    stitchingCompleteCallbackDto.getCcdBundleId().toString(),
                    stitchingCompleteCallbackDto.getDocumentTaskDTO().toString()));
            ccdBundleDTO.setStitchStatus(stitchingCompleteCallbackDto.getDocumentTaskDTO().getTaskState().toString());
            ccdBundleDTO.setEligibleForCloningAsBoolean(false);
            ccdBundleDTO.setStitchingFailureMessage(stitchingCompleteCallbackDto.getDocumentTaskDTO()
                    .getFailureDescription());

            if (stitchingCompleteCallbackDto.getDocumentTaskDTO().getTaskState().equals(TaskState.DONE)) {

                ccdBundleDTO.setStitchedDocument(new CcdDocument(
                        stitchingCompleteCallbackDto.getDocumentTaskDTO().getBundle().getStitchedDocumentURI(),
                        ensurePdfExtension(stitchingCompleteCallbackDto.getDocumentTaskDTO().getBundle().getFileName()),
                        stitchingCompleteCallbackDto.getDocumentTaskDTO().getBundle().getStitchedDocumentURI() + "/binary"));

            }

            JsonNode updatedCcdBundle = objectMapper.valueToTree(ccdBundleDTO);
            ObjectNode ccdBundleObjectNode = (ObjectNode) ccdBundle;
            ccdBundleObjectNode.set("stitchStatus", updatedCcdBundle.get("stitchStatus"));
            ccdBundleObjectNode.set("eligibleForCloning", updatedCcdBundle.get("eligibleForCloning"));
            ccdBundleObjectNode.set("stitchingFailureMessage", updatedCcdBundle.get("stitchingFailureMessage"));
            ccdBundleObjectNode.set("stitchedDocument", updatedCcdBundle.get("stitchedDocument"));
            return ccdBundle;
        } catch (JsonProcessingException e) {
            throw new CallbackException(400, null, String.format("Error processing JSON %s", e.getMessage()));
        }

    }

}
