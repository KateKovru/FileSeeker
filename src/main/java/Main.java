import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length !=4) {
            printUsage();
            System.exit(1);
        }

        try {
            String directory = args[0];
            String regex = args[1];
            int threads = Integer.parseInt(args[2]);
            boolean codeOnly;

            if("true".equalsIgnoreCase(args[3])){
                codeOnly = true;
            } else if ("false".equalsIgnoreCase(args[3])) {
                codeOnly = false;
            }else{
                throw new IllegalArgumentException(
                        "codeOnly must be true or false"
                );
            }

            SearchConfig config = new SearchConfig(directory, regex, threads, codeOnly);
            List<SearchResult> results = new SearchEngine().search(config);

            if (results.isEmpty()) {
                System.out.println("No matches found.");
            } else {
                for (SearchResult result : results) {
                    System.out.printf("%s:%d: %s%n",
                            result.filePath(), result.lineNumber(), result.lineContent().trim());
                }
                System.out.printf("%nFound %d matching line(s)%n", results.size());
            }
        } catch (IllegalArgumentException | IOException e) {
            System.err.println("Search failed: " + e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Search interrupted");
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java Main <directory> <regex> [threads] [--code]");
        System.out.println("Example: java Main . 'TODO|FIXME' 4 --code");
        System.out.println("--code searches source/config files only; excluded folders include .git, target and node_modules");
    }
}
