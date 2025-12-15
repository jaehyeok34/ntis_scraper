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
    
    public static String getDate() {
        try {
            return Files.readString(DATE_FILE);
        } catch (IOException e) {
            System.err.println("? getDate: " + e.getMessage());

            return "";
        }
    }

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
