package io.sicfran.quickSleep.listeners;

import io.sicfran.quickSleep.QuickSleep;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.TextColor.color;

public class OnPlayerBedEnter implements Listener {

    private final QuickSleep plugin;

    public OnPlayerBedEnter(QuickSleep plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        int playerAmount = plugin.getServer().getOnlinePlayers().size();
        if ((playerAmount > 1 || plugin.getConfig().getBoolean("quick_sleep.enable_when_alone", false)) && event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.OK) {

            Player player = event.getPlayer();
            plugin.addSleeper(player.getUniqueId());

            final Component message = text()
                    .append(text("[Click here]", color(0x54ff62)).decorate(TextDecoration.BOLD).decorate(TextDecoration.UNDERLINED)
                            .clickEvent(ClickEvent.runCommand("/sleep confirm"))
                            .hoverEvent(HoverEvent.showText(
                                    text("Start night skip countdown (" +
                                            plugin.getConfig().getInt("quick_sleep.timer", 10) +
                                            " seconds)", color(0x54ff62))
                                    )
                            )
                    )
                    .append(text(" or type "))
                    .append(text("/sleep confirm", color(0x54ff62)))
                    .append(text(" to turn it daytime."))
                    .build();
            player.sendMessage(message);
        }
    }
}
