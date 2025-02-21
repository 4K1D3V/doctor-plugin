package pro.akii.ks.utils;

import org.bukkit.entity.Player;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Utility class for NMS operations (health, particles, attributes).
 */
public class NMSUtil {

    public static void applyCustomHeal(Player player, int doctorLevel) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        float healAmount = 2.0F + doctorLevel;
        nmsPlayer.setHealth(Math.min(nmsPlayer.getHealth() + healAmount, 20.0F));

        ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(
            ParticleTypes.HEART, true,
            nmsPlayer.getX(), nmsPlayer.getY() + 1.0, nmsPlayer.getZ(),
            0.5F, 0.5F, 0.5F, 0.1F, 10
        );
        nmsPlayer.connection.send(packet);
    }

    public static void applyBrokenLeg(Player player) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        AttributeInstance speed = nmsPlayer.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(0.05); // Half speed
    }

    public static void applyBleedingEffect(Player player) {
        ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(
            ParticleTypes.DAMAGE_INDICATOR, true,
            player.getX(), player.getY() + 1.0, player.getZ(),
            0.3F, 0.3F, 0.3F, 0.1F, 5
        );
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    public static void cureBrokenLeg(Player player) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        AttributeInstance speed = nmsPlayer.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(0.1); // Reset to default
    }

    public static void cureInfection(Player player) {
        // No additional NMS effect needed; handled by InjuryManager
    }

    public static void cureBleeding(Player player) {
        // No additional NMS effect needed; handled by InjuryManager
    }

    public static boolean hasBrokenLeg(Player player) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        AttributeInstance speed = nmsPlayer.getAttribute(Attributes.MOVEMENT_SPEED);
        return speed != null && speed.getBaseValue() < 0.1;
    }

    public static boolean hasInfection(Player player) {
        return ((DoctorPlugin) player.getServer().getPluginManager().getPlugin("Doctor"))
            .getInjuryManager().hasInjury(player, "infection");
    }

    public static boolean hasBleeding(Player player) {
        return ((DoctorPlugin) player.getServer().getPluginManager().getPlugin("Doctor"))
            .getInjuryManager().hasInjury(player, "bleeding");
    }

    public static boolean hasAnyInjury(Player player) {
        return hasBrokenLeg(player) || hasInfection(player) || hasBleeding(player);
    }
}