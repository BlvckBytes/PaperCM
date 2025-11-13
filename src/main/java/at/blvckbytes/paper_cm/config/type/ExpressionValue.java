package at.blvckbytes.paper_cm.config.type;

import at.blvckbytes.component_markup.expression.ast.ExpressionNode;
import at.blvckbytes.component_markup.expression.interpreter.ExpressionInterpreter;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.expression.parser.ExpressionParseException;
import at.blvckbytes.component_markup.expression.parser.ExpressionParser;
import at.blvckbytes.component_markup.util.ErrorScreen;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.paper_cm.config.PostProcessState;
import at.blvckbytes.paper_cm.config.PostProcessedConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

public class ExpressionValue extends PostProcessedConfig {

  public final @NotNull Object rawValue;
  public transient ExpressionNode value;

  public ExpressionValue(@NotNull Object rawValue) {
    this.rawValue = rawValue;
  }

  public static ExpressionValue ofLines(String[] lines) {
    var joiner = new StringJoiner("\n");

    for (var line : lines)
      joiner.add(line);

    return new ExpressionValue(joiner.toString());
  }

  @Override
  public void postProcess(PostProcessState postProcessState) {
    var lineNumbers = postProcessState.getCurrentLineNumbers();

    if (!(rawValue instanceof String || rawValue instanceof Number || rawValue instanceof Boolean)) {
      var lineView = InputView.of(lineNumbers.keyLine(), lineNumbers.keyLineNumber());
      postProcessState.errorScreens().add(ErrorScreen.make(lineView, 0, "Only supports strings, numbers or booleans"));
      return;
    }

    var stringValue = String.valueOf(rawValue);

    try {
      value = ExpressionParser.parse(InputView.of(stringValue, lineNumbers.valueLineNumber()), null);
    } catch (ExpressionParseException e) {
      postProcessState.errorScreens().add(e.makeErrorScreen());
    }
  }

  public static @Nullable Object evaluateRaw(@Nullable ExpressionValue expressionValue, InterpretationEnvironment environment) {
    if (expressionValue == null || expressionValue.value == null)
      return null;

    return ExpressionInterpreter.interpret(expressionValue.value, environment);
  }
}
