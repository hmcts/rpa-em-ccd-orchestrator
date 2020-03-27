package uk.gov.hmcts.reform.em.orchestrator.stitching.mapper;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBoolean;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDocumentDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleFolderDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.ImageRendering;
import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.ImageRenderingLocation;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.*;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentImage;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleDTO;

public class StitchingDTOMapperTest {

    @Test
    public void convert() {
        CcdBundleFolderDTO folder1DTO = getFolder(1);
        CcdBundleFolderDTO folder2DTO = getFolder(2);
        CcdBundleFolderDTO folder3DTO = getFolder(3);
        CcdBundleDocumentDTO document1DTO = getDocument(1);
        CcdBundleDocumentDTO document2DTO = getDocument(2);
        CcdBundleDocumentDTO document3DTO = getDocument(3);
        CcdBundleDocumentDTO document4DTO = getDocument(4);
        CcdBundleDocumentDTO document5DTO = getDocument(5);

        folder1DTO.getDocuments().add(new CcdValue<>(document1DTO));
        folder1DTO.getFolders().add(new CcdValue<>(folder2DTO));

        folder2DTO.getDocuments().add(new CcdValue<>(document2DTO));
        folder2DTO.getDocuments().add(new CcdValue<>(document3DTO));

        folder3DTO.getDocuments().add(new CcdValue<>(document4DTO));
        folder3DTO.getDocuments().add(new CcdValue<>(document5DTO));

        DocumentImage documentImage = new DocumentImage();
        documentImage.setDocmosisAssetId("schmcts.png");
        documentImage.setCoordinateY(50);
        documentImage.setCoordinateY(50);
        documentImage.setImageRenderingLocation(ImageRenderingLocation.firstPage);
        documentImage.setImageRendering(ImageRendering.translucent);

        CcdBundleDTO bundleDTO = new CcdBundleDTO();
        bundleDTO.setTitle("title");
        bundleDTO.setDescription("description");
        bundleDTO.setFileName("a-file.pdf");
        bundleDTO.setHasCoversheets(CcdBoolean.Yes);
        bundleDTO.setHasTableOfContents(CcdBoolean.Yes);
        bundleDTO.setHasFolderCoversheets(CcdBoolean.Yes);
        bundleDTO.getDocuments().add(new CcdValue<>(document1DTO));
        bundleDTO.getFolders().add(new CcdValue<>(folder1DTO));
        bundleDTO.getFolders().add(new CcdValue<>(folder3DTO));
        bundleDTO.setEnableEmailNotification(CcdBoolean.Yes);
        bundleDTO.setDocumentImage(documentImage);

        StitchingDTOMapper mapper = new StitchingDTOMapper();
        StitchingBundleDTO stitchingBundleDTO = mapper.toStitchingDTO(bundleDTO);

        assertEquals(bundleDTO.getTitle(), stitchingBundleDTO.getBundleTitle());
        assertEquals(bundleDTO.getDescription(), stitchingBundleDTO.getDescription());
        assertEquals(bundleDTO.getFileName(), stitchingBundleDTO.getFileName());
        assertEquals(bundleDTO.getHasCoversheets() == CcdBoolean.Yes, stitchingBundleDTO.getHasCoversheets());
        assertEquals(bundleDTO.getHasTableOfContents() == CcdBoolean.Yes, stitchingBundleDTO.getHasTableOfContents());
        assertEquals(bundleDTO.getHasFolderCoversheets() == CcdBoolean.Yes, stitchingBundleDTO.getHasFolderCoversheets());
        assertEquals(bundleDTO.getEnableEmailNotification() == CcdBoolean.Yes, stitchingBundleDTO.getEnableEmailNotification());
        assertEquals(bundleDTO.getDocumentImage().getDocmosisAssetId(), stitchingBundleDTO.getDocumentImage().getDocmosisAssetId());
        assertEquals(bundleDTO.getDocumentImage().getCoordinateX(), stitchingBundleDTO.getDocumentImage().getCoordinateX());
        assertEquals(bundleDTO.getDocumentImage().getImageRendering(), stitchingBundleDTO.getDocumentImage().getImageRendering());
        assertEquals(bundleDTO.getDocumentImage().getImageRenderingLocation(), stitchingBundleDTO.getDocumentImage().getImageRenderingLocation());

        assertEquals(
                bundleDTO.getDocuments().get(0).getValue().getSourceDocument().getUrl(),
                stitchingBundleDTO.getDocuments().get(0).getDocumentURI());
        assertEquals(
                bundleDTO.getDocuments().get(0).getValue().getName(),
                stitchingBundleDTO.getDocuments().get(0).getDocTitle());
        assertEquals(
                bundleDTO.getDocuments().get(0).getValue().getSortIndex(),
                stitchingBundleDTO.getDocuments().get(0).getSortIndex());

        assertEquals(
                bundleDTO.getFolders().get(0).getValue().getName(),
                stitchingBundleDTO.getFolders().get(0).getFolderName());
        assertEquals(
                bundleDTO.getFolders().get(0).getValue().getSortIndex(),
                stitchingBundleDTO.getFolders().get(0).getSortIndex());
        assertEquals(
                bundleDTO.getFolders().get(0).getValue().getDocuments().get(0).getValue().getSourceDocument().getUrl(),
                stitchingBundleDTO.getFolders().get(0).getDocuments().get(0).getDocumentURI());
        assertEquals(
                bundleDTO.getFolders().get(0).getValue().getDocuments().get(0).getValue().getSortIndex(),
                stitchingBundleDTO.getFolders().get(0).getDocuments().get(0).getSortIndex());

        assertEquals(
                bundleDTO.getFolders().get(0).getValue().getFolders().get(0).getValue().getName(),
                stitchingBundleDTO.getFolders().get(0).getFolders().get(0).getFolderName());
        assertEquals(
                bundleDTO.getFolders().get(0).getValue().getFolders().get(0).getValue().getSortIndex(),
                stitchingBundleDTO.getFolders().get(0).getFolders().get(0).getSortIndex());
        assertEquals(
                bundleDTO.getFolders().get(0).getValue().getFolders().get(0).getValue()
                        .getDocuments().get(0).getValue().getSourceDocument().getUrl(),
                stitchingBundleDTO.getFolders().get(0).getFolders().get(0).getDocuments().get(0).getDocumentURI());
        assertEquals(bundleDTO.getFolders().get(0).getValue().getFolders().get(0).getValue()
                        .getDocuments().get(0).getValue().getSortIndex(),
                stitchingBundleDTO.getFolders().get(0).getFolders().get(0).getDocuments().get(0).getSortIndex());

        assertEquals(
                bundleDTO.getFolders().get(1).getValue().getName(),
                stitchingBundleDTO.getFolders().get(1).getFolderName());
        assertEquals(
                bundleDTO.getFolders().get(1).getValue().getSortIndex(),
                stitchingBundleDTO.getFolders().get(1).getSortIndex());
        assertEquals(
                bundleDTO.getFolders().get(1).getValue().getDocuments().get(0).getValue().getSourceDocument().getUrl(),
                stitchingBundleDTO.getFolders().get(1).getDocuments().get(0).getDocumentURI());
        assertEquals(
                bundleDTO.getFolders().get(1).getValue().getDocuments().get(0).getValue().getSortIndex(),
                stitchingBundleDTO.getFolders().get(1).getDocuments().get(0).getSortIndex());

    }

