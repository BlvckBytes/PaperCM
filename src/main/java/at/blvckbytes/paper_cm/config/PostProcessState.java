package at.blvckbytes.paper_cm.config;

import at.blvckbytes.component_markup.util.logging.InterpreterLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public record PostProcessState(
  Stack<String> pathParts,
  LineNumberResolver lineNumberResolver,
  List<List<String>> errorScreens,
  InterpreterLogger logger
) {
  public LineNumbers getCurrentLineNumbers() {
    return lineNumberResolver.resolve(new ArrayList<>(pathParts));
  }
}
