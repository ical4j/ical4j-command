package org.ical4j.command.mcp;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import org.ical4j.command.ops.Context;
import org.ical4j.command.ops.Operation;
import org.ical4j.command.ops.OperationException;
import org.ical4j.command.ops.OperationId;
import org.ical4j.command.ops.Param;
import org.ical4j.command.ops.Params;
import org.ical4j.command.ops.Registry;
import org.ical4j.command.ops.Surface;
import org.ical4j.command.vocab.Verb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The MCP view of the registry: one tool per MCP-exposed operation, named
 * {@code <verb>_<subject>}, with an input schema derived from the parameter descriptors. Tools are
 * registered programmatically; nothing is discovered by annotation or reflection.
 */
public final class Tools {

    private static final Logger LOG = LoggerFactory.getLogger(Tools.class);

    private static final Set<Verb> READ_ONLY = Set.of(Verb.LIST, Verb.GET, Verb.EXPORT, Verb.VALIDATE, Verb.FILTER);

    private static final Set<Verb> DESTRUCTIVE = Set.of(Verb.REMOVE, Verb.MOVE, Verb.UPDATE);

    private Tools() {
    }

    /** The tool name for an operation: {@code list_event}, {@code import_calendar}. */
    public static String name(OperationId id) {
        return id.verbKey() + "_" + id.subjectKey();
    }

    /**
     * Tool specifications for every MCP-exposed operation, in registry order. Each call builds a
     * fresh context and runs the operation through the registry.
     *
     * @throws IllegalStateException if two exposed operations would share a tool name
     */
    public static List<SyncToolSpecification> specifications(Registry registry, Supplier<Context> context) {
        Map<String, Operation> byName = new LinkedHashMap<>();
        List<SyncToolSpecification> specs = new ArrayList<>();
        for (Operation op : registry.forSurface(Surface.MCP)) {
            String name = name(op.id());
            Operation clash = byName.putIfAbsent(name, op);
            if (clash != null) {
                throw new IllegalStateException("MCP cannot distinguish '" + clash.id() + "' from '" + op.id()
                        + "': both are tool " + name);
            }
            specs.add(SyncToolSpecification.builder()
                    .tool(tool(op))
                    .callHandler((exchange, request) -> call(registry, op, context, request.arguments()))
                    .build());
        }
        return specs;
    }

    static McpSchema.Tool tool(Operation op) {
        Verb verb = op.id().verb();
        return McpSchema.Tool.builder(name(op.id()), inputSchema(op.params()))
                .description(op.description())
                .annotations(McpSchema.ToolAnnotations.builder()
                        .readOnlyHint(READ_ONLY.contains(verb))
                        .destructiveHint(DESTRUCTIVE.contains(verb))
                        .openWorldHint(false)
                        .build())
                .build();
    }

    /** A JSON schema object for the descriptors. Unknown properties are rejected. */
    static Map<String, Object> inputSchema(List<Param> params) {
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();
        for (Param p : params) {
            Map<String, Object> property = new LinkedHashMap<>(valueSchema(p));
            if (p.multiple()) {
                property = new LinkedHashMap<>(Map.of("type", "array", "items", property));
            }
            property.put("description", p.description());
            if (p.defaultValue() != null) {
                Object value = typed(p, p.defaultValue());
                property.put("default", p.multiple() ? List.of(value) : value);
            }
            properties.put(p.name(), property);
            if (p.required()) {
                required.add(p.name());
            }
        }
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("required", required);
        schema.put("additionalProperties", false);
        return schema;
    }

    private static Map<String, Object> valueSchema(Param p) {
        switch (p.type()) {
            case INTEGER:
                return Map.of("type", "integer");
            case BOOLEAN:
                return Map.of("type", "boolean");
            case ENUM:
                return Map.of("type", "string", "enum", p.choices());
            case PATH:
            case STRING:
            default:
                return Map.of("type", "string");
        }
    }

    private static Object typed(Param p, String value) {
        switch (p.type()) {
            case INTEGER:
                return Integer.valueOf(value);
            case BOOLEAN:
                return Boolean.valueOf(value);
            default:
                return value;
        }
    }

    private static McpSchema.CallToolResult call(Registry registry, Operation op, Supplier<Context> context,
                                                 Map<String, Object> arguments) {
        try {
            Params params = Params.of(arguments == null ? Map.of() : arguments);
            LOG.debug("Calling {} with {}", op.id(), params);
            return Contents.result(registry.execute(op.id(), context.get(), params));
        } catch (OperationException e) {
            LOG.debug("{} failed: {}", op.id(), e.getMessage());
            return Contents.error(e.getMessage());
        } catch (RuntimeException e) {
            LOG.error("{} failed unexpectedly", op.id(), e);
            return Contents.error(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        }
    }
}
