package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import org.junit.Test;

import static org.junit.Assert.*;

public class CcdCaseDataContentTest {

    @Test
    public void testProperties() {
        CcdCaseDataContent ccdCaseDataContent = new CcdCaseDataContent();
        ccdCaseDataContent.setToken("token");
        CcdEvent ccdEvent = new CcdEvent("eventx");
        ccdCaseDataContent.setEvent(ccdEvent);
        ccdCaseDataContent.setData(null);
        ccdCaseDataContent.setEventData(null);
        ccdCaseDataContent.setCaseReference("x");
        ccdCaseDataContent.setDataClassification(null);
        ccdCaseDataContent.setDraftId("draftId");
        ccdCaseDataContent.setIgnoreWarning(true);
        ccdCaseDataContent.setSecurityClassification("x");

        assertEquals("eventx", ccdCaseDataContent.getEventId());
        assertEquals("token", ccdCaseDataContent.getToken());
        assertEquals(null, ccdCaseDataContent.getData());
        assertEquals(null, ccdCaseDataContent.getEventData());
        assertEquals("x", ccdCaseDataContent.getCaseReference());
        assertEquals(null, ccdCaseDataContent.getDataClassification());
        assertEquals("draftId", ccdCaseDataContent.getDraftId());
        assertEquals(Boolean.TRUE, ccdCaseDataContent.getIgnoreWarning());
        assertEquals("x", ccdCaseDataContent.getSecurityClassification());
        assertEquals("CcdCaseDataContent(event=CcdEvent(eventId=eventx), data=null, eventData=null, "
                + "securityClassification=x, dataClassification=null, token=token, ignoreWarning=true, draftId=draftId, "
                + "caseReference=x)", ccdCaseDataContent.toString());

    }

}
