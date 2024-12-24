package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class OTPService {
    private static final int MAX_ATTEMPTS = 3;
    private final Map<String, Integer> otpCache = new HashMap<>();
    private int attemptCounter = 0;

    public boolean authenticateWithOTP(String senderEmail, String senderPassword) {
        Scanner scanner = new Scanner(System.in);

        if (!otpCache.containsKey(senderEmail)) {
            int otp = generateOTP();
            otpCache.put(senderEmail, otp);

            if (!sendOTPEmail(senderEmail, senderPassword, senderEmail, otp)) {
                System.out.println("❌ Failed to send OTP email. Please check email configuration.");
                return false;
            }

            System.out.println("📧 OTP has been sent to your email.");
        }

        while (attemptCounter < MAX_ATTEMPTS) {
            System.out.print("Enter the OTP sent to your email: ");
            int enteredOtp = scanner.nextInt();
            attemptCounter++;

            if (validateOTP(senderEmail, enteredOtp)) {
                System.out.println("✅ OTP verified successfully.");
                return true;
            } else {
                System.out.println("❌ Incorrect OTP. Attempts left: " + (MAX_ATTEMPTS - attemptCounter));
            }
        }

        return false;
    }

    private int generateOTP() {
        Random random = new Random();
        return 100000 + random.nextInt(900000); // Generate a 6-digit OTP
    }

   private boolean validateOTP(String senderEmail, int enteredOtp) {
        return otpCache.getOrDefault(senderEmail, -1) == enteredOtp;
    }

    private boolean sendOTPEmail(String senderEmail, String senderPassword, String recipientEmail, int otp) {
        String host = "smtp.gmail.com";
        int port = 587;

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);

        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Your OTP Code");
            message.setText("Dear user,\n\nYour OTP code is: " + otp + "\n\nThank you.");

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            System.err.println("⚠️ Error sending email: " + e.getMessage());
            return false;
        }
    }
}






