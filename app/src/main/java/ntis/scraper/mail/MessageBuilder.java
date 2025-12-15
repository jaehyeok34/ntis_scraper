package ntis.scraper.mail;

import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.jspecify.annotations.Nullable;

import ntis.scraper.Post;
import ntis.scraper.utils.FileUtils;

public class MessageBuilder {
    
    private static final int FONT_SIZE = 18;
    private static final String LOGO_CID = "logo";
    private static final String SUBJECT = "산학연구처 RnD 공고입니다.";

    public static @Nullable MimeMessage build(String id, String password, InternetAddress[] addresses, Collection<Post> posts) {
        Session session = getSession(id, password);

        try {
            MimeMessage message = new MimeMessage(session) {{
                setFrom(new InternetAddress(id));
                setRecipients(MimeMessage.RecipientType.BCC, addresses); // 숨은 참조로 발송
                setSubject(SUBJECT);
            }};

            MimeMultipart multipart = new MimeMultipart("related");
            message.setContent(multipart);

            MimeBodyPart html = new MimeBodyPart();
            html.setContent(getHtml(posts), "text/html; charset=utf-8");
            multipart.addBodyPart(html);

            ByteArrayDataSource imageSource = new ByteArrayDataSource(FileUtils.getLogo(), "image/png");
            MimeBodyPart image = new MimeBodyPart() {{
                setDataHandler(new DataHandler(imageSource));
                setHeader("Content-ID", "<" + LOGO_CID + ">");
                setDisposition(MimeBodyPart.INLINE);
            }};
            multipart.addBodyPart(image);

            return message;
        } catch (Exception e) {
            System.err.println("? build: " + e.getMessage());
            return null;
        }
    }

    private static Session getSession(String id, String password) {
        Properties properties = new Properties() {{
            put("mail.smtp.host", "smtp.gmail.com");
            put("mail.smtp.port", "587");
            put("mail.smtp.auth", "true");
            put("mail.smtp.starttls.enable", "true");
        }};
        
        return Session.getInstance(properties, new Authenticator() {
             @Override
             protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(id, password);
             }
        });
    }

    private static String getHtml(Collection<Post> posts) {
        return getForm() + "<ol>" + posts.stream()
            .map(post -> String.format(
                """
                <li style="font-size: %dpx;">
                    <a href="%s">%s</a>
                </li>
                """, FONT_SIZE, post.url(), post.title()
            )).collect(Collectors.joining()) + "</ol>";
    }

    private static String getForm() {
        return String.format("""
            <img src="cid:%s" width="201" height="43">
            <p style="font-size: %dpx;"><b>국립한밭대학교 산학연구처</b>에서 <b>제공</b>해 드리는 <b>맞춤형 연구과제 공모 정보</b>입니다.</p>
            <p style="font-size: %dpx;">- 아래 <b>공고 제목을 클릭</b>하시면, <b>NTIS 공고문</b>으로 연결됩니다.</p>
        """, LOGO_CID, FONT_SIZE, FONT_SIZE);
    }
}
