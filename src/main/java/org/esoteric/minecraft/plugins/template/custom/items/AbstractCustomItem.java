package org.esoteric.minecraft.plugins.template.custom.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.esoteric.minecraft.plugins.template.PaperTemplatePlugin;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCustomItem implements Listener {

  private final PaperTemplatePlugin plugin;

  private final CustomItem itemId;
  private final Material material;

  public AbstractCustomItem(@NotNull CustomItemManager customItemManager, CustomItem itemId, Material material) {
    this.plugin = customItemManager.getPlugin();

    this.itemId = itemId;
    this.material = material;

    Bukkit.getPluginManager().registerEvents(this, plugin);

    customItemManager.addCustomItem(itemId, this);
  }

  protected abstract ItemStack generateCustomItem(ItemStack baseCustomItem, Player player);

  public ItemStack getCustomItem(Player player) {
    ItemStack item = new ItemStack(material);
    item.editMeta((meta) -> meta.getPersistentDataContainer().set(plugin.getCustomItemIdKey(), PersistentDataType.STRING, itemId.name()));
    return generateCustomItem(item, player);
  }

  public boolean isItem(ItemStack itemStack) {
    if (itemStack == null) {
      return false;
    }

    if (!itemStack.hasItemMeta()) {
      return false;
    }

    String dataContainerItemIdValue = itemStack.getItemMeta().getPersistentDataContainer().get(plugin.getCustomItemIdKey(), PersistentDataType.STRING);
    if (dataContainerItemIdValue == null) {
      return false;
    }

    try {
      return itemId == CustomItem.valueOf(dataContainerItemIdValue);
    } catch (IllegalArgumentException exception) {
      return false;
    }
  }

  public void give(@NotNull Player player) {
    player.getInventory().addItem(getCustomItem(player));
  }
}
