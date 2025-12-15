package ntis.scraper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ntis.scraper.utils.FileUtils;

public class ScraperTest {
    
    @Test
    public void scrapeTest() {
        assertEquals(100, Scraper.scrape().size());
    }

    @Test
    public void addCodesTest() {
        Post post = new Post("title", "", "해양수산부", "0000-00-00");
        assertEquals(true, post.addCodes(FileUtils.getDomain()));
        assertEquals(3, post.codes().size());

        System.out.println(post.codes());
    }
}
