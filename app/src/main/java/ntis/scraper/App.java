package ntis.scraper;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import ntis.scraper.mail.MailSender;
import ntis.scraper.utils.FileUtils;
import ntis.scraper.utils.PostUtils;
import ntis.scraper.utils.ReceiverUtils;

public class App {

    public static void main(String[] args) {
        List<Post> posts = Scraper.scrape(FileUtils.getDate());
        System.out.println("게시글 개수: " + posts.size() + "개");

        if (!posts.isEmpty()) {
            FileUtils.updateDate(posts.get(0).date());
        }

        Set<Integer> codeSet = PostUtils.getCodeSet(posts);
        System.out.println("전체 코드 세트: " + codeSet + "(" + codeSet.size() + "개)");

        List<Receiver> receivers = ReceiverUtils.getReceivers(codeSet).stream()
            .map(receiver -> ReceiverUtils.select(receiver, posts))
            .collect(Collectors.toCollection(ArrayList::new));

        Properties env = FileUtils.getEnv();
        for (String email: env.getProperty("CONFIRM").split(", ")) {
            // 컨펌 메일 수신자 추가
            receivers.add(new Receiver(email, List.of()).addPosts(posts));
        }

        MailSender.send(env.getProperty("ID"), env.getProperty("PASSWORD"), receivers);
        System.out.println("메일 전송 완료.");
    }
}

