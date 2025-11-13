package at.blvckbytes.paper_cm.config;

import at.blvckbytes.component_markup.constructor.SlotType;
import at.blvckbytes.component_markup.expression.interpreter.InterpretationEnvironment;
import at.blvckbytes.component_markup.util.ErrorScreen;
import at.blvckbytes.component_markup.util.LoggerProvider;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.logging.Level;

public class ItemSection extends PostProcessedConfig {

  public @Nullable CMValue amount;
  public @Nullable CMValue name;
  public @Nullable CMValue lore;
  public @Nullable CMValue material;
  public @Nullable CMValue textures;

  public ItemSection amount(String... initialValue) {
    this.amount = CMValue.ofLines(initialValue);
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

  public ItemStack build(InterpretationEnvironment environment) {
    var itemMaterial = CMValue.evaluatePlain(material, environment, (view, value) -> {
      try {
        return Material.valueOf(value);
      } catch (IllegalArgumentException e) {
        for (var line : ErrorScreen.make(view, "Invalid item material-constant: \"" + value + "\""))
          LoggerProvider.log(Level.WARNING, line, false);
        return null;
      }
    }, Material.BARRIER);

    var itemAmount = CMValue.evaluatePlain(amount, environment, (view, value) -> {
      try {
        var numericValue = Integer.parseInt(value);

        if (numericValue <= 0) {
          for (var line : ErrorScreen.make(view, "Item amount cannot be less than or equal to zero: \"" + value + "\""))
            LoggerProvider.log(Level.WARNING, line, false);
          return null;
        }

        return numericValue;
      } catch (NumberFormatException e) {
        for (var line : ErrorScreen.make(view, "Non-numeric item amount: \"" + value + "\""))
          LoggerProvider.log(Level.WARNING, line, false);
        return null;
      }
    }, 1);

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
