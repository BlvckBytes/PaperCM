package at.blvckbytes.paper_cm.config.section;

import at.blvckbytes.component_markup.constructor.SlotType;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.ErrorScreen;
import at.blvckbytes.paper_cm.config.type.CMValue;
import at.blvckbytes.paper_cm.config.PostProcessedConfig;
import eu.okaeri.configs.annotation.Exclude;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

public abstract class GuiSection<ItemSectionType extends PostProcessedConfig, Self extends GuiSection<ItemSectionType, Self>> extends PostProcessedConfig {

  @Exclude
  private static final int DEFAULT_ROWS = 3;

  public @Nullable CMValue title;
  public @Nullable CMValue rows;

  public ItemSectionType items;

  public GuiSection(ItemSectionType items, Class<ItemSectionType> type) {
    this.items = items;

    getDeclaration().getField("items").orElseThrow().getType().setType(type);
  }

  protected abstract Self self();

  public Self title(String... initialValue) {
    this.title = CMValue.ofLines(initialValue);
    return self();
  }

  public Self rows(String... initialValue) {
    this.rows = CMValue.ofLines(initialValue);
    return self();
  }

  public Inventory buildInventory(InterpretationEnvironment environment) {
    int rowCount = CMValue.evaluatePlain(rows, environment, (view, value) -> {
      var numericValue = environment.getValueInterpreter().asLong(value);

      if (numericValue <= 0 || numericValue > 6) {
        logRuntimeErrorScreen(ErrorScreen.make(view, "Row-count cannot be less than or equal to zero or greater than 6: \"" + value + "\""));
        return DEFAULT_ROWS;
      }

      return (int) numericValue;
    }, DEFAULT_ROWS);

    if (title != null)
      return Bukkit.createInventory(null, rowCount * 9, title.interpretComponent(environment, SlotType.ITEM_NAME));

    return Bukkit.createInventory(null, rowCount * 9);
  }
}
