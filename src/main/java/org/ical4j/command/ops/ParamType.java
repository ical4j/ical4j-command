package org.ical4j.command.ops;

/**
 * Surface-neutral parameter value types. Each surface maps these to its own representation:
 * picocli option types on the CLI, JSON schema types for MCP. No JDK type such as
 * {@code java.net.URL} appears here, so no surface ever registers one wholesale for reflection.
 */
public enum ParamType {
    STRING,
    INTEGER,
    BOOLEAN,
    /** One of a fixed set of lowercase choices, see {@link Param#choices()}. */
    ENUM,
    /** A filesystem path, passed as a string and resolved by the handler. */
    PATH
}
