package com.lxb.lxbsessential.heal.fake;

/** Mirrors SimpleDifficulty's TemperatureEnum, whose NORMAL range defines a comfortable temperature. */
public enum FakeTemperatureEnum {
    COLD(6, 10),
    NORMAL(11, 14),
    HOT(15, 19);

    private final int lower;
    private final int upper;

    FakeTemperatureEnum(int lower, int upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public int getMiddle() {
        return (lower + upper) / 2;
    }
}
