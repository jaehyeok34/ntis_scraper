package ntis.scraper.mail;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import ntis.scraper.Post;
import ntis.scraper.Receiver;

public class MailSender {

    public static void send(String id, String password, List<Receiver> receivers) {
        group(receivers).forEach((posts, receivers_) -> {
            InternetAddress[] addresses = receivers_.stream()
                .map(receiver -> {
                    try{
                        return Optional.of(new InternetAddress(receiver.email()));
                    } catch (Exception ignored) {}

                    return Optional.empty();
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toArray(InternetAddress[]::new);
        

            MimeMessage message = MessageBuilder.build(id, password, addresses, posts);
            if (message == null) {
                System.err.println("? 메일 작성 실패");
                return;
            }

            try {
                Transport.send(message);
            } catch (Exception e) {
                System.err.println("? 메일 전송 실패: " + e.getMessage());
            }
        });
    }

    /**
     * 수신자의 관심 게시글(posts)을 기준으로 수신자 그룹화
     * 서로다른 수신자여도 관심 게시글이 동일하다면 같은 그룹으로 묶임
     * 메일 전송 시 함께 묶여서 전송됨
     */
    private static Map<Set<Post>, List<Receiver>> group(List<Receiver> receivers) {
        return receivers.stream()
            .collect(Collectors.groupingBy(
                receiver -> Set.copyOf(receiver.posts()),
                Collectors.toList()
            ));
    }
}
