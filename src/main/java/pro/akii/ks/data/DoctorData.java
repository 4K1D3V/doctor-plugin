package pro.akii.ks.data;

import java.util.UUID;

/**
 * Data model for a Doctor, storing level, XP, and cooldowns.
 */
public class DoctorData {

    private final UUID uuid;
    private int level;
    private int xp;
    private long lastHealTime;
    private long lastDiagnoseTime;

    public DoctorData(UUID uuid) {
        this.uuid = uuid;
        this.level = 1;
        this.xp = 0;
        this.lastHealTime = 0;
        this.lastDiagnoseTime = 0;
    }

    public DoctorData(UUID uuid, int level, int xp, long lastHealTime, long lastDiagnoseTime) {
        this.uuid = uuid;
        this.level = level;
        this.xp = xp;
        this.lastHealTime = lastHealTime;
        this.lastDiagnoseTime = lastDiagnoseTime;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    public long getLastHealTime() {
        return lastHealTime;
    }

    public long getLastDiagnoseTime() {
        return lastDiagnoseTime;
    }

    public void addXp(int amount) {
        this.xp += amount;
        int xpForNextLevel = level * 100; // Max level 5 enforced in DoctorManager
        while (xp >= xpForNextLevel && level < ConfigUtil.getMaxLevel()) {
            level++;
            xp -= xpForNextLevel;
            xpForNextLevel = level * 100;
        }
    }

    public void setLastHealTime(long time) {
        this.lastHealTime = time;
    }

    public void setLastDiagnoseTime(long time) {
        this.lastDiagnoseTime = time;
    }
}