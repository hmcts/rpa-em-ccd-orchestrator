package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDocumentDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

import java.util.Comparator;

public enum BundleConfigurationSortOrder implements Comparator<CcdValue<CcdBundleDocumentDTO>> {
    ascending {
        @Override
        public int compare(CcdValue<CcdBundleDocumentDTO> a, CcdValue<CcdBundleDocumentDTO> b) {
            return a.getValue().getSourceDocument().getCreatedDatetime().compareTo(
                b.getValue().getSourceDocument().getCreatedDatetime()
            );
        }
    },
    descending {
        @Override
        public int compare(CcdValue<CcdBundleDocumentDTO> a, CcdValue<CcdBundleDocumentDTO> b) {
            return b.getValue().getSourceDocument().getCreatedDatetime().compareTo(
                a.getValue().getSourceDocument().getCreatedDatetime()
            );
        }
    };

    @Override
    public abstract int compare(CcdValue<CcdBundleDocumentDTO> a, CcdValue<CcdBundleDocumentDTO> b);
}
