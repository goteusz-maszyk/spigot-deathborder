package me.gotitim.deathborder.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.gotitim.deathborder.Deathborder.getInstance;

public class PlayerListener implements Listener {
    private static final Map<UUID, Integer> playerEffectLevels = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if(!getInstance().isPlayerInBorder(player)) return;

        player.sendTitle("You exited safe world area!", "Be careful.", 5, 40, 5);

        String[] effectsToApply = getInstance().getBorder(player).getStringList("effects").toArray(new String[0]);
        Integer effectsInterval = getInstance().getBorder(player).getInt("effectsInterval");

        Bukkit.getScheduler().scheduleSyncRepeatingTask(getInstance(), () -> {

        }, effectsInterval*20, effectsInterval*20);
    }
}
