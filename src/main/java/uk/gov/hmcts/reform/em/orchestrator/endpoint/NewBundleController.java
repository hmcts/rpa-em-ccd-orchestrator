package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.AutomatedCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.DefaultUpdateCaller;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;

import javax.servlet.http.HttpServletRequest;

@Controller
public class NewBundleController {

    private final DefaultUpdateCaller defaultUpdateCaller;
    private final AutomatedCaseUpdater automatedCaseUpdater;

    public NewBundleController(DefaultUpdateCaller defaultUpdateCaller, AutomatedCaseUpdater automatedCaseUpdater) {
        this.defaultUpdateCaller = defaultUpdateCaller;
        this.automatedCaseUpdater = automatedCaseUpdater;
    }

    @PostMapping(value = "/api/new-bundle",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CcdCallbackResponseDto> prepareNewBundle(HttpServletRequest request) {
        return ResponseEntity.ok(defaultUpdateCaller.executeUpdate(automatedCaseUpdater, request));
    }
}
