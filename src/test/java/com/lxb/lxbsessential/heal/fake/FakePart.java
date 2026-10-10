package com.lxb.lxbsessential.heal.fake;

import net.minecraft.entity.player.EntityPlayer;

/** Mirrors First Aid's AbstractDamageablePart. */
public final class FakePart {

    public final int maxHealth;
    public float currentHealth;
    public Boolean lastApplyDebuff;

    public FakePart(int maxHealth, float currentHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public float heal(float amount, EntityPlayer player, boolean applyDebuff) {
        lastApplyDebuff = applyDebuff;
        float healed = Math.min(amount, maxHealth - currentHealth);
        currentHealth += healed;
        return amount - healed;
    }
}
