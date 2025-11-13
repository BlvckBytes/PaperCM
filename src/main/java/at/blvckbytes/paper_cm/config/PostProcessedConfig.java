package at.blvckbytes.paper_cm.config;

import eu.okaeri.configs.OkaeriConfig;

import java.util.List;

public abstract class PostProcessedConfig extends OkaeriConfig {

  public void postProcess(PostProcessState postProcessState) {
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
