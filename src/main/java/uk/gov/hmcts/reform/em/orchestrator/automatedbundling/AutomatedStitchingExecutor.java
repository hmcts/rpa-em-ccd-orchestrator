package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.CallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.mapper.StitchingDTOMapper;

import java.io.IOException;
import java.util.UUID;

@Service
public class AutomatedStitchingExecutor {

    private final Logger log = LoggerFactory.getLogger(AutomatedStitchingExecutor.class);

    private final StitchingService stitchingService;
    private final StitchingDTOMapper stitchingDTOMapper;
    private final CallbackUrlCreator callbackUrlCreator;

    public static final String DEFAULT_TRIGGER_NAME  = "asyncStitchingComplete";

    public AutomatedStitchingExecutor(StitchingService stitchingService, StitchingDTOMapper stitchingDTOMapper, CallbackUrlCreator callbackUrlCreator) {
        this.stitchingService = stitchingService;
        this.stitchingDTOMapper = stitchingDTOMapper;
        this.callbackUrlCreator = callbackUrlCreator;
    }

    public void startStitching(String caseId, String jwt, CcdBundleDTO ccdBundleDTO) {
        startStitching(caseId, DEFAULT_TRIGGER_NAME, jwt, ccdBundleDTO);
    }

    public void startStitching(String caseId, String triggerId, String jwt, CcdBundleDTO ccdBundleDTO) {

        if (StringUtils.isEmpty(ccdBundleDTO.getId())) {
            ccdBundleDTO.setId(UUID.randomUUID().toString());
        }

        final DocumentTaskDTO documentTask = new DocumentTaskDTO();
        documentTask.setBundle(stitchingDTOMapper.toStitchingDTO(ccdBundleDTO));

        CallbackDto callbackDto = new CallbackDto();
        callbackDto.setCallbackUrl(callbackUrlCreator.createCallbackUrl(caseId, triggerId, ccdBundleDTO.getId()));
        documentTask.setCallback(callbackDto);

        try {
            log.info("Creating new stitching task {}", documentTask.toString());
            final DocumentTaskDTO createdDocumentTaskDTO = stitchingService.startStitchingTask(documentTask, jwt);
            ccdBundleDTO.setStitchStatus(createdDocumentTaskDTO.getTaskState().toString());
            log.info("Created new stitching task {}", createdDocumentTaskDTO.toString());
        } catch (IOException e) {
            log.error(String.format("Starting new stitching task - failed %s", documentTask.toString()), e);
            throw new StartStitchingException(String.format("Could not start stitching: %s", e.getMessage()), e);
        }

    }


}