    @Test
    public void convertNullEmailNotification() {
        CcdBundleDTO bundleDTO = new CcdBundleDTO();
        bundleDTO.setTitle("title");
        bundleDTO.setDescription("description");
        bundleDTO.setFileName("a-file.pdf");
        bundleDTO.setHasCoversheets(CcdBoolean.Yes);
        bundleDTO.setHasTableOfContents(CcdBoolean.Yes);
        bundleDTO.setHasFolderCoversheets(CcdBoolean.Yes);
        bundleDTO.setEnableEmailNotification(null);

        StitchingDTOMapper mapper = new StitchingDTOMapper();
        StitchingBundleDTO stitchingBundleDTO = mapper.toStitchingDTO(bundleDTO);

        assertEquals(null, stitchingBundleDTO.getEnableEmailNotification());
    }

    @Test
    public void convertNoEmailNotification() {
        CcdBundleDTO bundleDTO = new CcdBundleDTO();
        bundleDTO.setTitle("title");
        bundleDTO.setDescription("description");
        bundleDTO.setFileName("a-file.pdf");
        bundleDTO.setHasCoversheets(CcdBoolean.Yes);
        bundleDTO.setHasTableOfContents(CcdBoolean.Yes);
        bundleDTO.setHasFolderCoversheets(CcdBoolean.Yes);
        bundleDTO.setEnableEmailNotification(CcdBoolean.No);

        StitchingDTOMapper mapper = new StitchingDTOMapper();
        StitchingBundleDTO stitchingBundleDTO = mapper.toStitchingDTO(bundleDTO);

        assertEquals(false, stitchingBundleDTO.getEnableEmailNotification());
    }

    private CcdBundleFolderDTO getFolder(int index) {
        CcdBundleFolderDTO folderDTO = new CcdBundleFolderDTO();
        folderDTO.setName(String.format("folder %s name", index));
        folderDTO.setSortIndex(index);

        return folderDTO;
    }

    private CcdBundleDocumentDTO getDocument(int index) {
        CcdBundleDocumentDTO documentDTO = new CcdBundleDocumentDTO(
            String.format("document %s title", index),
            String.format("document %s description", index),
            index,
                new CcdDocument(String.format("/document/%s", index), "xxx",
                        String.format("/document/%s/binary", index))

        );

        return documentDTO;
    }

}
