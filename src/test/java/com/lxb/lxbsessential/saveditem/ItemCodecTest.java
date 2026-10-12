package com.lxb.lxbsessential.saveditem;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemCodecTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    private static ItemStack roundTrip(ItemStack stack) throws Exception {
        return ItemCodec.decode(ItemCodec.encode(stack));
    }

    /** Mod data such as SRP adaptation or Quality Tools quality lives in arbitrary nested NBT. */
    @Test
    void enchantedRenamedSwordWithModDataComesBackIdentical() throws Exception {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.addEnchantment(Enchantments.SHARPNESS, 5);
        sword.setStackDisplayName("剑 \"Blade\" \\ end");
        sword.setItemDamage(17);
        NBTTagCompound inner = new NBTTagCompound();
        inner.setIntArray("ints", new int[]{1, -2, 3});
        inner.setTag("longs", new NBTTagLongArray(new long[]{Long.MAX_VALUE, Long.MIN_VALUE}));
        inner.setByteArray("bytes", new byte[]{1, 2});
        NBTTagList list = new NBTTagList();
        list.appendTag(inner);
        NBTTagCompound modData = new NBTTagCompound();
        modData.setTag("list", list);
        modData.setFloat("float", 0.1F);
        modData.setDouble("double", 0.1);
        modData.setLong("long", Long.MAX_VALUE);
        modData.setShort("short", (short) -3);
        modData.setByte("byte", (byte) 7);
        sword.getTagCompound().setTag("modData", modData);

        ItemStack back = roundTrip(sword);

        assertTrue(ItemStack.areItemStacksEqual(sword, back));
        assertEquals(sword.writeToNBT(new NBTTagCompound()), back.writeToNBT(new NBTTagCompound()));
        assertEquals("剑 \"Blade\" \\ end", back.getDisplayName());
    }

    @Test
    void stackCountIsKept() throws Exception {
        assertEquals(37, roundTrip(new ItemStack(Items.STICK, 37)).getCount());
    }

    @Test
    void keysWithSpecialCharactersSurvive() throws Exception {
        ItemStack stack = new ItemStack(Items.STICK);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("weird key:with/chars \"and quotes\"", 1);
        stack.setTagCompound(tag);

        assertTrue(ItemStack.areItemStacksEqual(stack, roundTrip(stack)));
    }

    /** NaN prints as "NaNf", which reads back as a string: such an item cannot be stored exactly. */
    @Test
    void notANumberIsRefused() {
        ItemStack stack = new ItemStack(Items.STICK);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setFloat("bad", Float.NaN);
        stack.setTagCompound(tag);

        assertThrows(ItemCodec.NotExact.class, () -> ItemCodec.encode(stack));
    }

    /** A saved item whose mod was removed decodes to an empty stack instead of something else. */
    @Test
    void unknownItemDecodesEmpty() throws Exception {
        assertTrue(ItemCodec.decode("{id:\"nomod:thing\",Count:1b,Damage:0s}").isEmpty());
    }

    @Test
    void malformedTextThrows() {
        assertThrows(NBTException.class, () -> ItemCodec.decode("{id:"));
    }
}
