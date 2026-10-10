package com.lxb.lxbsessential.phase.fake;

/** Mirrors SRPCommandEvolution.getNeededPoints: points needed to reach the given phase, 0 past the last one. */
public final class FakeCommand {

    private FakeCommand() {
    }

    public static int getNeededPoints(byte phase) {
        switch (phase) {
            case 1:
                return 400;
            case 4:
                return 25000;
            case 10:
                return 1800000000;
            default:
                return 0;
        }
    }
}
