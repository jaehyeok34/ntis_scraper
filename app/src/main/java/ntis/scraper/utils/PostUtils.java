package ntis.scraper.utils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ntis.scraper.Post;

public class PostUtils {

    /**
     * 주어진 게시글 목록의 코드 집합 반환(중복 X)
     */
    public static Set<Integer> getCodeSet(List<Post> posts) {
        return posts.stream()
            .flatMap(post -> post.codes().stream())
            .collect(Collectors.toSet());
    }
}
