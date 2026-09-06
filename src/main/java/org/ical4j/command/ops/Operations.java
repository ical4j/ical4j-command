package org.ical4j.command.ops;

/**
 * Assembles the standard registry: every admitted operation, in the order the surfaces list
 * them. Handlers are contributed per subject from the {@code ops} subpackages as they are
 * re-admitted (see {@code docs/dropped-commands.md}); each contribution arrives with a Spock
 * spec that names its tuple, which {@code RegistryConformanceTest} enforces.
 */
public final class Operations {

    private Operations() {
    }

    public static Registry standard() {
        return Registry.builder()
                .addAll(CollectionOperations.all())
                .addAll(CalendarOperations.all())
                .addAll(CardOperations.all())
                .addAll(ListOperations.all())
                .build();
    }
}
