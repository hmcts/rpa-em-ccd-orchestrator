package uk.gov.hmcts.reform.em.orchestrator.stitching.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBoolean;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDocumentDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleFolderDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleDocumentDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleFolderDTO;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StitchingDTOMapper {

    public StitchingBundleDTO toStitchingDTO(CcdBundleDTO bundleDTO) {
        StitchingBundleDTO bundle = new StitchingBundleDTO();
        bundle.setBundleTitle(bundleDTO.getTitle());
        bundle.setDescription(bundleDTO.getDescription());
        bundle.setFileName(bundleDTO.getFileName());
        bundle.setCoverpageTemplate(bundleDTO.getCoverpageTemplate());
        bundle.setCoverpageTemplateData(bundleDTO.getCoverpageTemplateData());
        bundle.setHasTableOfContents(bundleDTO.getHasTableOfContents() == CcdBoolean.Yes);
        bundle.setHasCoversheets(bundleDTO.getHasCoversheets() == CcdBoolean.Yes);
        bundle.setHasFolderCoversheets(bundleDTO.getHasFolderCoversheets() == CcdBoolean.Yes);
        bundle.setPageNumberFormat(bundleDTO.getPageNumberFormat());
        bundle.setDocuments(getDocuments(bundleDTO.getDocuments()));
        bundle.setFolders(getFolders(bundleDTO.getFolders()));
        bundle.setPaginationStyle(bundleDTO.getPaginationStyle());
        bundle.setEnableEmailNotification(
                bundleDTO.getEnableEmailNotification() != null
                        ? bundleDTO.getEnableEmailNotification() == CcdBoolean.Yes
                        : null
        );
        bundle.setDocumentImage(bundleDTO.getDocumentImage());

        return bundle;
    }

    private List<StitchingBundleFolderDTO> getFolders(List<CcdValue<CcdBundleFolderDTO>> folders) {
        return folders.stream().map(this::getFolder).collect(Collectors.toList());
    }

    private StitchingBundleFolderDTO getFolder(CcdValue<CcdBundleFolderDTO> folderDto) {
        StitchingBundleFolderDTO folder = new StitchingBundleFolderDTO();
        folder.setFolderName(folderDto.getValue().getName());
        folder.setSortIndex(folderDto.getValue().getSortIndex());
        folder.setDocuments(getDocuments(folderDto.getValue().getDocuments()));
        folder.setFolders(getFolders(folderDto.getValue().getFolders()));

        return folder;
    }

    private List<StitchingBundleDocumentDTO> getDocuments(List<CcdValue<CcdBundleDocumentDTO>> bundleDocument) {
        return bundleDocument.stream().map(this::getDocument).collect(Collectors.toList());
    }

    private StitchingBundleDocumentDTO getDocument(CcdValue<CcdBundleDocumentDTO> bundleDocument) {
        String uri = bundleDocument.getValue().getSourceDocument().getUrl();

        StitchingBundleDocumentDTO document = new StitchingBundleDocumentDTO();
        document.setDocTitle(bundleDocument.getValue().getName());
        document.setDocDescription(bundleDocument.getValue().getDescription());
        document.setSortIndex(bundleDocument.getValue().getSortIndex());
        document.setDocumentURI(uri);
        document.setDocumentId(uri.substring(uri.lastIndexOf("/") + 1));

        return document;
    }
}
