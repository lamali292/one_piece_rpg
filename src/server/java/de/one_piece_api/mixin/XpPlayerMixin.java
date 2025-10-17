package de.one_piece_api.mixin;

import de.one_piece_api.config.XpTimeConfig;
import de.one_piece_api.mixin_interface.IXpPlayer;
import de.one_piece_api.util.OnePieceCategory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.SkillsAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Mixin to add XP and Skill Points to ServerPlayerEntity
 */
@Mixin(ServerPlayerEntity.class)
public abstract class XpPlayerMixin implements IXpPlayer {


    @Unique
    private XpTimeConfig onepiece$xpTimeConfig = XpTimeConfig.DEFAULT;

    @Unique
    private int onepiece$ticksSinceLastXp = 0;

    @Unique
    private long onepiece$lastActivityTick = 0;

    // AFK threshold: 5 minutes without activity
    @Unique
    private static final int AFK_THRESHOLD_TICKS = 5 * 60 * 20;


    @Unique
    private ServerPlayerEntity onepiece$getXpSelf() {
        return (ServerPlayerEntity) (Object) this;
    }


    @Override
    public XpTimeConfig onepiece$getXpTimeConfig() {
        return onepiece$xpTimeConfig;
    }

    @Override
    public void onepiece$setXpTimeConfig(XpTimeConfig config) {
        this.onepiece$xpTimeConfig = config != null ? config : XpTimeConfig.DEFAULT;
    }

    @Override
    public int onepiece$getTicksSinceLastXp() {
        return onepiece$ticksSinceLastXp;
    }

    @Override
    public void onepiece$setTicksSinceLastXp(int ticks) {
        this.onepiece$ticksSinceLastXp = ticks;
    }

    @Override
    public void onepiece$resetXpTimer() {
        this.onepiece$ticksSinceLastXp = 0;
    }

    @Override
    public long onepiece$getLastActivityTick() {
        return onepiece$lastActivityTick;
    }

    @Override
    public void onepiece$updateActivity() {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        this.onepiece$lastActivityTick = player.getWorld().getTime();
    }

    @Override
    public boolean onepiece$isAfk() {
        return false;
        /*
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        long currentTick = player.getWorld().getTime();
        return (currentTick - onepiece$lastActivityTick) > AFK_THRESHOLD_TICKS;*/
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeXpData(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound xpData = new NbtCompound();
        xpData.putInt("ticksSinceLastXp", onepiece$ticksSinceLastXp);
        xpData.put("xpTimeConfig", onepiece$xpTimeConfig.toNbt());
        nbt.put("onepiece_xp_data", xpData);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readXpData(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("onepiece_xp_data")) {
            NbtCompound xpData = nbt.getCompound("onepiece_xp_data");
            onepiece$ticksSinceLastXp = xpData.getInt("ticksSinceLastXp");

            if (xpData.contains("xpTimeConfig")) {
                onepiece$xpTimeConfig = XpTimeConfig.fromNbt(xpData.getCompound("xpTimeConfig"));
            }
        }
    }


    @Override
    public void onepiece$addXp(int amount) {
        var player = onepiece$getXpSelf();
        SkillsAPI.getCategory(OnePieceCategory.ID)
                .flatMap(Category::getExperience)
                .ifPresent(experience -> experience.addTotal(player, amount));
    }

    @Override
    public void onepiece$setXp(int amount) {
        var player = onepiece$getXpSelf();
        SkillsAPI.getCategory(OnePieceCategory.ID)
                .flatMap(Category::getExperience)
                .ifPresent(experience -> experience.setTotal(player, amount));
    }

    @Override
    public int onepiece$getTotalSkillPoints() {
        var player = onepiece$getXpSelf();
        return SkillsAPI.getCategory(OnePieceCategory.ID)
                .map(c -> c.getPointsTotal(player))
                .orElse(0);
    }

    @Override
    public Map<Identifier, Integer> onepiece$getSkillPointsWithSources() {
        var player = onepiece$getXpSelf();
        Map<Identifier, Integer> map = new HashMap<>();
        SkillsAPI.getCategory(OnePieceCategory.ID)
                .ifPresent(c ->
                        c.streamPointsSources(player)
                                .forEach(id ->
                                        map.put(id, c.getPoints(player, id))
                                )
                );
        return map;
    }

    @Override
    public int onepiece$getSkillPoints(Identifier source) {
        var player = onepiece$getXpSelf();
        return SkillsAPI.getCategory(OnePieceCategory.ID)
                .map(c -> c.getPoints(player, source))
                .orElse(0);
    }


    @Override
    public int onepiece$getXp() {
        var player = onepiece$getXpSelf();
        return SkillsAPI.getCategory(OnePieceCategory.ID)
                .flatMap(Category::getExperience)
                .map(experience -> experience.getTotal(player)).orElse(0);
    }

    @Override
    public void onepiece$setSkillPoints(int i, Identifier source) {
        var player = onepiece$getXpSelf();
        SkillsAPI.getCategory(OnePieceCategory.ID)
                .ifPresent(c -> c.setPoints(player, source, i));
    }

    @Override
    public void onepiece$addSkillPoints(int i, Identifier source) {
        var player = onepiece$getXpSelf();
        SkillsAPI.getCategory(OnePieceCategory.ID)
                .ifPresent(c -> c.addPoints(player, source, i));
    }
}