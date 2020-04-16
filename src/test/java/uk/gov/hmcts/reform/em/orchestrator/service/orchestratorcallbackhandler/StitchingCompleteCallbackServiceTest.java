package uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdapi.CcdDataApiCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdapi.CcdDataApiEventCreator;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class StitchingCompleteCallbackServiceTest {

    @Mock
    private CcdDataApiEventCreator ccdDataApiEventCreator;
    @Mock
    private CcdDataApiCaseUpdater ccdDataApiCaseUpdater;
    @Mock
    private CcdCallbackBundleUpdater ccdCallbackBundleUpdater;

    @InjectMocks
    private StitchingCompleteCallbackService stitchingCompleteCallbackService;

    @Test
    public void handleCallback() {
        Mockito
            .when(ccdDataApiEventCreator.executeTrigger("a","1", "x"))
            .thenReturn(new CcdCallbackDto());
        stitchingCompleteCallbackService.handleCallback(new StitchingCompleteCallbackDto("x", "a",
                "1", UUID.randomUUID().toString(), new DocumentTaskDTO()));
        assertTrue(true, "No exception should be thrown");
    }

    @Test
    public void handleCallbackException()  {
        Mockito
                .when(ccdDataApiEventCreator.executeTrigger("a","1", "x"))
                .thenThrow(new CallbackException(111, "err body", "err"));
        assertThrows(CallbackException.class, () ->
                stitchingCompleteCallbackService.handleCallback(new StitchingCompleteCallbackDto("x",
                        "a", "1", UUID.randomUUID().toString(), new DocumentTaskDTO())));
    }

    @Test
    public void handleCallbackExceptionFinally() {
        Mockito
                .when(ccdDataApiEventCreator.executeTrigger(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(new CcdCallbackDto());

        Mockito.doThrow(new CallbackException(1, "", "")).when(ccdDataApiCaseUpdater)
                .executeUpdate(Mockito.any(), Mockito.any());

        assertThrows(CallbackException.class, () ->
                stitchingCompleteCallbackService.handleCallback(new StitchingCompleteCallbackDto("x",
                        "a", "1", UUID.randomUUID().toString(), new DocumentTaskDTO())));
    }

    @Test
    public void handleCallbackExceptionFinallyNullCcdCallbackDto() {
        Mockito
                .when(ccdDataApiEventCreator.executeTrigger(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(null);

        stitchingCompleteCallbackService.handleCallback(new StitchingCompleteCallbackDto("x",
                "a", "1", UUID.randomUUID().toString(), new DocumentTaskDTO()));

        Mockito.verify(ccdDataApiCaseUpdater, Mockito.never()).executeUpdate(Mockito.any(), Mockito.any());

        assertTrue(true, "No exception should be thrown");
    }

}
