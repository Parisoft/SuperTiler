package com.parisoft.supertiler.pojo;

public enum SpriteSize {

    _8x8_16x6(8, 16),
    _8x8_32x32(8, 32),
    _8x8_64x64(8, 64),
    _16x16_32x32(16, 32),
    _16x16_64x64(16, 64),
    _32x32_64x64(32, 64);

    String name;
    byte small;
    byte large;

    SpriteSize(int small, int large) {
        this.name = String.format("%dx%d and %dx%d", small, small, large, large);
        this.small = (byte) small;
        this.large = (byte) large;
    }

    public static SpriteSize valueOf(Integer index) {
        if (index == null) {
            return null;
        }

        return values()[index];
    }
}
