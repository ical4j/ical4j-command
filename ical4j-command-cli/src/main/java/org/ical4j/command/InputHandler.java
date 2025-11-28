package org.ical4j.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Support for command line prompt for input.
 */
public interface InputHandler {

    /**
     * Prompt user for a single line input.
     * @param prompt
     * @return
     */
    default String singleInput(String prompt) {
        return System.console().readLine(prompt + ": ");
    }

    /**
     * Prompt user for multi-line input.
     * @param prompt
     * @return
     */
    default String multiInput(String prompt) {
        List<String> lines = new ArrayList<>();
        System.console().printf(prompt + " (^D to exit): ");
        Scanner input = new Scanner(System.in);
        while (input.hasNextLine()) {
            lines.add(input.nextLine());
        }
        return String.join("\n", lines);
    }
}
