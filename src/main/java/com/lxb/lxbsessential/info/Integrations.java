package com.lxb.lxbsessential.info;

import javax.annotation.Nullable;

/** Optional mod bridges bound at server start; a bridge is null when its mod is missing or incompatible. */
public final class Integrations {

    public static final Integrations NONE = new Integrations(null, null, false);

    @Nullable
    public final SpartanBridge spartan;
    @Nullable
    public final FirstAidArmor firstAid;
    /** First Aid is loaded even if firstAid failed to bind, so armor numbers must say they are estimates. */
    public final boolean firstAidInstalled;

    public Integrations(@Nullable SpartanBridge spartan, @Nullable FirstAidArmor firstAid, boolean firstAidInstalled) {
        this.spartan = spartan;
        this.firstAid = firstAid;
        this.firstAidInstalled = firstAidInstalled;
    }
}
