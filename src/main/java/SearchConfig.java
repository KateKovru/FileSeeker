import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class SearchConfig {
    private final Path rootPath;
    private final Pattern pattern;
    private final int threadCount;
    private final boolean codeOnly;


    public SearchConfig(String rootPath, String regex, int threadCount, boolean codeOnly) {
        if (rootPath == null || rootPath.isBlank()) {
            throw new IllegalArgumentException("Directory path cannot be empty");
        }
        this.rootPath = Path.of(rootPath).toAbsolutePath().normalize();
        if (!Files.isDirectory(this.rootPath)) {
            throw new IllegalArgumentException("Not a directory: " + this.rootPath);
        }
        if (regex == null || regex.isEmpty()) {
            throw new IllegalArgumentException("Search regex cannot be empty");
        }
        if (threadCount < 1) {
            throw new IllegalArgumentException("Thread count must be greater than zero");
        }
        this.pattern = Pattern.compile(regex);
        this.threadCount = threadCount;
        this.codeOnly = codeOnly;
    }

    public Path getRootPath() { return rootPath; }
    public Pattern getCompiledPattern() { return pattern; }
    public int getThreadCount() { return threadCount; }
    public boolean isCodeOnly() { return codeOnly; }
}
