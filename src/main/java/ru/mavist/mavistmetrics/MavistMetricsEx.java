package ru.mavist.mavistmetrics;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class MavistMetricsEx extends JavaPlugin implements Listener {

    private final ConcurrentHashMap<UUID, Long> playTime = new ConcurrentHashMap<>();
    private final HashMap<UUID, Integer> blocksBroken = new HashMap<>();
    private final HashMap<UUID, Integer> blocksPlaced = new HashMap<>();
    private final HashMap<UUID, Integer> deaths = new HashMap<>();
    private final HashMap<UUID, String> playerIPs = new HashMap<>();
    private final HashMap<UUID, Integer> playerInventoryOpen = new HashMap<>();
    private final HashMap<UUID, Integer> playerGamemodeChanged = new HashMap<>();
    private final HashMap<UUID, Integer> playerTeleport = new HashMap<>();
    private final HashMap<UUID, Integer> playerSneak = new HashMap<>();
    private final HashMap<UUID, Integer> playerSprint = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        String playerIP = event.getPlayer().getAddress().getAddress().getHostAddress();

        playTime.put(uuid, System.currentTimeMillis());
        playerIPs.put(uuid, playerIP);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        String playerName = event.getPlayer().getName();

        // Проверка наличия записи в playTime
        if (!playTime.containsKey(uuid)) {
            return;
        }

        long timePlayed = (System.currentTimeMillis() - playTime.get(uuid)) / 1000;
        String playerIP = playerIPs.getOrDefault(uuid, "Unknown");

        sendPlayerData(uuid, playerName, timePlayed,
                blocksBroken.getOrDefault(uuid, 0),
                blocksPlaced.getOrDefault(uuid, 0),
                deaths.getOrDefault(uuid, 0),
                playerIP,
                playerSprint.getOrDefault(uuid, 0),
                playerTeleport.getOrDefault(uuid, 0),
                playerSneak.getOrDefault(uuid, 0),
                playerGamemodeChanged.getOrDefault(uuid, 0),
                playerInventoryOpen.getOrDefault(uuid, 0));

        // Очищаем данные после отправки
        playTime.remove(uuid);
        blocksBroken.remove(uuid);
        blocksPlaced.remove(uuid);
        deaths.remove(uuid);
        playerIPs.remove(uuid);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        blocksBroken.put(uuid, blocksBroken.getOrDefault(uuid, 0) + 1);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        blocksPlaced.put(uuid, blocksPlaced.getOrDefault(uuid, 0) + 1);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        UUID uuid = event.getEntity().getUniqueId();
        deaths.put(uuid, deaths.getOrDefault(uuid, 0) + 1);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        String playerName = event.getPlayer().getName();
        String inventoryType = event.getInventory().getType().name();
        playerInventoryOpen.put(uuid, playerInventoryOpen.getOrDefault(uuid, 0) + 1);

    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        String playerName = event.getPlayer().getName();
        String gameMode = event.getNewGameMode().name();
        playerGamemodeChanged.put(uuid, playerGamemodeChanged.getOrDefault(uuid, 0) + 1);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        String playerName = event.getPlayer().getName();
        String from = event.getFrom().toString();
        String to = event.getTo().toString();
        playerTeleport.put(uuid, playerTeleport.getOrDefault(uuid, 0) + 1);
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        String playerName = event.getPlayer().getName();
        String action = event.isSneaking() ? "SneakOn" : "SneakOff";
        playerSneak.put(uuid, playerSneak.getOrDefault(uuid, 0) + 1);
    }

    @EventHandler
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        String playerName = event.getPlayer().getName();
        String action = event.isSprinting() ? "SprintOn" : "SprintOff";
        playerSprint.put(uuid, playerSprint.getOrDefault(uuid, 0) + 1);
    }


    private void sendPlayerData(UUID uuid, String playerName, long timePlayed, int blocksBroken, int blocksPlaced, int deaths, String playerIP, int sprint, int teleport, int sneak, int gamemodechanged, int inventoryopen) {
        try {
            URL url = new URL("http://api.mavist.ru/MavistMetricsMinecraft/metr.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = String.format(
                    "{\"uuid\":\"%s\",\"playerName\":\"%s\",\"timePlayed\":%d,\"blocksBroken\":%d,\"blocksPlaced\":%d,\"deaths\":%d,\"lastIP\":\"%s\",\"sprint\":\"%d\",\"teleport\":\"%d\",\"sneak\":\"%d\",\"gamemodechanged\":\"%d\",\"invop\":\"%d\"}",
                    uuid.toString(), playerName, timePlayed, blocksBroken, blocksPlaced, deaths, playerIP, sprint, teleport, sneak, gamemodechanged, inventoryopen
            );

            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            getLogger().warning(conn.getResponseMessage());
            if (code != 200 || code != 301) {
                getLogger().warning("Failed to send player data for " + uuid + ". Response code: " + code);
                getLogger().warning("Trying to send by https");
                sendPlayerDataHttps(uuid, playerName, timePlayed, blocksBroken, blocksPlaced, deaths, playerIP, sprint, teleport, sneak, gamemodechanged, inventoryopen);
            }

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPlayerDataHttps(UUID uuid, String playerName, long timePlayed, int blocksBroken, int blocksPlaced, int deaths, String playerIP, int sprint, int teleport, int sneak, int gamemodechanged, int inventoryopen) {
        try {
            URL url = new URL("https://api.mavist.ru/MavistMetricsMinecraft/metr.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = String.format(
                    "{\"uuid\":\"%s\",\"playerName\":\"%s\",\"timePlayed\":%d,\"blocksBroken\":%d,\"blocksPlaced\":%d,\"deaths\":%d,\"lastIP\":\"%s\",\"sprint\":\"%d\",\"teleport\":\"%d\",\"sneak\":\"%d\",\"gamemodechanged\":\"%d\",\"invop\":\"%d\"}",
                    uuid.toString(), playerName, timePlayed, blocksBroken, blocksPlaced, deaths, playerIP, sprint, teleport, sneak, gamemodechanged, inventoryopen
            );

            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            getLogger().warning(conn.getResponseMessage());
            if (code != 200 || code != 301) {
                getLogger().warning("Failed to send player data for " + uuid + ". Response code: " + code);
            }

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}