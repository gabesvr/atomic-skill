package com.gabesvr.atomicskill.listeners;

import com.gabesvr.atomicskill.AtomicSkillPlugin;
import com.gabesvr.atomicskill.ability.AtomicManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AtomicItemListener implements Listener {

    private final AtomicSkillPlugin plugin;
    private final AtomicManager atomicManager;

    public AtomicItemListener(AtomicSkillPlugin plugin, AtomicManager atomicManager) {
        this.plugin = plugin;
        this.atomicManager = atomicManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("resource-pack.auto-send-on-join", true)) {
            return;
        }

        String url = plugin.getConfig().getString("resource-pack.url");
        if (url == null || url.isBlank()) {
            return;
        }

        String hash = plugin.getConfig().getString("resource-pack.hash", "");
        boolean required = plugin.getConfig().getBoolean("resource-pack.required", false);
        String rawPrompt = plugin.getConfig().getString("resource-pack.prompt-message", "&5&lAtomic Skill &8» &7Baixe os efeitos sonoros originais do anime (&dI AM ATOMIC&7)!");
        Component prompt = LegacyComponentSerializer.legacyAmpersand().deserialize(rawPrompt);

        // Aguarda 30 ticks após o login do jogador para garantir que o cliente já carregou a tela
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player player = event.getPlayer();
            if (player.isOnline()) {
                if (hash != null && !hash.isBlank()) {
                    player.setResourcePack(url, hash, required, prompt);
                } else {
                    player.setResourcePack(url);
                }
            }
        }, 30L);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) {
            return;
        }

        String plainName = PlainTextComponentSerializer.plainText().serialize(meta.displayName());
        if (plainName.contains("Atomic Blade")) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            if (!player.hasPermission("atomicskill.use")) {
                player.sendMessage(Component.text("Você não é digno de invocar o I Am Atomic! Apenas o Dono pode liberar este poder.", NamedTextColor.RED));
                return;
            }
            atomicManager.cast(player);
        }
    }
}
