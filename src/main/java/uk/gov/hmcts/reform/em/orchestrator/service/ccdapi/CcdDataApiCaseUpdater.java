package uk.gov.hmcts.reform.em.orchestrator.service.ccdapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCaseDataContent;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdEvent;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.CallbackException;

import java.io.IOException;

@Service
public class CcdDataApiCaseUpdater {

    private final Logger log = LoggerFactory.getLogger(CcdDataApiCaseUpdater.class);

    private final OkHttpClient http;
    private final AuthTokenGenerator authTokenGenerator;
    private final String ccdDataBaseUrl;
    private final String ccdUpdateCasePath = "/cases/%s/events";
    private final ObjectMapper objectMapper;

    public CcdDataApiCaseUpdater(OkHttpClient http,
                                 AuthTokenGenerator authTokenGenerator,
                                 @Value("${ccd.data.api.url}") String ccdDataBaseUrl,
                                 ObjectMapper objectMapper) {
        this.http = http;
        this.authTokenGenerator = authTokenGenerator;
        this.ccdDataBaseUrl = ccdDataBaseUrl;
        this.objectMapper = objectMapper;
    }

    /**
     * Call to https://hmcts.github.io/reform-api-docs/swagger.html?url=https://hmcts.github.io/reform-api-docs/specs/ccd-data-store-api.v2.json#/case-controller/createEventUsingPOST.
     *
     * @param ccdCallbackDto - callbackDTO
     * @param jwt - authentication
     */
    public void executeUpdate(CcdCallbackDto ccdCallbackDto, String jwt) {
        try {
            final String json = objectMapper.writeValueAsString(createCcdCaseDataContent(ccdCallbackDto));
            final RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
            final Request updateRequest = new Request.Builder()
                    .addHeader("Authorization", jwt)
                    .addHeader("experimental", "true")
                    .addHeader("ServiceAuthorization", authTokenGenerator.generate())
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.get("application/json").toString())
                    .url(String.format(ccdDataBaseUrl + ccdUpdateCasePath, ccdCallbackDto.getCaseId()))
                    .method("POST", body)
                    .build();

            log.info(String.format("Ccd Update URL :  %s", updateRequest.url()));

            final Response updateResponse = http.newCall(updateRequest).execute();

            if (!updateResponse.isSuccessful()) {
                throw new CallbackException(updateResponse.code(), updateResponse.body().string(), "Update of case data failed");
            }
        } catch (IOException e) {
            throw new CallbackException(500, null, String.format("IOException: %s", e.getMessage()));
        }

    }

    private CcdCaseDataContent createCcdCaseDataContent(CcdCallbackDto ccdCallbackDto) {
        CcdCaseDataContent ccdCaseDataContent = new CcdCaseDataContent();
        ccdCaseDataContent.setEvent(new CcdEvent(ccdCallbackDto.getEventId()));
        ccdCaseDataContent.setEventData(ccdCallbackDto.getCaseData());
        ccdCaseDataContent.setToken(ccdCallbackDto.getEventToken());
        ccdCaseDataContent.setData(ccdCallbackDto.getCaseData());
        return ccdCaseDataContent;
    }

}
