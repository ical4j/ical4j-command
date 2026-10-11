package org.ical4j.command.cli;

import org.ical4j.command.config.CommandConfig;
import org.ical4j.command.mcp.McpCommand;
import org.ical4j.command.ops.Context;
import org.ical4j.command.ops.OperationException;
import org.ical4j.command.ops.Operations;
import org.ical4j.command.ops.Registry;
import org.ical4j.command.ops.Renderer;
import org.ical4j.command.ops.Result;
import picocli.CommandLine;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * The {@code ict} executable. The root command carries the global options; everything under it
 * is derived from the operation registry by {@link CommandTree}, apart from {@code config} and
 * {@code mcp}, which serves the same registry over MCP.
 * <p>
 * Exit codes: 0 success, 1 operation failure (not found, invalid data, store error), 2 usage
 * error. Results go to stdout; diagnostics go to stderr.
 */
@CommandLine.Command(name = "ict", description = "@|bold,fg(104) iCal4j Command-line Tool|@",
        scope = CommandLine.ScopeType.INHERIT, mixinStandardHelpOptions = true, versionProvider = VersionProvider.class,
        commandListHeading = "%n@|bold,fg(104) Commands:|@%n",
        footer = "@|yellow Copyright (c) Ben Fortuna |@", showAtFileInUsageHelp = true)
public class Main extends GlobalOptions {

    static final int OK = 0;
    static final int FAILURE = 1;
    static final int USAGE = 2;

    public static void main(String[] args) {
        LogLevel.configure(args);
        System.exit(run(args, System.out, System.err, System.in, CommandConfig::new));
    }

    /**
     * Builds the standard tree and runs it. The seam for in-process specs: streams and
     * configuration are supplied rather than taken from {@link System}.
     */
    public static int run(String[] args, OutputStream out, OutputStream err, InputStream in,
                          Supplier<CommandConfig> config) {
        return commandLine(Operations.standard(), out, err, in, config).execute(args);
    }

    /**
     * The command tree for a registry, with output, error and input bound explicitly.
     * Configuration is opened on first use, so {@code --help} and {@code --version} touch no files.
     */
    public static CommandLine commandLine(Registry registry, OutputStream out, OutputStream err, InputStream in,
                                          Supplier<CommandConfig> config) {
        Main main = new Main();
        Supplier<CommandConfig> lazyConfig = memoize(config);
        CommandLine cli = new CommandLine(main);
        CommandTree.attach(cli, registry, (operation, params) -> {
            Result result = registry.execute(operation.id(), main.context(lazyConfig, in), params);
            cli.getOut().print(new Renderer(true).render(result, main.format()));
            cli.getOut().flush();
            return OK;
        });
        cli.addSubcommand("config", ConfigCommand.create(lazyConfig));
        // MCP owns stdin, so its handlers get an empty one.
        cli.addSubcommand("mcp", McpCommand.create(registry,
                () -> main.context(lazyConfig, InputStream.nullInputStream()), in, out));
        // After the subcommands exist: these setters propagate to the whole tree.
        cli.setOut(writer(out));
        cli.setErr(writer(err));
        cli.setCaseInsensitiveEnumValuesAllowed(true);
        cli.setExecutionExceptionHandler(main::failure);
        return cli;
    }

    /** The handler context: configuration, with {@code --workspace} applied. */
    private Context context(Supplier<CommandConfig> config, InputStream in) {
        Context.Builder context = config.get().contextBuilder().stdin(() -> in);
        if (workspace() != null) {
            context.activeWorkspace(workspace());
        }
        return context.build();
    }

    /** Maps a failure to an exit code and a stderr diagnostic. stdout is left untouched. */
    private int failure(Exception e, CommandLine command, CommandLine.ParseResult parsed) {
        PrintWriter err = command.getErr();
        String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        err.println("ict: " + message);
        if (verbosity() > 0 || !(e instanceof OperationException)) {
            e.printStackTrace(err);
        }
        if (e instanceof OperationException.Usage) {
            err.println("Try '" + command.getCommandSpec().qualifiedName() + " --help' for more information.");
            err.flush();
            return USAGE;
        }
        err.flush();
        return FAILURE;
    }

    private static PrintWriter writer(OutputStream stream) {
        return new PrintWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8), true);
    }

    private static <T> Supplier<T> memoize(Supplier<T> supplier) {
        return new Supplier<>() {
            private T value;

            @Override
            public synchronized T get() {
                if (value == null) {
                    value = supplier.get();
                }
                return value;
            }
        };
    }
}
