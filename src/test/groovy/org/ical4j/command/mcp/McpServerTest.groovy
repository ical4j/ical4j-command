package org.ical4j.command.mcp

import com.fasterxml.jackson.databind.ObjectMapper
import org.ical4j.command.Version
import org.ical4j.command.cli.Main
import org.ical4j.command.config.CommandConfig
import org.ical4j.command.ops.Operations
import org.ical4j.command.ops.Surface
import org.ical4j.command.util.Filesystem
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

import static org.ical4j.command.ops.Fixtures.event
import static org.ical4j.command.ops.Fixtures.write

/**
 * {@code ict mcp} as a client sees it: a separate JVM spoken to over its real stdin and stdout,
 * so the stdout discipline is tested where it matters. The child gets the test runtime
 * classpath and an isolated data directory.
 */
class McpServerTest extends Specification {

    static final ObjectMapper JSON = new ObjectMapper()

    @TempDir
    Path dir

    Client client

    def setup() {
        // Seed a collection with one event through the CLI, in-process.
        def config = { new CommandConfig(dir.toFile()) }
        def sink = new ByteArrayOutputStream()
        assert Main.run(['add', 'collection', '--collection', 'work'] as String[], sink, sink, InputStream.nullInputStream(), config) == 0
        def ics = write(dir, 'e.ics', event('e1@ict', 'Planning'))
        assert Main.run(['import', 'calendar', '--collection', 'work', '--file', ics.toString()] as String[], sink, sink,
                InputStream.nullInputStream(), config) == 0
    }

    def cleanup() {
        client?.kill()
    }

    def "initialize, tools/list and tools/call for 'list event in collection', then a clean shutdown on EOF"() {
        given:
        client = Client.start(dir)

        when:
        def init = client.request('initialize', [protocolVersion: '2025-06-18', capabilities: [:],
                                                 clientInfo     : [name: 'spec', version: '1']])
        client.notify('notifications/initialized')

        then:
        init.result.serverInfo == [name: 'ict', version: Version.get()]
        init.result.capabilities.tools != null

        when:
        def tools = client.request('tools/list').result.tools
        def listEvent = tools.find { it.name == 'list_event' }

        then: 'one tool per MCP-exposed operation'
        tools*.name as Set == Operations.standard().forSurface(Surface.MCP).collect { Tools.name(it.id()) } as Set
        listEvent.inputSchema.properties.collection.type == 'string'
        listEvent.inputSchema.required == ['collection']

        when:
        def listed = client.request('tools/call', [name: 'list_event', arguments: [collection: 'work']]).result

        then:
        !listed.isError
        JSON.readValue(listed.content[0].text as String, List)*.uid == ['e1@ict']

        when: 'a not-found failure is a tool result, and the server carries on'
        def missing = client.request('tools/call', [name: 'get_collection', arguments: [collection: 'nope']]).result
        def exported = client.request('tools/call', [name: 'export_calendar', arguments: [collection: 'work', uid: 'e1@ict']]).result

        then:
        missing.isError
        missing.content[0].text == 'No such collection: nope'
        !exported.isError
        exported.content[0].text.startsWith('BEGIN:VCALENDAR')

        when:
        def exit = client.closeAndWait()

        then:
        exit == 0
        client.unread().isEmpty()
    }

    def 'with verbose logging every stdout line is a JSON-RPC message and the logs are on stderr'() {
        given:
        client = Client.start(dir, '-vvv')

        when:
        client.request('initialize', [protocolVersion: '2025-06-18', capabilities: [:], clientInfo: [name: 'spec', version: '1']])
        client.notify('notifications/initialized')
        client.request('tools/list')
        client.request('tools/call', [name: 'list_collection', arguments: [:]])
        client.request('tools/call', [name: 'get_collection', arguments: [collection: 'nope']])
        def exit = client.closeAndWait()

        then:
        exit == 0
        client.stdoutLines.size() == 4
        client.stdoutLines.every { line -> JSON.readValue(line, Map).jsonrpc == '2.0' }
        def stderr = client.stderr()
        stderr.contains('[DEBUG] Tools - Calling list collection in workspace')
        stderr.contains('[INFO] McpCommand - MCP server ready')
    }

    /** A minimal line-oriented JSON-RPC client over a child process's stdio. */
    static class Client {
        final Process process
        final LinkedBlockingQueue<String> lines = new LinkedBlockingQueue<>()
        final List<String> stdoutLines = Collections.synchronizedList([])
        final ByteArrayOutputStream errBytes = new ByteArrayOutputStream()
        final Thread stdoutPump
        final Thread stderrPump
        int nextId = 1

        static Client start(Path dataDir, String... flags) {
            def java = ProcessHandle.current().info().command().orElse('java')
            def command = [java, '-cp', System.getProperty('java.class.path'), Main.name, 'mcp', *flags]
            def builder = new ProcessBuilder(command)
            builder.environment().put(Filesystem.DATA_DIR_ENV, dataDir.toString())
            builder.environment().remove('ICT_LOG_LEVEL')
            new Client(builder.start())
        }

        Client(Process process) {
            this.process = process
            stdoutPump = Thread.start {
                process.inputStream.newReader(StandardCharsets.UTF_8.name()).eachLine { line ->
                    stdoutLines << line
                    lines.put(line)
                }
            }
            stderrPump = Thread.start { errBytes << process.errorStream }
        }

        Map request(String method, Map params = null) {
            int id = nextId++
            send([jsonrpc: '2.0', id: id, method: method] + (params == null ? [:] : [params: params]))
            def line = lines.poll(30, TimeUnit.SECONDS)
            assert line != null: "no response to $method; stderr:\n${stderr()}"
            def response = JSON.readValue(line, Map)
            assert response.id == id
            response
        }

        void notify(String method) {
            send([jsonrpc: '2.0', method: method])
        }

        private void send(Map message) {
            process.outputStream.write((JSON.writeValueAsString(message) + '\n').getBytes(StandardCharsets.UTF_8))
            process.outputStream.flush()
        }

        int closeAndWait() {
            process.outputStream.close()
            assert process.waitFor(30, TimeUnit.SECONDS): "server did not exit after EOF; stderr:\n${stderr()}"
            stdoutPump.join(5000)
            stderrPump.join(5000)
            process.exitValue()
        }

        List<String> unread() {
            new ArrayList<>(lines)
        }

        String stderr() {
            synchronized (errBytes) {
                errBytes.toString(StandardCharsets.UTF_8)
            }
        }

        void kill() {
            if (process.alive) {
                process.destroyForcibly()
            }
        }
    }
}
