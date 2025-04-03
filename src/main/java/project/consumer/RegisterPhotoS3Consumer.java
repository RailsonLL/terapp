package project.consumer;

import com.amazonaws.services.s3.event.S3EventNotification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import project.models.SnsMessage;
import project.services.UserService;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.IOException;
import java.util.UUID;

@Service
public class RegisterPhotoS3Consumer {
    private static final Logger log = LoggerFactory.getLogger(
            RegisterPhotoS3Consumer.class);
    private ObjectMapper objectMapper;
    private UserService userService;

    @Autowired
    public RegisterPhotoS3Consumer(ObjectMapper objectMapper, UserService userService) {
        this.objectMapper = objectMapper;
        this.userService = userService;
    }

    @JmsListener(destination = "${aws.sqs.queue.invoice.events.name}")
    public void receiveS3Event(TextMessage textMessage)
            throws JMSException, IOException {
        SnsMessage snsMessage = objectMapper.readValue(textMessage.getText(),
                SnsMessage.class);

        S3EventNotification s3EventNotification = objectMapper
                .readValue(snsMessage.getMessage(), S3EventNotification.class);

        saveUserPhoto(s3EventNotification);
    }

    private void saveUserPhoto(S3EventNotification
                                                    s3EventNotification){
        for (S3EventNotification.S3EventNotificationRecord
                s3EventNotificationRecord : s3EventNotification.getRecords()) {
            S3EventNotification.S3Entity s3Entity =
                    s3EventNotificationRecord.getS3();

            String objectKey = s3Entity.getObject().getKey();

            log.info("Photo = {}", objectKey);
            log.info("UserID = {}", getUserIdFromKey(objectKey));
            userService.saveUserPhoto(objectKey, getUserIdFromKey(objectKey));
            log.info("User photo successfully registered. Photo = {}", objectKey);
        }
    }

    private UUID getUserIdFromKey(String objectKey) {
        String prefixo = "user-";
        String sufixo = "-user";

        int inicioIndex = objectKey.indexOf(prefixo);
        if (inicioIndex == -1) {
            return null; // Não encontrou o prefixo
        }

        // Ajusta o índice para começar depois do prefixo
        inicioIndex += prefixo.length();

        int fimIndex = objectKey.indexOf(sufixo, inicioIndex);
        if (fimIndex == -1) {
            return null; // Não encontrou o sufixo
        }

        return UUID.fromString(objectKey.substring(inicioIndex, fimIndex));
    }
}




















