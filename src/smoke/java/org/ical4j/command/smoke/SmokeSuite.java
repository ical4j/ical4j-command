package org.ical4j.command.smoke;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ical4j.command.ops.Operation;
import org.ical4j.command.ops.Operations;
import org.ical4j.command.ops.Surface;
import org.ical4j.command.util.Filesystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Black-box smoke run of an {@code ict} executable: {@code --help} for every operation, one real
 * invocation per operation, the native-sensitive paths (time zones, ServiceLoader factories,
 * JSON and XML output) and one {@code ict mcp} handshake.
 * <p>
 * The target is a command prefix, so the same steps run against the native binary
 * ({@code SmokeSuite build/native/nativeCompile/ict}) and against the JVM build under the
 * tracing agent, which is how the native metadata is captured (design D6). Before running, the
 * suite checks that every operation in the standard registry is covered by a step and fails if
 * one is not (design D8).
 */
public final class SmokeSuite {

    private static final ObjectMapper JSON = new ObjectMapper();

    private static final long TIMEOUT_SECONDS = 60;

    /**
     * One invocation.
     *
     * @param covers the operation tuple this step is the real invocation of, or {@code null}
     * @param args   arguments after the executable
     * @param stdin  standard input, or {@code null}
     * @param exit   expected exit code
     * @param expect substrings stdout must contain
     */
    record Step(String covers, List<String> args, String stdin, int exit, List<String> expect) {

        static Step of(String covers, int exit, String args, String... expect) {
            return new Step(covers, List.of(args.split(" ")), null, exit, List.of(expect));
        }

        Step withStdin(String input) {
            return new Step(covers, args, input, exit, expect);
        }

        /** Appends one argument verbatim, for paths that may contain spaces. */
        Step plus(Object arg) {
            List<String> more = new ArrayList<>(args);
            more.add(arg.toString());
            return new Step(covers, more, stdin, exit, expect);
        }
    }

    private SmokeSuite() {
    }

    public static void main(String[] argv) throws Exception {
        if (argv.length == 0) {
            System.err.println("Usage: SmokeSuite <ict executable> [args before the command ...]");
            System.exit(2);
        }
        Set<String> missing = missing();
        if (!missing.isEmpty()) {
            System.err.println("Operations with no smoke step: " + missing);
            System.exit(1);
        }
        List<String> prefix = List.of(argv);
        Path work = Files.createTempDirectory("ict-smoke");
        Path data = Files.createDirectories(work.resolve("data"));
        Path fixtures = Fixtures.write(Files.createDirectories(work.resolve("fixtures")));

        int failures = 0;
        for (Step step : steps(fixtures)) {
            failures += run(prefix, data, step) ? 0 : 1;
        }
        failures += mcpHandshake(prefix, data) ? 0 : 1;

        System.out.println(failures == 0 ? "Smoke suite passed" : "Smoke suite FAILED: " + failures + " step(s)");
        System.exit(failures == 0 ? 0 : 1);
    }

    /** Operation tuples in the standard registry that no step claims to cover. */
    public static Set<String> missing() {
        Set<String> missing = new LinkedHashSet<>();
        Operations.standard().all().forEach(op -> missing.add(op.id().toString()));
        steps(Path.of("fixtures")).forEach(s -> missing.remove(s.covers()));
        return missing;
    }

