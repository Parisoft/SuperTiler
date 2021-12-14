package com.parisoft.supertiler.pojo;

import java.awt.image.Raster;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.stream.IntStream;

import static com.parisoft.supertiler.SuperTiler.bpp;
import static com.parisoft.supertiler.SuperTiler.colorMap;

public class Tile {

    static final Tile EMPTY = new Tile();

    private byte[][] pixels = new byte[8][8];
    private byte[][] planes = new byte[8][bpp];//https://sneslab.net/wiki/Graphics_Format

    Tile(Raster img, int x, int y) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                byte pixel = colorMap.get(img.getPixel(x + col, y + row, (int[]) null)[0]).byteValue();

                pixels[row][col] = pixel;

                BitSet bits = BitSet.valueOf(new byte[]{pixel});

                for (int bp = 0; bp < bpp; bp++) {
                    if (bits.get(bp)) {
                        planes[row][bp] |= 1 << (7 - col);
                    }
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
//        for (int i = 0; i < 8; i++) {
//            for (int j = 0; j < 8; j+=2) {
//                output.write(pixels[i][j]|(pixels[i][j+1]<<4));
//            }
//        }
    }

    static boolean isNullOrEmpty(Tile tile) {
        return tile == null || tile.isEmpty();
    }
}
