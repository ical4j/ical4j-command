package org.ical4j.command.cli;

import org.ical4j.command.ops.Operation;
import org.ical4j.command.ops.Param;
import org.ical4j.command.ops.ParamType;
import org.ical4j.command.ops.Params;
import org.ical4j.command.ops.Registry;
import org.ical4j.command.ops.Surface;
import org.ical4j.command.vocab.Verb;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.OptionSpec;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

/**
 * The CLI view of the registry: {@code ict <verb> <type> [--param value ...]}.
 * <p>
 * One subcommand per verb that has CLI-exposed operations, and under it one subcommand per
 * operation, named by its subject. Every parameter becomes a {@code --name} option; loci such
 * as {@code --collection} are ordinary parameters of the operations that act within them. The
 * tree is built with picocli's programmatic API, so no class here is annotated and none needs
 * reflection metadata in the native image.
 */
final class CommandTree {

    /** Runs an operation with parsed parameters and returns the exit code. */
    @FunctionalInterface
    interface Invoker {
        int invoke(Operation operation, Params params);
    }

    private static final Map<Verb, String> VERB_DESCRIPTIONS = new EnumMap<>(Map.ofEntries(
            Map.entry(Verb.LIST, "List the items of a type within a locus"),
            Map.entry(Verb.GET, "Return one item of a type"),
            Map.entry(Verb.GENERATE, "Synthesise a new item from a strategy or template"),
            Map.entry(Verb.ADD, "Place an item into a locus"),
            Map.entry(Verb.UPDATE, "Modify an existing item in place"),
            Map.entry(Verb.REMOVE, "Take an item out of a locus"),
            Map.entry(Verb.COPY, "Duplicate an item into a locus"),
            Map.entry(Verb.MOVE, "Relocate an item from one locus to another"),
            Map.entry(Verb.FILTER, "Derive a subset of a type by expression"),
            Map.entry(Verb.VALIDATE, "Check an item or locus for conformance"),
            Map.entry(Verb.IMPORT, "Read calendar or card data into a locus"),
            Map.entry(Verb.EXPORT, "Write calendar or card data out of a locus"),
            Map.entry(Verb.SEND, "Transmit items over a channel"),
            Map.entry(Verb.RECEIVE, "Accept incoming items over a channel")));

    private CommandTree() {
    }

    /**
     * Adds a verb subcommand to {@code root} for each verb with CLI operations, in vocabulary
     * order.
     *
     * @throws IllegalStateException if two operations share a verb and subject (the CLI names
     *                               operations without their locus), or a parameter name clashes
     *                               with a global option
     */
    static void attach(CommandLine root, Registry registry, Invoker invoker) {
        Set<String> reserved = new HashSet<>();
        root.getCommandSpec().options().forEach(o -> reserved.addAll(List.of(o.names())));

        for (Map.Entry<Verb, List<Operation>> entry : registry.byVerb(Surface.CLI).entrySet()) {
            Verb verb = entry.getKey();
            String verbKey = verb.name().toLowerCase(Locale.ROOT);
            CommandSpec verbSpec = CommandSpec.create().name(verbKey);
            verbSpec.usageMessage().description(VERB_DESCRIPTIONS.get(verb)).commandListHeading("%nTypes:%n");
            CommandLine verbCommand = new CommandLine(verbSpec);
            // Attach before adding leaves so that inherited options flow root -> verb -> type.
            root.addSubcommand(verbKey, verbCommand);

            Map<String, Operation> bySubject = new LinkedHashMap<>();
            for (Operation op : entry.getValue()) {
                Operation clash = bySubject.putIfAbsent(op.id().subjectKey(), op);
                if (clash != null) {
                    throw new IllegalStateException("CLI cannot distinguish '" + clash.id() + "' from '" + op.id()
                            + "': both are 'ict " + verbKey + " " + op.id().subjectKey() + "'");
                }
                verbCommand.addSubcommand(op.id().subjectKey(), leaf(op, reserved, invoker));
            }
        }
    }

    private static CommandLine leaf(Operation op, Set<String> reserved, Invoker invoker) {
        OperationCommand command = new OperationCommand(op, invoker);
        CommandSpec spec = CommandSpec.wrapWithoutInspection(command).name(op.id().subjectKey());
        spec.usageMessage().description(op.description());
        for (Param p : op.params()) {
            String name = "--" + p.name();
            if (reserved.contains(name)) {
                throw new IllegalStateException("Parameter '" + p.name() + "' of '" + op.id()
                        + "' clashes with the global option " + name);
            }
            spec.addOption(option(p));
        }
        command.spec = spec;
        return new CommandLine(spec);
    }

    /** Derives a picocli option from a descriptor. Defaults and choices are enforced by {@link Params#resolve}. */
    static OptionSpec option(Param p) {
        StringBuilder description = new StringBuilder(p.description());
        if (!p.choices().isEmpty()) {
            description.append(" (one of: ").append(String.join(", ", p.choices())).append(')');
        }
        if (p.defaultValue() != null && p.type() != ParamType.BOOLEAN) {
            description.append(" (default: ").append(p.defaultValue()).append(')');
        }
        OptionSpec.Builder b = OptionSpec.builder("--" + p.name())
                .description(description.toString())
                .required(p.required());
        switch (p.type()) {
            case BOOLEAN:
                b.type(p.multiple() ? boolean[].class : boolean.class).arity("0..1");
                break;
            case ENUM:
                b.completionCandidates(p.choices());
                // fall through: enum values travel as strings
            default:
                b.type(p.multiple() ? String[].class : String.class).paramLabel("<" + p.name() + ">");
        }
        return b.build();
    }

    /** The executable leaf: collects matched options into {@link Params} and hands off. */
    private static final class OperationCommand implements Callable<Integer> {

        private final Operation operation;
        private final Invoker invoker;
        private CommandSpec spec;

        OperationCommand(Operation operation, Invoker invoker) {
            this.operation = operation;
            this.invoker = invoker;
        }

        @Override
        public Integer call() {
            CommandLine.ParseResult parsed = spec.commandLine().getParseResult();
            Map<String, Object> values = new LinkedHashMap<>();
            for (Param p : operation.params()) {
                OptionSpec option = spec.findOption("--" + p.name());
                if (parsed.hasMatchedOption(option)) {
                    values.put(p.name(), asValue(option.getValue()));
                }
            }
            return invoker.invoke(operation, Params.of(values));
        }

        private static Object asValue(Object value) {
            if (value instanceof String[] strings) {
                return List.of(strings);
            }
            if (value instanceof boolean[] flags) {
                return IntStream.range(0, flags.length).mapToObj(i -> String.valueOf(flags[i])).toList();
            }
            return value;
        }
    }
}
