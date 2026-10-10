package com.lxb.lxbsessential.heal;

import com.lxb.lxbsessential.LxbsEssential;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.FoodStats;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** A full heal: negative effects, fire, health (incl. First Aid body parts), hunger, thirst and temperature. */
public final class Healing {

    private static final int MAX_FOOD = 20;

    private Healing() {
    }

    private interface Step {
        void run() throws ReflectiveOperationException;
    }

    public static void heal(EntityPlayerMP player, @Nullable FirstAidBridge firstAid,
                            @Nullable SimpleDifficultyBridge simpleDifficulty) {
        clearNegativeEffects(player);
        player.extinguish();
        if (firstAid != null) {
            attempt("First Aid body parts", player, () -> firstAid.healPlayer(player));
        }
        player.setHealth(player.getMaxHealth());
        FoodStats food = player.getFoodStats();
        food.setFoodLevel(MAX_FOOD);
        // setFoodSaturationLevel is client-only; eating a full meal fills saturation up to the food level
        food.addStats(MAX_FOOD, 1.0F);
        if (simpleDifficulty != null) {
            attempt("thirst", player, () -> simpleDifficulty.quench(player));
            attempt("temperature", player, () -> simpleDifficulty.normalizeTemperature(player));
        }
    }

    private static void clearNegativeEffects(EntityPlayerMP player) {
        List<Potion> negative = new ArrayList<>();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getPotion().isBadEffect()) {
                negative.add(effect.getPotion());
            }
        }
        for (Potion potion : negative) {
            player.removePotionEffect(potion);
        }
    }

    /** One optional integration failing must not stop the rest of the heal. */
    private static void attempt(String what, EntityPlayerMP player, Step step) {
        try {
            step.run();
        } catch (Exception e) {
            LxbsEssential.LOGGER.warn("Could not restore {} for {}", what, player.getName(), e);
        }
    }
}
