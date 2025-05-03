package io.sicfran.quickSleep.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.sicfran.quickSleep.QuickSleep;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Statistic;
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
public class SleepCommand {

    private final QuickSleep plugin;

    public SleepCommand(QuickSleep plugin){
        this.plugin = plugin;
    }

    protected int sleepConfirm(@NotNull CommandContext<CommandSourceStack> ctx){
        CommandSender sender = ctx.getSource().getSender();
        Entity executor = ctx.getSource().getExecutor();

        if(plugin.getSleepingPlayers().isEmpty()){
            sender.sendPlainMessage("You must be in a bed to use this command.");

        } else if(!(executor instanceof Player)){
            sender.sendPlainMessage("You can only use this command as a player.");
        } else {
            plugin.getServer().broadcast(text()
                    .append(text(sender.getName(), color(0x00FFFF)))
                    .append(text(" has begun to sleep!")).build()
            );
            final Component message = text()
                    .append(text("You have "))
                    .append(text(plugin.getConfig().getInt("quick_sleep.timer", 10), color(0x32a852)))
                    .append(text(" seconds to "))
                    .append(text("[click here]", color(0xff424f)).decorate(TextDecoration.UNDERLINED)
                            .clickEvent(ClickEvent.runCommand("/sleep cancel"))
                            .hoverEvent(HoverEvent.showText(
                                            text("Cancel night skip countdown", color(0xff424f))
                                    )
                            )
                    )
                    .append(text(" or type "))
                    .append(text("/sleep cancel", color(0xE63E44)))
                    .append(text(" to keep it from becoming daytime."))
                    .build();
            plugin.getServer().broadcast(message);

            this.startSleep(executor.getWorld(), executor.getUniqueId());
        }
        return Command.SINGLE_SUCCESS;
    }

    protected int sleepCancel(@NotNull CommandContext<CommandSourceStack> ctx){
        CommandSender sender = ctx.getSource().getSender();
        Entity executor = ctx.getSource().getExecutor();

        if(plugin.isSleepTimerStarted()) {
            plugin.setSleepTimerStarted(false);
            // Force all sleeping players to wake up
            for (UUID playerID : plugin.getSleepingPlayers()) {
                Objects.requireNonNull(plugin.getServer().getPlayer(playerID)).wakeup(false);
            }
            plugin.clearSleepers();


            String savedCancelMessage = QuickSleep.DEFAULT_CANCEL;
            // Get player data cancel message
            if(executor instanceof Player player){
                savedCancelMessage = plugin.getPlayerData().loadPlayerData(player.getUniqueId()).cancelMessage();
            }
            Component cancelMessage = MiniMessage.miniMessage().deserialize(savedCancelMessage);

            // Expose the canceller
            final Component message = text()
                    .append(text(sender.getName(), color(0x00FFFF)))
                    .append(text(" has cancelled the sleep. "))
                    .append(cancelMessage)
                    .build();
            plugin.getServer().broadcast(message);
        } else if(executor instanceof Player player && plugin.getSleepingPlayers().contains(player.getUniqueId())){
            sender.sendPlainMessage("You haven't confirmed the sleep skip.");
        } else {
            sender.sendPlainMessage("No one has started the sleep skip countdown.");
        }
        return Command.SINGLE_SUCCESS;
    }

    protected int sleepTimer(@NotNull CommandContext<CommandSourceStack> ctx){
        CommandSender sender = ctx.getSource().getSender();

        int seconds = ctx.getArgument("seconds", int.class);
        plugin.getConfig().set("quick_sleep.timer", seconds);
        plugin.saveConfig();
        sender.sendPlainMessage("Sleep timer changed to " + seconds + " seconds.");

        return Command.SINGLE_SUCCESS;
    }

    protected int getSleepTimer(@NotNull CommandContext<CommandSourceStack> ctx){
        CommandSender sender = ctx.getSource().getSender();
        sender.sendPlainMessage("Timer set to " + plugin.getConfig().getInt("quick_sleep.timer", 10) + " seconds.");
        return Command.SINGLE_SUCCESS;
    }

    private void startSleep(World world, UUID playerId) {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        final int[] seconds = {plugin.getConfig().getInt("quick_sleep.timer", 10)};
        plugin.setSleepTimerStarted(true);

        scheduler.runTaskTimer(plugin, task -> {
            if (seconds[0] == 0) {
                executeSleepResets(world, playerId);
                plugin.setSleepTimerStarted(false);
                task.cancel();
            } else if (!plugin.isSleepTimerStarted()){
                task.cancel();
            } else if (seconds[0] <= 3){
                plugin.getServer().broadcast(text(seconds[0] + "..."));
            }
            seconds[0]--;
        }, 0, 20);
    }

    private void executeSleepResets(@NotNull World world, UUID playerId){
        world.setTime(0);
        if(plugin.getConfig().getBoolean("quick_sleep.reset_rain", true)) world.setStorm(false);
        if(plugin.getConfig().getBoolean("quick_sleep.reset_thunderstorm", true)) world.setThundering(false);

        // Reset phantom spawn time of rest (based on config)
        if (plugin.getConfig().getBoolean("quick_sleep.reset_phantom_time", true)){
            for(Player p : plugin.getServer().getOnlinePlayers()){
                p.setStatistic(Statistic.TIME_SINCE_REST, 0);
            }
        }

        // Load player's wake up message
        String playerMessage = plugin.getPlayerData().loadPlayerData(playerId).wakeupMessage();
        Component message = MiniMessage.miniMessage().deserialize(playerMessage);
        plugin.getServer().broadcast(message);
    }
}
