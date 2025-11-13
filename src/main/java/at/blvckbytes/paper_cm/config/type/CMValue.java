package at.blvckbytes.paper_cm.config.type;

import at.blvckbytes.component_markup.constructor.SlotType;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.markup.ast.node.MarkupNode;
import at.blvckbytes.component_markup.markup.ast.tag.built_in.BuiltInTagRegistry;
import at.blvckbytes.component_markup.markup.interpreter.MarkupInterpreter;
import at.blvckbytes.component_markup.markup.parser.MarkupParseException;
import at.blvckbytes.component_markup.markup.parser.MarkupParser;
import at.blvckbytes.component_markup.util.ErrorScreen;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.paper_cm.config.PostProcessState;
import at.blvckbytes.paper_cm.config.PostProcessedConfig;
import at.blvckbytes.paper_cm.constructor.AdventureComponentConstructor;
import at.blvckbytes.paper_cm.constructor.PlainStringComponentConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.StringJoiner;
import java.util.function.BiFunction;

public class CMValue extends PostProcessedConfig {

  public final @NotNull Object rawValue;
  public transient MarkupNode value;

  public CMValue(@NotNull Object rawValue) {
    this.rawValue = rawValue;
  }

  public static CMValue ofLines(String[] lines) {
    var joiner = new StringJoiner("\n");

    for (var line : lines)
      joiner.add(line);

    return new CMValue(joiner.toString());
  }

  @Override
  public void postProcess(PostProcessState postProcessState) {
    super.postProcess(postProcessState);

    var lineNumbers = postProcessState.getCurrentLineNumbers();

    if (!(rawValue instanceof String || rawValue instanceof Number || rawValue instanceof Boolean)) {
      var lineView = InputView.of(lineNumbers.keyLine(), lineNumbers.keyLineNumber());
      postProcessState.errorScreens().add(ErrorScreen.make(lineView, 0, "Only supports strings, numbers or booleans"));
      return;
    }

    var stringValue = String.valueOf(rawValue);

    try {
      value = MarkupParser.parse(InputView.of(stringValue, lineNumbers.valueLineNumber()), BuiltInTagRegistry.INSTANCE);
    } catch (MarkupParseException e) {
      postProcessState.errorScreens().add(e.makeErrorScreen());
    }
  }

  public Component interpretComponent(InterpretationEnvironment environment, SlotType slotType) {
    return MarkupInterpreter.interpret(value, slotType, environment, AdventureComponentConstructor.INSTANCE, logger).getFirst();
  }

  public List<Component> interpretComponents(InterpretationEnvironment environment, SlotType slotType) {
    return MarkupInterpreter.interpret(value, slotType, environment, AdventureComponentConstructor.INSTANCE, logger);
  }

  public static <T> @NotNull T evaluatePlain(
    @Nullable CMValue markupValue,
    InterpretationEnvironment environment,
    BiFunction<InputView, String, @Nullable T> mapper,
    @NotNull T nullFallback
  ) {
    if (markupValue == null || markupValue.value == null)
      return nullFallback;

    var text = MarkupInterpreter.interpret(markupValue.value, SlotType.CHAT, environment, PlainStringComponentConstructor.INSTANCE, markupValue.logger).getFirst();

    if (text.isBlank())
      return nullFallback;

    var mappedValue = mapper.apply(markupValue.value.positionProvider, text);

    if (mappedValue == null)
      return nullFallback;

    return mappedValue;
  }
}
