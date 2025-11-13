package at.blvckbytes.paper_cm.config;

import at.blvckbytes.component_markup.util.LoggerProvider;
import eu.okaeri.configs.OkaeriConfig;

import java.util.List;
import java.util.logging.Level;

public abstract class PostProcessedConfig extends OkaeriConfig {

  // TODO: I should introduce the notion of a "config-scoped logger", since all of the loggers
  //       within the interpreters are also missing this informative preamble.
  protected String configFilePath;

  public void logRuntimeErrorScreen(List<String> lines) {
    LoggerProvider.log(Level.SEVERE, "There was an error while working with data from the config at " + configFilePath + "; the line-numbers below reference it.", false);

    for (var line : lines)
      LoggerProvider.log(Level.SEVERE, line, false);
  }

  public void postProcess(PostProcessState postProcessState) {
    configFilePath = postProcessState.configFilePath();

    getDeclaration().getFields().forEach(field -> {
      var value = field.getValue();

      if (value instanceof PostProcessedConfig postProcessedConfig) {
        postProcessState.pathParts().push(field.getName());
        postProcessedConfig.postProcess(postProcessState);
        postProcessState.pathParts().pop();
      }

      if (value instanceof List<?> collection) {
        postProcessState.pathParts().push(field.getName());

        for (var i = 0; i < collection.size(); ++i) {
          var item = collection.get(i);

          if (!(item instanceof PostProcessedConfig postProcessedConfig))
            continue;

          postProcessState.pathParts().push(String.valueOf(i));
          postProcessedConfig.postProcess(postProcessState);
          postProcessState.pathParts().pop();
        }

        postProcessState.pathParts().pop();
      }
    });
  }
}
