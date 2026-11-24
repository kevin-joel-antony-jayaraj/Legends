package com.legends.board;

import java.util.List;

/**
 * The outcome of checking a board. Lists every problem found, not just the first,
 * so a bad board is easy to debug.
 */
public record ValidationResult(List<String> problems) {

    public ValidationResult {
        problems = List.copyOf(problems);
    }

    public boolean isValid() {
        return problems.isEmpty();
    }
}
