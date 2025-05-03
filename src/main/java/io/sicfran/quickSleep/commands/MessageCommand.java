package io.sicfran.quickSleep.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.sicfran.quickSleep.QuickSleep;
import io.sicfran.quickSleep.data.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.TextColor.color;

@SuppressWarnings("UnstableApiUsage")
public class MessageCommand {

    private final QuickSleep plugin;

    public MessageCommand(QuickSleep plugin){
        this.plugin = plugin;
    }

    private Player verifyPlayer(@NotNull CommandContext<CommandSourceStack> ctx, CommandSender sender){
        Entity executor = ctx.getSource().getExecutor();

        try {
            final PlayerSelectorArgumentResolver resolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
            return resolver.resolve(ctx.getSource()).getFirst();
        } catch (IllegalArgumentException e) {
            if (executor instanceof Player) {
                return (Player) executor;
            } else {
                sender.sendPlainMessage("You must be a player or specify one.");
            }
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public int changeMessage(@NotNull CommandContext<CommandSourceStack> ctx, String messageType) {
        CommandSender sender = ctx.getSource().getSender();
        String message = ctx.getArgument("message", String.class);

        Player targetPlayer = verifyPlayer(ctx, sender);

        if (targetPlayer != null){
            switch (messageType) {
                case "Wake up" -> plugin.getPlayerData().savePlayerWakeupMsg(targetPlayer.getUniqueId(), message);
                case "Cancel"  -> plugin.getPlayerData().savePlayerCancelMsg(targetPlayer.getUniqueId(), message);
                default        -> {
                    sender.sendPlainMessage("Invalid message type: " + messageType);
                    return Command.SINGLE_SUCCESS;
                }
            }

            sender.sendPlainMessage(sender.getName() + ": " + messageType + " message successfully changed!");
        }

        return Command.SINGLE_SUCCESS;
    }

    public int viewMessage(@NotNull CommandContext<CommandSourceStack> ctx, String messageType){
        CommandSender sender = ctx.getSource().getSender();

        Player targetPlayer = verifyPlayer(ctx, sender);

        if (targetPlayer != null){
            PlayerDataManager.PlayerData playerData = plugin.getPlayerData().loadPlayerData(targetPlayer.getUniqueId());

            String message;
            String suggestedCommand = "/sleep message ";
            switch (messageType) {
                case "Wake up" -> {
                    message = playerData.wakeupMessage();
                    suggestedCommand += "wakeup ";
                }
                case "Cancel" -> {
                    message = playerData.cancelMessage();
                    suggestedCommand += "cancel ";
                }
                default -> message = "(message type not found)";
            }

            sender.sendRichMessage(messageType + " message: " + message);
            Component editMessage = text("[Edit message]", color(0x54fff9)).decorate(TextDecoration.UNDERLINED)
                    .clickEvent(ClickEvent.suggestCommand(suggestedCommand + message))
                    .hoverEvent(HoverEvent.showText(
                                    text("Click to edit", color(0x54fff9))
                            )
                    );

            sender.sendMessage(editMessage);
        }

        return Command.SINGLE_SUCCESS;
    }
}