    static List<Step> steps(Path fixtures) {
        List<Step> steps = new ArrayList<>();
        steps.add(Step.of(null, 0, "--help", "Commands:", "mcp"));
        steps.add(Step.of(null, 0, "--version", "ict "));
        for (Operation op : Operations.standard().forSurface(Surface.CLI)) {
            steps.add(Step.of(null, 0, op.id().verbKey() + " " + op.id().subjectKey() + " --help", leadingWords(op.description())));
        }
        steps.add(Step.of(null, 2, "list"));

        steps.add(Step.of("add collection in workspace", 0, "add collection --collection work", "Added calendar collection 'work'"));
        steps.add(Step.of(null, 0, "add collection --collection people --type card --description Contacts"));
        steps.add(Step.of("list collection in workspace", 0, "list collection --format json", "\"work\"", "\"people\""));
        steps.add(Step.of("get collection in workspace", 0, "get collection --collection people --type card", "description: Contacts"));
        steps.add(Step.of(null, 1, "get collection --collection missing"));

        // ServiceLoader factories: VEVENT/VTODO/VJOURNAL/VAVAILABILITY are all created through
        // ical4j's ComponentFactory services, X-WR-CALNAME through ical4j-extensions' PropertyFactory.
        steps.add(Step.of("import calendar in collection", 0, "import calendar --collection work --file", "tz1@ict")
                .plus(fixtures.resolve("smoke.ics")));
        steps.add(Step.of(null, 0, "import calendar --collection work", "av1@ict").withStdin(Fixtures.AVAILABILITY));
        steps.add(Step.of("list event in collection", 0, "list event --collection work", "tz1@ict", "Melbourne meeting"));
        steps.add(Step.of("list action in collection", 0, "list action --collection work", "todo1@ict"));
        steps.add(Step.of("list note in collection", 0, "list note --collection work", "note1@ict"));

        // Time zones: the VTIMEZONE comes from ical4j's zoneinfo resources.
        steps.add(Step.of("export calendar in collection", 0, "export calendar --collection work --uid tz1@ict",
                "BEGIN:VCALENDAR", "X-WR-CALNAME:Smoke", "X-ICT-SMOKE:yes", "BEGIN:VTIMEZONE", "TZID:Australia/Melbourne"));
        steps.add(Step.of(null, 0, "export calendar --collection work --uid av1@ict",
                "BEGIN:VAVAILABILITY", "BEGIN:AVAILABLE", "RRULE:FREQ=WEEKLY"));
        steps.add(Step.of(null, 0, "export calendar --collection work --uid tz1@ict --format json", "\"vcalendar\"", "\"vtimezone\""));
        steps.add(Step.of(null, 0, "export calendar --collection work --uid tz1@ict --format xml",
                "urn:ietf:params:xml:ns:icalendar-2.0", "<vtimezone>"));
        steps.add(Step.of(null, 0, "export calendar --collection work", "UID:tz1@ict", "UID:todo1@ict", "UID:av1@ict"));
        steps.add(Step.of("remove calendar in collection", 0, "remove calendar --collection work --uid note1@ict", "Removed calendar"));
        steps.add(Step.of(null, 1, "remove calendar --collection work --uid note1@ict"));

        steps.add(Step.of("import card in collection", 0, "import card --collection people --file", "ada@ict")
                .plus(fixtures.resolve("ada.vcf")));
        steps.add(Step.of("list entity in collection", 0, "list entity --collection people", "Ada Lovelace"));
        steps.add(Step.of("export card in collection", 0, "export card --collection people --uid ada@ict",
                "BEGIN:VCARD", "FN:Ada Lovelace"));
        steps.add(Step.of("remove card in collection", 0, "remove card --collection people --uid ada@ict", "Removed card"));
        steps.add(Step.of("remove collection in workspace", 0, "remove collection --collection people --type card",
                "Removed card collection 'people'"));

        steps.add(Step.of(null, 0, "config workspace.active", "default"));
        return steps;
    }

    /** The start of a description, short enough that usage help never wraps it. */
    private static String leadingWords(String text) {
        if (text.length() <= 40) {
            return text;
        }
        int cut = text.lastIndexOf(' ', 40);
        return text.substring(0, cut > 0 ? cut : 40);
    }

