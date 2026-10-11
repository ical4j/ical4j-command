package org.ical4j.command.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import org.ical4j.command.ops.Format;
import org.ical4j.command.ops.Renderer;
import org.ical4j.command.ops.Result;

/**
 * Renders operation results as MCP tool results: calendars and cards as iCalendar or vCard text,
 * everything else as JSON text. Failures are tool results with {@code isError} set, never
 * protocol errors, so the model sees the message and the server keeps running.
 */
final class Contents {

    private Contents() {
    }

    static McpSchema.CallToolResult result(Result result) {
        Format format = result instanceof Result.CalendarData || result instanceof Result.CardData
                ? Format.TEXT : Format.JSON;
        return McpSchema.CallToolResult.builder()
                // A renderer per call: it initialises its mappers lazily and tool calls may run concurrently.
                .addTextContent(new Renderer(false).render(result, format))
                .isError(false)
                .build();
    }

    static McpSchema.CallToolResult error(String message) {
        return McpSchema.CallToolResult.builder()
                .addTextContent(message)
                .isError(true)
                .build();
    }
}
