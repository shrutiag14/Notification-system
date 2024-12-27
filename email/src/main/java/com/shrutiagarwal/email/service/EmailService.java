package com.shrutiagarwal.email.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.shrutiagarwal.email.request.EmailRequest;
import com.shrutiagarwal.email.response.EmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@Slf4j
public class EmailService {
    private String SENDGRID_API_KEY = "your_api_key_goes_here";


    public EmailResponse sendEmail(EmailRequest emailRequest) {

        Email from = new Email("your_sendgrid_verified__sender_email");

        String subject = emailRequest.getEmailSubject() + " | Scalable Notification System";
        Email to = new Email(emailRequest.getEmailId());
        Content content = new Content("text/plain", emailRequest.getMessage());
        Mail mail = new Mail(from, subject, to, content);
        Optional.ofNullable(emailRequest.getEmailAttachments())
                .filter(attachments -> attachments.length > 0)
                .ifPresent(attachments -> addAttachments(mail, attachments));

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = new SendGrid(SENDGRID_API_KEY).api(request);
            log.info("Email Request (Notification Id: {}). Response from SendGrid: Status Code: {}, Body: {}, Headers: {}", emailRequest.getNotificationId(), response.getStatusCode(), response.getBody(), response.getHeaders());
            return new EmailResponse(response.getStatusCode(), response.getBody());
        } catch (IOException ex) {
            log.error("Something went wrong with SendGrid. Exception: {}", ex.toString());
            return new EmailResponse(500, "IO Exception occurred in Email Service-SendGrid");
        }
    }

    private void addAttachments(Mail mail, String[] attachments) {
        for (String attachment : attachments) {
            Attachments attach = new Attachments();
            attach.setContent(attachment);  // Assuming Base64 content here
            attach.setType("image/png");  // Can be parameterized
            attach.setFilename("banner.png");  // Can be dynamic or optional
            attach.setDisposition("inline");
            attach.setContentId("Banner");
            mail.addAttachments(attach);
        }
    }
}
