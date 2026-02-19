package ntis.scraper.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class FileUtils {

    private static final Path RESOURCES_DIR = Path.of("./resources");
    private static final Path DATE_FILE = RESOURCES_DIR.resolve("date.txt");
    private static final Path DOMAIN_FILE = RESOURCES_DIR.resolve("domain.properties");
    private static final Path ENV_FILE = RESOURCES_DIR.resolve(".env");
    private static final Path LOGO_FILE = RESOURCES_DIR.resolve("logo.png");
    
    /**
     * 단일 라인으로 구성된 date.txt 파일에서 마지막 스크래핑 날짜를 읽어 옴
     * 예: 2026-01-01
     */
    public static String getDate() {
        try {
            return Files.readString(DATE_FILE);
        } catch (IOException e) {
            System.err.println("? getDate: " + e.getMessage());

            return "";
        }
    }

    /**
     * 스크래핑한 게시글의 가장 최신 날짜로 resources/date.txt 파일을 업데이트 함
     * 파일이 존재하지 않을 경우 새롭게 생성함
     */
    public static boolean updateDate(String date) {
        try {
            OpenOption[] options = new OpenOption[] {
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
            };
            
            Files.createDirectories(DATE_FILE.getParent());
            Files.writeString(DATE_FILE, date, options);

            return true;
        } catch (IOException e) {
            System.err.println("? updateDate: " + e.getMessage());

            return false;
        }
    }

    public static Map<String, List<Integer>> getDomain() {
        /**
         * resources/domain.properties 파일에서 도메인-코드 매핑 정보를 읽어 옴
         * 해당 파일이 존재하지 않을 경우 도메인 생성에 실패하여 빈 맵을 반환
         */
        try (BufferedReader reader = Files.newBufferedReader(DOMAIN_FILE)) {
            Properties properties = new Properties();
            properties.load(reader);

            return properties.entrySet().stream()
                .collect(Collectors.toMap(
                    entry -> (String) entry.getKey(),
                    entry -> List.of(((String) entry.getValue()).split(", ")).stream()
                        .map(Integer::parseInt)
                        .collect(Collectors.toList())
                ));
        } catch (IOException e) {
            System.err.println("? getDomain: " + e.getMessage());

            return Map.of();
        }
    }

    /**
     * resources/.env 파일에서 환경변수 정보를 읽어옴.
     * 키-값 쌍으로 이루어져 있기 때문에 프로퍼티 객체로 반환하여 활용함
     * 해당 파일이 존재하지 않을 경우 빈 프로퍼티 객체가 반환되며, 이 경우 메일 전송 준비 과정에서 에러가 발생할 것임
     * (메일 전송 준비 과정에서 발생한 에러는 아무도 처리하지 않기 떄문에 프로그램 다운으로 이어짐)
     */
    public static Properties getEnv() {
        Properties environment = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(ENV_FILE)) {
            environment.load(reader);
        } catch (IOException e) {
            System.err.println("? getEnv: " + e.getMessage());
        }

        return environment;
    }

    public static byte[] getLogo() {
        try {
            return Files.readAllBytes(LOGO_FILE);
        } catch (IOException e) {
            System.err.println("? getLogo: " + e.getMessage());
            return new byte[0];
        }
    }
}
