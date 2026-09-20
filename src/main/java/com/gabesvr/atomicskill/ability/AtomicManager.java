package com.gabesvr.atomicskill.ability;

import com.gabesvr.atomicskill.AtomicSkillPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.*;

public class AtomicManager {

    private final AtomicSkillPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Set<UUID> currentlyCasting = new HashSet<>();

    public AtomicManager(AtomicSkillPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isCurrentlyCasting(UUID uuid) {
        return currentlyCasting.contains(uuid);
    }

    public long getRemainingCooldown(UUID uuid) {
        if (!cooldowns.containsKey(uuid)) return 0;
        long expireTime = cooldowns.get(uuid);
        long diff = (expireTime - System.currentTimeMillis()) / 1000;
        return Math.max(0, diff);
    }

    public void setCooldown(UUID uuid, int seconds) {
        cooldowns.put(uuid, System.currentTimeMillis() + (seconds * 1000L));
    }

    public boolean cast(Player caster) {
        UUID uuid = caster.getUniqueId();

        if (currentlyCasting.contains(uuid)) {
            caster.sendMessage(Component.text("Você já está canalizando o I Am Atomic!", NamedTextColor.RED));
            return false;
        }

        long remaining = getRemainingCooldown(uuid);
        if (remaining > 0 && !caster.hasPermission("atomicskill.admin")) {
            caster.sendMessage(Component.text("Aguarde " + remaining + "s para usar o I Am Atomic novamente!", NamedTextColor.RED));
            return false;
        }

        int cooldownSeconds = plugin.getConfig().getInt("skill.cooldown-seconds", 45);
        int maxRadius = plugin.getConfig().getInt("skill.radius", 28);
        double damage = plugin.getConfig().getDouble("skill.damage", 500.0);
        boolean vaporizeBlocks = plugin.getConfig().getBoolean("skill.vaporize-blocks", true);
        boolean protectBedrock = plugin.getConfig().getBoolean("skill.protect-bedrock", true);
        boolean regenerateTerrain = plugin.getConfig().getBoolean("skill.regenerate-terrain", false);
        int regenDelay = plugin.getConfig().getInt("skill.regeneration-delay-seconds", 30);
        String soundKey = plugin.getConfig().getString("skill.sound.key", "atomic:skill.atomic");
        float soundVolume = (float) plugin.getConfig().getDouble("skill.sound.volume", 60.0);
        int darkRadius = plugin.getConfig().getInt("skill.cinematics.darkness-nearby-radius", 60);

        currentlyCasting.add(uuid);
        setCooldown(uuid, cooldownSeconds);

        Location center = caster.getLocation().clone();
        World world = caster.getWorld();

        // Partículas Violeta Neon características da Eminência nas Sombras
        Particle.DustOptions purpleDust = new Particle.DustOptions(Color.fromRGB(185, 10, 255), 2.5f);
        Particle.DustOptions darkPurpleDust = new Particle.DustOptions(Color.fromRGB(110, 0, 180), 2.0f);
        Particle.DustOptions coreWhiteDust = new Particle.DustOptions(Color.fromRGB(245, 230, 255), 1.8f);

        // 1. Toca o áudio original do anime sincronizado (17.7s)
        try {
            world.playSound(center, soundKey, SoundCategory.MASTER, soundVolume, 1.0f);
        } catch (Exception ignored) {
            world.playSound(center, Sound.ITEM_TRIDENT_THUNDER, 5.0f, 0.5f);
        }

        final Map<Location, BlockData> savedBlocks = new LinkedHashMap<>();

        new BukkitRunnable() {
            int tick = 0;
            int currentSphereRadius = 2;

            @Override
            public void run() {
                if (!caster.isOnline()) {
                    currentlyCasting.remove(uuid);
                    cancel();
                    return;
                }

                tick++;

                // ====================================================================
                // FASE 1: CANALIZAÇÃO (ticks 0 a 174 | 0.0s a 8.7s)
                // O monólogo lendário com a áurea se contraindo
                // ====================================================================
                if (tick < 175) {
                    // Mantém o jogador levitando imóvel no ar
                    caster.setVelocity(new Vector(0, 0.035, 0));
                    caster.setFallDistance(0);

                    // Espiral dupla de energia roxa subindo e convergindo
                    double angle = tick * 0.35;
                    double radius = Math.max(0.6, 2.5 - (tick * 0.01));
                    double x1 = Math.cos(angle) * radius;
                    double z1 = Math.sin(angle) * radius;
                    double y = (tick % 35) * 0.08;

                    world.spawnParticle(Particle.REDSTONE, center.clone().add(x1, y, z1), 3, 0.05, 0.05, 0.05, purpleDust);
                    world.spawnParticle(Particle.REDSTONE, center.clone().add(-x1, y, -z1), 3, 0.05, 0.05, 0.05, darkPurpleDust);
                    world.spawnParticle(Particle.REVERSE_PORTAL, center.clone().add(0, 1.0, 0), 5, 0.8, 0.8, 0.8, 0.08);

                    // Escuridão em volta para ambientação cinematográfica
                    for (Player nearby : world.getNearbyPlayers(center, darkRadius)) {
                        nearby.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 45, 1, false, false));
                    }

                    // Títulos sincronizados com as falas do áudio
                    if (tick == 20) {
                        showAllNearbyTitle(world, center, darkRadius,
                                Component.text("I...", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD),
                                Component.empty(), 200, 1500, 300);
                        world.playSound(center, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 2.5f, 0.5f);
                    } else if (tick == 85) {
                        showAllNearbyTitle(world, center, darkRadius,
                                Component.text("am...", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD),
                                Component.empty(), 200, 1500, 300);
                        world.playSound(center, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 3.0f, 0.7f);
                    }
                }

                // ====================================================================
                // FASE 2: A PAUSA DRAMÁTICA (ticks 175 a 186 | 8.7s a 9.3s)
                // Silêncio absoluto antes do drop nuclear
                // ====================================================================
                else if (tick >= 175 && tick < 186) {
                    if (tick == 175) {
                        showAllNearbyTitle(world, center, darkRadius,
                                Component.empty(),
                                Component.text("...", NamedTextColor.DARK_GRAY), 0, 500, 100);
                    }
                }

                // ====================================================================
                // FASE 3: "ATOMIC" -> O FEIXE DE LUZ E A ESFERA NUCLEAR (tick 186+)
                // ====================================================================
                else if (tick >= 186) {
                    if (tick == 186) {
                        showAllNearbyTitle(world, center, darkRadius + 20,
                                Component.text("I AM ATOMIC", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD),
                                Component.text("The ultimate secret technique", NamedTextColor.DARK_PURPLE, TextDecoration.ITALIC),
                                0, 3000, 1000);

                        for (Player nearby : world.getNearbyPlayers(center, darkRadius + 20)) {
                            nearby.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 2, false, false));
                        }

                        // Sons brutais de explosão e trovão sincronizados
                        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 15.0f, 0.5f);
                        world.playSound(center, Sound.ITEM_TRIDENT_THUNDER, 15.0f, 0.5f);
                        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 8.0f, 0.6f);

