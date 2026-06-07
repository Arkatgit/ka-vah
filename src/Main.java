import ca.brock.cs.lambda.abstractmachine.X86Emitter;
import ca.brock.cs.lambda.abstractmachine.X86Program;
import ca.brock.cs.lambda.combinators.*;
import ca.brock.cs.lambda.parser.ProgParser;
import ca.brock.cs.lambda.parser.ScottEncoding;
import ca.brock.cs.lambda.parser.Term;
import ca.brock.cs.lambda.types.DefinedValue;
import ca.brock.cs.lambda.types.FunctionDefinition;
import ca.brock.cs.lambda.types.Type;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Main {
    enum Pipeline { NAIVE, OPTIMIZED }

    static class Options {
        String sourceCode;
        Path inputFile;
        String outputName = "program";
        Pipeline pipeline = Pipeline.OPTIMIZED;
        String optLevel = "all";
        boolean useBC = true;
        boolean stats = false;
        boolean compileExecutable = false;
        boolean keepAsm = true;
        boolean printStages = false;
    }

    public static void main(String[] args) {
        try {
            Options options = parseArgs(args);
            String source = loadSource(options);

            printStage(options, "Source Program", source);

            long start = System.nanoTime();

            ProgParser.ParsedProgram parsed = LambdaCompiler.parse(source);

            printDefinedValuesStage(options, "Parsed Program / Symbol Map", parsed.symbolMap);

            Map<String, Type> types = LambdaCompiler.typeCheck(parsed.symbolMap);

            printTypesStage(options, "Type Checking Result", types);

            Map<String, DefinedValue> symbolMap =
                options.pipeline == Pipeline.OPTIMIZED && shouldRun(options, "cse")
                    ? LambdaCompiler.optimizeTerms(parsed.symbolMap)
                    : parsed.symbolMap;

            printDefinedValuesStage(options, "After CSE", symbolMap);

            Map<String, DefinedValue> scottEncoded =
                ScottEncoding.desugarProgram(symbolMap);

            printDefinedValuesStage(options, "After Scott Encoding", scottEncoded);

            Map<String, Combinator> rawCombinators =
                translate(scottEncoded, options.useBC);

            printCombinatorStage(
                options,
                options.useBC
                    ? "Raw Combinators with B/C Translation"
                    : "Raw Naive SKI Combinators",
                rawCombinators
            );

            if (options.stats) {
                printCombinatorStats(
                    options.useBC
                        ? "Raw Combinators with B/C Translation"
                        : "Raw Naive SKI Combinators",
                    rawCombinators
                );
            }

            Map<String, Combinator> finalCombinators = rawCombinators;

            if (options.pipeline == Pipeline.OPTIMIZED) {
                if (shouldRun(options, "inline")) {
                    finalCombinators =
                        CombinatorInliner.inlineAll(finalCombinators);

                    printCombinatorStage(options, "After Inlining", finalCombinators);

                    if (options.stats) {
                        printCombinatorStats("After Inlining", finalCombinators);
                    }
                }

                if (shouldRun(options, "combinator-opt")) {
                    finalCombinators =
                        CombinatorInliner.optimizeAfterInlining(finalCombinators);

                    printCombinatorStage(options, "After Combinator Optimization", finalCombinators);

                    if (options.stats) {
                        printCombinatorStats("After Combinator Optimization", finalCombinators);
                    }
                }

                if (shouldRun(options, "partial-eval")) {
                    finalCombinators =
                        CombinatorPartialEvaluator.partialEvaluateAll(finalCombinators);

                    printCombinatorStage(options, "After Partial Evaluation", finalCombinators);

                    if (options.stats) {
                        printCombinatorStats("After Partial Evaluation", finalCombinators);
                    }
                }
            }

            String entryPoint =
                parsed.mainFunction != null
                    ? parsed.mainFunction.getName()
                    : "main";

            X86Emitter emitter = new X86Emitter();

            X86Program x86Program =
                emitter.compile(finalCombinators, types, entryPoint);

            String asm = x86Program.emit();

            printStage(options, "Generated Assembly", asm);

            Path asmPath = Path.of(options.outputName + ".asm");

            Files.write(asmPath, asm.getBytes(StandardCharsets.UTF_8));

            writeCompileScript();
            writeRunScript();

            if (options.compileExecutable) {
                compileExecutable(asmPath, options.outputName);

                if (!options.keepAsm) {
                    Files.deleteIfExists(asmPath);
                }
            }

            long elapsed = System.nanoTime() - start;

            if (options.keepAsm || !options.compileExecutable) {
                System.out.println("Assembly written to: " + asmPath);
            }

            if (options.compileExecutable) {
                System.out.println("Executable written to: " + options.outputName);
                System.out.println("Run with: ./run_kv.sh " + options.outputName);
            } else {
                System.out.println(
                    "Compile with: ./compile_kv.sh "
                        + asmPath
                        + " "
                        + options.outputName
                );

                System.out.println("Run with:     ./run_kv.sh " + options.outputName);
            }

            if (options.stats) {
                System.out.println();
                System.out.println("========== Summary Metrics ==========");
                System.out.println("Raw combinator size: " + sizeOfMap(rawCombinators));
                System.out.println("Final combinator size: " + sizeOfMap(finalCombinators));
                System.out.println("Reduction steps: not instrumented");
                System.out.println(
                    "Generated assembly size: "
                        + asm.getBytes(StandardCharsets.UTF_8).length
                        + " bytes"
                );
                System.out.println(
                    "Compilation time: "
                        + (elapsed / 1_000_000.0)
                        + " ms"
                );
                System.out.println("Execution time: not measured by compiler");
            }

        } catch (Exception e) {
            System.err.println("Compilation failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Map<String, Combinator> translate(
        Map<String, DefinedValue> symbolMap,
        boolean useBC
    ) {
        Map<String, Combinator> rawCombinators =
            new HashMap<String, Combinator>();

        for (Map.Entry<String, DefinedValue> entry : symbolMap.entrySet()) {
            if (entry.getValue() instanceof FunctionDefinition) {
                FunctionDefinition funcDef =
                    (FunctionDefinition) entry.getValue();

                Term scottEncodedTerm =
                    ScottEncoding.desugar(funcDef.getTerm());

                Combinator combinator =
                    scottEncodedTerm
                        .toIntermediateTerm()
                        .methodT(useBC)
                        .toCombinatorTerm();

                rawCombinators.put(entry.getKey(), combinator);
            }
        }

        return rawCombinators;
    }

    private static boolean shouldRun(Options options, String pass) {
        if ("all".equalsIgnoreCase(options.optLevel)) {
            return true;
        }

        if ("0".equals(options.optLevel)) {
            return false;
        }

        if ("1".equals(options.optLevel)) {
            return "cse".equals(pass);
        }

        if ("2".equals(options.optLevel)) {
            return "cse".equals(pass)
                || "inline".equals(pass)
                || "combinator-opt".equals(pass);
        }

        if ("3".equals(options.optLevel)) {
            return true;
        }

        throw new IllegalArgumentException(
            "Unknown optimization level: " + options.optLevel);
    }

    private static int sizeOfMap(Map<String, Combinator> combinators) {
        int total = 0;

        for (Combinator combinator : combinators.values()) {
            total += sizeOf(combinator);
        }

        return total;
    }

    private static int sizeOf(Combinator combinator) {
        if (combinator instanceof CombinatorApplication) {
            CombinatorApplication app =
                (CombinatorApplication) combinator;

            return 1
                + sizeOf(app.getFunction())
                + sizeOf(app.getArgument());
        }

        return 1;
    }

    private static void printCombinatorStats(
        String title,
        Map<String, Combinator> combinators
    ) {
        System.out.println();
        System.out.println("========== " + title + " ==========");
        System.out.println("Total size: " + sizeOfMap(combinators));

        for (Map.Entry<String, Combinator> entry : combinators.entrySet()) {
            System.out.println();
            System.out.println(entry.getKey() + " size: " + sizeOf(entry.getValue()));
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }

    private static void printStage(
        Options options,
        String title,
        Object value
    ) {
        if (!options.printStages) {
            return;
        }

        System.out.println();
        System.out.println("========== " + title + " ==========");
        System.out.println(value);
    }

    private static void printDefinedValuesStage(
        Options options,
        String title,
        Map<String, DefinedValue> values
    ) {
        if (!options.printStages) {
            return;
        }

        System.out.println();
        System.out.println("========== " + title + " ==========");

        for (Map.Entry<String, DefinedValue> entry : values.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }

    private static void printTypesStage(
        Options options,
        String title,
        Map<String, Type> types
    ) {
        if (!options.printStages) {
            return;
        }

        System.out.println();
        System.out.println("========== " + title + " ==========");

        for (Map.Entry<String, Type> entry : types.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    private static void printCombinatorStage(
        Options options,
        String title,
        Map<String, Combinator> combinators
    ) {
        if (!options.printStages) {
            return;
        }

        System.out.println();
        System.out.println("========== " + title + " ==========");

        for (Map.Entry<String, Combinator> entry : combinators.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }

    private static String loadSource(Options options) throws Exception {
        if (options.sourceCode != null) {
            return options.sourceCode;
        }

        if (options.inputFile == null) {
            throw new IllegalArgumentException(
                "Provide either --code \"...\" or --file program.kv");
        }

        String fileName = options.inputFile.getFileName().toString();

        if (!fileName.endsWith(".kv")) {
            throw new IllegalArgumentException(
                "Input file must have the .kv extension: " + fileName);
        }

        return new String(
            Files.readAllBytes(options.inputFile),
            StandardCharsets.UTF_8
        );
    }

    private static Options parseArgs(String[] args) {
        Options options = new Options();

        if (args.length == 0) {
            printUsageAndExit();
        }

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if ("--code".equals(arg)) {
                options.sourceCode = requireValue(args, ++i, arg);

            } else if ("--file".equals(arg)) {
                options.inputFile =
                    Path.of(requireValue(args, ++i, arg));

            } else if ("--output".equals(arg) || "-o".equals(arg)) {
                options.outputName = requireValue(args, ++i, arg);

            } else if ("--pipeline".equals(arg)) {
                String value =
                    requireValue(args, ++i, arg).toLowerCase();

                if ("naive".equals(value)) {
                    options.pipeline = Pipeline.NAIVE;
                } else if ("optimized".equals(value)) {
                    options.pipeline = Pipeline.OPTIMIZED;
                } else {
                    throw new IllegalArgumentException(
                        "--pipeline must be naive or optimized");
                }

            } else if ("--opt-level".equals(arg)) {
                options.optLevel = requireValue(args, ++i, arg);

            } else if ("--bc".equals(arg)) {
                options.useBC = true;

            } else if ("--no-bc".equals(arg)) {
                options.useBC = false;

            } else if ("--stats".equals(arg)) {
                options.stats = true;

            } else if ("--stages".equals(arg)
                || "--print-stages".equals(arg)) {
                options.printStages = true;

            } else if ("--exe".equals(arg)) {
                options.compileExecutable = true;

            } else if ("--asm-only".equals(arg)) {
                options.compileExecutable = false;

            } else if ("--no-keep-asm".equals(arg)) {
                options.keepAsm = false;

            } else if ("--help".equals(arg) || "-h".equals(arg)) {
                printUsageAndExit();

            } else {
                throw new IllegalArgumentException(
                    "Unknown option: " + arg);
            }
        }

        if (options.sourceCode != null && options.inputFile != null) {
            throw new IllegalArgumentException(
                "Use either --code or --file, not both");
        }

        if (!options.compileExecutable && !options.keepAsm) {
            throw new IllegalArgumentException(
                "--no-keep-asm can only be used with --exe");
        }

        return options;
    }

    private static String requireValue(
        String[] args,
        int index,
        String optionName
    ) {
        if (index >= args.length) {
            throw new IllegalArgumentException(
                "Missing value for " + optionName);
        }

        return args[index];
    }

    private static void printUsageAndExit() {
        System.out.println(
            "Ka-Vah compiler CLI\n\n" +
                "Required input:\n" +
                "  --file program.kv\n" +
                "  --code \"main = 42;\"\n\n" +
                "Options:\n" +
                "  --pipeline naive|optimized\n" +
                "  --opt-level 0|1|2|3|all\n" +
                "  --bc\n" +
                "  --no-bc\n" +
                "  --stats\n" +
                "  --stages\n" +
                "  --print-stages\n" +
                "  --output name\n" +
                "  --asm-only\n" +
                "  --exe\n" +
                "  --no-keep-asm\n"
        );

        System.exit(0);
    }

    private static void compileExecutable(
        Path asmPath,
        String outputName
    ) throws Exception {
        ProcessBuilder pb =
            new ProcessBuilder(
                "docker",
                "run",
                "--rm",
                "--platform",
                "linux/amd64",
                "-v",
                System.getProperty("user.dir") + ":/work",
                "-w",
                "/work",
                "gcc:latest",
                "bash",
                "-c",
                "gcc -x assembler -nostdlib -no-pie -Wl,-e,_start "
                    + asmPath.getFileName()
                    + " -o "
                    + outputName
            );

        pb.inheritIO();

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Executable compilation failed");
        }
    }

    private static void writeCompileScript() throws Exception {
        String script =
            "#!/usr/bin/env bash\n" +
                "set -e\n\n" +
                "ASM_FILE=\"${1:-test.asm}\"\n" +
                "OUT_FILE=\"${2:-test}\"\n\n" +
                "docker run --rm \\\n" +
                "  --platform linux/amd64 \\\n" +
                "  -v \"$PWD\":/work \\\n" +
                "  -w /work \\\n" +
                "  gcc:latest \\\n" +
                "  bash -c \"gcc -x assembler -nostdlib -no-pie -Wl,-e,_start '$ASM_FILE' -o '$OUT_FILE'\"\n";

        Path path = Path.of("compile_kv.sh");
        Files.write(path, script.getBytes(StandardCharsets.UTF_8));
        makeExecutable(path);
    }

    private static void writeRunScript() throws Exception {
        String script =
            "#!/usr/bin/env bash\n" +
                "set -e\n\n" +
                "PROGRAM=\"${1:-program}\"\n\n" +
                "docker run --rm \\\n" +
                "  --platform linux/amd64 \\\n" +
                "  -v \"$PWD\":/work \\\n" +
                "  -w /work \\\n" +
                "  ubuntu:latest \\\n" +
                "  bash -c \"./$PROGRAM\"\n";

        Path path = Path.of("run_kv.sh");
        Files.write(path, script.getBytes(StandardCharsets.UTF_8));
        makeExecutable(path);
    }

    private static void makeExecutable(Path path) {
        File file = path.toFile();
        file.setExecutable(true, false);
    }
}