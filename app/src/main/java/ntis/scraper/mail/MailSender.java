package ntis.scraper.mail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import ntis.scraper.Post;
import ntis.scraper.Receiver;

public class MailSender {

    /**
     * 하나의 메일에 최대 포함 가능한 수신자 수
     * 그룹화를 통해 동일한 양식의 메일을 받는 수신자가 batch size를 초과할 경우
     * 동일한 양식의 메일을 여러 번 나누어 최대 20명씩 전송함
     * 예: 동일한 양식을 받는 수신자 수가 45명일 경우 -> 3회에 걸쳐 전송(20, 20, 5)
     */
    private static final int BATCH_SIZE = 20; 

    public static void send(String id, String password, List<Receiver> receivers) {
        Map<Set<Post>, List<Receiver>> grouped = group(receivers);
        System.out.println("수신자 그룹 수: " + grouped.size() + "개");

        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<?>> futures = new ArrayList<>();

        grouped.forEach((posts, receivers_) -> {
            InternetAddress[] addresses = getValidAddresses(receivers_);
            int total = addresses.length;

            for (int i = 0; i < total; i += BATCH_SIZE) {
                int start = i;
                int end = Math.min(i + BATCH_SIZE, total);
                Runnable task = () -> {
                    InternetAddress[] batch = Arrays.copyOfRange(addresses, start, end);
                    
                    try {
                        MimeMessage message = MessageBuilder.build(id, password, batch, posts);
                        if (message == null) {
                            System.err.println("? 메일 작성 실패(배치 시작: " + start + ")");
                            return; 
                        }

                        Transport.send(message);
                        System.out.println("! 그룹 메일 전송 성공: 배치 시작 " + start + ", 주소" + batch.length + "개");
                    } catch (Exception e) {
                        System.err.println("? 메일 전송 실패(배치 시작: " + start + "): " + e.getMessage());
                    }
                };

                futures.add(executor.submit(task));
            }
        });

        for (Future<?> future: futures) {
            try {
                future.get();
            } catch (Exception e) {
                System.err.println("? 작업 중 오류 발생: " + e.getMessage());
            }
        }

        executor.shutdownNow();
        System.out.println("! 모든 메일 전송 작업 완료.");
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

    private static InternetAddress[] getValidAddresses(List<Receiver> receivers) {
        return receivers.stream()
            .map(receiver -> {
                try{
                    return Optional.of(new InternetAddress(receiver.email()));
                } catch (Exception ignored) {}

                return Optional.empty();
            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toArray(InternetAddress[]::new);
    }
}
