package com.lxb.lxbsessential.info.fake;

/** Mirrors FirstAidConfig.enchantmentHandling.armorEnchantmentMode. */
public final class FakeFirstAidConfig {

    public static EnchantmentHandling enchantmentHandling = new EnchantmentHandling();

    private FakeFirstAidConfig() {
    }

    public static class EnchantmentHandling {

        public ArmorEnchantmentMode armorEnchantmentMode = ArmorEnchantmentMode.LOCAL_ENCHANTMENTS;

        public enum ArmorEnchantmentMode {
            LOCAL_ENCHANTMENTS, GLOBAL_ENCHANTMENTS
        }
    }
}
