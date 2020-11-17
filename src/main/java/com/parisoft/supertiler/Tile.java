package com.parisoft.supertiler;

import java.awt.image.Raster;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;

import static com.parisoft.supertiler.SuperTiler.bpp;
import static java.lang.Math.pow;

class Tile {

    static final Tile EMPTY = new Tile();

    private byte[][] pixels = new byte[8][8];
    private byte[][] planes = new byte[bpp][8];

    Tile(Raster img, int x, int y) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                pixels[row][col] = (byte) img.getPixel(x + col, y + row, (int[]) null)[0];
            }
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                byte pixel = pixels[row][col];

                for (int p = 0; p < planes.length; p++) {
                    planes[p][row] |= ((pixel & (byte) pow(2, p)) >> p) << (7 - col);
                }
            }
        }
    }

    private Tile() {
        super();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Tile)) {
            return false;
        }

        return IntStream.range(0, pixels.length).allMatch(row -> Arrays.equals(this.pixels[row], ((Tile) other).pixels[row]));
    }

    boolean isEmpty() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (this.pixels[row][col] != 0) {
                    return false;
                }
            }
        }

        return true;
    }

    boolean isHMirrorOf(Tile other) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (this.pixels[row][col] != other.pixels[row][7 - col]) {
                    return false;
                }
            }
        }

        return true;
    }

    boolean isVMirrorOf(Tile other) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (this.pixels[row][col] != other.pixels[7 - row][col]) {
                    return false;
                }
            }
        }

        return true;
    }

    boolean isHVMirrorOf(Tile other) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (this.pixels[row][col] != other.pixels[7 - row][7 - col]) {
                    return false;
                }
            }
        }

        return true;
    }

    void write(FileOutputStream output) throws IOException {
        for (int plane = 0; plane < planes.length; plane += 2) {
            for (int b = 0; b < 8; b++) {
                for (int p = plane; p < plane + 2; p++) {
                    output.write(planes[p][b]);
                }
            }
        }
    }

    public static boolean isNullOrEmpty(Tile tile) {
        return tile == null || tile.isEmpty();
    }
}
