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

    public void sendVerificationOtpEmail(String firstName, String email, String otp)
            throws UnsupportedEncodingException, MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message);


        String subject = "Here's your One Time Password (OTP) - Expire in 5 minutes";
        String content = "<p>Hello " + firstName + "<p>"
                +"<p> For security reason, you're required to use to following"
                +"One Time Password to register:</p>"
                +"<p><b>" + otp + "</b></p>"
                +"<br>"
                +"<p>Note: This OTP is set to expire in 5 minutes.</p>";
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

    public void sendUpcomingEventEmail(String firstName, String lastName, String email, String eventName)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message);

        String subject = "Coming soon " + eventName;
        String content = "<p> Hello" + lastName + firstName + "<p>" +
                "<h2 style='color: #333;'>Sự kiện sắp diễn ra!</h2>" +
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
