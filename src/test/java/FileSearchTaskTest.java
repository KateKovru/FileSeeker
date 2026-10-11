import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileSearchTaskTest {

    @Test
    void findMatchingLines() throws Exception {
        Path tempFile = Files.createTempFile("test", "txt");
        Files.writeString(tempFile, "hello world\nhi there\nhello java");

        var task = new FileSearchTask(tempFile, Pattern.compile("hello"));
        var result = task.call();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).lineNumber());
        assertEquals(3, result.get(1).lineNumber());

        Files.delete(tempFile);

    }

    @Test
    void returnsEmptyWhenNoMatch() throws Exception {
        Path tempFile = Files.createTempFile("test", "txt");
        Files.writeString(tempFile, "nothing here");

        var task = new FileSearchTask(tempFile, Pattern.compile("xyz"));
        var result = task.call();

        assertTrue(result.isEmpty());
        Files.delete(tempFile);
    }
}
