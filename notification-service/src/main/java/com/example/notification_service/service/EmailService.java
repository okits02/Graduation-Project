package com.example.notification_service.service;

import com.example.notification_service.dto.CustomerVM;
import com.example.notification_service.dto.NotificationEvent;
import com.example.notification_service.dto.ProductSkuVM;
import com.example.notification_service.dto.request.SendEmailRequest;
import com.example.notification_service.enums.Status;
import com.example.notification_service.repository.NotificationRepository;
import com.example.notification_service.repository.httpClient.OrderClient;
import com.example.notification_service.repository.httpClient.ProfileClient;
import com.example.notification_service.repository.httpClient.SearchClient;
import com.example.notification_service.repository.httpClient.UserClient;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private final OrderClient orderClient;
    private final UserClient userClient;
    private final ProfileClient profileClient;
    private final SearchClient searchClient;

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

    public void sendEmailForOrder(NotificationEvent notificationEvent) throws MessagingException,
            UnsupportedEncodingException {
        if(notificationEvent == null) return;
        var response = userClient.getEmailById(notificationEvent.getUserId());
        if(response == null || response.getCode() != 200){
            log.info("userid or email must be failed");
        }
        var productResponse = searchClient.getProductDetails(notificationEvent.getSkus());
        if(response == null || response.getCode() != 200){
            log.info("skus or product must be failed");
        }
        var profileResponse = profileClient.getProfileForRating(notificationEvent.getUserId());
        if(response == null || response.getCode() != 200){
            log.info("profile or userId must be failed");
        }
        sendOrderEmail(profileResponse.getBody().getResult(), productResponse.getResult(),
                notificationEvent.getTotalPrice(), notificationEvent.getStatus(),
                response.getResult());
    }

    public void sendOrderEmail(
            CustomerVM customer,
            List<ProductSkuVM> products,
            BigDecimal totalPrice,
            Status status,
            String email
    ) throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("AnhTu13@gmail.com", "Shop Support");
        helper.setTo(email);
        helper.setSubject(buildSubject(status));

        String content = buildOrderEmailContent(
                customer,
                products,
                totalPrice,
                status
        );

        helper.setText(content, true);
        mailSender.send(message);
    }

    public void sendMarketingEmailToTopBuyers(SendEmailRequest request) {
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        var userIdResponse = orderClient.getListUserId(authHeader);
        var userIds = userIdResponse.getResult().getUserIds();

        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        var emailResponse = userClient.getListEmail(authHeader,userIds);
        var emails = emailResponse.getResult().getEmails();

        if (emails == null || emails.isEmpty()) {
            return;
        }
        sendBulkMarketingEmail(emails, request);
    }



    private void sendBulkMarketingEmail(
            List<String> emails,
            SendEmailRequest request
    ) {
        for (String email : emails) {
            try {
                sendMarketingEmail(email, request);
            } catch (Exception e) {
                // log lỗi, KHÔNG break vòng lặp
                System.err.println("Failed to send email to: " + email);
            }
        }
    }

    private void sendMarketingEmail(
            String email,
            SendEmailRequest request
    ) throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        String content = buildMarketingEmailTemplate(
                request.getContent(),
                request.getBannerUrl()
        );

        helper.setFrom("AnhTu13@gmail.com", "Shop Promotion");
        helper.setTo(email);
        helper.setSubject(request.getSubject());
        helper.setText(content, true);

        mailSender.send(message);
    }

    private String buildSubject(Status status) {
        return status == Status.COMPLETED
                ? "🎉 Đặt hàng thành công"
                : "📦 Giao hàng thành công";
    }

    private String renderProductList(List<ProductSkuVM> products) {
        return products.stream()
                .map(this::renderProductItem)
                .collect(Collectors.joining());
    }

    private String renderProductItem(ProductSkuVM p) {
        return """
        <tr>
            <td style="padding: 10px;">
                <img src="%s" width="80" style="border-radius: 8px;" />
            </td>
            <td style="padding: 10px;">
                <div style="font-weight: bold;">%s</div>
                <div style="color: #777;">SKU: %s</div>
                <div style="color: #777;">Giá: %s VND</div>
            </td>
        </tr>
        """.formatted(
                p.getThumbnailUrl(),
                p.getVariantName(),
                p.getSku(),
                p.getSellPrice()
        );
    }
    private String buildOrderEmailContent(
            CustomerVM customer,
            List<ProductSkuVM> products,
            BigDecimal totalPrice,
            Status status
    ) {

        String title = status == Status.COMPLETED
                ? "Đơn hàng của bạn đã được đặt thành công 🎉"
                : "Đơn hàng của bạn đã được giao thành công 📦";

        String productHtml = renderProductList(products);

        return """
        <div style="font-family: Arial, sans-serif; background-color: #f6f6f6; padding: 20px;">
            <div style="max-width: 650px; margin: auto; background: #ffffff; border-radius: 12px; overflow: hidden;">
                
                <div style="padding: 20px;">
                    <h2 style="color: #2c3e50;">Xin chào %s %s,</h2>
                    <p>%s</p>

                    <table width="100%%" style="border-collapse: collapse;">
                        %s
                    </table>

                    <hr style="margin: 20px 0;" />

                    <p style="font-size: 16px;">
                        <strong>Tổng giá trị đơn hàng:</strong>
                        <span style="color: #e74c3c;">%s VND</span>
                    </p>

                    <p style="margin-top: 30px; font-size: 14px; color: #999;">
                        Cảm ơn bạn đã mua sắm tại Shop ❤️
                    </p>
                </div>
            </div>
        </div>
        """.formatted(
                customer.getFirstName(),
                customer.getLastName(),
                title,
                productHtml,
                totalPrice
        );
    }


    private String buildMarketingEmailTemplate(String content, String bannerUrl) {
        return """
        <div style="font-family: Arial, sans-serif; background-color: #f6f6f6; padding: 20px;">
            <div style="max-width: 600px; margin: auto; background: #ffffff; border-radius: 10px; overflow: hidden;">
                
                <img src="%s" alt="Promotion Banner" style="width: 100%%; height: auto;" />

                <div style="padding: 20px;">
                    <p style="font-size: 16px; color: #333;">
                        %s
                    </p>

                    <div style="margin-top: 30px; text-align: center;">
                        <a href="#" style="
                            background-color: #e74c3c;
                            color: #ffffff;
                            padding: 12px 25px;
                            text-decoration: none;
                            border-radius: 5px;
                            font-weight: bold;
                        ">
                            Mua ngay
                        </a>
                    </div>

                    <p style="margin-top: 30px; font-size: 12px; color: #999;">
                        Bạn nhận được email này vì đã từng mua hàng tại Shop.
                    </p>
                </div>
            </div>
        </div>
        """.formatted(bannerUrl, content);
    }
}
