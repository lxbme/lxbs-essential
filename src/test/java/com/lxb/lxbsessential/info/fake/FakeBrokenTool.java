package com.lxb.lxbsessential.info.fake;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

import java.util.Set;

/** A mod weapon whose tool lookup blows up, e.g. compiled against a different library version. */
public class FakeBrokenTool extends ItemSword {

    private final Throwable failure;

    public FakeBrokenTool(Throwable failure) {
        super(ToolMaterial.DIAMOND);
        this.failure = failure;
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        if (failure instanceof RuntimeException) {
            throw (RuntimeException) failure;
        }
        throw (Error) failure;
    }
}
