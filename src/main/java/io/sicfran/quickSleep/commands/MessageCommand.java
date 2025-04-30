package io.sicfran.quickSleep.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.sicfran.quickSleep.QuickSleep;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class MessageCommand {

    private final QuickSleep plugin;

    public MessageCommand(QuickSleep plugin){
        this.plugin = plugin;
    }

    public int changeMessage(@NotNull CommandContext<CommandSourceStack> ctx, String messageType) {
        CommandSender sender = ctx.getSource().getSender();
        Entity executor = ctx.getSource().getExecutor();
        String message = ctx.getArgument("message", String.class);

        Player targetPlayer;

        try {
            final PlayerSelectorArgumentResolver resolver = ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
            targetPlayer = resolver.resolve(ctx.getSource()).getFirst();
        } catch (IllegalArgumentException e) {
            if (executor instanceof Player) {
                targetPlayer = (Player) executor;
            } else {
                sender.sendPlainMessage("You must be a player or specify one.");
                return Command.SINGLE_SUCCESS;
            }
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }

        switch (messageType){
            case "Wake up":
                plugin.getPlayerData().savePlayerWakeupMsg(targetPlayer.getUniqueId(), message);
                break;
            case "Cancel":
                plugin.getPlayerData().savePlayerCancelMsg(targetPlayer.getUniqueId(), message);
                break;
        }

        sender.sendPlainMessage(messageType + " message successfully changed!");

        return Command.SINGLE_SUCCESS;
    }
}

