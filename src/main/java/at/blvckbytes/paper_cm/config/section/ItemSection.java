package at.blvckbytes.paper_cm.config.section;

import at.blvckbytes.component_markup.constructor.SlotType;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.DeepIterator;
import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.paper_cm.config.type.CMValue;
import at.blvckbytes.paper_cm.config.PostProcessedConfig;
import at.blvckbytes.paper_cm.config.type.ExpressionValue;
import com.destroystokyo.paper.profile.ProfileProperty;
import eu.okaeri.configs.annotation.Exclude;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.UUID;
import java.util.function.IntConsumer;

public class ItemSection extends PostProcessedConfig {

  @Exclude
  private static final int DEFAULT_AMOUNT = 1;

  @Exclude
  private static final Material DEFAULT_TYPE = Material.BARRIER;

  public @Nullable ExpressionValue amount$;
  public @Nullable CMValue name;
  public @Nullable CMValue lore;
  public @Nullable CMValue material;
  public @Nullable CMValue textures;
  public @Nullable ExpressionValue slot$;

  public ItemSection amount(String... initialValue) {
    this.amount$ = ExpressionValue.ofLines(initialValue);
    return this;
  }

  public ItemSection name(String... initialValue) {
    this.name = CMValue.ofLines(initialValue);
    return this;
  }

  public ItemSection lore(String... initialValue) {
    this.lore = CMValue.ofLines(initialValue);
    return this;
  }

  public ItemSection material(String... initialValue) {
    this.material = CMValue.ofLines(initialValue);
    return this;
  }

  public ItemSection textures(String... initialValue) {
    this.textures = CMValue.ofLines(initialValue);
    return this;
  }

  public ItemSection slot(String... initialValue) {
    this.slot$ = ExpressionValue.ofLines(initialValue);
    return this;
  }

  private boolean setItemOrLog(Inventory inventory, ItemStack item, int slot, InputView view) {
    var maxSlot = inventory.getSize() - 1;

    if (slot < 0 || slot > maxSlot) {
      logger.logErrorScreen(view, "Slot cannot be less than zero or greater than " + maxSlot + ": \"" + slot + "\"");
      return false;
    }

    inventory.setItem(slot, item);
    return true;
  }

  public void buildAndRenderInto(Inventory inventory, InterpretationEnvironment environment, @Nullable IntConsumer slotConsumer) {
    ExpressionValue.consumeRaw(slot$, environment, (view, value) -> {
      var rawSlots = environment.getValueInterpreter().asList(value);

      ItemStack item = null;

      for (Iterator<Long> slotIterator = new DeepIterator<>(rawSlots, environment.getValueInterpreter()::asLong); slotIterator.hasNext();) {
        var slotNumber = slotIterator.next();

        if (item == null)
          item = build(environment);

        if (setItemOrLog(inventory, item, slotNumber.intValue(), view)) {
          if (slotConsumer != null)
            slotConsumer.accept(slotNumber.intValue());
        }
      }
    });
  }

  public ItemStack build(InterpretationEnvironment environment) {
    var itemMaterial = CMValue.evaluatePlain(material, environment, (view, value) -> {
      try {
        return Material.valueOf(value);
      } catch (IllegalArgumentException e) {
        logger.logErrorScreen(view, "Invalid item material-constant: \"" + value + "\"");
        return null;
      }
    }, DEFAULT_TYPE);

    var itemAmount = ExpressionValue.evaluateRaw(amount$, environment, (view, value) -> {
      var numericValue = environment.getValueInterpreter().asLong(value);

      if (numericValue <= 0) {
        logger.logErrorScreen(view, "Item amount cannot be less than or equal to zero: \"" + value + "\"");
        return DEFAULT_AMOUNT;
      }

      return (int) numericValue;
    }, DEFAULT_AMOUNT);

    var item = new ItemStack(itemMaterial, itemAmount);
    var meta = item.getItemMeta();

    if (name != null)
      meta.displayName(name.interpretComponent(environment, SlotType.ITEM_NAME));

    if (lore != null)
      meta.lore(lore.interpretComponents(environment, SlotType.ITEM_LORE));

    if (meta instanceof SkullMeta skullMeta && textures != null) {
      var texturesValue = CMValue.evaluatePlain(textures, environment, (view, input) -> input, "");

      if (!texturesValue.isBlank()) {
        var profile = Bukkit.createProfile(UUID.randomUUID());
        profile.setProperty(new ProfileProperty("textures", texturesValue));
        skullMeta.setPlayerProfile(profile);
      }
    }

    item.setItemMeta(meta);
    return item;
  }
}
