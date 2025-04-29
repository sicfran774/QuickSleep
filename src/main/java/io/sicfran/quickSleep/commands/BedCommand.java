package io.sicfran.quickSleep.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.sicfran.quickSleep.QuickSleep;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.TextColor.color;

@SuppressWarnings("UnstableApiUsage")
public class BedCommand {

    private final QuickSleep plugin;

    public BedCommand(QuickSleep plugin){
        this.plugin = plugin;
    }

    public LiteralArgumentBuilder<CommandSourceStack> createCommand(){
        return Commands.literal("sleep")
            .then(Commands.literal("confirm") // /sleep confirm
                    .requires(ctx -> ctx.getSender().hasPermission(CommandsPermissions.CONFIRM))
                    .executes(this::sleepConfirm)
            )
            .then(Commands.literal("cancel") // /sleep cancel
                    .requires(ctx -> ctx.getSender().hasPermission(CommandsPermissions.CANCEL))
                    .executes(this::sleepCancel)
            ).then(Commands.literal("timer")
                    .then(Commands.argument("seconds", IntegerArgumentType.integer(5,15))
                            .requires(ctx -> ctx.getSender().hasPermission(CommandsPermissions.TIMER))
                            .executes(this::sleepTimer)
                    )
            );
    }

    private int sleepConfirm(@NotNull CommandContext<CommandSourceStack> ctx){
        CommandSender sender = ctx.getSource().getSender();
        Entity executor = ctx.getSource().getExecutor();



        if(plugin.getSleepingPlayers().isEmpty()){
            sender.sendPlainMessage("You must be in a bed to use this command.");

        } else if(!(executor instanceof Player)){
            sender.sendPlainMessage("You can only use this command as a player.");
        } else {
            final Component message = text()
                    .append(text(sender.getName(), color(0x00FFFF)))
                    .append(text(" has begun to sleep! Type "))
                    .append(text("/sleep cancel", color(0xE63E44)))
                    .append(text(" to keep it from becoming daytime."))
                    .build();
            plugin.getServer().broadcast(message);

            this.startSleep(executor.getWorld());
        }
        return Command.SINGLE_SUCCESS;
    }

    public int sleepCancel(@NotNull CommandContext<CommandSourceStack> ctx){
        CommandSender sender = ctx.getSource().getSender();
        Entity executor = ctx.getSource().getExecutor();

        if(plugin.isSleepTimerStarted()) {
            plugin.setSleepTimerStarted(false);
            // Force all sleeping players to wake up
            for (UUID playerID : plugin.getSleepingPlayers()) {
                Objects.requireNonNull(plugin.getServer().getPlayer(playerID)).wakeup(false);
            }
            plugin.clearSleepers();

            // Expose the canceller
            final Component message = text()
                    .append(text(sender.getName(), color(0x00FFFF)))
                    .append(text(" has cancelled the sleep. Boooooo!!! "))
                    .build();
            plugin.getServer().broadcast(message);
        } else if(executor instanceof Player player && plugin.getSleepingPlayers().contains(player.getUniqueId())){
            sender.sendPlainMessage("You haven't confirmed the sleep.");
        } else {
            sender.sendPlainMessage("No one is sleeping...");
        }
        return Command.SINGLE_SUCCESS;
    }

    public int sleepTimer(@NotNull CommandContext<CommandSourceStack> ctx){
        CommandSender sender = ctx.getSource().getSender();

        int seconds = ctx.getArgument("seconds", int.class);
        plugin.setTimerLength(seconds);
        sender.sendPlainMessage("Sleep timer changed to " + seconds + " seconds.");

        return Command.SINGLE_SUCCESS;
    }

    private void startSleep(World world) {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        final int[] seconds = {plugin.getTimerLength()};
        plugin.setSleepTimerStarted(true);

        scheduler.runTaskTimer(plugin, task -> {
            if (seconds[0] == 0) {
                executeSleepResets(world);
                plugin.setSleepTimerStarted(false);
                task.cancel();
            } else if (!plugin.isSleepTimerStarted()){
                task.cancel();
            } else {
                plugin.getServer().broadcast(text(seconds[0] + "..."));
                seconds[0]--;
            }
        }, 0, 20);
    }

    private void executeSleepResets(@NotNull World world){
        world.setTime(0);
        world.setStorm(false);

        plugin.getServer().broadcast(text("Good morning!"));
    }
}
