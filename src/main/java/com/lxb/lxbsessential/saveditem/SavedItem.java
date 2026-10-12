package com.lxb.lxbsessential.saveditem;

/** One entry of saved_items.json; the item is SNBT text from ItemCodec. */
public final class SavedItem {

    public String name;
    public String item;
    public String savedBy;
    public String savedById;
    public String savedAt;

    /** For Gson. */
    public SavedItem() {
    }

    public SavedItem(String name, String item, String savedBy, String savedById, String savedAt) {
        this.name = name;
        this.item = item;
        this.savedBy = savedBy;
        this.savedById = savedById;
        this.savedAt = savedAt;
    }
}
