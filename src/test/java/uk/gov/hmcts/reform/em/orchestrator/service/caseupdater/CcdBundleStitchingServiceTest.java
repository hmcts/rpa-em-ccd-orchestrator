package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.em.orchestrator.config.Constants;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingServiceException;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class CcdBundleStitchingServiceTest {

    @Mock
    private StitchingService stitchingService;

    private CcdBundleStitchingService ccdBundleStitchingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        CcdDocument ccdDocument = new CcdDocument("", "", "");
        BDDMockito.given(stitchingService.stitch(any(), any())).willReturn(ccdDocument);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        ccdBundleStitchingService = new CcdBundleStitchingService(objectMapper, stitchingService, validator);
    }

    @Test
    public void testUpdateCase() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":{\"eligibleForStitching\":\"yes\"}},{\"value\":{}}]}");
        ccdCallbackDto.setPropertyName(Optional.of("cb"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt("jwt");
        ccdBundleStitchingService.updateCase(ccdCallbackDto);

        assertEquals(2, ((ArrayNode)node.get("cb")).size());
        assertEquals("", node.get("cb").get(0).path("value").path("stitchedDocument").path("document_url").textValue());
        assertNull(node.get("cb").get(1).path("value").path("stitchedDocument").path("document_url").textValue());

        Mockito.verify(stitchingService, Mockito.times(1))
                .stitch(Mockito.any(CcdBundleDTO.class), Mockito.any(String.class));
    }

    @Test(expected = StitchingServiceException.class)
    public void testUpdateCaseStitchingException() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":{\"eligibleForStitching\":\"yes\"}},{\"value\":{}}]}");
        ccdCallbackDto.setPropertyName(Optional.of("cb"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt("jwt");

        Mockito.when(stitchingService.stitch(Mockito.any(CcdBundleDTO.class), Mockito.any(String.class))).thenThrow(new StitchingServiceException("x"));

        ccdBundleStitchingService.updateCase(ccdCallbackDto);
    }

    @Test(expected = InputValidationException.class)
    public void testInvalidFilename() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":{\"eligibleForStitching\":\"yes\", \"fileName\":\"$.pdf\"}}]}");
        ccdCallbackDto.setPropertyName(Optional.of("cb"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt("jwt");

        ccdBundleStitchingService.updateCase(ccdCallbackDto);
    }

    @Test(expected = StitchingServiceException.class)
    public void testUpdateCaseInterruptedException() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":{\"eligibleForStitching\":\"yes\"}},{\"value\":{}}]}");
        ccdCallbackDto.setPropertyName(Optional.of("cb"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt("jwt");

        Mockito.when(stitchingService.stitch(Mockito.any(CcdBundleDTO.class), Mockito.any(String.class))).thenThrow(new InterruptedException("x"));

        ccdBundleStitchingService.updateCase(ccdCallbackDto);
    }

    @Test
    public void testUpdateCaseMissingBundles() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ccdCallbackDto.setCaseData(objectMapper.getNodeFactory().arrayNode());
        JsonNode node = ccdBundleStitchingService.updateCase(ccdCallbackDto);
        assertNull(node.get(0));
    }

    @Test
    public void testUpdateCaseFileNameOneChar() throws Exception {

        try {
            CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
            JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":{\"eligibleForStitching\":\"yes\", "
                + "\"fileName\":\"a\"}}]}");
            ccdCallbackDto.setPropertyName(Optional.of("cb"));
            ccdCallbackDto.setCaseData(node);
            ccdCallbackDto.setJwt("jwt");

            ccdBundleStitchingService.updateCase(ccdCallbackDto);
        } catch (InputValidationException exc) {
            assertEquals(Constants.STITCHED_FILE_NAME_FIELD_LENGTH_ERROR_MSG,exc.getViolations().get(0));
        }
    }

    @Test
    public void testUpdateCaseFileName51Char() throws Exception {

        try {
            CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
            JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":{\"eligibleForStitching\":\"yes\", "
                + "\"fileName\":\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"}}]}");
            ccdCallbackDto.setPropertyName(Optional.of("cb"));
            ccdCallbackDto.setCaseData(node);
            ccdCallbackDto.setJwt("jwt");

            ccdBundleStitchingService.updateCase(ccdCallbackDto);
        } catch (InputValidationException exc) {
            assertEquals(Constants.STITCHED_FILE_NAME_FIELD_LENGTH_ERROR_MSG,exc.getViolations().get(0));
        }
    }

}
