package org.esotericorganisation.template_paper_plugin.data.player;

import java.util.UUID;

public class PlayerProfile {

  private final UUID uuid;

  private String language;

  public UUID getUuid() {
    return uuid;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public PlayerProfile(UUID uuid, String language) {
    this.uuid = uuid;
    this.language = language;
  }
}
