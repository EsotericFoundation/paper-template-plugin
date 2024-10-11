package org.esotericorganisation.template_paper_plugin.data.player;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.google.gson.Gson;

import org.esotericorganisation.template_paper_plugin.TemplatePaperPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PlayerDataManager {

  private final TemplatePaperPlugin plugin;

  private final String playerDataFilesExtension = ".json";

  private final String playerDataFolderName = "data" + File.separator + "player";
  private final String playerDataFolderPath;
  private final File playerDataFolder;

  private final Map<UUID, PlayerProfile> playerData = new HashMap<>();

  public PlayerDataManager(TemplatePaperPlugin plugin) {
    this.plugin = plugin;

    playerDataFolderPath = plugin.getDataPath() + File.separator + playerDataFolderName;
    playerDataFolder = new File(playerDataFolderPath);

    load();
  }

  private void load() {
    if (!playerDataFolder.exists()) {
      return;
    }

    Gson gson = new Gson();

    File[] playerDataFiles = playerDataFolder.listFiles();
    assert playerDataFiles != null;
    for (File playerDataFile : playerDataFiles) {
      String fileName = playerDataFile.getName();
      String playerUuidString = fileName.split("\\.", 2)[0];

      UUID playerUuid = UUID.fromString(playerUuidString);
      PlayerProfile profile;

      try {
        FileReader reader = new FileReader(playerDataFile);

        profile = gson.fromJson(reader, PlayerProfile.class);

        reader.close();
      } catch (IOException exception) {
        exception.printStackTrace();
        continue;
      }

      playerData.put(playerUuid, profile);
    }
  }

  public void save() {
    if (playerData.isEmpty()) {
      return;
    }

    playerDataFolder.mkdir();

    Gson gson = new Gson();

    for (Entry<UUID, PlayerProfile> entry : playerData.entrySet()) {
      UUID uuid = entry.getKey();
      PlayerProfile profile = entry.getValue();

      File file = new File(playerDataFolderPath + File.separator + uuid.toString() + playerDataFilesExtension);

      try {
        file.createNewFile();

        FileWriter writer = new FileWriter(file);

        String json = gson.toJson(profile);

        writer.write(json);

        writer.flush();
        writer.close();
      } catch (IOException exception) {
        exception.printStackTrace();
      }
    }
  }

  public PlayerProfile getPlayerProfile(UUID uuid, boolean createNewProfile) {
    PlayerProfile profile = playerData.get(uuid);

    if (profile == null) {
      if (!createNewProfile) {
        return null;
      }

      assert plugin.getLanguageManager() != null;
      profile = new PlayerProfile(uuid, plugin.getLanguageManager().getDefaultLanguage());

      playerData.put(uuid, profile);
    }

    return playerData.get(uuid);
  }

  public PlayerProfile getPlayerProfile(UUID uuid) {
    return getPlayerProfile(uuid, true);
  }

  public PlayerProfile getPlayerProfile(@NonNull Player player, boolean createNewProfile) {
    return getPlayerProfile(player.getUniqueId(), createNewProfile);
  }

  public PlayerProfile getPlayerProfile(Player player) {
    return getPlayerProfile(player, true);
  }
}