    private static boolean run(List<String> prefix, Path data, Step step) throws IOException, InterruptedException {
        long start = System.nanoTime();
        List<String> command = new ArrayList<>(prefix);
        command.addAll(step.args());
        Process process = start(command, data);
        try (OutputStream in = process.getOutputStream()) {
            if (step.stdin() != null) {
                in.write(step.stdin().getBytes(StandardCharsets.UTF_8));
            }
        }
        // Drain stderr concurrently so a chatty process cannot block on a full pipe.
        StringBuilder err = new StringBuilder();
        Thread errPump = pump(process, err);
        String out = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            return report(false, step.args(), start, "timed out");
        }
        errPump.join();
        List<String> problems = new ArrayList<>();
        if (process.exitValue() != step.exit()) {
            problems.add("exit " + process.exitValue() + ", expected " + step.exit());
        }
        for (String expected : step.expect()) {
            if (!out.contains(expected)) {
                problems.add("stdout lacks '" + expected + "'");
            }
        }
        if (step.exit() == 0 && !err.toString().isBlank()) {
            problems.add("unexpected stderr");
        }
        String detail = problems.isEmpty() ? "" : String.join("; ", problems) + "\n--- stdout\n" + out + "--- stderr\n" + err;
        return report(problems.isEmpty(), step.args(), start, detail);
    }

    /** initialize, tools/list, tools/call, then EOF: exit 0 and nothing but JSON-RPC on stdout. */
    private static boolean mcpHandshake(List<String> prefix, Path data) throws IOException, InterruptedException {
        long start = System.nanoTime();
        List<String> command = new ArrayList<>(prefix);
        command.add("mcp");
        Process process = start(command, data);
        StringBuilder err = new StringBuilder();
        Thread errPump = pump(process, err);
        List<String> problems = new ArrayList<>();
        try (OutputStream in = process.getOutputStream();
             BufferedReader out = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            Map<?, ?> init = rpc(in, out, 1, "initialize", Map.of("protocolVersion", "2025-06-18",
                    "capabilities", Map.of(), "clientInfo", Map.of("name", "smoke", "version", "1")));
            if (!"ict".equals(((Map<?, ?>) ((Map<?, ?>) init.get("result")).get("serverInfo")).get("name"))) {
                problems.add("initialize: unexpected serverInfo " + init);
            }
            send(in, Map.of("jsonrpc", "2.0", "method", "notifications/initialized"));
            List<?> tools = (List<?>) ((Map<?, ?>) rpc(in, out, 2, "tools/list", Map.of()).get("result")).get("tools");
            long exposed = Operations.standard().forSurface(Surface.MCP).size();
            if (tools.size() != exposed) {
                problems.add("tools/list: " + tools.size() + " tools, expected " + exposed);
            }
            Map<?, ?> call = (Map<?, ?>) rpc(in, out, 3, "tools/call",
                    Map.of("name", "export_calendar", "arguments", Map.of("collection", "work", "uid", "tz1@ict"))).get("result");
            String text = String.valueOf(((Map<?, ?>) ((List<?>) call.get("content")).get(0)).get("text"));
            if (Boolean.TRUE.equals(call.get("isError")) || !text.contains("TZID:Australia/Melbourne")) {
                problems.add("tools/call export_calendar: " + call);
            }
            in.close();
            String trailing = out.readLine();
            if (trailing != null) {
                problems.add("stdout after EOF: " + trailing);
            }
        } catch (RuntimeException | IOException e) {
            problems.add(e.toString());
        }
        if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            problems.add("did not exit after EOF");
        } else if (process.exitValue() != 0) {
            problems.add("exit " + process.exitValue());
        }
        errPump.join();
        String detail = problems.isEmpty() ? "" : String.join("; ", problems) + "\n--- stderr\n" + err;
        return report(problems.isEmpty(), List.of("mcp", "(handshake)"), start, detail);
    }

    private static Map<?, ?> rpc(OutputStream in, BufferedReader out, int id, String method, Map<String, ?> params)
            throws IOException {
        send(in, Map.of("jsonrpc", "2.0", "id", id, "method", method, "params", params));
        String line = out.readLine();
        if (line == null) {
            throw new IOException("no response to " + method);
        }
        Map<?, ?> response = JSON.readValue(line, Map.class);
        if (!"2.0".equals(response.get("jsonrpc")) || !Integer.valueOf(id).equals(response.get("id"))) {
            throw new IOException("not a JSON-RPC response to " + method + ": " + line);
        }
        return response;
    }

    private static void send(OutputStream in, Map<String, ?> message) throws IOException {
        in.write((JSON.writeValueAsString(message) + "\n").getBytes(StandardCharsets.UTF_8));
        in.flush();
    }

    private static Process start(List<String> command, Path data) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.environment().put(Filesystem.DATA_DIR_ENV, data.toString());
        builder.environment().remove("ICT_LOG_LEVEL");
        return builder.start();
    }

    private static Thread pump(Process process, StringBuilder into) {
        Thread t = new Thread(() -> {
            try {
                String s = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                synchronized (into) {
                    into.append(s);
                }
            } catch (IOException e) {
                synchronized (into) {
                    into.append(e);
                }
            }
        });
        t.start();
        return t;
    }

    private static boolean report(boolean ok, List<String> args, long start, String detail) {
        long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        System.out.printf("%s %5d ms  ict %s%n", ok ? "PASS" : "FAIL", ms, String.join(" ", args));
        if (!ok) {
            System.out.println(detail.indent(4));
        }
        return ok;
    }

    /** Input files. */
    static final class Fixtures {

        static final String AVAILABILITY = String.join("\r\n",
                "BEGIN:VCALENDAR", "VERSION:2.0", "PRODID:-//ict smoke//EN",
                "BEGIN:VAVAILABILITY", "UID:av1@ict", "DTSTAMP:20260101T000000Z",
                "DTSTART;TZID=Australia/Melbourne:20260101T090000",
                "BEGIN:AVAILABLE", "UID:av1-a@ict", "DTSTAMP:20260101T000000Z",
                "DTSTART;TZID=Australia/Melbourne:20260105T090000", "DTEND;TZID=Australia/Melbourne:20260105T170000",
                "RRULE:FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR", "END:AVAILABLE",
                "END:VAVAILABILITY", "END:VCALENDAR", "");

        static final String CALENDAR = String.join("\r\n",
                "BEGIN:VCALENDAR", "VERSION:2.0", "PRODID:-//ict smoke//EN", "X-WR-CALNAME:Smoke",
                "BEGIN:VEVENT", "UID:tz1@ict", "DTSTAMP:20260101T000000Z",
                "DTSTART;TZID=Australia/Melbourne:20260310T100000", "DTEND;TZID=Australia/Melbourne:20260310T110000",
                "SUMMARY:Melbourne meeting", "X-ICT-SMOKE:yes", "END:VEVENT",
                "BEGIN:VTODO", "UID:todo1@ict", "DTSTAMP:20260101T000000Z", "SUMMARY:Smoke todo", "END:VTODO",
                "BEGIN:VJOURNAL", "UID:note1@ict", "DTSTAMP:20260101T000000Z", "SUMMARY:Smoke note", "END:VJOURNAL",
                "END:VCALENDAR", "");

        static final String CARD = String.join("\r\n",
                "BEGIN:VCARD", "VERSION:4.0", "UID:ada@ict", "FN:Ada Lovelace", "N:Lovelace;Ada;;;", "END:VCARD", "");

        private Fixtures() {
        }

        static Path write(Path dir) throws IOException {
            Files.writeString(dir.resolve("smoke.ics"), CALENDAR);
            Files.writeString(dir.resolve("ada.vcf"), CARD);
            return dir;
        }
    }
}
