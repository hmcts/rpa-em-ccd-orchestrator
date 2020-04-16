package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.*;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static pl.touk.throwing.ThrowingFunction.unchecked;

/**
 * Creates a new bundle from a bundle configuration and some case json.
 */
public class BundleFactory {

    public CcdBundleDTO create(BundleConfiguration configuration, JsonNode caseJson) throws DocumentSelectorException {
        CcdBundleDTO bundle = new CcdBundleDTO();
        bundle.setId(UUID.randomUUID().toString());
        bundle.setTitle(configuration.title);
        bundle.setCoverpageTemplate(configuration.coverpageTemplate);
        bundle.setHasCoversheetsAsBoolean(configuration.hasCoversheets);
        bundle.setHasTableOfContentsAsBoolean(configuration.hasTableOfContents);
        bundle.setHasFolderCoversheetsAsBoolean(configuration.hasFolderCoversheets);
        bundle.setPageNumberFormat(configuration.pageNumberFormat);
        bundle.setPaginationStyle(configuration.paginationStyle);
        bundle.setFileName(configuration.filename);
        bundle.setFileNameIdentifier(configuration.filenameIdentifier);
        bundle.setEligibleForCloningAsBoolean(false);
        bundle.setEligibleForStitchingAsBoolean(false);
        bundle.setEnableEmailNotificationAsBoolean(configuration.enableEmailNotification);
        bundle.setDocumentImage(configuration.documentImage);

        addFolders(configuration.folders, bundle.getFolders(), configuration.sortOrder, configuration.documentNameValue, caseJson);
        addDocuments(configuration.documents, bundle.getDocuments(), configuration.sortOrder, configuration.documentNameValue, caseJson);

        return bundle;
    }

    private void addFolders(List<BundleConfigurationFolder> sourceFolders,
                            List<CcdValue<CcdBundleFolderDTO>> destinationFolders,
                            BundleConfigurationSort sortOrder,
                            String documentNameValue,
                            JsonNode caseData) throws DocumentSelectorException {
        int sortIndex = 0;

        for (BundleConfigurationFolder folder : sourceFolders) {
            CcdBundleFolderDTO ccdFolder = new CcdBundleFolderDTO();
            ccdFolder.setName(folder.name);
            ccdFolder.setSortIndex(sortIndex++);
            destinationFolders.add(new CcdValue<>(ccdFolder));

            addDocuments(folder.documents, ccdFolder.getDocuments(), sortOrder, documentNameValue, caseData);

            if (folder.folders != null && !folder.folders.isEmpty()) {
                addFolders(folder.folders, ccdFolder.getFolders(), sortOrder, documentNameValue, caseData);
            }
        }
    }

    private void addDocuments(List<BundleConfigurationDocumentSelector> sourceDocuments,
                              List<CcdValue<CcdBundleDocumentDTO>> destinationDocuments,
                              BundleConfigurationSort sortOrder,
                              String documentNameValue,
                              JsonNode caseData) throws DocumentSelectorException {

        for (BundleConfigurationDocumentSelector selector : sourceDocuments) {
            List<CcdValue<CcdBundleDocumentDTO>> documents = selector instanceof BundleConfigurationDocument
                ? addDocument((BundleConfigurationDocument) selector, sortOrder, documentNameValue, caseData)
                : addDocumentSet((BundleConfigurationDocumentSet) selector, sortOrder, documentNameValue, caseData);

            destinationDocuments.addAll(documents);
        }

        if (sortOrder != null) {
            destinationDocuments.sort(sortOrder.order);

            for (int i = 0; i < destinationDocuments.size(); i++) {
                destinationDocuments.get(i).getValue().setSortIndex(i);
            }
        }
    }

    private List<CcdValue<CcdBundleDocumentDTO>> addDocument(BundleConfigurationDocument documentSelector,
                                                             BundleConfigurationSort sortOrder,
                                                             String documentNameValue,
                                                             JsonNode caseData) throws DocumentSelectorException {
        ArrayList<CcdValue<CcdBundleDocumentDTO>> list = new ArrayList<>();
        JsonNode node = caseData.at(documentSelector.property);

        if (node.isMissingNode()) {
            return new ArrayList<>();
        }

        if (node.isArray()) {
            throw new DocumentSelectorException("Element is an array: " + documentSelector.property);
        }

        list.add(getDocumentFromNode(node, sortOrder, documentNameValue));

        return list;
    }

    private CcdValue<CcdBundleDocumentDTO> getDocumentFromNode(JsonNode node,
                                                               BundleConfigurationSort sortOrder,
                                                               String documentNameValue) throws DocumentSelectorException {
        CcdDocument sourceDocument = new CcdDocument();

        sourceDocument.setUrl(getField(node, "/documentLink/document_url").asText());
        sourceDocument.setBinaryUrl(getField(node, "/documentLink/document_binary_url").asText());
        sourceDocument.setFileName(getField(node, "/documentLink/document_filename").asText());

        if (sortOrder != null) {
            JsonNode dateNode = node.at(sortOrder.field);
            if (!dateNode.isNull() && !dateNode.isMissingNode()) {
                sourceDocument.setCreatedDatetime(getDate(dateNode.asText()));
            } else {
                sourceDocument.setCreatedDatetime(LocalDateTime.MIN);
            }
        }

        CcdBundleDocumentDTO document = new CcdBundleDocumentDTO();
        document.setName(getField(node, StringUtils.defaultIfEmpty(documentNameValue,"/documentName")).asText());
        document.setSourceDocument(sourceDocument);

        return new CcdValue<>(document);
    }

    private LocalDateTime getDate(String date) {
        return date.length() > 10 ? LocalDateTime.parse(date) : LocalDate.parse(date).atStartOfDay();
    }

    private JsonNode getField(JsonNode outerNode, String path) throws DocumentSelectorException {
        JsonNode innerNode = outerNode.at(path);
        if (innerNode.isMissingNode()) {
            throw new DocumentSelectorException("Could not find the property " + path + " in the node: " + outerNode.asText());
        }
        return innerNode;
    }

    private List<CcdValue<CcdBundleDocumentDTO>> addDocumentSet(BundleConfigurationDocumentSet documentSelector,
                                                                BundleConfigurationSort sortOrder,
                                                                String documentNameValue,
                                                                JsonNode caseData) throws DocumentSelectorException {

        JsonNode list = caseData.at(documentSelector.property);

        if (list.isMissingNode()) {
            return new ArrayList<>();
        }

        if (!list.isArray()) {
            throw new DocumentSelectorException("Element is not an array: " + documentSelector.property);
        }

        return StreamSupport
            .stream(list.spliterator(), true)
            .map(n -> n.at("/value"))
            .filter(n -> anyFilterMatches(documentSelector.filters, n))
            .map(unchecked(node -> this.getDocumentFromNode(node, sortOrder, documentNameValue)))
            .collect(Collectors.toList());
    }

    private boolean anyFilterMatches(List<BundleConfigurationDocumentSet.BundleConfigurationFilter> filters,
                                     JsonNode node) {

        for (BundleConfigurationDocumentSet.BundleConfigurationFilter filter : filters) {
            if (!node.at(filter.property).asText().matches(filter.value)) {
                return false;
            }
        }

        return true;
    }
}
