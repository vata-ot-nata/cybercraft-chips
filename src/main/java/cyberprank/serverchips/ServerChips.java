package cyberprank.serverchips;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerChips extends JavaPlugin implements Listener {

    public static final String CHIP_NAME = "§8Chip";
    public static final String STATE_NONE = "none";
    public static final String STATE_EDITING = "§8Chip редактирование";

    private final ArrayList<String> chips = new ArrayList<>();
    private static final ConcurrentHashMap<Player, String> playersChipUse = new ConcurrentHashMap<>();

    public static void setChipUse(Player p, String value) {
        playersChipUse.put(p, value);
    }

    public static String getChipUse(Player p) {
        return playersChipUse.getOrDefault(p, STATE_NONE);
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);

        File config = new File(getDataFolder(), "config.yml");
        if (!config.exists()) {
            getConfig().options().copyDefaults(true);
            saveConfig();
        }

        chips.add(CHIP_NAME);
    }

    @Override
    public void onDisable() {
        saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        switch (command.getName().toLowerCase()) {
            case "createserver": {
                World w = p.getWorld();
                p.getLocation().getBlock().setType(Material.BARRIER);

                ItemFrame frame = (ItemFrame) w.spawnEntity(
                        w.getBlockAt(p.getLocation()).getLocation().add(new Vector(0, 1, 0)),
                        EntityType.ITEM_FRAME);

                ItemStack server = new ItemStack(Material.STONE);
                ItemMeta meta = server.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("Server Block");
                    server.setItemMeta(meta);
                }
                frame.setItem(server);
                break;
            }

            case "editdata": {
                if (CHIP_NAME.equals(getChipUse(p))) {
                    setChipUse(p, STATE_EDITING);
                    p.sendMessage("Введите данные для сохранения.");
                } else {
                    p.sendMessage("Сначала вставьте чип в сервер (ПКМ по рамке).");
                }
                break;
            }

            case "checkdata": {
                if (CHIP_NAME.equals(getChipUse(p))) {
                    try {
                        ItemStack item = p.getInventory().getItemInMainHand();
                        ItemMeta meta = item.getItemMeta();
                        if (meta == null || !meta.hasLore()) throw new Exception();
                        String id = meta.getLore().get(0).replace("§8", "").trim();
                        p.sendMessage("Данные: " + getConfig().getString("data." + id, "<пусто>"));
                    } catch (Exception ignored) {
                        p.sendMessage("Не удалось прочитать данные чипа.");
                    }
                }
                break;
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEntityEvent event) {
        try {
            Player player = event.getPlayer();
            Entity entity = event.getRightClicked();

            if (!(entity instanceof ItemFrame)) return;

            ItemStack chip = player.getInventory().getItemInMainHand();
            if (chip.getType() == Material.AIR || !chip.hasItemMeta()) return;

            ItemMeta meta = chip.getItemMeta();
            if (meta == null) return;

            String name = meta.getDisplayName();
            if (!chips.contains(name)) return;

            String current = getChipUse(player);

            if (STATE_NONE.equals(current)) {
                setChipUse(player, name);
                player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_OPEN, 100, 1);
                player.sendMessage("Вы вставили чип в сервер.");

                player.spigot().sendMessage(new ComponentBuilder("§a[Редактировать данные]")
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/editdata"))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("*Клик*")))
                        .create());
                player.spigot().sendMessage(new ComponentBuilder("§a[Посмотреть данные]")
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/checkdata"))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("*Клик*")))
                        .create());
            } else if (current.equals(name)) {
                setChipUse(player, STATE_NONE);
                player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 100, 1);
                player.sendMessage("Вы вытащили чип из сервера.");
            } else {
                player.sendMessage("Вы уже используете какой-то чип!");
            }
        } catch (Exception ignored) {}
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        try {
            Player player = event.getPlayer();
            if (!STATE_EDITING.equals(getChipUse(player))) return;

            event.setCancelled(true);
            setChipUse(player, STATE_NONE);

            String value = event.getMessage();

            ItemStack item = player.getInventory().getItemInMainHand();
            ItemMeta meta = item.getItemMeta();
            if (meta == null || !CHIP_NAME.equals(meta.getDisplayName())) {
                player.sendMessage("В руке должен быть чип.");
                return;
            }

            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            if (lore == null) lore = new ArrayList<>();

            int id;
            String first = lore.isEmpty() ? "" : lore.get(0).replace("§8", "").trim();

            if (first.isEmpty()) {
                id = getConfig().getInt("next-id", 0);
                getConfig().set("next-id", id + 1);
                if (lore.isEmpty()) lore.add("§8" + id);
                else lore.set(0, "§8" + id);
            } else {
                try {
                    id = Integer.parseInt(first);
                } catch (NumberFormatException e) {
                    id = getConfig().getInt("next-id", 0);
                    getConfig().set("next-id", id + 1);
                    lore.set(0, "§8" + id);
                }
            }

            getConfig().set("data." + id, value);
            saveConfig();

            meta.setLore(lore);
            item.setItemMeta(meta);

            player.sendMessage("Успешно записано значение: " + value);
        } catch (Exception ignored) {}
    }

    @EventHandler
    public void onPlayerClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String state = getChipUse(player);
        if (CHIP_NAME.equals(state) || STATE_EDITING.equals(state)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerItemHoldEvent(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        String state = getChipUse(player);
        if (CHIP_NAME.equals(state) || STATE_EDITING.equals(state)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        String state = getChipUse(player);
        if (CHIP_NAME.equals(state) || STATE_EDITING.equals(state)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        setChipUse(event.getPlayer(), STATE_NONE);
    }
}
