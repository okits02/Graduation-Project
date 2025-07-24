package com.example.notification_service.service;

import com.example.notification_service.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    public void sendVerificationOtpEmail( String email, String otp)
            throws UnsupportedEncodingException, MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message);


        String subject = "Here's your One Time Password (OTP) - Expire in 5 minutes";
        String content = """
            <div style="font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px; border-radius: 10px;">
            <h2 style="color: #2c3e50;">Your OTP Verification Code</h2>
            <p>For security reasons, please use the following One-Time Password (OTP) to complete your verification:</p>
            <div style="font-size: 24px; font-weight: bold; color: #e74c3c; background-color: #fff; border: 2px dashed #e74c3c; padding: 15px; text-align: center; border-radius: 8px; margin: 20px 0;">
            """ + otp + """
            </div>
            <p style="color: #555;">Note: This OTP will expire in <strong>5 minutes</strong>.</p>
            <br>
            <p style="font-size: 14px; color: #999;">If you did not request this code, please ignore this email.</p>
            <p style="font-size: 14px; color: #999;">Thank you,<br><strong>Shop Support Team</strong></p>
            </div>
            """;
        try {
            messageHelper.setFrom("AnhTu13@gmail.com", "Shop Support");
            messageHelper.setTo(email);
            messageHelper.setSubject(subject);
            messageHelper.setText(content, true);
            mailSender.send(message);
        } catch (MailException e)
        {
            throw new MailSendException("Failed to send email: " + e.getMessage());
        } catch (MessagingException e) {
            throw  new MessagingException("Error configuring email message: " + e.getMessage());
        }
    }

    public void sendUpcomingEventEmail( String email, String eventName)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message);

        String subject = "Coming soon " + eventName;
        String content =
                "<p style='color: #333;'>san pham sap ra mat!</p>" +
                "<h3 style='color: #4CAF50;'>" + eventName + "</h3>" +
                "<p>Hãy theo dõi để không bỏ lỡ nhé!</p>" +
                "</div>";
        try
        {
            messageHelper.setFrom("AnhTu13@gmail.com", "Shop Staff");
            messageHelper.setTo(email);
            messageHelper.setSubject(subject);
            messageHelper.setText(content, true);
            mailSender.send(message);
        }catch (MailException e)
        {
            throw new MailSendException("Failed to send email: " + e.getMessage());
        }catch (MessagingException e)
        {
            throw new MessagingException("Error configuring email message: " + e.getMessage());
        }
    }
}
