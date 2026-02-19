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
        /**
         * 1. 게시글 스크래핑
         *  resources/date.txt에 저장된 마지막 스크래핑 날짜 이후의 게시글을 스크래핑 함
         *  resources/date.txt 파일이 없을 경우 전체 게시글(100개)을 스크래핑하며, 새롭게 date.txt 파일이 생성 됨
         */
        List<Post> posts = Scraper.scrape(FileUtils.getDate());
        if (posts.isEmpty()) {
            System.out.println("새로운 게시글이 없습니다.");
            return;
        }

        /**
         * 2. 스크래핑한 게시글의 가장 최신 날짜로 resources/date.txt 파일 갱신 및 게시글 수 출력
         *  단순 디버깅용 출력이므로, 출력 과정은 생략해도 됨
         */
        System.out.println("게시글 개수: " + posts.size() + "개");
        FileUtils.updateDate(posts.get(0).date());

        /**
         * 3. 스크래핑된 모든 게시글의 코드 집합 출력
         *  단순 디버깅용 이므로, 출력 과정은 생략해도 됨
         */
        Set<Integer> codeSet = PostUtils.getCodeSet(posts);
        System.out.println("전체 코드 세트: " + codeSet + "(" + codeSet.size() + "개)");

        /**
         * 4. resources/receivers.xlsx 파일에 있는 교수님들 정보를 기반으로 코드 집합 내에 존재하는 코드를
         * 하나라도 관심사로 등록한 교수님을 추출하여 집합으로 생성 및 관심사에 해당하는 게시글을 교수님 객체에 추가
         * 
         * Detail.
         * 4-1. resources/receivers.xlsx 내용을 전부 읽어서 객체로 리스트로 반환
         * 4-2. 객체 리스트를 순회하며, 각 객체가 가진 관심사 코드와 코드 집합을 비교하여 하나라도 일치하는 코드가 있을 경우 선택 및 객체(Receiver)에 추가
         * 4-3. 선택된 객체들만 남겨 새로운 리스트 생성
         */
        List<Receiver> receivers = ReceiverUtils.getReceivers(codeSet).stream()
            .map(receiver -> ReceiverUtils.select(receiver, posts))
            .collect(Collectors.toCollection(ArrayList::new));

        /**
         * 중간 요약
         * 1. R&D 홈페이지에서 게시글을 스크래핑하고, 해당 게시글에 코드를 부여함. 
         * 2. 스크래핑된 모든 게시글에 해당하는 코드 집합을 생성하고, 교수님 중 하나라도 관심사 코드가 집합에 포함되면 선택함
         */


        /**
         * 5. 메일 전송 준비
         *  resources/.env 파일에 저장된 환경변수들을 획득하고, 컨펌 메일 주소를 추출하여 수신자 리스트에 추가함
         *  컨펌 메일 수신자는 팀장님 및 담당 선생님 메일과 본인 메일 등이 될 수 있음
         */
        Properties env = FileUtils.getEnv();
        for (String email: env.getProperty("CONFIRM").split(", ")) {
            // 컨펌 메일 수신자 추가
            receivers.add(new Receiver(email, List.of()).addPosts(posts));
        }

        /**
         * 6. 메일 전송
         *  resources/.env 파일에 저장된 메일 아이디, 비밀번호를 이용하여 메일 전송
         */
        MailSender.send(env.getProperty("ID"), env.getProperty("PASSWORD"), receivers);
        System.out.println("메일 전송 완료.");
    }
}

