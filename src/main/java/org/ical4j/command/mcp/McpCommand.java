package org.ical4j.command.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.ical4j.command.Version;
import org.ical4j.command.ops.Context;
import org.ical4j.command.ops.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

/**
 * {@code ict mcp}: serves the registry as MCP tools over stdio until the client closes stdin.
 * <p>
 * stdout belongs to the protocol for the whole run. The transport holds the only reference to
 * the real stdout; {@link System#out} and picocli's output are pointed at stderr, and logging
 * already goes there, so a stray print cannot corrupt the stream.
 */
public final class McpCommand implements Callable<Integer> {

    private static final Logger LOG = LoggerFactory.getLogger(McpCommand.class);

    private final Registry registry;
    private final Supplier<Context> context;
    private final InputStream in;
    private final OutputStream out;
    private CommandSpec spec;

    private McpCommand(Registry registry, Supplier<Context> context, InputStream in, OutputStream out) {
        this.registry = registry;
        this.context = context;
        this.in = in;
        this.out = out;
    }

    /**
     * @param context supplies a fresh handler context per tool call; its stdin must not be the
     *                protocol stream
     * @param in      the protocol input, normally {@link System#in}
     * @param out     the protocol output, normally {@link System#out}
     */
    public static CommandLine create(Registry registry, Supplier<Context> context, InputStream in, OutputStream out) {
        McpCommand command = new McpCommand(registry, context, in, out);
        CommandSpec spec = CommandSpec.wrapWithoutInspection(command).name("mcp");
        spec.usageMessage().description("Serve ict operations as Model Context Protocol tools over stdio",
                "Runs until the client closes standard input. Logs go to standard error.");
        command.spec = spec;
        return new CommandLine(spec);
    }

    @Override
    public Integer call() throws InterruptedException {
        CommandLine root = spec.root().commandLine();
        root.setOut(root.getErr());
        PrintStream systemOut = System.out;
        if (out == systemOut) {
            System.setOut(System.err);
        }
        try {
            CountDownLatch closed = new CountDownLatch(1);
            McpJsonMapper json = new JacksonMcpJsonMapper(new ObjectMapper());
            StdioServerTransportProvider transport = new StdioServerTransportProvider(json, new EofSignal(in, closed), out);
            McpSyncServer server = McpServer.sync(transport)
                    .serverInfo("ict", Version.get())
                    .capabilities(McpSchema.ServerCapabilities.builder().tools(false).build())
                    .jsonMapper(json)
                    .tools(Tools.specifications(registry, context))
                    .build();
            LOG.info("MCP server ready with {} tools", server.listTools().size());
            closed.await();
            LOG.info("Client closed the stream; shutting down");
            server.close();
            return 0;
        } finally {
            if (out == systemOut) {
                System.setOut(systemOut);
            }
        }
    }

    /** Counts down when the wrapped stream ends or fails, which is how a stdio client disconnects. */
    private static final class EofSignal extends FilterInputStream {

        private final CountDownLatch closed;

        EofSignal(InputStream in, CountDownLatch closed) {
            super(in);
            this.closed = closed;
        }

        @Override
        public int read() throws IOException {
            return watch(() -> super.read());
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return watch(() -> super.read(b, off, len));
        }

        private int watch(Read read) throws IOException {
            try {
                int n = read.run();
                if (n < 0) {
                    closed.countDown();
                }
                return n;
            } catch (IOException e) {
                closed.countDown();
                throw e;
            }
        }

        @FunctionalInterface
        private interface Read {
            int run() throws IOException;
        }
    }
}
