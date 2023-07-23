package me.gotitim.deathborder;

import me.gotitim.deathborder.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Deathborder extends JavaPlugin {
    public final Map<UUID, Integer> playerEffects = new HashMap<>();
    private static Deathborder instance;

    @Override
    public void onEnable() {
        instance = this;

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        saveDefaultConfig();

        for (Player player : getServer().getOnlinePlayers()) playerEffects.remove(player.getUniqueId());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public boolean isPlayerInBorder(Player player) {
        ConfigurationSection worldBorderData = getBorder(player);

        if (worldBorderData == null) return true;
        Location location = player.getLocation();

        if(worldBorderData.getInt("x", (int) Math.abs(location.getX())) <= Math.abs(location.getX())) return false;
        return !(worldBorderData.getInt("z", (int) Math.abs(location.getZ())) <= Math.abs(location.getZ()));
    }

    @Nullable
    public ConfigurationSection getBorder(Player player) {
        return getBorder(player.getWorld());
    }

    @Nullable
    public ConfigurationSection getBorder(World world) {
        ConfigurationSection borders = getConfig().getConfigurationSection("borders");
        if(borders == null) {
            Bukkit.getLogger().severe("Warning: No borders specified in config.yml!");
            Bukkit.getPluginManager().disablePlugin(this);
            return null;
        }

        return borders.getConfigurationSection(world.getName());
    }

    public static Deathborder getInstance() {
        return instance;
    }
}
