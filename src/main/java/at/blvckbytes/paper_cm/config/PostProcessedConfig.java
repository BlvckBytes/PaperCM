package at.blvckbytes.paper_cm.config;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;
import eu.okaeri.configs.OkaeriConfig;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class PostProcessedConfig extends OkaeriConfig {

  protected InterpreterLogger logger;
  private @Nullable GlobalEnvironmentResolver globalEnvironmentResolver;

  protected void extendEnvironment(InterpretationEnvironment input) {
    if (globalEnvironmentResolver != null)
      input.withVariable("env", globalEnvironmentResolver);
  }

  public void postProcess(PostProcessState postProcessState) {
    globalEnvironmentResolver = postProcessState.globalEnvironmentResolver;
    logger = postProcessState.logger;

    getDeclaration().getFields().forEach(field -> {
      var value = field.getValue();

      if (value instanceof PostProcessedConfig postProcessedConfig) {
        postProcessState.pathParts.push(field.getName());
        postProcessedConfig.postProcess(postProcessState);
        postProcessState.pathParts.pop();
      }

      if (value instanceof List<?> collection) {
        postProcessState.pathParts.push(field.getName());

        for (var i = 0; i < collection.size(); ++i) {
          var item = collection.get(i);

          if (!(item instanceof PostProcessedConfig postProcessedConfig))
            continue;

          postProcessState.pathParts.push(String.valueOf(i));
          postProcessedConfig.postProcess(postProcessState);
          postProcessState.pathParts.pop();
        }

        postProcessState.pathParts.pop();
      }
    });
  }
}
