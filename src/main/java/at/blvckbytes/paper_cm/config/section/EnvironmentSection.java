package at.blvckbytes.paper_cm.config.section;

import at.blvckbytes.component_markup.markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.markup.parser.MarkupParseException;
import at.blvckbytes.component_markup.markup.parser.MarkupParser;
import at.blvckbytes.component_markup.util.ErrorScreen;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.paper_cm.config.PostProcessState;
import at.blvckbytes.paper_cm.config.PostProcessedConfig;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EnvironmentSection extends PostProcessedConfig {

  public Map<String, Object> plain = new HashMap<>();
  public Map<String, Object> component = new HashMap<>();

  public EnvironmentSection plain(String key, String value) {
    ensureKeyAbsence(key);
    plain.put(key, value);
    return this;
  }

  public EnvironmentSection component(String key, String value) {
    ensureKeyAbsence(key);
    component.put(key, value);
    return this;
  }

  @Override
  public void postProcess(PostProcessState postProcessState) {
    super.postProcess(postProcessState);

    var seenKeysLower = new HashSet<String>();

    postProcessState.pathParts.push("plain");
    loadEntries(plain, postProcessState, seenKeysLower, false);
    postProcessState.pathParts.pop();

    postProcessState.pathParts.push("component");
    loadEntries(component, postProcessState, seenKeysLower, true);
    postProcessState.pathParts.pop();
  }

  private void loadEntries(
    Map<String, Object> map,
    PostProcessState postProcessState,
    Set<String> seenKeysLower,
    boolean parseComponents
  ) {
    for (var plainEntry : map.entrySet()) {
      var key = plainEntry.getKey();

      postProcessState.pathParts.push(key);
      var lineNumbers = postProcessState.getCurrentLineNumbers();
      postProcessState.pathParts.pop();

      if (!seenKeysLower.add(key.toLowerCase())) {
        var lineView = InputView.of(lineNumbers.keyLine(), lineNumbers.keyLineNumber());
        postProcessState.errorScreens.add(ErrorScreen.make(lineView, 0, "This environment-variable already exists; please choose another name!"));
        continue;
      }

      var rawValue = plainEntry.getValue();

      if (rawValue == null) {
        postProcessState.globalEnvironmentResolver.environment.withVariable(key, null);
        continue;
      }

      if (!(rawValue instanceof String || rawValue instanceof Number || rawValue instanceof Boolean)) {
        var lineView = InputView.of(lineNumbers.keyLine(), lineNumbers.keyLineNumber());
        postProcessState.errorScreens.add(ErrorScreen.make(lineView, 0, "Only supports strings, numbers, booleans or the null-value"));
        continue;
      }

      if (parseComponents) {
        var stringValue = String.valueOf(rawValue);

        try {
          postProcessState.globalEnvironmentResolver.environment.withVariable(key, MarkupParser.parse(InputView.of(stringValue, lineNumbers.valueLineNumber()), BuiltInTagRegistry.INSTANCE));
        } catch (MarkupParseException e) {
          postProcessState.errorScreens.add(e.makeErrorScreen());
        }
        continue;
      }

      postProcessState.globalEnvironmentResolver.environment.withVariable(key, rawValue);
    }
  }

  private void ensureKeyAbsence(String key) {
    if (plain.keySet().stream().anyMatch(existingKey -> existingKey.equalsIgnoreCase(key)))
      throw new IllegalStateException("Key \"" + key + "\" already exists as a plain entry");

    if (component.keySet().stream().anyMatch(existingKey -> existingKey.equalsIgnoreCase(key)))
      throw new IllegalStateException("Key \"" + key + "\" already exists as a component entry");
  }
}
