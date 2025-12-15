package ntis.scraper.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ntis.scraper.Post;
import ntis.scraper.Receiver;

public class ReceiverUtils {

    private static final int EMAIL = 1;
    private static final int CODE = 5;
    
    /**
     * 전달된 codeSet에 해당하는 수신자 목록 반환
     * 수신자 목록은 ./resources/receivers.xlsx 파일에서 읽어옴
     * 수신자의 codes 중 하나라도 codeSet에 포함되면 수신자 목록에 포함
     */
    public static List<Receiver> getReceivers(Set<Integer> codeSet) {
        try (
            FileInputStream input = new FileInputStream("./resources/receivers.xlsx");
            Workbook workbook = new XSSFWorkbook(input);
        ) {
            return StreamSupport.stream(workbook.getSheetAt(0).spliterator(), false)
                .skip(1) // header 제외
                .flatMap(row -> {
                    List<Integer> codes = Arrays.stream(row.getCell(CODE).getStringCellValue().split(", "))
                        .map(Integer::parseInt)
                        .toList();
                    
                    if (codes.stream().noneMatch(codeSet::contains)) {
                        return Stream.empty();
                    }

                    return Stream.of(new Receiver(
                        row.getCell(EMAIL).getStringCellValue(),
                        codes
                    ));
                }).toList();
        } catch (IOException e) {
            System.err.println("? getReceivers(): " + e.getMessage());
            return List.of();
        }
    }

    /**
     * 주어진 수신자의 코드가 포함된 게시글을 수신자의 관심 게시글(posts)에 추가
     * 즉, 수신자의 코드 중 하나라도 포함된 게시글은 모두 추가
     */
    public static Receiver select(Receiver receiver, List<Post> posts) {
        posts.stream()
            .filter(post -> receiver.codes().stream().anyMatch(post.codes()::contains))
            .forEach(receiver::addPost);

        return receiver;
    }
}
