import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SearchEngine {
    private static final Set<String> IGNORED_DIRECTORIES = Set.of(
            ".git", ".idea", "node_modules", "target", "build", "dist", ".venv", "venv"
    );

    private static final Set<String> SOURCE_EXTENSIONS = Set.of(
            "java", "py", "js", "jsx", "ts", "tsx", "json", "xml",
            "yaml", "yml", "properties", "gradle", "sql", "html", "css", "md"
    );

    private static final Set<String> BINARY_EXTENSIONS = Set.of(
            "class", "jar", "zip", "png", "jpg", "jpeg", "gif", "ico",
            "pdf", "exe", "dll", "mp4", "webp", "woff", "ttf"
    );

    public List<SearchResult> search(SearchConfig config) throws IOException, InterruptedException {
        List<Path> files = new ArrayList<>();
        Files.walkFileTree(config.getRootPath(), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attrs) {
                String name = directory.getFileName().toString().toLowerCase(Locale.ROOT);
                if (!directory.equals(config.getRootPath()) && IGNORED_DIRECTORIES.contains(name)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (attrs.isRegularFile() && isSearchable(file, config)) {
                    files.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        ExecutorService executor = Executors.newFixedThreadPool(config.getThreadCount());
        try {
            List<Future<List<SearchResult>>> futures = new ArrayList<>();
            for (Path file : files) {
                futures.add(executor.submit(new FileSearchTask(file, config.getCompiledPattern())));
            }

            List<SearchResult> results = new ArrayList<>();
            for (int i = 0; i < futures.size(); i++) {
                try {
                    results.addAll(futures.get(i).get());
                } catch (ExecutionException e) {
                    throw new IOException("Error searching file: " + files.get(i), e.getCause());
                }
            }

            results.sort(Comparator.comparing(SearchResult::filePath)
                    .thenComparingInt(SearchResult::lineNumber));
            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    private boolean isSearchable(Path file, SearchConfig config) {
        String fileName = file.getFileName().toString().toLowerCase(Locale.ROOT);
        int dot = fileName.lastIndexOf('.');
        String ext = dot < 0 ? "" : fileName.substring(dot + 1);
        if (BINARY_EXTENSIONS.contains(ext)) return false;
        if (!config.isCodeOnly()) return true;
        return SOURCE_EXTENSIONS.contains(ext)
                || fileName.equals("dockerfile")
                || fileName.equals("makefile");
    }
}
