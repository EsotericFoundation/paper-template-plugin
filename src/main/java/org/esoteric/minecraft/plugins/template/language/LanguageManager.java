package org.esoteric.minecraft.plugins.template.language;

import org.esoteric.minecraft.plugins.template.TemplatePaperPlugin;
import org.esoteric.minecraft.plugins.template.data.player.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.UUID;
import java.util.stream.Stream;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Locale;
import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LanguageManager {

  private final MiniMessage miniMessage = MiniMessage.miniMessage();

  private final TemplatePaperPlugin plugin;

  private final String languageMessageFilesExtension = ".yaml";

  private final String languagesFolderName = "languages";
  private final String languagesFolderPath;
  private final File languagesFolder;

  private final String defaultLanguage;

  private final Map<String, Map<Message, String>> languages = new HashMap<>();

  public String getDefaultLanguage() {
    return defaultLanguage;
  }

  public Set<String> getLanguages() {
    return languages.keySet();
  }

  public LanguageManager(@NotNull TemplatePaperPlugin plugin) {
    this.plugin = plugin;

    languagesFolderPath = plugin.getDataPath() + File.separator + languagesFolderName;
    languagesFolder = new File(languagesFolderPath);

    saveLanguageFiles();
    loadLanguageMessages();

    defaultLanguage = plugin.getConfig().getString("language.default-language");
  }

  private void saveLanguageFiles() {
    String languagesResourceFolderName = languagesFolderName;
    plugin.getFileManager().saveResourceFileFolder(languagesResourceFolderName, !plugin.getConfig().getBoolean("language.use-custom-messages"));
  }

  private void loadLanguageMessages() {
    for (File languageMessagesFile : languagesFolder.listFiles()) {
      String languageName = languageMessagesFile.getName().split("\\.", 2)[0];

      String languageMessagesResourcePath = languagesFolderName + File.separator + languageName + languageMessageFilesExtension;
      plugin.saveResource(languageMessagesResourcePath, false);

      YamlConfiguration messagesConfiguration = YamlConfiguration.loadConfiguration(languageMessagesFile);
      Map<Message, String> messages = new HashMap<>();

      for (Message message : Message.values()) {
        String mappedResult = messagesConfiguration.getString(message.name());

        if (mappedResult != null) {
          messages.put(message, mappedResult);
        }
      }

      languages.put(languageName, messages);
    }
  }

  public String getLanguage(CommandSender commandSender) {
    String language = getProfileLanguage(commandSender);

    if (language == null) {
      language = getLocale(commandSender);
    }

    return language;
  }

  public String getLanguage(UUID uuid) {
    String language = getProfileLanguage(uuid);

    if (language == null) {
      language = getLocale(uuid);
    }

    return language;
  }

  public String getLanguage(@NotNull PlayerProfile profile) {
    return getLanguage(profile.getUuid());
  }

  public void setLanguage(@NotNull PlayerProfile profile, String language) {
    profile.setLanguage(language);
  }

  public void setLanguage(UUID uuid, String language) {
    assert plugin.getPlayerDataManager() != null;
    setLanguage(plugin.getPlayerDataManager().getPlayerProfile(uuid), language);
  }

  public void setLanguage(@NotNull Player player, String language) {
    setLanguage(player.getUniqueId(), language);
  }

  private String getLocale(CommandSender commandSender) {
    if (!(commandSender instanceof Player player)) {
      return defaultLanguage;
    }

    Locale playerLocale = player.locale();
    String localeDisplayName = playerLocale.getDisplayName();

    if (!getLanguages().contains(localeDisplayName)) {
      return defaultLanguage;
    }

    return localeDisplayName;
  }

  private String getLocale(UUID uuid) {
    Player player = Bukkit.getPlayer(uuid);
    return getLocale(player);
  }

  private String getLocale(@NotNull PlayerProfile profile) {
    return getLocale(profile.getUuid());
  }

  private String getProfileLanguage(PlayerProfile profile) {
    if (profile == null) {
      return null;
    }

    return profile.getLanguage();
  }

  private String getProfileLanguage(UUID uuid) {
    return getProfileLanguage(plugin.getPlayerDataManager().getPlayerProfile(uuid));
  }

  private String getProfileLanguage(CommandSender commandSender) {
    if (commandSender == null) {
      return null;
    } else if (commandSender instanceof Player player) {
      return getProfileLanguage(player.getUniqueId());
    } else {
      return defaultLanguage;
    }
  }

  private @Nullable String getRawMessageString(Message message, String language, boolean fallbackOnDefaultLanguage) {
    Map<Message, String> languageMessageMap = languages.get(language);
    String miniMessageString = languageMessageMap.get(message);

    if (miniMessageString == null) {
      return fallbackOnDefaultLanguage ? getRawMessageString(message, defaultLanguage, false) : null;
    }

    return miniMessageString;
  }

  private String getRawMessageString(Message message, String language) {
    return getRawMessageString(message, language, true);
  }

  private Component getMessage(Message message, String language, boolean fallbackOnDefaultLanguage, Component @NotNull ... arguments) {
    String miniMessageString = getRawMessageString(message, language, fallbackOnDefaultLanguage);

    assert miniMessageString != null;
    Component result = miniMessage.deserialize(miniMessageString);

    for (int i = 0; i < arguments.length; i++) {
      final int argumentIndex = i;

      result = result.replaceText(TextReplacementConfig.builder().matchLiteral("{" + i + "}").replacement((matchResult, builder) -> arguments[argumentIndex]).build());
    }

    return result;
  }

  private Component getMessage(Message message, String language, Component... arguments) {
    return getMessage(message, language, true, arguments);
  }

  private Component getMessage(Message message, String language, boolean fallbackOnDefaultLanguage, Object... arguments) {
    return getMessage(message, language, fallbackOnDefaultLanguage, toComponents(arguments));
  }

  private Component getMessage(Message message, String language, Object... arguments) {
    return getMessage(message, language, true, arguments);
  }

  public Component getMessage(Message message, CommandSender commandSender, boolean fallbackOnDefaultLanguage, Component... arguments) {
    return getMessage(message, getLanguage(commandSender), fallbackOnDefaultLanguage, arguments);
  }

  public Component getMessage(Message message, CommandSender commandSender, Component... arguments) {
    return getMessage(message, commandSender, true, arguments);
  }

  public Component getMessage(Message message, CommandSender commandSender, boolean fallbackOnDefaultLanguage, Object... arguments) {
    return getMessage(message, commandSender, fallbackOnDefaultLanguage, toComponents(arguments));
  }

  public Component getMessage(Message message, CommandSender commandSender, Object... arguments) {
    return getMessage(message, commandSender, true, arguments);
  }

  public Component getMessage(Message message, UUID uuid, boolean fallbackOnDefaultLanguage, Component... arguments) {
    return getMessage(message, getLanguage(uuid), fallbackOnDefaultLanguage, arguments);
  }

  public Component getMessage(Message message, UUID uuid, Component... arguments) {
    return getMessage(message, uuid, true, arguments);
  }

  public Component getMessage(Message message, UUID uuid, boolean fallbackOnDefaultLanguage, Object... arguments) {
    return getMessage(message, uuid, fallbackOnDefaultLanguage, toComponents(arguments));
  }

  public Component getMessage(Message message, UUID uuid, Object... arguments) {
    return getMessage(message, uuid, true, arguments);
  }

  public Component getMessage(Message message, PlayerProfile playerProfile, boolean fallbackOnDefaultLanguage, Component... arguments) {
    return getMessage(message, getLanguage(playerProfile), fallbackOnDefaultLanguage, arguments);
  }

  public Component getMessage(Message message, PlayerProfile playerProfile, Component... arguments) {
    return getMessage(message, playerProfile, true, arguments);
  }

  public Component getMessage(Message message, PlayerProfile playerProfile, boolean fallbackOnDefaultLanguage, Object... arguments) {
    return getMessage(message, playerProfile, fallbackOnDefaultLanguage, toComponents(arguments));
  }

  public Component getMessage(Message message, PlayerProfile playerProfile, Object... arguments) {
    return getMessage(message, playerProfile, true, arguments);
  }

  public void sendMessage(Message message, @NotNull CommandSender commandSender, boolean fallbackOnDefaultLanguage, Component... arguments) {
    commandSender.sendMessage(getMessage(message, getLanguage(commandSender), fallbackOnDefaultLanguage, arguments));
  }

  public void sendMessage(Message message, @NotNull CommandSender commandSender, Component... arguments) {
    commandSender.sendMessage(getMessage(message, commandSender, true, arguments));
  }

  public void sendMessage(Message message, @NotNull CommandSender commandSender, boolean fallbackOnDefaultLanguage, Object... arguments) {
    commandSender.sendMessage(getMessage(message, commandSender, fallbackOnDefaultLanguage, toComponents(arguments)));
  }

  public void sendMessage(Message message, @NotNull CommandSender commandSender, Object... arguments) {
    commandSender.sendMessage(getMessage(message, commandSender, true, arguments));
  }

  public void sendMessage(Message message, UUID uuid, boolean fallbackOnDefaultLanguage, Component... arguments) {
    Bukkit.getPlayer(uuid).sendMessage(getMessage(message, getLanguage(uuid), fallbackOnDefaultLanguage, arguments));
  }

  public void sendMessage(Message message, UUID uuid, Component... arguments) {
    Bukkit.getPlayer(uuid).sendMessage(getMessage(message, uuid, true, arguments));
  }

  public void sendMessage(Message message, UUID uuid, boolean fallbackOnDefaultLanguage, Object... arguments) {
    Bukkit.getPlayer(uuid).sendMessage(getMessage(message, uuid, fallbackOnDefaultLanguage, toComponents(arguments)));
  }

  public void sendMessage(Message message, UUID uuid, Object... arguments) {
    Bukkit.getPlayer(uuid).sendMessage(getMessage(message, uuid, true, arguments));
  }

  public void sendMessage(Message message, @NotNull PlayerProfile playerProfile, boolean fallbackOnDefaultLanguage, Component... arguments) {
    Bukkit.getPlayer(playerProfile.getUuid()).sendMessage(getMessage(message, getLanguage(playerProfile), fallbackOnDefaultLanguage, arguments));
  }

  public void sendMessage(Message message, @NotNull PlayerProfile playerProfile, Component... arguments) {
    Bukkit.getPlayer(playerProfile.getUuid()).sendMessage(getMessage(message, playerProfile, true, arguments));
  }

  public void sendMessage(Message message, @NotNull PlayerProfile playerProfile, boolean fallbackOnDefaultLanguage, Object... arguments) {
    Bukkit.getPlayer(playerProfile.getUuid()).sendMessage(getMessage(message, playerProfile, fallbackOnDefaultLanguage, toComponents(arguments)));
  }

  public void sendMessage(Message message, @NotNull PlayerProfile playerProfile, Object... arguments) {
    Bukkit.getPlayer(playerProfile.getUuid()).sendMessage(getMessage(message, playerProfile, true, arguments));
  }

  private Component @NotNull [] toComponents(Object... objects) {
    return Stream.of(objects).map(this::toComponent).toArray(Component[]::new);
  }

  private Component toComponent(Object object) {
    if (object instanceof Component component) {
      return component;
    }

    return Component.text(String.valueOf(object));
  }
}
