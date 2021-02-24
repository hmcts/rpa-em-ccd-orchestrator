package uk.gov.hmcts.reform.em.orchestrator.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.em.orchestrator.config.Constants;
import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.PageNumberFormat;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentImage;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.LinkedList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CcdBundleDTO {

    private String id;
    private String title;
    @Size(max = 255)
    private String description;
    private String eligibleForStitching;
    private String eligibleForCloning;
    private CcdDocument stitchedDocument;
    private List<CcdValue<CcdBundleDocumentDTO>> documents = new LinkedList<>();
    private List<CcdValue<CcdBundleFolderDTO>> folders = new LinkedList<>();

    @Size(min = 2, max = 50, message = Constants.STITCHED_FILE_NAME_FIELD_LENGTH_ERROR_MSG)
    @Pattern(regexp = "^[-._A-Za-z0-9]*$")
    private String fileName;
    private String fileNameIdentifier;
    private String coverpageTemplate;
    @JsonIgnore
    private JsonNode coverpageTemplateData;
    private CcdBoolean hasTableOfContents;
    private CcdBoolean hasCoversheets;
    private CcdBoolean hasFolderCoversheets;
    private String stitchStatus;
    private CcdBundlePaginationStyle paginationStyle = CcdBundlePaginationStyle.off;
    private PageNumberFormat pageNumberFormat = PageNumberFormat.numberOfPages;
    private String stitchingFailureMessage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CcdBoolean enableEmailNotification;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DocumentImage documentImage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String isEligibleForStitching() {
        return eligibleForStitching;
    }

    public void setEligibleForStitching(String eligibleForStitching) {
        this.eligibleForStitching = eligibleForStitching;
    }

    public String getEligibleForStitching() {
        return eligibleForStitching;
    }

    @JsonIgnore
    public boolean getEligibleForStitchingAsBoolean() {
        return eligibleForStitching != null && eligibleForStitching.equalsIgnoreCase("yes");
    }

    @JsonIgnore
    public void setEligibleForStitchingAsBoolean(boolean eligibleForStitching) {
        this.eligibleForStitching = eligibleForStitching ? "yes" : "no";
    }

    public void setEligibleForCloning(String eligibleForCloning) {
        this.eligibleForCloning = eligibleForCloning;
    }

    public String getEligibleForCloning() {
        return eligibleForCloning;
    }

    @JsonIgnore
    public boolean getEligibleForCloningAsBoolean() {
        return eligibleForCloning != null && eligibleForCloning.equalsIgnoreCase("yes");
    }

    @JsonIgnore
    public void setEligibleForCloningAsBoolean(boolean eligibleForCloning) {
        this.eligibleForCloning = eligibleForCloning ? "yes" : "no";
    }

    public CcdDocument getStitchedDocument() {
        return stitchedDocument;
    }

    public void setStitchedDocument(CcdDocument stitchedDocument) {
        this.stitchedDocument = stitchedDocument;
    }

    public List<CcdValue<CcdBundleDocumentDTO>> getDocuments() {
        return documents;
    }

    public void setDocuments(List<CcdValue<CcdBundleDocumentDTO>> documents) {
        this.documents = documents;
    }

    public List<CcdValue<CcdBundleFolderDTO>> getFolders() {
        return folders;
    }

    public void setFolders(List<CcdValue<CcdBundleFolderDTO>> folders) {
        this.folders = folders;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileNameIdentifier() {
        return fileNameIdentifier;
    }

    public void setFileNameIdentifier(String fileNameIdentifier) {
        this.fileNameIdentifier = fileNameIdentifier;
    }

    public String getCoverpageTemplate() {
        return coverpageTemplate;
    }

    public void setCoverpageTemplate(String coverpageTemplate) {
        this.coverpageTemplate = coverpageTemplate;
    }

    public JsonNode getCoverpageTemplateData() {
        return coverpageTemplateData;
    }

    public void setCoverpageTemplateData(JsonNode coverpageTemplateData) {
        this.coverpageTemplateData = coverpageTemplateData;
    }

    public CcdBoolean getHasTableOfContents() {
        return hasTableOfContents;
    }

    public void setHasTableOfContents(CcdBoolean hasTableOfContents) {
        this.hasTableOfContents = hasTableOfContents;
    }

    public CcdBoolean getHasCoversheets() {
        return hasCoversheets;
    }

    public void setHasCoversheets(CcdBoolean hasCoversheets) {
        this.hasCoversheets = hasCoversheets;
    }

    public String getStitchStatus() {
        return stitchStatus;
    }

    public void setStitchStatus(String stitchStatus) {
        this.stitchStatus = stitchStatus;
    }

    public CcdBoolean getHasFolderCoversheets() {
        return hasFolderCoversheets;
    }

    public void setHasFolderCoversheets(CcdBoolean hasFolderCoversheets) {
        this.hasFolderCoversheets = hasFolderCoversheets;
    }

    public void setHasCoversheetsAsBoolean(boolean hasCoversheets) {
        this.hasCoversheets = hasCoversheets ? CcdBoolean.Yes : CcdBoolean.No;
    }

    public void setHasTableOfContentsAsBoolean(boolean hasTableOfContents) {
        this.hasTableOfContents = hasTableOfContents ? CcdBoolean.Yes : CcdBoolean.No;
    }

    public void setHasFolderCoversheetsAsBoolean(boolean hasFolderCoversheets) {
        this.hasFolderCoversheets = hasFolderCoversheets ? CcdBoolean.Yes : CcdBoolean.No;
    }

    public CcdBundlePaginationStyle getPaginationStyle() {
        return paginationStyle;
    }

    public void setPaginationStyle(CcdBundlePaginationStyle paginationStyle) {
        this.paginationStyle = paginationStyle;
    }

    public PageNumberFormat getPageNumberFormat() {
        return pageNumberFormat;
    }

    public void setPageNumberFormat(PageNumberFormat pageNumberFormat) {
        this.pageNumberFormat = pageNumberFormat;
    }

    public String getStitchingFailureMessage() {
        return stitchingFailureMessage;
    }

    public void setStitchingFailureMessage(String stitchingFailureMessage) {
        this.stitchingFailureMessage = stitchingFailureMessage;

    }

    public CcdBoolean getEnableEmailNotification() {
        return enableEmailNotification;
    }

    public void setEnableEmailNotification(CcdBoolean enableEmailNotification) {
        this.enableEmailNotification = enableEmailNotification;
    }

    @JsonIgnore
    public void setEnableEmailNotificationAsBoolean(Boolean enableEmailNotification) {
        if (enableEmailNotification == null) {
            this.enableEmailNotification = null;
        } else {
            this.enableEmailNotification = enableEmailNotification ? CcdBoolean.Yes : CcdBoolean.No;
        }
    }

    @JsonIgnore
    public Boolean getEnableEmailNotificationAsBoolean() {
        return enableEmailNotification != null ? enableEmailNotification == CcdBoolean.Yes : null;
    }

    public DocumentImage getDocumentImage() {
        return documentImage;
    }

    public void setDocumentImage(DocumentImage documentImage) {
        this.documentImage = documentImage;
    }
}
