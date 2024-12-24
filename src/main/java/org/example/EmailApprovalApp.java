package org.example;

import org.example.DatabaseUtils;
import org.example.EmailService;
import org.example.GmailReader;
import org.example.OTPService;

import java.sql.Timestamp;
import java.util.Scanner;

public class EmailApprovalApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n=== Approval Request System ===\n");

        // Static Sender Email and Password
        String senderEmail = "aniketchourasiya1632000@gmail.com";
        String senderPassword = "iieg irch mlpr akdv";

        // OTP Authentication
        OTPService otpService = new OTPService();
        if (!otpService.authenticateWithOTP(senderEmail, senderPassword)) {
            System.out.println("❌ OTP authentication failed after maximum attempts. Exiting application.");
            return;
        }

        // Dynamic Input for Recipient and Email Details
        System.out.print("Enter recipient's email: ");
        String recipientEmail = scanner.nextLine();

        System.out.print("Enter email subject: ");
        String subject = scanner.nextLine();

        System.out.print("Enter email body message: ");
        String bodyMessage = scanner.nextLine();

        // Append buttons for approval and rejection
        String htmlContent = generateApprovalEmailBody(bodyMessage, senderEmail);

        // Initialize EmailService and GmailReader
        EmailService emailService = new EmailService(senderEmail, senderPassword);
        GmailReader gmailReader = new GmailReader(senderEmail, senderPassword);

        // Retry logic
        int maxAttempts = 3;
        boolean responseReceived = false;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            System.out.println("\n🔄 Attempt " + attempt + " to send email...");
            emailService.sendEmail(recipientEmail, subject, htmlContent);
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            DatabaseUtils.saveEmailToDatabase(recipientEmail, subject, "Sent", timestamp);

            System.out.println("⏳ Waiting for recipient's response...");
            try {
                Thread.sleep(20000); // Wait for 40 seconds
            } catch (InterruptedException e) {
                System.err.println("⚠️ Error while waiting: " + e.getMessage());
            }

            // Check for response
            responseReceived = gmailReader.checkForApprovalResponse(recipientEmail);
            if (responseReceived) break;
        }

        // Final Response Message
        if (responseReceived) {
            System.out.println("🎉 Response received from recipient!");
        } else {
            System.out.println("❌ No response received after " + maxAttempts + " attempts.");
        }
    }

    // Generates dynamic HTML content with buttons
    private static String generateApprovalEmailBody(String bodyMessage, String senderEmail) {
        return "<html>" +
                "<body>" +
                "<h3>" + bodyMessage + "</h3>" +
                "<p style=\"text-align:center;\">" +
                "<a href=\"mailto:" + senderEmail +
                "?subject=Approval%20Response&body=Your%20changes%20are%20Approved%20by%20Client\" " +
                "style=\"padding:10px;background-color:green;color:white;text-decoration:none;border-radius:5px;display:inline-block;margin-right:20px;\">Approve</a>" +
                "<a href=\"mailto:" + senderEmail +
                "?subject=Approval Response&body=Your%20changes%20are%20Rejected%20by%20Client\" " +
                "style=\"padding:10px;background-color:red;color:white;text-decoration:none;border-radius:5px;display:inline-block;\">Reject</a>" +
                "</p>" +
                "</body>" +
                "</html>";
    }
}






