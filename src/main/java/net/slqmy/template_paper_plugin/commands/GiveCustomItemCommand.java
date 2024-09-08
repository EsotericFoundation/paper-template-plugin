package net.slqmy.template_paper_plugin.commands;

import java.util.stream.Stream;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentException;
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentInfo;
import dev.jorel.commandapi.arguments.CustomArgument.CustomArgumentInfoParser;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import net.slqmy.template_paper_plugin.TemplatePaperPlugin;
import net.slqmy.template_paper_plugin.custom_item.CustomItem;

public class GiveCustomItemCommand extends CommandAPICommand {

  public GiveCustomItemCommand(TemplatePaperPlugin plugin) {
    super("give-custom-item");

    String customItemArgumentNodeName = "custom-item";

    String[] customItemNames = Stream.of(CustomItem.values()).map((customItem) -> customItem.name()).toArray(String[]::new);

    Argument<CustomItem> customItemArgument = new CustomArgument<CustomItem, String>(
        new MultiLiteralArgument(customItemArgumentNodeName, customItemNames),
        new CustomArgumentInfoParser<>() {
          @Override
          public CustomItem apply(CustomArgumentInfo<String> info) throws CustomArgumentException {
            return CustomItem.valueOf(info.currentInput());
          }
        });

    executesPlayer((info) -> {
      CustomItem item = (CustomItem) info.args().get(customItemArgumentNodeName);

      plugin.getCustomItemManager().giveCustomItem(item, info.sender());
    });

    withArguments(customItemArgument);

    register(plugin);
  }
}