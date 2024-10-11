package org.esoteric.minecraft.plugins.template.custom.multientities;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;

import org.esoteric.minecraft.plugins.template.TemplatePaperPlugin;

public abstract class AbstractCustomMultientity<E extends Entity> implements Listener {

  protected final TemplatePaperPlugin plugin;

  private final CustomMultientity entityId;

  protected AbstractCustomMultientity(CustomMultientityManager customMultientityManager, CustomMultientity entityId) {
    this.plugin = customMultientityManager.getPlugin();

    this.entityId = entityId;

    Bukkit.getPluginManager().registerEvents(this, plugin);

    customMultientityManager.addCustomEntity(entityId, this);
  }

  protected abstract List<E> generateCustomEntity(Location spawnLocation);

  public List<E> getCustomEntity(Location spawnLocation) {
    List<E> entities = generateCustomEntity(spawnLocation);
    for (E entity : entities) {
      entity.getPersistentDataContainer().set(plugin.getCustomEntityIdKey(), PersistentDataType.STRING, entityId.name());
    }

    return entities;
  }

  public boolean isEntity(Entity entity) {
    if (entity == null) {
      return false;
    }

    try {
      return entityId == CustomMultientity.valueOf(entity.getPersistentDataContainer().get(plugin.getCustomEntityIdKey(), PersistentDataType.STRING));
    } catch (IllegalArgumentException | NullPointerException exception) {
      return false;
    }
  }
}
