package ntis.scraper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import ntis.scraper.utils.FileUtils;

public class FileUtilsTest {
    
    @Test
    public void getDomainTest() {
        Map<String, List<Integer>> domain = FileUtils.getDomain();
        assertNotEquals(0, domain.size());
        assertEquals(List.of(27, 44, 55), domain.get("해양수산부"));
    }
}
