package com.lxb.lxbsessential.heal.fake;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/** Mirrors First Aid's AbstractPlayerDamageModel: iterable body parts plus a client resync. */
public final class FakeDamageModel implements Iterable<FakePart> {

    public final List<FakePart> parts;
    public boolean resynced;

    public FakeDamageModel(FakePart... parts) {
        this.parts = Arrays.asList(parts);
    }

    @Override
    public Iterator<FakePart> iterator() {
        return parts.iterator();
    }

    public void scheduleResync() {
        resynced = true;
    }
}
