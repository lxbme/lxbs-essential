package com.lxb.lxbsessential.saveditem;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Items as SNBT text, the format /give uses. It records the registry name rather than a numeric id, plus
 * count, damage, tag and Forge capability data. Saving checks the text reads back to the same NBT, so
 * a saved item is always an exact copy.
 */
public final class ItemCodec {

    private ItemCodec() {
    }

    public static String encode(ItemStack stack) throws NotExact {
        NBTTagCompound tag = stack.writeToNBT(new NBTTagCompound());
        String text = tag.toString();
        NBTTagCompound parsed;
        try {
            parsed = JsonToNBT.getTagFromJson(text);
        } catch (NBTException e) {
            throw new NotExact("its NBT text does not parse: " + e.getMessage());
        }
        if (!tag.equals(parsed)) {
            throw new NotExact("its NBT text reads back different");
        }
        return text;
    }

    /** Empty when the item no longer exists, e.g. its mod was removed. */
    public static ItemStack decode(String text) throws NBTException {
        return new ItemStack(JsonToNBT.getTagFromJson(text));
    }

    public static final class NotExact extends Exception {

        NotExact(String message) {
            super(message);
        }
    }
}
