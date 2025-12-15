package ntis.scraper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record Receiver(
    String email, 
    List<Integer> codes,
    Set<Post> posts
) {
    public Receiver(String email, List<Integer> codes) {
        this(email, codes, new HashSet<>());
    }

    public Receiver addPost(Post post) {
        posts.add(post);

        return this;
    }

    public Receiver addPosts(List<Post> posts) {
        this.posts.addAll(posts);

        return this;
    }
}
