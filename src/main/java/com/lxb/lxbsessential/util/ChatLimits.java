package com.lxb.lxbsessential.util;

import net.minecraft.util.text.ITextComponent;

import java.nio.charset.StandardCharsets;

/**
 * PacketBuffer.writeString refuses chat JSON over 32767 UTF-8 bytes, and the failed encode disconnects
 * every receiver. Characters are not enough: CJK text is three bytes each and Gson escapes item hovers.
 */
public final class ChatLimits {

    public static final int MAX_JSON_BYTES = 32767;

    private ChatLimits() {
    }

    public static boolean fits(ITextComponent component) {
        return ITextComponent.Serializer.componentToJson(component).getBytes(StandardCharsets.UTF_8).length
                <= MAX_JSON_BYTES;
    }
}
