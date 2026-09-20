package com.gabesvr.atomicskill;

import com.gabesvr.atomicskill.ability.AtomicManager;
import com.gabesvr.atomicskill.commands.AtomicCommand;
import com.gabesvr.atomicskill.listeners.AtomicItemListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class AtomicSkillPlugin extends JavaPlugin {

    private static AtomicSkillPlugin instance;
    private AtomicManager atomicManager;

    @Override
    public void onEnable() {
        instance = this;

        // Salvar configuração padrão
        saveDefaultConfig();

        // Inicializar Gerenciador da Habilidade
        this.atomicManager = new AtomicManager(this);

        // Registrar Comandos e Eventos
        if (getCommand("atomic") != null) {
            AtomicCommand cmd = new AtomicCommand(this, atomicManager);
            getCommand("atomic").setExecutor(cmd);
            getCommand("atomic").setTabCompleter(cmd);
        }

        getServer().getPluginManager().registerEvents(new AtomicItemListener(this, atomicManager), this);

        getLogger().info("==========================================");
        getLogger().info(" AtomicSkill ativado com sucesso!");
        getLogger().info(" Criado para Purpur / Paper por gabesvr");
        getLogger().info(" I... AM... ATOMIC!");
        getLogger().info("==========================================");
    }

    @Override
    public void onDisable() {
        if (atomicManager != null) {
            atomicManager.cleanup();
        }
        getLogger().info("AtomicSkill desativado.");
    }

    public static AtomicSkillPlugin getInstance() {
        return instance;
    }

    public AtomicManager getAtomicManager() {
        return atomicManager;
    }
}
