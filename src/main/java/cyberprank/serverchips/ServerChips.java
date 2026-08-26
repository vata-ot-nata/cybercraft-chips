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
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerChips extends JavaPlugin implements Listener {

    ArrayList<String> chips = new ArrayList<>();

    private static final ConcurrentHashMap<Player, String> playersChipUse = new ConcurrentHashMap<>();

    public static void setChipUse(Player p, String value) {
        playersChipUse.put(p, value);
    }

    public static String getChipUse(Player p) {
        if (playersChipUse.containsKey(p)) {
            return playersChipUse.get(p);
        }
        return "none";
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        File config = new File(getDataFolder() + File.separator + "config.yml");
        if(!config.exists()) {
            getConfig().options().copyDefaults(true);
        }
        chips.add("Чип пустышка");
    }

    @Override
    public void onDisable() {
        this.saveConfig();
    }

    public void stopUsing(Player player) {
        setChipUse(player, "none");
    }

    // EVENTS
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {

            Player p = (Player) sender;

            switch (command.getName()) {
                case ("createserver"):
                    World w = p.getWorld();

                    p.getLocation().getBlock().setType(Material.BARRIER);

                    ItemFrame frame = (ItemFrame) w.spawnEntity(w.getBlockAt(p.getLocation()).getLocation().add(new Vector(0, 1, 0)), EntityType.ITEM_FRAME);
                    ItemStack server = new ItemStack(Material.STONE);
                    ItemMeta meta = server.getItemMeta();
                    meta.setDisplayName("Server Block");
                    server.setItemMeta(meta);
                    frame.setItem(server);
                    break;

                case ("editdata"):
                    if (Objects.equals(getChipUse(p), "Чип пустышка"))  {
                        setChipUse(p, "Чип пустышка редактирование");
                        p.sendMessage("Введите данные для сохранения.");
                    }
                    break;

                case ("checkdata"):
                    if (Objects.equals(getChipUse(p), "Чип пустышка"))  {
                        try {
                            setChipUse(p, "none");
                            String id = p.getInventory().getItemInMainHand().getItemMeta().getLore().get(0).replace("§8", "");
                            p.sendMessage("Данные: " + getConfig().getString(id));
                        } catch (Exception ignored) {}
                    }
                    break;
            }
        }

        return super.onCommand(sender, command, label, args);
    }


    @EventHandler
    public void onPlayerUse(PlayerInteractEntityEvent event) {
        try {

            Player player = event.getPlayer();
            Entity entity = event.getRightClicked();
            ItemStack chip = player.getInventory().getItemInMainHand();
            String name = chip.getItemMeta().getDisplayName();

            if (entity instanceof ItemFrame) {

                if (name.equals("§8Chip")) {
                    if (chips.contains(name)) {
                        if (Objects.equals(getChipUse(player), "none")) {
                            setChipUse(player, name);

                            player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_OPEN, 100, 1);
                            player.sendMessage("Вы вставили чип в сервер.");

                            player.spigot().sendMessage(new ComponentBuilder("§a[Редактировать данные]")
                                    .event(new net.md_5.bungee.api.chat.ClickEvent(ClickEvent.Action.RUN_COMMAND, "/editdata "))
                                    .event(new net.md_5.bungee.api.chat.HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("*Клик*")))
                                    .create());
                            player.spigot().sendMessage(new ComponentBuilder("§a[Посмотреть данные]")
                                    .event(new net.md_5.bungee.api.chat.ClickEvent(ClickEvent.Action.RUN_COMMAND, "/checkdata "))
                                    .event(new net.md_5.bungee.api.chat.HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("*Клик*")))
                                    .create());

                        } else player.sendMessage("Вы уже используете какой-то чип!");

                    } else if (chip.getType() != Material.AIR) {
                        setChipUse(player, "none");
                        player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE, 100, 1);
                        player.sendMessage("Вы вытащили чип из сервера.");
                    }
                }
            }

        } catch (Exception ignored) {}
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        try {
            Player player = event.getPlayer();

            if (Objects.equals(getChipUse(player), "Чип пустышка редактирование")) {
                event.setCancelled(true);
                setChipUse(player, "none");

                String value = event.getMessage();

                ItemStack item = player.getInventory().getItemInMainHand();
                ItemMeta meta = item.getItemMeta();

                if (meta.getDisplayName().equals("§8Chip")) {
                    List<String> lore;

                    int id = this.getConfig().getInt(".ids");

                    if (meta.hasLore()) {
                        lore = meta.getLore();
                        if (Objects.equals(lore.get(0), "")) {
                            this.getConfig().set(".ids", id + 1);
                        }
                    } else {
                        lore = new ArrayList<>();
                        lore.add("");
                        this.getConfig().set(".ids", id + 1);
                    }

                    this.getConfig().set(String.valueOf(id), value);

                    lore.set(0, "§8" + id);
                    meta.setLore(lore);
                    item.setItemMeta(meta);

                    player.sendMessage("Успешно записано значение: " + value);
                }
            }
        } catch (Exception ignored) {}
    }

    @EventHandler
    public void onPlayerClick(InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();

        if (Objects.equals(getChipUse(player), "Чип пустышка")
                || Objects.equals(getChipUse(player), "Чип пустышка редактирование")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerItemHoldEvent(PlayerItemHeldEvent event){

        Player player = event.getPlayer();

        if (Objects.equals(getChipUse(player), "Чип пустышка")
                || Objects.equals(getChipUse(player), "Чип пустышка редактирование")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (Objects.equals(getChipUse(player), "Чип пустышка")
                || Objects.equals(getChipUse(player), "Чип пустышка редактирование")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){

        Player player = event.getPlayer();

        setChipUse(player, "none");
    }
}

