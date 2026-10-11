import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

public class FileSearchTask implements Callable<List<SearchResult>> {
    private final Path file;
    private final Pattern pattern;

    public FileSearchTask(Path file, Pattern pattern) {
        this.file = file;
        this.pattern = pattern;
    }

    @Override
    public List<SearchResult> call() throws IOException {
        List<SearchResult> results = new ArrayList<>();
        try (var reader = Files.newBufferedReader(file)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (pattern.matcher(line).find()) {
                    results.add(new SearchResult(file.toString(), lineNumber, line));
                }
            }
        } catch (MalformedInputException e) {
            System.err.println("Skipping non-UTF-8 file: " + file);
            return List.of();
        }
        return results;
    }
}
