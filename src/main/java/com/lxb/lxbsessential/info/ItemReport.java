package com.lxb.lxbsessential.info;

import com.lxb.lxbsessential.Tags;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Everything /info shows about one item, computed once so [Share] can broadcast the same snapshot.
 * Each section is computed on its own: mod items are varied enough that one may throw, and that must
 * not hide the others.
 */
public final class ItemReport {

    private static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    public final ItemStack stack;
    public final String holderName;
    public final String modName;
    @Nullable
    public final MeleeStats melee;
    @Nullable
    public final RangedStats ranged;
    @Nullable
    public final ToolStats tools;
    @Nullable
    public final ArmorStats armor;
    public final boolean shield;

    private ItemReport(ItemStack stack, String holderName, String modName, @Nullable MeleeStats melee,
                       @Nullable RangedStats ranged, @Nullable ToolStats tools, @Nullable ArmorStats armor,
                       boolean shield) {
        this.stack = stack;
        this.holderName = holderName;
        this.modName = modName;
        this.melee = melee;
        this.ranged = ranged;
        this.tools = tools;
        this.armor = armor;
        this.shield = shield;
    }

    /** holderDamage and holderSpeed are the holder's current attribute values with the item in hand. */
    public static ItemReport build(ItemStack stack, String holderName, String modName, double holderDamage,
                                   double holderSpeed, Integrations integrations) {
        ItemStack copy = stack.copy();
        MeleeStats melee = section("melee", copy,
                () -> MeleeStats.appliesTo(copy) ? MeleeStats.of(copy, holderDamage, holderSpeed) : null);
        RangedStats ranged = section("ranged", copy,
                () -> RangedStats.appliesTo(copy) ? RangedStats.of(copy, integrations.spartan) : null);
        ToolStats tools = section("tool", copy,
                () -> ToolStats.appliesTo(copy) ? ToolStats.of(copy) : null);
        ArmorStats armor = section("armor", copy,
                () -> ArmorStats.appliesTo(copy)
                        ? ArmorStats.of(copy, integrations.firstAid, integrations.firstAidInstalled)
                        : null);
        Boolean shield = section("shield", copy, () -> copy.getItem().isShield(copy, null));
        return new ItemReport(copy, holderName, modName, melee, ranged, tools, armor, Boolean.TRUE.equals(shield));
    }

    @Nullable
    private static <T> T section(String name, ItemStack stack, Supplier<T> compute) {
        try {
            return compute.get();
        } catch (RuntimeException | LinkageError e) {
            LOGGER.warn("/info skipped the {} section for {}", name, stack.getItem().getRegistryName(), e);
            return null;
        }
    }
}
