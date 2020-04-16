package uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler;

import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;

public class StitchingCompleteCallbackDto {

    private final String jwt;
    private final String caseId;
    private final String triggerId;
    private final String ccdBundleId;
    private final DocumentTaskDTO documentTaskDTO;

    public StitchingCompleteCallbackDto(String jwt, String caseId, String triggerId, String ccdBundleId,
                                        DocumentTaskDTO documentTaskDTO) {
        this.jwt = jwt;
        this.caseId = caseId;
        this.triggerId = triggerId;
        this.ccdBundleId = ccdBundleId;
        this.documentTaskDTO = documentTaskDTO;
    }

    public String getJwt() {
        return jwt;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public DocumentTaskDTO getDocumentTaskDTO() {
        return documentTaskDTO;
    }

    public String getCcdBundleId() {
        return ccdBundleId;
    }

}
