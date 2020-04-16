package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.em.orchestrator.service.notification.NotificationService;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.CallbackException;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.StitchingCompleteCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.StitchingCompleteCallbackService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.TaskState;

import javax.servlet.http.HttpServletRequest;


@Controller
@ConditionalOnProperty("endpoint-toggles.stitching-complete-callback")
public class StitchingCompleteCallbackController {

    private final Logger log = LoggerFactory.getLogger(StitchingCompleteCallbackController.class);

    private final StitchingCompleteCallbackService stitchingCompleteCallbackService;
    private final NotificationService notificationService;

    @Value("${notify.successTemplateId}")
    private String successTemplateId;

    @Value("${notify.failureTemplateId}")
    private String failureTemplateId;

    public StitchingCompleteCallbackController(StitchingCompleteCallbackService stitchingCompleteCallbackService,
                                               NotificationService notificationService) {
        this.stitchingCompleteCallbackService = stitchingCompleteCallbackService;
        this.notificationService = notificationService;
    }

    @PostMapping(
            value = "/api/stitching-complete-callback/{caseId}/{triggerId}/{bundleId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CallbackException> stitchingCompleteCallback(HttpServletRequest request,
                                                                       @PathVariable String caseId,
                                                                       @PathVariable String triggerId,
                                                                       @PathVariable String bundleId,
                                                                       @RequestBody DocumentTaskDTO documentTaskDTO) throws JsonProcessingException {
        String jwt = request.getHeader("authorization");
        TaskState taskState = documentTaskDTO.getTaskState();

        try {
            StitchingCompleteCallbackDto stitchingCompleteCallbackDto =
                    new StitchingCompleteCallbackDto(
                            jwt,
                            caseId,
                            triggerId,
                            bundleId,
                            documentTaskDTO);

            stitchingCompleteCallbackService.handleCallback(stitchingCompleteCallbackDto);

            log.info(String.format("Successful callback for caseId: %s and triggerId %s", caseId, triggerId));

            if ((documentTaskDTO.getBundle().getEnableEmailNotification() != null
                    && documentTaskDTO.getBundle().getEnableEmailNotification())
                    && (taskState.equals(TaskState.DONE) || taskState.equals(TaskState.FAILED))) {
                notificationService.sendEmailNotification(
                        taskState.equals(TaskState.DONE) ? successTemplateId : failureTemplateId,
                        jwt,
                        caseId,
                        documentTaskDTO.getBundle().getBundleTitle(),
                        taskState.equals(TaskState.DONE) ? null : documentTaskDTO.getFailureDescription()
                );
            }

            return ResponseEntity.ok().build();

        } catch (CallbackException e) {
            log.error(String.format("Unsuccessful callback: %s", e.toString()));
            return ResponseEntity.status(e.getHttpStatus()).body(e);
        }
    }
}
