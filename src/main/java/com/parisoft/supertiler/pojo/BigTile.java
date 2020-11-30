package com.parisoft.supertiler.pojo;

import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.parisoft.supertiler.SuperTiler.tileSize;
import static java.util.Collections.emptyList;

class BigTile {

    Tile[][] tiles;
    AtomicInteger index = new AtomicInteger(0);
    boolean vFlip = false;
    boolean hFlip = false;
    int x;
    int y;

    private BigTile(Tile[][] tiles, int len, int row, int col) {
        this.tiles = new Tile[len][len];

        for (int i = 0; i < len; i++) {
            System.arraycopy(tiles[row + i], col, this.tiles[i], 0, len);
        }
    }

    BigTile(int pixels, Raster img, int x, int y) {
        tiles = new Tile[pixels / 8][pixels / 8];
        this.x = x;
        this.y = y;

        for (int row = 0; row < tiles.length; row++) {
            for (int col = 0; col < tiles[row].length; col++) {
                tiles[row][col] = new Tile(img, x + col * 8, y + row * 8);
            }
        }
    }

    int getWidth() {
        return tiles[0].length * 8;
    }

    int getHeight() {
        return tiles.length * 8;
    }

    private boolean match(BigTile other, BiPredicate<Tile, Tile> predicate) {
        if (this.tiles.length != other.tiles.length) {
            return false;
        }

        for (int row = 0; row < tiles.length; row++) {
            for (int col = 0; col < tiles.length; col++) {
                Tile thisTile = this.tiles[row][col];
                Tile otherTile = other.tiles[row][col];

                if (thisTile == null || otherTile == null || predicate.negate().test(thisTile, otherTile)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BigTile)) {
            return false;
        }

        return match((BigTile) other, Tile::equals);
    }

    boolean isEmpty() {
        return IntStream.range(0, tiles.length)
                .allMatch(row -> Stream.of(tiles[row]).allMatch(Tile::isNullOrEmpty));
    }

    boolean isNull() {
        return IntStream.range(0, tiles.length)
                .allMatch(row -> Stream.of(tiles[row]).allMatch(Objects::isNull));
    }

    boolean isHFlipOf(BigTile other) {
        return match(other, Tile::isHMirrorOf);
    }

    boolean isVFlipOf(BigTile other) {
        return match(other, Tile::isVMirrorOf);
    }

    boolean isHVFlipOf(BigTile other) {
        return match(other, Tile::isHVMirrorOf);
    }

    List<BigTile> split() {
        List<BigTile> smallTiles = new ArrayList<>();
        byte smallLen = (byte) (tileSize.small / 8);

        for (int row = 0; row <= tiles.length - smallLen; row += smallLen) {
            for (int col = 0; col <= tiles[row].length - smallLen; col += smallLen) {
                BigTile smallTile = new BigTile(tiles, smallLen, row, col);

                if (smallTile.isEmpty()) {
                    continue;
                }

                smallTile.x = this.x + col * 8;
                smallTile.y = this.y + row * 8;
                smallTiles.add(smallTile);
            }
        }

        if (smallTiles.size() > getWidth() * getHeight() / 128) {
            return emptyList();
        }

        return smallTiles;
    }

    Obj toObj(byte xOff, byte yOff, byte size) {
        if (isEmpty()) {
            return null;
        }

        return new Obj(xOff, yOff, index.get(), size, hFlip, vFlip);
    }

    BG toBG() {
        if (isNull()) {
            return null;
        }

        return new BG(index.get(), hFlip, vFlip);
    }
}

