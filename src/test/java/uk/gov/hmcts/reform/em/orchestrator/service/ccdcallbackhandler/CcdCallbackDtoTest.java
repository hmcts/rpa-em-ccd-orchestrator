package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.*;

public class CcdCallbackDtoTest {

    @Test
    public void getCaseIdNull() {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        assertNull(ccdCallbackDto.getCaseId());
    }

    @Test
    public void getEnableEmailNotificationNull() {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        assertFalse(ccdCallbackDto.getEnableEmailNotification());
    }

    @Test
    public void getPayloadStaticProperties() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode ccdPayload = objectMapper.readTree("{\"ccdPayload\": { \"id\": \"1\", "
                + "\"jurisdiction\": \"j\", \"case_type_id\": \"c\", \"token\": \"t\", \"event_id\":\"e\" }}");
        ccdCallbackDto.setCcdPayload(ccdPayload);
        assertEquals("1", ccdCallbackDto.getCaseId());
        assertEquals("j", ccdCallbackDto.getJurisdiction());
        assertEquals("c", ccdCallbackDto.getCaseTypeId());
        assertEquals("t", ccdCallbackDto.getEventToken());
        assertEquals("e", ccdCallbackDto.getEventId());
        assertEquals("1", ccdCallbackDto.getIdentifierFromCcdPayload("/ccdPayload/id"));
    }

    @Test
    public void getInvalidIdentifier() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode ccdPayload = objectMapper.readTree("{\"ccdPayload\": { \"id\": \"1\", "
                + "\"jurisdiction\": \"j\", \"case_type_id\": \"c\", \"token\": \"t\", \"event_id\":\"e\" }}");
        ccdCallbackDto.setCcdPayload(ccdPayload);
        assertEquals("", ccdCallbackDto.getIdentifierFromCcdPayload("/ccdPayload/idea"));
    }

    @Test
    public void returnEmptyForNullIdentifier() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode ccdPayload = objectMapper.readTree("{\"ccdPayload\": { \"id\": \"1\", "
                + "\"jurisdiction\": \"j\", \"case_type_id\": \"c\", \"token\": \"t\", \"event_id\":\"e\" }}");
        ccdCallbackDto.setCcdPayload(ccdPayload);
        assertEquals("", ccdCallbackDto.getIdentifierFromCcdPayload(null));
    }
}
