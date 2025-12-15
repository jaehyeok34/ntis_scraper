package ntis.scraper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record Post(
    String title, 
    String url,
    String author,
    String date,
    List<Integer> codes
) {
    
    public Post(String title, String url, String author, String date) {
        this(title, url, author, date, new ArrayList<>());
    }

    public void addCodes(int... codes) {
        for (int code: codes) {
            this.codes.add(code);
        }
    }

    public Post addCodes(Map<String, List<Integer>> domain) {
        if (domain.containsKey(author)) {
            codes.addAll(domain.get(author));
        }

        return this;
    }

    @Override
    public final String toString() {
        return "제목: " + title + 
            "\n URL: " + url + 
            "\n 작성자: " + author +
            "\n 날짜: " + date +
            "\n 코드: " + codes;
    }
}