                        // FEIXE DE LUZ GIGANTE SUBINDO AO CÉU
                        for (double y = center.getY(); y < Math.min(320, center.getY() + 120); y += 1.5) {
                            Location beamLoc = new Location(world, center.getX(), y, center.getZ());
                            world.spawnParticle(Particle.FLASH, beamLoc, 1, 0, 0, 0, 0);
                            world.spawnParticle(Particle.END_ROD, beamLoc, 3, 0.3, 0.5, 0.3, 0.05);
                            world.spawnParticle(Particle.REDSTONE, beamLoc, 6, 0.4, 0.8, 0.4, purpleDust);
                            world.spawnParticle(Particle.REDSTONE, beamLoc, 3, 0.2, 0.8, 0.2, coreWhiteDust);
                        }
                    }

                    // Expansão veloz da Esfera Roxa (3 blocos de raio por tick)
                    if (currentSphereRadius <= maxRadius) {
                        renderAndVaporizeSphere(world, center, currentSphereRadius, caster, damage,
                                vaporizeBlocks, protectBedrock, savedBlocks, purpleDust);
                        currentSphereRadius += 3;
                    }

                    // Conclusão no final do áudio (tick 350 / ~17.5s)
                    if (tick >= 350) {
                        currentlyCasting.remove(uuid);
                        caster.sendMessage(Component.text("I Am Atomic executado com maestria!", NamedTextColor.LIGHT_PURPLE));

                        if (regenerateTerrain && !savedBlocks.isEmpty()) {
                            scheduleTerrainRegen(world, savedBlocks, regenDelay);
                        }

                        cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    private void showAllNearbyTitle(World world, Location center, double radius, Component title, Component subtitle, int in, int stay, int out) {
        Title t = Title.title(title, subtitle, Title.Times.times(Duration.ofMillis(in), Duration.ofMillis(stay), Duration.ofMillis(out)));
        for (Player p : world.getNearbyPlayers(center, radius)) {
            p.showTitle(t);
        }
    }

    private void renderAndVaporizeSphere(World world, Location center, int radius, Player caster, double damage,
                                         boolean vaporizeBlocks, boolean protectBedrock, Map<Location, BlockData> savedBlocks,
                                         Particle.DustOptions dust) {
        int rSquared = radius * radius;
        int prevRSquared = (radius - 3) * (radius - 3);

        int step = Math.max(12, 36 - radius);
        for (int pitch = -90; pitch <= 90; pitch += step) {
            double rPitch = Math.toRadians(pitch);
            double y = radius * Math.sin(rPitch);
            double sliceRadius = radius * Math.cos(rPitch);

            for (int yaw = 0; yaw < 360; yaw += step) {
                double rYaw = Math.toRadians(yaw);
                double x = sliceRadius * Math.cos(rYaw);
                double z = sliceRadius * Math.sin(rYaw);

                Location pLoc = center.clone().add(x, y, z);
                world.spawnParticle(Particle.REDSTONE, pLoc, 1, 0, 0, 0, 0, dust);
                if (Math.random() < 0.12) {
                    world.spawnParticle(Particle.DRAGON_BREATH, pLoc, 1, 0, 0, 0, 0.02);
                }
            }
        }

        if (vaporizeBlocks) {
            int minX = center.getBlockX() - radius;
            int maxX = center.getBlockX() + radius;
            int minY = Math.max(world.getMinHeight(), center.getBlockY() - radius);
            int maxY = Math.min(world.getMaxHeight(), center.getBlockY() + radius);
            int minZ = center.getBlockZ() - radius;
            int maxZ = center.getBlockZ() + radius;

            for (int bx = minX; bx <= maxX; bx++) {
                for (int bz = minZ; bz <= maxZ; bz++) {
                    for (int by = minY; by <= maxY; by++) {
                        Location bLoc = new Location(world, bx, by, bz);
                        double distSq = center.distanceSquared(bLoc);

                        if (distSq <= rSquared && distSq > prevRSquared) {
                            Block block = world.getBlockAt(bx, by, bz);
                            Material type = block.getType();

                            if (type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR) {
                                continue;
                            }
                            if (protectBedrock && (type == Material.BEDROCK || type == Material.BARRIER)) {
                                continue;
                            }

                            if (savedBlocks != null) {
                                savedBlocks.put(block.getLocation(), block.getBlockData().clone());
                            }

                            block.setType(Material.AIR, false);
                        }
                    }
                }
            }
        }

        for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
            if (entity.equals(caster)) continue;

            if (entity.getLocation().distanceSquared(center) <= rSquared) {
                if (entity instanceof LivingEntity target) {
                    world.spawnParticle(Particle.REDSTONE, target.getLocation(), 25, 0.4, 0.8, 0.4, dust);
                    world.spawnParticle(Particle.DRAGON_BREATH, target.getLocation(), 15, 0.3, 0.5, 0.3, 0.05);

                    target.damage(damage, caster);
                } else if (entity instanceof Item) {
                    entity.remove();
                }
            }
        }
    }

    private void scheduleTerrainRegen(World world, Map<Location, BlockData> savedBlocks, int delaySeconds) {
        new BukkitRunnable() {
            @Override
            public void run() {
                List<Map.Entry<Location, BlockData>> entries = new ArrayList<>(savedBlocks.entrySet());
                new BukkitRunnable() {
                    int index = 0;
                    final int batchSize = 120;

                    @Override
                    public void run() {
                        for (int i = 0; i < batchSize && index < entries.size(); i++, index++) {
                            Map.Entry<Location, BlockData> entry = entries.get(index);
                            Block block = entry.getKey().getBlock();
                            block.setBlockData(entry.getValue(), false);
                            if (Math.random() < 0.04) {
                                world.spawnParticle(Particle.REVERSE_PORTAL, entry.getKey(), 2, 0.2, 0.2, 0.2, 0.05);
                            }
                        }
                        if (index >= entries.size()) {
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }
        }.runTaskLater(plugin, delaySeconds * 20L);
    }

    public ItemStack createAtomicBlade() {
        String matName = plugin.getConfig().getString("item.material", "NETHERITE_SWORD");
        Material mat = Material.matchMaterial(matName);
        if (mat == null) mat = Material.NETHERITE_SWORD;

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String rawName = plugin.getConfig().getString("item.name", "&#9400D3&lAtomic Blade");
            meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(rawName));

            List<String> rawLore = plugin.getConfig().getStringList("item.lore");
            int cooldown = plugin.getConfig().getInt("skill.cooldown-seconds", 45);
            List<Component> lore = new ArrayList<>();
            for (String line : rawLore) {
                line = line.replace("%cooldown%", String.valueOf(cooldown));
                lore.add(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
            }
            meta.lore(lore);

            if (plugin.getConfig().getBoolean("item.unbreakable", true)) {
                meta.setUnbreakable(true);
            }

            Enchantment sharpness = Enchantment.getByKey(NamespacedKey.minecraft("sharpness"));
            if (sharpness != null) {
                meta.addEnchant(sharpness, 5, true);
            }
            Enchantment unbreaking = Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"));
            if (unbreaking != null) {
                meta.addEnchant(unbreaking, 3, true);
            }
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);

            item.setItemMeta(meta);
        }
        return item;
    }

    public void cleanup() {
        currentlyCasting.clear();
        cooldowns.clear();
    }
}
