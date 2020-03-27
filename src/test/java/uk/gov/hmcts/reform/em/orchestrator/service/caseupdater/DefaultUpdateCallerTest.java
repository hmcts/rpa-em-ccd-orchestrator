package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDtoCreator;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;
import uk.gov.hmcts.reform.em.orchestrator.service.notification.NotificationService;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(MockitoJUnitRunner.class)
public class DefaultUpdateCallerTest {

    @Mock
    private CcdCallbackDtoCreator ccdCallbackDtoCreator;

    @Mock
    private CcdCaseUpdater ccdCaseUpdater;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private NotificationService notificationService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    DefaultUpdateCaller defaultUpdateCaller;

    @Test
    public void executeUpdate() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode caseData = mock(JsonNode.class);
        ccdCallbackDto.setCcdPayload(caseData);

        when(ccdCallbackDtoCreator.createDto(Mockito.any(HttpServletRequest.class), Mockito.any(String.class)))
            .thenReturn(ccdCallbackDto);

        when(ccdCaseUpdater.updateCase(Mockito.any(CcdCallbackDto.class)))
            .thenReturn(objectMapper.readTree("{ \"p\" : 1 }"));

        CcdCallbackResponseDto ccdCallbackResponseDto =
                defaultUpdateCaller.executeUpdate(ccdCaseUpdater, httpServletRequest);

        Assert.assertEquals(1, ccdCallbackResponseDto.getData().get("p").asInt());
    }

    @Test
    public void executeUpdateInputValidationException() {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode caseData = mock(JsonNode.class);
        ccdCallbackDto.setCcdPayload(caseData);
        ccdCallbackDto.setCaseData(caseData);
        ccdCallbackDto.setEnableEmailNotification(true);

        when(caseData.has(anyString())).thenReturn(true);
        when(caseData.get(anyString())).thenReturn(caseData);
        when(caseData.asText()).thenReturn("json value");

        when(ccdCallbackDtoCreator.createDto(Mockito.any(HttpServletRequest.class), Mockito.any(String.class)))
                .thenReturn(ccdCallbackDto);

        InputValidationException e = mock(InputValidationException.class);

        when(e.getViolations())
                .thenReturn(Stream.of("abc").collect(Collectors.toList()));

        when(ccdCaseUpdater.updateCase(Mockito.any(CcdCallbackDto.class)))
                .thenThrow(e);

        CcdCallbackResponseDto ccdCallbackResponseDto =
                defaultUpdateCaller.executeUpdate(ccdCaseUpdater, httpServletRequest);

        Assert.assertEquals("abc", ccdCallbackResponseDto.getErrors().get(0));
    }

    @Test
    public void executeUpdateException() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode caseData = mock(JsonNode.class);
        ccdCallbackDto.setCcdPayload(caseData);
        ccdCallbackDto.setCaseData(caseData);
        ccdCallbackDto.setEnableEmailNotification(true);

        when(caseData.has(anyString())).thenReturn(true);
        when(caseData.get(anyString())).thenReturn(caseData);
        when(caseData.asText()).thenReturn("json value");

        when(ccdCallbackDtoCreator.createDto(Mockito.any(HttpServletRequest.class), Mockito.any(String.class)))
                .thenReturn(ccdCallbackDto);

        when(ccdCaseUpdater.updateCase(Mockito.any(CcdCallbackDto.class)))
                .thenThrow(new RuntimeException("x"));

        CcdCallbackResponseDto ccdCallbackResponseDto =
                defaultUpdateCaller.executeUpdate(ccdCaseUpdater, httpServletRequest);

        Assert.assertEquals("x", ccdCallbackResponseDto.getErrors().get(0));
    }

    @Test
    public void executeUpdateExceptionWithNoEmailNotification() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode caseData = mock(JsonNode.class);
        ccdCallbackDto.setCcdPayload(caseData);
        ccdCallbackDto.setCaseData(caseData);
        ccdCallbackDto.setEnableEmailNotification(false);

        when(ccdCallbackDtoCreator.createDto(Mockito.any(HttpServletRequest.class), Mockito.any(String.class)))
                .thenReturn(ccdCallbackDto);

        when(ccdCaseUpdater.updateCase(Mockito.any(CcdCallbackDto.class)))
                .thenThrow(new RuntimeException("x"));

        CcdCallbackResponseDto ccdCallbackResponseDto =
                defaultUpdateCaller.executeUpdate(ccdCaseUpdater, httpServletRequest);

        Assert.assertEquals("x", ccdCallbackResponseDto.getErrors().get(0));
    }

}
