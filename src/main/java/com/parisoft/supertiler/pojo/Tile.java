package com.parisoft.supertiler.pojo;

import java.awt.image.Raster;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;

import static com.parisoft.supertiler.SuperTiler.bpp;
import static java.lang.Math.pow;

public class Tile {

    static final Tile EMPTY = new Tile();

    private byte[][] pixels = new byte[8][8];
    private byte[][] planes = new byte[8][bpp];//https://sneslab.net/wiki/Graphics_Format

    Tile(Raster img, int x, int y) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                byte pixel = (byte) img.getPixel(x + col, y + row, (int[]) null)[0];

                pixels[row][col] = pixel;

                for (int p = 0; p < bpp; p++) {
                    planes[row][p] |= ((pixel & (byte) pow(2, p)) >> p) << (7 - col);
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

    private boolean isEmpty() {
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

    public void write(FileOutputStream output) throws IOException {
        for (int bp = 0; bp < bpp; bp += 2) {
            for (int row = 0; row < 8; row++) {
                for (int i = 0; i < 2; i++) {
                    output.write(planes[row][bp + i]);
                }
            }
        }
    }

    static boolean isNullOrEmpty(Tile tile) {
        return tile == null || tile.isEmpty();
    }
}
