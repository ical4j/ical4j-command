/**
 * Shared command vocabulary (verbs, entity types, loci, serialization families). This
 * module contains vocabulary definitions only - no command dispatch or execution logic -
 * preserving the "shared names, separate dispatch" model.
 */
module ical4j.command.core {
    requires java.base;

    exports org.ical4j.command.core;
}
