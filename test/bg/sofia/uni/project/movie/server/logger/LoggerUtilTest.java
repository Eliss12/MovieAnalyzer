package bg.sofia.uni.project.movie.server.logger;

import org.junit.jupiter.api.*;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class LoggerUtilTest {

    private static final String TEST_LOG = "test-server.log";

    @BeforeEach
    void setup() {
        LoggerUtil.setLogFile(TEST_LOG);
    }

    @AfterEach
    void cleanup() throws Exception {
        Files.deleteIfExists(Path.of(TEST_LOG));
    }

    @Test
    void testLogCreatesFile() throws Exception {

        LoggerUtil.log(new RuntimeException("Test error"));

        assertTrue(Files.exists(Path.of(TEST_LOG)));
    }

    @Test
    void testLogWritesMessage() throws Exception {

        String message = "Something went wrong";
        LoggerUtil.log(new RuntimeException(message));
        String content = Files.readString(Path.of(TEST_LOG));

        assertTrue(content.contains(message));
        assertTrue(content.contains("Error:"));
    }

    @Test
    void testLogWritesStackTrace() throws Exception {

        LoggerUtil.log(new RuntimeException("Boom"));
        String content = Files.readString(Path.of(TEST_LOG));

        assertTrue(content.contains("RuntimeException"));
    }
}

