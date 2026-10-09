package com.lxb.lxbsessential.teleport;

import java.util.function.Consumer;

/** Runs work from the server tick so a failure is reported instead of crashing the whole server. */
public final class SafeRun {

    private SafeRun() {
    }

    /** Returns false when the task threw; the exception goes to {@code onError}. */
    public static boolean run(Runnable task, Consumer<RuntimeException> onError) {
        try {
            task.run();
            return true;
        } catch (RuntimeException e) {
            onError.accept(e);
            return false;
        }
    }
}
