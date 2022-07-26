package me.gotitim.deathborder.listener;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static me.gotitim.deathborder.Deathborder.getInstance;
import static me.gotitim.deathborder.Deathborder.playerEffects;

public class PlayerListener implements Listener {
    private static final Map<UUID, Integer> effectTasks = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerEffects.put(event.getPlayer().getUniqueId(), -1);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().cancelTask(effectTasks.get(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        ConfigurationSection border = getInstance().getBorder(player);
        if(border == null) return;

        if(getInstance().isPlayerInBorder(player)) {
            playerEffects.put(player.getUniqueId(), -1);
            if(!effectTasks.containsKey(player.getUniqueId())) return;
            int taskID = effectTasks.get(player.getUniqueId());
            effectTasks.remove(player.getUniqueId());
            Bukkit.getScheduler().cancelTask(taskID);
            return;
        }

        if(playerEffects.get(player.getUniqueId()) == -1) {
            player.sendTitle("You exited safe world area!", "Be careful.", 5, 40, 5);
        }

        if(effectTasks.containsKey(player.getUniqueId())) return;
        playerEffects.put(player.getUniqueId(), 0);

        String[] effectsSequence = border.getStringList("effects").toArray(new String[0]);
        int effectsInterval = border.getInt("effectsInterval");

        effectTasks.put(player.getUniqueId(), Bukkit.getScheduler().scheduleSyncRepeatingTask(getInstance(), () -> {
            int sequenceLevel = playerEffects.get(player.getUniqueId());
            String[] effectsToApply = effectsSequence[sequenceLevel].split("-");
            Bukkit.getLogger().info(String.valueOf(sequenceLevel));
            for (String effectData : effectsToApply) {
                Bukkit.getLogger().info(effectData);
                if(effectData.equalsIgnoreCase("KILL")) {
                    player.setHealth(0);
                } else {
                    String[] effect = effectData.split("_");
                    try {
                        PotionEffectType type = PotionEffectType.getByName(effect[0]);
                        player.addPotionEffect(new PotionEffect(Objects.requireNonNull(type), Integer.parseInt(effect[2]) * 20, Integer.parseInt(effect[1]) - 1, false, true, true));
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("Can't find effect with name " + effect[0] + ". See https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html ");
                    }
                }
            }

            playerEffects.put(player.getUniqueId(), sequenceLevel+1);
        }, 0, effectsInterval * 20L));
    }
}
