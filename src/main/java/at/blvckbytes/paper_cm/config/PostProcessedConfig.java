package at.blvckbytes.paper_cm.config;

import at.blvckbytes.component_markup.util.logging.InterpreterLogger;
import eu.okaeri.configs.OkaeriConfig;

import java.util.List;

public abstract class PostProcessedConfig extends OkaeriConfig {

  protected InterpreterLogger logger;

  public void postProcess(PostProcessState postProcessState) {
    logger = postProcessState.logger();

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
