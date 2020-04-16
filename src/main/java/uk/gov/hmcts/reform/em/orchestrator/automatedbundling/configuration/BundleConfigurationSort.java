package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BundleConfigurationSort {

    public final String field;
    public final BundleConfigurationSortOrder order;

    public BundleConfigurationSort(@JsonProperty("field") String field,
                                     @JsonProperty("order") BundleConfigurationSortOrder order) {
        this.field = field;
        this.order = order;
    }
}
