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

import java.util.*;

import static me.gotitim.deathborder.Deathborder.getInstance;

public class PlayerListener implements Listener {
    private static final Map<UUID, Integer> effectTasks = new HashMap<>();
    private static final Map<UUID, List<PotionEffectType>> lastAddedEffects = new HashMap<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        getInstance().playerEffects.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!effectTasks.containsKey(event.getPlayer().getUniqueId())) return;
        Bukkit.getScheduler().cancelTask(effectTasks.get(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        final Map<UUID, Integer> playerEffects = getInstance().playerEffects;

        ConfigurationSection border = getInstance().getBorder(player);
        if(border == null) return;

        if(getInstance().isPlayerInBorder(player)) {
            if(!effectTasks.containsKey(player.getUniqueId())) return;
            lastAddedEffects.getOrDefault(player.getUniqueId(), new ArrayList<>()).forEach(player::removePotionEffect);
            int taskID = effectTasks.get(player.getUniqueId());
            effectTasks.remove(player.getUniqueId());
            Bukkit.getScheduler().cancelTask(taskID);
            return;
        }

        if(!playerEffects.containsKey(player.getUniqueId())) {
            String title = getInstance().getConfig().getString("texts.exit-border-title");
            String subtitle = getInstance().getConfig().getString("texts.exit-border-subtitle");
            if(!(title == null && subtitle == null)) {
                player.sendTitle(title, subtitle, 5, 40, 5);
            }
            playerEffects.put(player.getUniqueId(), 0);
        }

        if(effectTasks.containsKey(player.getUniqueId())) return;
        playerEffects.put(player.getUniqueId(), 0);

        String[] effectsSequence = border.getStringList("effects").toArray(new String[0]);
        int effectsInterval = border.getInt("effectsInterval");

        effectTasks.put(player.getUniqueId(), Bukkit.getScheduler().scheduleSyncRepeatingTask(getInstance(), () -> {
            int sequenceLevel = getInstance().playerEffects.get(player.getUniqueId());
            if(effectsSequence.length <= sequenceLevel) return;
            String[] effectsToApply = effectsSequence[sequenceLevel].split("-");
            List<PotionEffectType> appliedEffects = new ArrayList<>();
            for (String effectData : effectsToApply) {
                if(effectData.equalsIgnoreCase("KILL")) {
                    player.setHealth(0);
                } else {
                    String[] effect = effectData.split("_");
                    try {
                        PotionEffectType type = PotionEffectType.getByName(effect[0]);
                        player.addPotionEffect(new PotionEffect(Objects.requireNonNull(type), Integer.parseInt(effect[2]) * 20, Integer.parseInt(effect[1]) - 1, false, true, true));
                        appliedEffects.add(type);
                    } catch (Exception e) {
                        Bukkit.getLogger().severe("Can't find effect with name " + effect[0] + ". See https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html ");
                    }
                }
            }
            lastAddedEffects.put(player.getUniqueId(), appliedEffects);

            getInstance().playerEffects.put(player.getUniqueId(), sequenceLevel+1);
            String[] scheduledEffects;
            try {
                scheduledEffects = effectsSequence[sequenceLevel + 1].split("-");
            } catch (ArrayIndexOutOfBoundsException e) {
                return;
            }
            for (String effectData : scheduledEffects) {
                String messageFormat = getInstance().getConfig().getString("texts.effect-announcement");
                if(messageFormat == null) {
                    Bukkit.getLogger().warning("No effect announcement specified in config.yml!");
                    return;
                }
                if(effectData.equalsIgnoreCase("KILL")) {
                    player.sendMessage(messageFormat
                            .replace("%seconds%", String.valueOf(effectsInterval))
                            .replace("%effect%", "Death")
                    );
                } else {
                    String[] effect = effectData.split("_");
                    player.sendMessage(messageFormat
                            .replace("%seconds%", String.valueOf(effectsInterval))
                            .replace("%effect%", effect[0] + " " + effect[1])
                    );
                }
            }
        }, 0, effectsInterval * 20L));
    }
}
