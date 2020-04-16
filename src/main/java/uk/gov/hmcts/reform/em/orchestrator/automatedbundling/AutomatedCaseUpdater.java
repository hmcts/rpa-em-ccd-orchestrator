package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfiguration;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.ConfigurationLoader;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

import java.util.Map;

/**
 * This class will update add a new bundle to case based on some predefined configuration.
 */
public class AutomatedCaseUpdater implements CcdCaseUpdater {
    private static final String CONFIG_FIELD = "bundleConfiguration";
    private static final Map<String, String> CONFIG_MAP = ImmutableMap.of("SSCS", "sscs-bundle-config.yaml");
    private static final String DEFAULT_CONFIG = "default-config.yaml";

    private final ConfigurationLoader configurationLoader;
    private final ObjectMapper jsonMapper;
    private final BundleFactory bundleFactory;
    private final AutomatedStitchingExecutor automatedStitchingExecutor;

    public AutomatedCaseUpdater(ConfigurationLoader configurationLoader,
                                ObjectMapper jsonMapper,
                                BundleFactory bundleFactory,
                                AutomatedStitchingExecutor automatedStitchingExecutor) {
        this.configurationLoader = configurationLoader;
        this.jsonMapper = jsonMapper;
        this.bundleFactory = bundleFactory;
        this.automatedStitchingExecutor = automatedStitchingExecutor;
    }

    /**
     * Load the configuration file then add a new bundle to the case data based on that configuration. If an error occurs
     * during loading or processing the original case data will be returned with errors
     */
    @Override
    public JsonNode updateCase(CcdCallbackDto ccdCallbackDto) {
        String configurationName =
                ccdCallbackDto.getCaseData().has(CONFIG_FIELD) && !ccdCallbackDto.getCaseData().get(CONFIG_FIELD).asText().equals("null")
                ? ccdCallbackDto.getCaseData().get(CONFIG_FIELD).asText()
                : CONFIG_MAP.getOrDefault(ccdCallbackDto.getJurisdiction(), DEFAULT_CONFIG);

        BundleConfiguration configuration = configurationLoader.load(configurationName);
        final ArrayNode bundles = ccdCallbackDto
            .findCaseProperty(ArrayNode.class)
            .orElseGet(() -> {
                ArrayNode arrayNode = jsonMapper.createArrayNode();
                ((ObjectNode)ccdCallbackDto.getCaseData()).set(ccdCallbackDto.getPropertyName().get(), arrayNode);
                return arrayNode;
            });

        CcdBundleDTO bundle = bundleFactory.create(configuration, ccdCallbackDto.getCaseData());
        ccdCallbackDto.setEnableEmailNotification(bundle.getEnableEmailNotificationAsBoolean());
        if (bundle.getFileNameIdentifier() != null && !bundle.getFileNameIdentifier().isEmpty()) {
            bundle.setFileName(ccdCallbackDto.getIdentifierFromCcdPayload(bundle.getFileNameIdentifier()) + "-" + bundle.getFileName());
        }
        bundle.setCoverpageTemplateData(ccdCallbackDto.getCaseDetails());

        automatedStitchingExecutor.startStitching(
                ccdCallbackDto.getCaseId(),
                ccdCallbackDto.getJwt(),
                bundle);

        bundles.insert(0, bundleDtoToBundleJson(bundle));

        return ccdCallbackDto.getCaseData();
    }

    private JsonNode bundleDtoToBundleJson(CcdBundleDTO ccdBundle) {
        CcdValue<CcdBundleDTO> ccdValue = new CcdValue<>();
        ccdValue.setValue(ccdBundle);
        return jsonMapper.convertValue(ccdValue, JsonNode.class);
    }
}
