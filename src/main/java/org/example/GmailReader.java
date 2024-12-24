package org.example;
import org.example.DatabaseUtils;

import java.sql.Timestamp;
import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;
import java.util.Properties;
public class GmailReader {
    private final String userEmail;
    private final String appPassword;

    public GmailReader(String userEmail, String appPassword) {
        this.userEmail = userEmail;
        this.appPassword = appPassword;
    }

    // Checks for approval or rejection response
    public boolean checkForApprovalResponse(String recipientEmail) {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", "imap.gmail.com");
        props.put("mail.imaps.port", "993");
        props.put("mail.imaps.ssl.enable", "true");

        try (Store store = Session.getInstance(props).getStore("imaps")) {
            store.connect("imap.gmail.com", userEmail, appPassword);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            // Search for unread emails
            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            for (Message message : messages) {
                // Check if email is from the expected sender
                if (message.getFrom()[0].toString().contains(recipientEmail)) {
                    String subject = message.getSubject();
                    String content = getTextFromMessage(message).toLowerCase();
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                    String status;
                    // Print the full response
                    System.out.println("\n=== Response Details ===");
                    System.out.println("Sender: " + message.getFrom()[0]);
                    System.out.println("Subject: " + subject);
                    System.out.println("Body: " + content);

                    // Check for approval or rejection
                    if (content.contains("approve")) {
                        status = "Approved";
                        DatabaseUtils.saveEmailToDatabase(recipientEmail, subject, status, timestamp);
                        System.out.println("✅ Approval Received!   ");

                        return true;
                    } else if (content.contains("reject")) {
                        status = "Rejected";
                        DatabaseUtils.saveEmailToDatabase(recipientEmail, subject, status, timestamp);
                        System.out.println("❌ Rejection Received!");
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error reading emails: " + e.getMessage());
        }

        return false;
    }


    // Helper method to extract text from email content
    private String getTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("text/html")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            return getTextFromMimeMultipart(mimeMultipart);
        }
        return "Unsupported content type.";
    }

    // Helper method to extract text from MimeMultipart
    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < mimeMultipart.getCount(); i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent());
            } else if (bodyPart.isMimeType("text/html")) {
                result.append(bodyPart.getContent());
            }
        }
        return result.toString();
    }
}





