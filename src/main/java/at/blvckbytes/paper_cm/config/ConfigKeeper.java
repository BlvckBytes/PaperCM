package at.blvckbytes.paper_cm.config;

import at.blvckbytes.component_markup.util.logging.GlobalLogger;
import at.blvckbytes.paper_cm.config.section.RootSection;
import at.blvckbytes.paper_cm.config.type.CMValueSerializer;
import at.blvckbytes.paper_cm.config.type.ExpressionValueSerializer;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;

public class ConfigKeeper<ConfigType extends RootSection> {

  private final File configFile;
  private final Supplier<ConfigType> creator;
  private final ConfigLogger logger;
  private ConfigType config;

  private final Map<ReloadPriority, List<Runnable>> reloadListenersByPriority;

  public ConfigKeeper(File configFile, Supplier<ConfigType> creator) {
    this.creator = creator;
    this.configFile = configFile;
    this.logger = new ConfigLogger(configFile.getPath());
    this.reloadListenersByPriority = new HashMap<>();
  }

  public void registerReloadListener(Runnable listener, ReloadPriority priority) {
    this.reloadListenersByPriority.computeIfAbsent(priority, key -> new ArrayList<>()).add(listener);
  }

  public void registerReloadListener(Runnable listener) {
    this.registerReloadListener(listener, ReloadPriority.MEDIUM);
  }

  public ConfigType getConfig() {
    return config;
  }

  public boolean reloadConfigAndGetIfError() {
    try {
      this.config = creator.get();

      this.config.configure(opt -> {
        opt.configurer(new YamlSnakeYamlConfigurer());
        opt.bindFile(configFile);
        opt.removeOrphans(true);
        opt.serdes(it -> {
          it.register(new CMValueSerializer());
          it.register(new ExpressionValueSerializer());
        });
      });

      config.saveDefaults();
      config.load(true);

      var errorScreens = new ArrayList<List<String>>();
      var lineNumberResolver = new LineNumberResolver(configFile);

      config.postProcess(new PostProcessState(new Stack<>(), lineNumberResolver, errorScreens, logger));

      if (errorScreens.isEmpty()) {
        for (ReloadPriority priority : ReloadPriority.VALUES_IN_CALL_ORDER) {
          var listeners = this.reloadListenersByPriority.get(priority);

          if (listeners == null)
            continue;

          for (var listener : listeners)
            listener.run();
        }

        return false;
      }

      GlobalLogger.log(Level.SEVERE, "There were errors while trying to load " + configFile.getPath() + "; the following line-numbers point at the issue.", false);

      for (var i = 0; i < errorScreens.size(); ++i) {
        if (i != 0)
          GlobalLogger.log(Level.SEVERE, " ", false);

        for (var line : errorScreens.get(i))
          GlobalLogger.log(Level.SEVERE, line, false);
      }

      return true;
    } catch (Throwable e) {
      GlobalLogger.log(Level.SEVERE, "An error occurred while trying to reload the config", e);
      return true;
    }
  }
}
