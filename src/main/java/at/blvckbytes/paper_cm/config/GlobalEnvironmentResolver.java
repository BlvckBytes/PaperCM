package at.blvckbytes.paper_cm.config;

import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.interpreter.DirectFieldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class GlobalEnvironmentResolver implements DirectFieldAccess {

  public final InterpretationEnvironment environment;

  public GlobalEnvironmentResolver() {
    this.environment = new InterpretationEnvironment();
  }

  @Override
  public @Nullable Object accessField(String rawIdentifier) {
    if (environment.doesVariableExist(rawIdentifier))
      return environment.getVariableValue(rawIdentifier);

    return DirectFieldAccess.UNKNOWN_FIELD_SENTINEL;
  }

  @Override
  public @Nullable Set<String> getAvailableFields() {
    var knownNames = new HashSet<String>();
    environment.forEachKnownName(knownNames::add);
    return knownNames;
  }
}
