package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingServiceException;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static pl.touk.throwing.ThrowingFunction.unchecked;

@Service
@Transactional
public class CcdBundleStitchingService implements CcdCaseUpdater {

    private final ObjectMapper objectMapper;
    private final JavaType type;
    private final Validator validator;
    private final StitchingService stitchingService;

    public CcdBundleStitchingService(ObjectMapper objectMapper,
                                     StitchingService stitchingService,
                                     Validator validator) {
        this.objectMapper = objectMapper;
        this.stitchingService = stitchingService;
        type = objectMapper.getTypeFactory().constructParametricType(CcdValue.class, CcdBundleDTO.class);
        this.validator = validator;
    }

    @Override
    public JsonNode updateCase(CcdCallbackDto ccdCallbackDto) {
        Optional<ArrayNode> maybeBundles = ccdCallbackDto.findCaseProperty(ArrayNode.class);

        if (maybeBundles.isPresent()) {
            List<JsonNode> newBundles = StreamSupport
                    .stream(Spliterators.spliteratorUnknownSize(maybeBundles.get().iterator(), Spliterator.ORDERED), false)
                    .parallel()
                    .map(unchecked(this::bundleJsonToBundleValue))
                    .map(bundle -> bundle.getValue().getEligibleForStitchingAsBoolean()
                            ? this.stitchBundle(bundle, ccdCallbackDto) : bundle)
                    .map(bundleDto -> objectMapper.convertValue(bundleDto, JsonNode.class))
                    .collect(Collectors.toList());

            maybeBundles.get().removeAll();
            maybeBundles.get().addAll(CcdCaseUpdater.reorderBundles(newBundles, objectMapper, type));
        }

        return ccdCallbackDto.getCaseData();
    }

    private CcdValue<CcdBundleDTO> stitchBundle(CcdValue<CcdBundleDTO> bundle, CcdCallbackDto ccdCallbackDto) {
        bundle.getValue().setCoverpageTemplateData(ccdCallbackDto.getCaseDetails());
        ccdCallbackDto.setEnableEmailNotification(bundle.getValue().getEnableEmailNotificationAsBoolean());
        Set<ConstraintViolation<CcdBundleDTO>> violations = validator.validate(bundle.getValue());

        if (!violations.isEmpty()) {
            throw new InputValidationException(violations);
        }

        try {
            CcdDocument stitchedDocumentURI = stitchingService.stitch(bundle.getValue(), ccdCallbackDto.getJwt());
            bundle.getValue().setStitchedDocument(stitchedDocumentURI);

            return bundle;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new StitchingServiceException(e.getMessage(), e);
        }
    }

    private CcdValue<CcdBundleDTO> bundleJsonToBundleValue(JsonNode jsonNode) throws IOException {
        return objectMapper.readValue(objectMapper.treeAsTokens(jsonNode), type);
    }
}
