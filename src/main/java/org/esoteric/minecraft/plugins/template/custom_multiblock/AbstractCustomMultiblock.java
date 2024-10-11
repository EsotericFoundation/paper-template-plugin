package org.esoteric.minecraft.plugins.template.custom_multiblock;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;

import org.esoteric.minecraft.plugins.template.TemplatePaperPlugin;
import org.esoteric.minecraft.plugins.template.util.types.BlockLocation;

public abstract class AbstractCustomMultiblock implements Listener {

  protected final TemplatePaperPlugin plugin;

  private final List<List<BlockLocation>> multiblocks = new ArrayList<>();

  public List<List<BlockLocation>> getMultiblocks() {
    return multiblocks;
  }

  public void addMultiblocks(List<List<BlockLocation>> addedMultiblocks) {
    multiblocks.addAll(addedMultiblocks);
  }

  public AbstractCustomMultiblock(TemplatePaperPlugin plugin, CustomMultiblockManager customMultiblockManager, CustomMultiblock multiblockId) {
    this.plugin = plugin;

    Bukkit.getPluginManager().registerEvents(this, plugin);

    customMultiblockManager.addCustomMultiblock(multiblockId, this);
  }

  protected abstract List<Block> generateCustomMultiblock(Location placeLocation);

  public List<Block> getCustomMultiblock(Location placeLocation) {
    List<Block> multiblock = generateCustomMultiblock(placeLocation);

    multiblocks.add(multiblock.stream().map(BlockLocation::new).toList());

    return multiblock;
  }

  public boolean isBlock(Block block) {
    for (List<BlockLocation> multiblock : multiblocks) {
      if (multiblock.contains(new BlockLocation(block))) {
        return true;
      }
    }

    return false;
  }
}
