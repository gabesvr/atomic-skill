package com.gabesvr.atomicskill.listeners;

import com.gabesvr.atomicskill.AtomicSkillPlugin;
import com.gabesvr.atomicskill.ability.AtomicManager;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
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
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Apenas mão principal para evitar acionar duas vezes
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
            atomicManager.cast(player);
        }
    }
}
