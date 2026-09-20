package com.gabesvr.atomicskill.commands;

import com.gabesvr.atomicskill.AtomicSkillPlugin;
import com.gabesvr.atomicskill.ability.AtomicManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AtomicCommand implements CommandExecutor, TabCompleter {

    private final AtomicSkillPlugin plugin;
    private final AtomicManager atomicManager;

    public AtomicCommand(AtomicSkillPlugin plugin, AtomicManager atomicManager) {
        this.plugin = plugin;
        this.atomicManager = atomicManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("cast")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Apenas jogadores podem invocar o I Am Atomic!", NamedTextColor.RED));
                return true;
            }
            if (!player.hasPermission("atomicskill.use")) {
                player.sendMessage(Component.text("Você não tem permissão para usar esta técnica lendária!", NamedTextColor.RED));
                return true;
            }
            atomicManager.cast(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("sword") || args[0].equalsIgnoreCase("blade") || args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("atomicskill.admin")) {
                sender.sendMessage(Component.text("Você não tem permissão para pegar a Atomic Blade.", NamedTextColor.RED));
                return true;
            }

            Player target;
            if (args.length > 1) {
                target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(Component.text("Jogador não encontrado!", NamedTextColor.RED));
                    return true;
                }
            } else if (sender instanceof Player p) {
                target = p;
            } else {
                sender.sendMessage(Component.text("Especifique um jogador: /atomic sword <player>", NamedTextColor.RED));
                return true;
            }

            ItemStack blade = atomicManager.createAtomicBlade();
            target.getInventory().addItem(blade);
            target.sendMessage(Component.text("Você recebeu a lendária ", NamedTextColor.LIGHT_PURPLE)
                    .append(Component.text("Atomic Blade", NamedTextColor.DARK_PURPLE)));
            if (!sender.equals(target)) {
                sender.sendMessage(Component.text("Atomic Blade entregue para " + target.getName(), NamedTextColor.GREEN));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("atomicskill.admin")) {
                sender.sendMessage(Component.text("Sem permissão!", NamedTextColor.RED));
                return true;
            }
            plugin.reloadConfig();
            sender.sendMessage(Component.text("Configuração do AtomicSkill recarregada!", NamedTextColor.GREEN));
            return true;
        }

        sender.sendMessage(Component.text("Uso: /atomic [cast|sword|reload]", NamedTextColor.YELLOW));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(Arrays.asList("cast", "sword", "reload"));
            return list.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("sword") || args[0].equalsIgnoreCase("give"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())).toList();
        }
        return List.of();
    }
}
