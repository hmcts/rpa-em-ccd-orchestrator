package uk.gov.hmcts.reform.em.orchestrator.service.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.CallbackException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    @Value("${auth.idam.client.baseUrl}")
    private String idamBaseUrl;

    private NotificationClient notificationClient;
    private OkHttpClient http;

    private final ObjectMapper jsonMapper = new ObjectMapper();

    private static final String IDAM_USER_DETAILS_ENDPOINT = "/details";

    private final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService(NotificationClient notificationClient, OkHttpClient http) {
        this.notificationClient = notificationClient;
        this.http = http;
    }

    public void sendEmailNotification(String templateId, String jwt,
                                      String caseId, String bundleTitle, String failureMessage) {
        try {
            notificationClient.sendEmail(
                    templateId,
                    getUserEmail(jwt),
                    createPersonalisation(caseId, bundleTitle, failureMessage),
                    "Email Notification: " + caseId);

            log.info(String.format("Notification email sent for caseId: %s", caseId));

        } catch (NotificationClientException e) {
            throw new CallbackException(500, null, String.format("NotificationClientException: %s", e.getMessage()));
        }
    }

    private Map<String, String> createPersonalisation(String caseId, String bundleTitle, String failureMessage) {
        HashMap<String, String> personalisation = new HashMap<>();
        personalisation.put("case_reference", caseId);
        personalisation.put("bundle_name", bundleTitle);
        personalisation.put("system_error_message", failureMessage);
        return personalisation;
    }

    private String getUserEmail(String jwt) {
        try {
            final Request request = new Request.Builder()
                    .addHeader("authorization", jwt)
                    .url(idamBaseUrl + IDAM_USER_DETAILS_ENDPOINT)
                    .get()
                    .build();

            final Response response = http.newCall(request).execute();

            if (response.isSuccessful()) {
                JsonNode userDetails = jsonMapper.readValue(response.body().byteStream(), JsonNode.class);
                return userDetails.get("email").asText();
            } else {
                throw new CallbackException(500, response.body().string(), "Unable to retrieve user details");
            }
        } catch (IOException e) {
            throw new CallbackException(500, null, String.format("IOException: %s", e.getMessage()));
        }
    }
}
