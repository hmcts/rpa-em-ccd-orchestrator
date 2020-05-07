package uk.gov.hmcts.reform.em.orchestrator.stitching;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import okhttp3.*;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.TaskState;
import uk.gov.hmcts.reform.em.orchestrator.stitching.mapper.StitchingDTOMapper;

import java.io.IOException;

import static uk.gov.hmcts.reform.em.orchestrator.util.StringUtilities.ensurePdfExtension;

/**
 * Communicates with the Stitching API in order to turn a bundle into a stitched document.
 */
public class StitchingService {

    private static final int DEFAULT_MAX_RETRIES = 200;
    private static final int SLEEP_TIME = 500;
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final StitchingDTOMapper dtoMapper;
    private final OkHttpClient http;
    private final String documentTaskEndpoint;
    private final AuthTokenGenerator authTokenGenerator;
    private final int maxRetries;

    public StitchingService(StitchingDTOMapper dtoMapper, OkHttpClient http, String documentTaskEndpoint,
                            AuthTokenGenerator authTokenGenerator) {
        this(dtoMapper, http, documentTaskEndpoint, authTokenGenerator, DEFAULT_MAX_RETRIES);
    }

    public StitchingService(StitchingDTOMapper dtoMapper, OkHttpClient http, String documentTaskEndpoint,
                            AuthTokenGenerator authTokenGenerator, int maxRetries) {
        this.dtoMapper = dtoMapper;
        this.http = http;
        this.documentTaskEndpoint = documentTaskEndpoint;
        this.authTokenGenerator = authTokenGenerator;
        this.maxRetries = maxRetries;
    }

    /**
     * This method creates a document task in the stitching API and polls until it is complete.
     * If the document was succesfully
     * stitched the new document ID from DM store will be returned, otherwise an exception is thrown.
     */
    public CcdDocument stitch(CcdBundleDTO bundleDto, String jwt) throws InterruptedException  {
        final StitchingBundleDTO bundle = dtoMapper.toStitchingDTO(bundleDto);
        final DocumentTaskDTO documentTask = new DocumentTaskDTO();
        documentTask.setBundle(bundle);
        documentTask.setJwt(jwt);

        try {
            final DocumentTaskDTO createdDocumentTaskDTO = startStitchingTask(documentTask, jwt);
            final String response = poll(createdDocumentTaskDTO.getId(), jwt);
            final DocumentContext json = JsonPath
                .using(Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL))
                .parse(response);

            if (JsonPath.read(response, "$.taskState").equals(TaskState.DONE.toString())) {
                final String fileName = json.read("$.bundle.fileName");

                return new CcdDocument(
                    json.read("$.bundle.stitchedDocumentURI"),
                    ensurePdfExtension(fileName),
                    uriWithBinarySuffix(json.read("$.bundle.stitchedDocumentURI"))
                );
            } else {
                throw new StitchingServiceException(
                        "Stitching failed: " + json.read("$.failureDescription"));
            }
        } catch (IOException e) {
            throw new StitchingServiceException(
                    String.format("Unable to stitch bundle using %s: %s", documentTaskEndpoint, e.getMessage()), e);
        }
    }

    private static String uriWithBinarySuffix(String s) {
        return s.endsWith("/binary") ? s : s + "/binary";
    }

    public DocumentTaskDTO startStitchingTask(DocumentTaskDTO documentTask, String jwt) throws IOException {
        final String json = jsonMapper.writeValueAsString(documentTask);
        final RequestBody body = RequestBody.create(MediaType.get("application/json"), json);
        final Request request = new Request.Builder()
            .addHeader("Authorization", jwt)
            .addHeader("ServiceAuthorization", authTokenGenerator.generate())
            .url(documentTaskEndpoint)
            .method("POST", body)
            .build();

        final Response response = http.newCall(request).execute();

        if (response.isSuccessful()) {
            return jsonMapper.readValue(response.body().byteStream(), DocumentTaskDTO.class);

        } else {
            throw new IOException("Unable to create stitching task: " + response.body().string());
        }
    }

    private String poll(long taskId, String jwt) throws IOException, InterruptedException {
        final Request request = new Request.Builder()
            .addHeader("Authorization", jwt)
            .addHeader("ServiceAuthorization", authTokenGenerator.generate())
            .url(documentTaskEndpoint + taskId)
            .get()
            .build();

        for (int i = 0; i < maxRetries; i++) {
            final Response response = http.newCall(request).execute();
            final String responseBody = response.body().string();
            final String taskState = JsonPath.read(responseBody, "$.taskState");

            if (!taskState.equals(TaskState.NEW.toString())) {
                return responseBody;
            } else {
                Thread.sleep(SLEEP_TIME);
            }
        }

        throw new IOException("Task not complete after maximum number of retries");
    }
}
