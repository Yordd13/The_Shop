package app.email.service;

import app.email.client.EmailClient;
import app.email.client.dto.NotificationPreference;
import app.email.client.dto.NotificationRequest;
import app.email.client.dto.UpsertNotificationPreference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class EmailService {

    private final EmailClient emailClient;

    @Autowired
    public EmailService(EmailClient emailClient) {
        this.emailClient = emailClient;
    }

    public void saveNotificationPreference(UUID userId, boolean isEnabled, String email){
        UpsertNotificationPreference preference = UpsertNotificationPreference
                .builder()
                .contactInfo(email)
                .notificationEnabled(isEnabled)
                .userId(userId)
                .build();

        ResponseEntity<Void> httpResponse = emailClient.upsertNotificationPreference(preference);

        if(!httpResponse.getStatusCode().is2xxSuccessful()){
            log.error("Error saving notification preference for user with id {}.", userId);
        }
    }

    public NotificationPreference getNotificationPreference(UUID userId) {

        ResponseEntity<NotificationPreference> httpResponse = emailClient.getUserPreference(userId);

        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Notification preference for user id [%s] does not exist.".formatted(userId));
        }

        return httpResponse.getBody();
    }

    public void sendNotification(UUID userId, String emailSubject, String emailBody) {

        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(userId)
                .subject(emailSubject)
                .body(emailBody)
                .build();

        ResponseEntity<Void> httpResponse;
        try {
            httpResponse = emailClient.sendNotification(notificationRequest);
            if (!httpResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Can't send email to user with id {}", userId);
            }
        } catch (Exception e) {
            log.warn("Can't send email to user with id {} due to 500 Internal Server Error.", userId);
        }
    }
}
