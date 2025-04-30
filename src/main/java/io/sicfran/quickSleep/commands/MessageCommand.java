package io.sicfran.quickSleep.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
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
            targetPlayer = ctx.getArgument("player", Player.class);
        } catch (IllegalArgumentException e) {
            if (executor instanceof Player) {
                targetPlayer = (Player) executor;
            } else {
                sender.sendPlainMessage("You must be a player or specify one.");
                return Command.SINGLE_SUCCESS;
            }
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

