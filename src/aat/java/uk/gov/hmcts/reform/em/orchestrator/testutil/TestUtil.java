package uk.gov.hmcts.reform.em.orchestrator.testutil;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.ImageRendering;
import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.ImageRenderingLocation;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBoolean;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDocumentDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentImage;
import uk.gov.hmcts.reform.em.test.dm.DmHelper;
import uk.gov.hmcts.reform.em.test.idam.IdamHelper;
import uk.gov.hmcts.reform.em.test.s2s.S2sHelper;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TestUtil {

    private String idamAuth;
    private String s2sAuth;

    @Autowired
    private IdamHelper idamHelper;
    @Autowired
    private S2sHelper s2sHelper;
    @Autowired
    private DmHelper dmHelper;
    @Value("${test.url}")
    private String testUrl;
    @Value("${document_management.url}")
    private String dmApiUrl;
    @Value("${document_management.docker_url}")
    private String dmDocumentApiUrl;

    @PostConstruct
    public void init() {
        idamHelper.createUser(getUsername(), Stream.of("caseworker").collect(Collectors.toList()));
        RestAssured.useRelaxedHTTPSValidation();
        idamAuth = idamHelper.authenticateUser(getUsername());
        s2sAuth = s2sHelper.getS2sToken();
    }

    public String uploadDocument(String fileName, String mimeType) {
        try {
            String url = dmHelper.getDocumentMetadata(
                    dmHelper.uploadAndGetId(
                            ClassLoader.getSystemResourceAsStream(fileName), mimeType, fileName))
                    .links.self.href;

            return getDmApiUrl().equals("http://localhost:4603")
                    ? url.replaceAll(getDmApiUrl(), getDmDocumentApiUrl())
                    : url;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String uploadDocument() {
        return uploadDocument("annotationTemplate.pdf", "application/pdf");
    }

    public RequestSpecification s2sAuthRequest() {
        return RestAssured
                .given()
                .header("ServiceAuthorization", s2sAuth);
    }

    public RequestSpecification authRequest() {
        return s2sAuthRequest()
                .header("Authorization", idamAuth);
    }

    public CcdBundleDTO getTestBundle() {
        CcdBundleDTO bundle = new CcdBundleDTO();
        bundle.setId(UUID.randomUUID().toString());
        bundle.setTitle("Bundle title");
        bundle.setDescription("Test bundle");
        bundle.setEligibleForStitchingAsBoolean(true);
        bundle.setEligibleForCloningAsBoolean(false);

        CcdDocument doc = new CcdDocument();
        doc.setBinaryUrl("www.exampleurl.com/binary");
        doc.setFileName("doc filename");
        doc.setUrl("www.exampleurl.com");
        bundle.setStitchedDocument(doc);

        List<CcdValue<CcdBundleDocumentDTO>> docs = new ArrayList<>();
        docs.add(getTestBundleDocument(uploadDocument()));
        bundle.setDocuments(docs);

        bundle.setFileName("fileName");
        bundle.setHasTableOfContents(CcdBoolean.Yes);
        bundle.setHasCoversheets(CcdBoolean.Yes);
        bundle.setStitchStatus("");
        return bundle;
    }

    private CcdValue<CcdBundleDocumentDTO> getTestBundleDocument(String documentUrl) {
        CcdBundleDocumentDTO document = new CcdBundleDocumentDTO("test document",
                "description", 1, new CcdDocument(documentUrl, "fn",
                uriWithBinarySuffix(documentUrl)));
        return new CcdValue<>(document);
    }

    public static String uriWithBinarySuffix(String s) {
        return s.endsWith("/binary") ? s : s + "/binary";
    }

    public CcdBundleDTO getTestBundleWithWordDoc() {
        CcdBundleDTO bundle = new CcdBundleDTO();
        bundle.setTitle("Bundle title");
        bundle.setDescription("Test bundle");
        bundle.setEligibleForStitchingAsBoolean(true);
        List<CcdValue<CcdBundleDocumentDTO>> docs = new ArrayList<>();
        docs.add(getTestBundleDocument(uploadDocX("wordDocument2.docx")));
        bundle.setDocuments(docs);

        return bundle;
    }

    public String uploadDocX(String docName) {
        return uploadDocument(docName, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    public static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.US_ASCII);
    }

    public String getUsername() {
        return "testytesttest" + getTestUrl().hashCode() + "@test.net";
    }

    public String getTestUrl() {
        return testUrl;
    }

    public String getDmApiUrl() {
        return dmApiUrl;
    }

    public String getDmDocumentApiUrl() {
        return dmDocumentApiUrl;
    }

    public CcdBundleDTO getTestBundleWithImageRendered() {
        DocumentImage documentImage = new DocumentImage();
        documentImage.setImageRendering(ImageRendering.translucent);
        documentImage.setImageRenderingLocation(ImageRenderingLocation.firstPage);
        documentImage.setCoordinateX(50);
        documentImage.setCoordinateY(50);
        documentImage.setDocmosisAssetId("schmcts.png");
        CcdBundleDTO bundle = new CcdBundleDTO();
        bundle.setId(UUID.randomUUID().toString());
        bundle.setTitle("Bundle title");
        bundle.setDescription("Test bundle");
        bundle.setEligibleForStitchingAsBoolean(true);
        bundle.setEligibleForCloningAsBoolean(false);
        bundle.setDocumentImage(documentImage);

        CcdDocument doc = new CcdDocument();
        doc.setBinaryUrl("www.exampleurl.com/binary");
        doc.setFileName("doc filename");
        doc.setUrl("www.exampleurl.com");
        bundle.setStitchedDocument(doc);

        List<CcdValue<CcdBundleDocumentDTO>> docs = new ArrayList<>();
        docs.add(getTestBundleDocument(uploadDocument()));
        bundle.setDocuments(docs);

        bundle.setFileName("fileName");
        bundle.setHasTableOfContents(CcdBoolean.Yes);
        bundle.setHasCoversheets(CcdBoolean.Yes);
        bundle.setStitchStatus("");
        bundle.setDocumentImage(documentImage);
        return bundle;
    }

    public RequestSpecification emptyIdamAuthRequest() {
        return s2sAuthRequest()
                .header("Authorization", null);
    }

    public RequestSpecification emptyIdamAuthAndEmptyS2SAuth() {
        return RestAssured
                .given()
                .header("ServiceAuthorization", null)
                .header("Authorization", null);
    }

    public RequestSpecification validAuthRequestWithEmptyS2SAuth() {
        return emptyS2sAuthRequest().header("Authorization", idamAuth);
    }

    public RequestSpecification validS2SAuthWithEmptyIdamAuth() {
        return s2sAuthRequest().header("Authorization", null);
    }

    private RequestSpecification emptyS2sAuthRequest() {
        return RestAssured.given().header("ServiceAuthorization", null);
    }

    public RequestSpecification invalidIdamAuthrequest() {
        return s2sAuthRequest().header("Authorization", "invalidIDAMAuthRequest");
    }

    public RequestSpecification invalidS2SAuth() {
        return invalidS2sAuthRequest().header("Authorization", idamAuth);
    }

    private RequestSpecification invalidS2sAuthRequest() {
        return RestAssured.given().header("ServiceAuthorization", "invalidS2SAuthorization");
    }

}

