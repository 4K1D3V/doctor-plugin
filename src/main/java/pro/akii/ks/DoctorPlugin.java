package pro.akii.ks;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import pro.akii.ks.commands.DoctorCommand;
import pro.akii.ks.listeners.DoctorListener;
import pro.akii.ks.managers.DoctorManager;
import pro.akii.ks.managers.EmergencyManager;
import pro.akii.ks.managers.InjuryManager;
import pro.akii.ks.utils.ConfigUtil;

/**
 * Main plugin class for the Doctor plugin.
 * Initializes managers, commands, events, and recipes.
 */
public class DoctorPlugin extends JavaPlugin {

    private DoctorManager doctorManager;
    private InjuryManager injuryManager;
    private EmergencyManager emergencyManager;

    @Override
    public void onEnable() {
        // Load configuration
        saveDefaultConfig();
        ConfigUtil.initialize(getConfig());

        // Initialize managers
        this.doctorManager = new DoctorManager(this);
        this.injuryManager = new InjuryManager(this);
        this.emergencyManager = new EmergencyManager(this);

        // Register command and listener
        DoctorCommand doctorCommand = new DoctorCommand(this);
        getCommand("doctor").setExecutor(doctorCommand);
        getCommand("doctor").setTabCompleter(doctorCommand);
        getServer().getPluginManager().registerEvents(new DoctorListener(this), this);

        // Register crafting recipes
        registerRecipes();

        getLogger().info("Doctor plugin v1.0 enabled!");
    }

    @Override
    public void onDisable() {
        doctorManager.saveData();
        injuryManager.shutdown();
        emergencyManager.shutdown();
        getLogger().info("Doctor plugin v1.0 disabled.");
    }

    public DoctorManager getDoctorManager() {
        return doctorManager;
    }

    public InjuryManager getInjuryManager() {
        return injuryManager;
    }

    public EmergencyManager getEmergencyManager() {
        return emergencyManager;
    }

    private void registerRecipes() {
        registerRecipe(ConfigUtil.getStethoscopeItem(), "stethoscope");
        registerRecipe(ConfigUtil.getBandageItem(), "bandage");
        registerRecipe(ConfigUtil.getAntidoteItem(), "antidote");
        registerRecipe(ConfigUtil.getSutureKitItem(), "suture-kit");
        registerRecipe(ConfigUtil.getSplintItem(), "splint");
    }

    private void registerRecipe(ItemStack result, String key) {
        try {
            ShapedRecipe recipe = new ShapedRecipe(new org.bukkit.NamespacedKey(this, key), result);
            recipe.shape(
                ConfigUtil.getConfig().getString("items." + key + ".recipe[0]"),
                ConfigUtil.getConfig().getString("items." + key + ".recipe[1]"),
                ConfigUtil.getConfig().getString("items." + key + ".recipe[2]")
            );
            for (String ingredient : ConfigUtil.getConfig().getConfigurationSection("items." + key + ".ingredients").getKeys(false)) {
                recipe.setIngredient(ingredient.charAt(0), org.bukkit.Material.valueOf(
                    ConfigUtil.getConfig().getString("items." + key + ".ingredients." + ingredient)));
            }
            Bukkit.addRecipe(recipe);
        } catch (Exception e) {
            getLogger().severe("Failed to register recipe for " + key + ": " + e.getMessage());
        }
    }
}