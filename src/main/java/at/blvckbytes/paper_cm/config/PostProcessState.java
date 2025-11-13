package at.blvckbytes.paper_cm.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public record PostProcessState(
  Stack<String> pathParts,
  LineNumberResolver lineNumberResolver,
  List<List<String>> errorScreens,
  String configFilePath
) {
  public LineNumbers getCurrentLineNumbers() {
    return lineNumberResolver.resolve(new ArrayList<>(pathParts));
  }
}
