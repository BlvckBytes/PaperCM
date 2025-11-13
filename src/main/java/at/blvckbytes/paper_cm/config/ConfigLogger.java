package at.blvckbytes.paper_cm.config;

import at.blvckbytes.component_markup.util.ErrorScreen;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.component_markup.util.logging.GlobalLogger;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;

import java.util.logging.Level;

public record ConfigLogger(String configPath) implements InterpreterLogger {

  @Override
  public void logErrorScreen(InputView positionProvider, String message) {
    GlobalLogger.log(Level.SEVERE, "An error occurred while interpreting the data of the config at " + configPath + "; the following line-numbers point at the issue.", false);

    for (var line : ErrorScreen.make(positionProvider, message))
      GlobalLogger.log(Level.SEVERE, line, false);
  }

  @Override
  public void logErrorScreen(InputView positionProvider, String message, Throwable e) {
    logErrorScreen(positionProvider, message);

    GlobalLogger.log(Level.SEVERE, "^- The following error occurred", e);
  }
}
