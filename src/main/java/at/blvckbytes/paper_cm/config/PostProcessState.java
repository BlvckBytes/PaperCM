package at.blvckbytes.paper_cm.config;

import at.blvckbytes.component_markup.util.logging.InterpreterLogger;

import java.util.List;
import java.util.Stack;

public class PostProcessState {

  public final Stack<String> pathParts;
  public final LineNumberResolver lineNumberResolver;
  public final List<List<String>> errorScreens;
  public final InterpreterLogger logger;
  public final GlobalEnvironmentResolver globalEnvironmentResolver;

  public PostProcessState(Stack<String> pathParts, LineNumberResolver lineNumberResolver, List<List<String>> errorScreens, InterpreterLogger logger) {
    this.pathParts = pathParts;
    this.lineNumberResolver = lineNumberResolver;
    this.errorScreens = errorScreens;
    this.logger = logger;
    this.globalEnvironmentResolver = new GlobalEnvironmentResolver();
  }

  public LineNumbers getCurrentLineNumbers() {
    return lineNumberResolver.resolve(pathParts);
  }
}
