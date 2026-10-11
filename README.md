# Codebase Search Assistant 🔎

A multithreaded Java command-line tool for searching source code and text files using regular expressions.

Designed to help developers explore unfamiliar repositories, locate relevant code, and collect **verifiable file and line references** for AI-assisted coding workflows.

> This tool does not call an LLM or autonomously modify code. It produces search results that developers can review or use as context for tools such as GitHub Copilot or Claude Code.

## Features

- Recursive directory traversal using `Files.walkFileTree()`
- Parallel file processing with `ExecutorService` and `Future`
- Regular expression matching using Java `Pattern`
- Optional code-only search mode for source and configuration files
- Automatic exclusion of `.git`, `.idea`, `node_modules`, `target`, `build`, `dist`, `.venv`, and `venv`
- Automatic exclusion of common binary file formats
- Search results with file paths, line numbers, and matching lines
- Automated tests with JUnit 5

## Tech Stack

- Java 21+
- Maven
- Java NIO
- Java Concurrency (`ExecutorService`, `Callable`, `Future`)
- Regular Expressions
- JUnit 5

## Getting Started

### Requirements

- Java 21 or newer
- Apache Maven

### Build and Test

Run the following commands from the project root:

```bash
mvn clean test
mvn compile
```

### Run the Application

The application requires four command-line arguments:

```bash
java -cp target/classes Main <directory> <regex> <threads> <codeOnly>
```

| Argument | Description |
|---|---|
| `directory` | Root directory to search |
| `regex` | Java regular expression used to find matching lines |
| `threads` | Number of worker threads (must be greater than 0) |
| `codeOnly` | `true` to search source/config files only, `false` for all non-excluded files |

### Usage Examples

**Search for TODO and FIXME comments in source code using four threads:**

```bash
java -cp target/classes Main . "TODO|FIXME" 4 true
```

**Search for the word "hello" in text files using two threads:**

```bash
java -cp target/classes Main ./test-data "hello" 2 false
```

**Search for references to UserService:**

```bash
java -cp target/classes Main ./src "UserService" 4 true
```

**Search using eight threads without code-only filtering:**

```bash
java -cp target/classes Main . "error" 8 false
```

### Windows PowerShell

For paths containing spaces, wrap the directory path in double quotes:

```powershell
java -cp target/classes Main "C:\Users\me\IdeaProjects\my-project" "TODO|FIXME" 4 true
```

## Example Output

```text
C:\repo\src\Main.java:18: // TODO: validate arguments
C:\repo\src\SearchEngine.java:42: // FIXME: handle edge case

Found 2 matching line(s)
```

Each result includes the file path, line number, and matching line content.

## Architecture

The application separates command-line handling, search configuration, file traversal, and file processing.

- **Main** — parses command-line arguments, starts the search, and displays results.
- **SearchConfig** — validates and stores search settings.
- **SearchEngine** — traverses directories, filters files, distributes tasks across worker threads, and collects results.
- **FileSearchTask** — implements `Callable` and searches a single file line by line.
- **SearchResult** — immutable Java record representing a matching line.

### Search Workflow

1. The user provides a directory, regular expression, thread count, and search mode.
2. `Main` creates a `SearchConfig`.
3. `SearchEngine` traverses the directory tree and collects eligible files.
4. A `FileSearchTask` is created for each eligible file.
5. Tasks are submitted to a fixed thread pool.
6. Each task searches its file and returns matching lines.
7. `SearchEngine` collects results using `Future.get()`.
8. Results are sorted by file path and line number.
9. `Main` prints the final results.

## AI-Assisted Development Workflow

FileSeeker can be used to gather code references before working with an AI coding assistant.

1. Search a repository for relevant methods, classes, `TODO`, or `FIXME` comments.
2. Review the returned file paths and line numbers.
3. Provide relevant, non-confidential code excerpts to an AI coding assistant.
4. Ask the assistant to suggest changes or refactoring.
5. Review the proposed changes and run:

```bash
mvn test
```

6. Inspect the Git diff before accepting changes.

## Testing

The project includes automated tests using JUnit 5.

Run all tests:

```bash
mvn test
```

Tests cover:

- Finding matching lines
- Handling files without matches
- Searching source files in code-only mode
- Skipping ignored directories
- Searching ordinary text files

## Limitations

- Regex-based search does not understand programming-language semantics.
- The application searches matching lines rather than individual occurrences.
- Very large repositories may require additional performance optimization.
- File filtering relies mainly on file extensions.
- The tool does not provide semantic search, AI inference, or automatic code modifications.

## Background

This project extends a Java multithreaded file-search exercise into a practical command-line tool for exploring software repositories.

It demonstrates Java file I/O, regular expressions, concurrency, object-oriented design, exception handling, and automated testing.