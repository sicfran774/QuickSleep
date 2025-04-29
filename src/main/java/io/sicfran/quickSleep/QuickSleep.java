
package io.sicfran.quickSleep;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.TextColor.color;

public final class QuickSleep extends JavaPlugin implements Listener {

    private final Set<UUID> sleepingPlayers;
    private boolean sleepTimerStarted;
    private int timerLength;

    public QuickSleep(){
        super();
        this.sleepingPlayers = new HashSet<>();
        this.sleepTimerStarted = false;
        this.timerLength = 5;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        // Commands
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("sleep")
                .then(Commands.literal("confirm") // /sleep confirm
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            Entity executor = ctx.getSource().getExecutor();

                            if(getSleepingPlayers().isEmpty()){
                                sender.sendPlainMessage("You must be in a bed to use this command.");
                                return Command.SINGLE_SUCCESS;
                            }
                            if(!(executor instanceof Player)){
                                sender.sendPlainMessage("You can only use this command as a player.");
                                return Command.SINGLE_SUCCESS;
                            }

                            final Component message = text()
                                    .append(text(sender.getName(), color(0x00FFFF)))
                                    .append(text(" has begun to sleep! Type "))
                                    .append(text("/sleep cancel", color(0xE63E44)))
                                    .append(text(" to keep it from becoming daytime."))
                                    .build();
                            getServer().broadcast(message);

                            startSleep(executor.getWorld());
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("cancel") // /sleep cancel
                        .executes(ctx ->{
                            CommandSender sender = ctx.getSource().getSender();
                            Entity executor = ctx.getSource().getExecutor();

                            if(sleepTimerStarted) {
                                sleepTimerStarted = false;
                                // Force all sleeping players to wake up
                                for (UUID playerID : getSleepingPlayers()) {
                                    Objects.requireNonNull(getServer().getPlayer(playerID)).wakeup(false);
                                }
                                clearSleepers();

                                // Expose the canceller
                                final Component message = text()
                                        .append(text(sender.getName(), color(0x00FFFF)))
                                        .append(text(" has cancelled the sleep. Boooooo!!! "))
                                        .build();
                                this.getServer().broadcast(message);
                            } else if(executor instanceof Player player && getSleepingPlayers().contains(player.getUniqueId())){
                                sender.sendPlainMessage("You haven't confirmed the sleep.");
                            } else {
                                sender.sendPlainMessage("No one is sleeping...");
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                ).then(Commands.literal("timer")
                        .then(Commands.argument("seconds", IntegerArgumentType.integer(5,15))
                                .executes(ctx -> {
                                    CommandSender sender = ctx.getSource().getSender();

                                    int seconds = ctx.getArgument("seconds", int.class);
                                    timerLength = seconds;
                                    sender.sendPlainMessage("Sleep timer changed to " + seconds + " seconds.");

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                );

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(command.build());
        });
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        int playerAmount = getServer().getOnlinePlayers().size();
        if (playerAmount > 1 && event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.OK) {

            Player player = event.getPlayer();
            boolean result = addSleeper(player.getUniqueId());

            final Component message = text()
                    .append(text("Type "))
                    .append(text("/sleep confirm", color(0xE63E44)))
                    .append(text(" to turn it daytime."))
                    .build();
            player.sendMessage(message);
        }
    }

    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event){
        Player player = event.getPlayer();
        long time = player.getWorld().getTime();
        boolean result = removeSleeper(player.getUniqueId());

        // If the player pressed "Leave Bed" (it's still night so sleep wasn't successful)
        // and wasn't interrupted by the command
        if((time >= 12000 && time < 24000) && sleepTimerStarted && getSleepingPlayers().isEmpty()){
            sleepTimerStarted = false;
            final Component message = text()
                    .append(text(player.getName(), color(0x00FFFF)))
                    .append(text(" got up. Nevermind!"))
                    .build();
            this.getServer().broadcast(message);
        }
    }

    private void startSleep(World world) {
        BukkitScheduler scheduler = getServer().getScheduler();

        final int[] seconds = {timerLength};
        sleepTimerStarted = true;

        scheduler.runTaskTimer(this, task -> {
            if (seconds[0] == 0) {
                executeSleepResets(world);
                sleepTimerStarted = false;
                task.cancel();
            } else if (!sleepTimerStarted){
                task.cancel();
            } else {
                getServer().broadcast(text(seconds[0] + "..."));
                seconds[0]--;
            }
        }, 0, 20);
    }

    private void executeSleepResets(World world){
        world.setTime(0);
        world.setStorm(false);

        getServer().broadcast(text("Good morning!"));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public boolean addSleeper(UUID player) {
        return sleepingPlayers.add(player);
    }

    public boolean removeSleeper(UUID player) {
        return sleepingPlayers.remove(player);
    }

    public Set<UUID> getSleepingPlayers() {
        return sleepingPlayers;
    }

    public void clearSleepers(){
        sleepingPlayers.clear();
    }

    public boolean isSleepTimerStarted() {
        return sleepTimerStarted;
    }

    public void setSleepTimerStarted(boolean sleepTimerStarted) {
        this.sleepTimerStarted = sleepTimerStarted;
    }

    public int getTimerLength() {
        return timerLength;
    }

    public void setTimerLength(int timerLength) {
        this.timerLength = timerLength;
    }
}
