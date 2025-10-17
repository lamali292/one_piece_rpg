package de.one_piece_api.config;

import net.minecraft.nbt.NbtCompound;

/**
 * Stores per-player XP configuration for time-based gains
 */
public class XpTimeConfig {
    private int xpAmount;
    private int intervalMinutes;

    // Default: 50 XP every 60 minutes
    public static final XpTimeConfig DEFAULT = new XpTimeConfig(50, 60);

    public XpTimeConfig(int xpAmount, int intervalMinutes) {
        this.xpAmount = Math.max(0, xpAmount);
        this.intervalMinutes = Math.max(1, intervalMinutes);
    }

    public int getXpAmount() {
        return xpAmount;
    }

    public int getIntervalMinutes() {
        return intervalMinutes;
    }

    public int getIntervalTicks() {
        return intervalMinutes * 60 * 20; // minutes * seconds * ticks
    }

    public void setXpAmount(int amount) {
        this.xpAmount = Math.max(0, amount);
    }

    public void setIntervalMinutes(int minutes) {
        this.intervalMinutes = Math.max(1, minutes);
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("xpAmount", xpAmount);
        nbt.putInt("intervalMinutes", intervalMinutes);
        return nbt;
    }

    public static XpTimeConfig fromNbt(NbtCompound nbt) {
        if (nbt == null || !nbt.contains("xpAmount")) {
            return DEFAULT;
        }
        return new XpTimeConfig(
                nbt.getInt("xpAmount"),
                nbt.getInt("intervalMinutes")
        );
    }

    @Override
    public String toString() {
        return xpAmount + " XP every " + intervalMinutes + " minutes";
    }
}