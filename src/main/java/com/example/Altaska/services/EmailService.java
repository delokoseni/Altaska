package com.example.Altaska.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final String username = System.getProperty("EMAIL");
    private final String password = System.getProperty("EMAIL_PASSWORD");

    public void sendEmail(String recipientEmail, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.yandex.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new jakarta.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            System.out.println("Email sent successfully");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
