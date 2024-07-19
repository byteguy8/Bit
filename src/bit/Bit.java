package bit;

import bit.compiler.Compiler;
import bit.name_resolver.NameResolver;
import bit.statement.Statement;
import bit.type_checker.TypeChecker;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Bit {
    public static void error(int line, int from, int to, String message) {
        System.err.printf("Error at line: %d [from: %d to: %d]:\n\t%s%n", line, from, to, message);
    }

    public static void error(Token token, String message) {
        error(
                token.line,
                token.from,
                token.to,
                message
        );
    }

    private static void printInput(InputStream input, boolean error) throws IOException {
        byte[] buffer = new byte[1024];

        while (true) {
            int count = input.read(buffer, 0, buffer.length);

            if (count == -1) break;

            String msg = new String(buffer, 0, count);

            if (error) System.err.println(msg);
            else System.out.println(msg);
        }
    }

    private static boolean printProcess(Process process) throws IOException {
        if (process.getErrorStream().available() == 0) {
            printInput(process.getInputStream(), false);
            return true;
        }

        printInput(process.getErrorStream(), true);

        return false;
    }

    private static boolean assemble(String source) {
        File sourceFile = new File("source.asm");

        try (FileOutputStream output = new FileOutputStream(sourceFile)) {
            output.write(source.getBytes());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }

        ProcessBuilder processBuilder = new ProcessBuilder("nasm", "-f elf64", "-g", "-F dwarf", "source.asm");
        processBuilder.directory(new File("."));

        try {
            Process process = processBuilder.start();
            process.waitFor();
            return printProcess(process);
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private static boolean link() {
        //gcc -o workshop workshop.o -no-pie -g
        ProcessBuilder processBuilder = new ProcessBuilder("gcc", "-o", "source", "source.o", "-no-pie", "-g");
        processBuilder.directory(new File("."));

        try {
            Process process = processBuilder.start();
            process.waitFor();
            return printProcess(process);
        } catch (IOException | InterruptedException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    private static boolean execute() {
        ProcessBuilder processBuilder = new ProcessBuilder("./source");

        try {
            Process process = processBuilder.start();
            process.waitFor(5, TimeUnit.SECONDS);
            printProcess(process);
            return true;
        } catch (IOException | InterruptedException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    private static String getSource(String[] args) {
        try {
            if (args.length == 0) {
                System.err.println("No source code submitted");
                System.exit(1);
            }

            String sourcePath = args[0];
            File sourceFile = new File(sourcePath);

            if (!sourceFile.exists()) {
                System.err.printf("File '%s' do not exists\n", sourcePath);
                System.exit(1);
            }

            if (sourceFile.isDirectory()) {
                System.err.printf("Expect '%s' be a text file\n", sourcePath);
                System.exit(1);
            }

            byte[] sourceBytes = Files.readAllBytes(Paths.get(sourcePath));

            return new String(sourceBytes);
        } catch (IOException ex) {
            System.err.printf("Failed to get source file:\n%s\n", ex.getMessage());
            System.exit(1);
        }

        return null;
    }

    public static void main(String[] args) {
        String source = getSource(args);

        Scanner scanner = new Scanner();
        Parser parser = new Parser();
        NameResolver nameResolver = new NameResolver();
        TypeChecker typeChecker = new TypeChecker();
        Compiler compiler = new Compiler();

        try {
            List<Token> tokens = scanner.scanTokens(source);
            List<Statement> statements = parser.parseTokens(tokens);

            nameResolver.resolve(statements);
            typeChecker.check(statements);

            String output = compiler.compile(statements);

            if (assemble(output) && link())
                execute();
        } catch (ScannerError error) {
            System.exit(7);
        } catch (ParserError error) {
            System.exit(11);
        } catch (ResolverError error) {
            System.exit(17);
        } catch (SymbolsError error) {
            System.exit(23);
        }
    }
}