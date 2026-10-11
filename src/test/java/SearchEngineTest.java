import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchEngineTest {
    @TempDir
    Path tempDir;

    @Test
    void shouldSearchSourceFilesAndIgnoreGeneratedFolders() throws Exception {
        Path src = Files.createDirectories(tempDir.resolve("src"));
        Path ignored = Files.createDirectories(tempDir.resolve("node_modules"));
        Files.writeString(src.resolve("Example.java"), "// TODO: refactor this code\nclass Example {}\n");
        Files.writeString(ignored.resolve("ignored.js"), "// TODO: ignore me\n");
        Files.writeString(tempDir.resolve("notes.txt"), "TODO in text notes\n");

        List<SearchResult> result = new SearchEngine().search(
                new SearchConfig(tempDir.toString(), "TODO", 2, true)
        );
        assertEquals(1, result.size());
        assertTrue(result.getFirst().filePath().endsWith("Example.java"));
        assertEquals(1, result.getFirst().lineNumber());
    }

    @Test
    void shouldFindPlainTextFilesInRegularMode() throws Exception {
        Files.writeString(tempDir.resolve("notes.txt"), "first line\nFIXME here\n");
        List<SearchResult> results = new SearchEngine().search(
                new SearchConfig(tempDir.toString(), "FIXME", 2, false)
        );
        assertEquals(1, results.size());
        assertEquals(2, results.getFirst().lineNumber());
    }

}
