package ntis.scraper;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import ntis.scraper.utils.FileUtils;

public class Scraper {

    private static final String URL = "https://research.hanbat.ac.kr/ko/projects?pagination.pageUnit=100";
    private static final String SELECTOR = "#sub-contents > div.bbs-default > ul > li";

    public static List<Post> scrape(String baseDate) {
        try (Scanner scanner = new Scanner(System.in)) {
            List<Post> posts = Jsoup.connect(URL).get() // document 획득
                .select(SELECTOR) // 요소 선택(elements 획득)
                .stream()
                .map(element -> new Post( // post 객체 생성
                    extractText(element, "title"),
                    extractUrl(element),
                    extractText(element, "author"),
                    extractText(element, "date")
                )).filter(post -> post.date().compareTo(baseDate) > 0) // 날짜 필터링
                .map(post -> post.addCodes(FileUtils.getDomain())) // domain 기반 코드 부여
                .toList();

            /**
             * domain 기반 코드 부여 실패한 게시글에 대해 수동 코드 부여
             * I/O 관련 사이드 이펙트의 경우 스트림에서 처리하지 않는것이 권장되기 때문에
             * 스트림 파이프라인 연산에서 처리하지 않음
             */
            for (Post post: posts) {
                if (!post.codes().isEmpty()) {
                    continue;
                }

                System.out.print("제목: " + post.title() + "\n" 
                    + "작성자: " + post.author() + "\n" +
                    "코드 입력(default: 45, 71, 77) > "
                );

                String line = scanner.nextLine();
                if (line.isBlank()) {
                    post.addCodes(45, 71, 77); // default codes
                } else {
                    Arrays.stream(line.split(", "))
                        .mapToInt(Integer::parseInt)
                        .forEach(codes -> post.addCodes(codes));
                }
            }

            return posts;
        } catch (Exception e) {
            System.err.println("? scrape: " + e.getMessage());
            return List.of();
        }
    }

    public static List<Post> scrape() {
        return scrape("0000-00-00");
    }

    private static String extractText(Element element, String field) {
        return element.select("div.col.col-" + field + " > span").text();
    }

    private static String extractUrl(Element element) {
        return element.select("div.col.col-title > span > a").attr("href");
    }
}
