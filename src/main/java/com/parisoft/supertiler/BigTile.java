package com.parisoft.supertiler;

import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.parisoft.supertiler.Obj.HORIZONTAL_MIRROR;
import static com.parisoft.supertiler.Obj.VERTICAL_MIRROR;
import static com.parisoft.supertiler.SuperTiler.objSpSize;
import static com.parisoft.supertiler.SuperTiler.tileset;
import static java.util.Collections.emptyList;

class BigTile {

    private Tile[][] tiles;
    int x;
    int y;

    private BigTile(Tile[][] tiles, int len, int row, int col) {
        this.tiles = new Tile[len][len];

        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                this.tiles[i][j] = tiles[row][col];
            }
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
                .allMatch(row -> Stream.of(tiles[row]).allMatch(tile -> tile == null || tile.isEmpty()));
    }

    private boolean isHMirrorOf(BigTile other) {
        return match(other, Tile::isHMirrorOf);
    }

    private boolean isVMirrorOf(BigTile other) {
        return match(other, Tile::isVMirrorOf);
    }

    private boolean isHVMirrorOf(BigTile other) {
        return match(other, Tile::isHVMirrorOf);
    }

    List<BigTile> split() {
        List<BigTile> smallTiles = new ArrayList<>();
        byte smallLen = (byte) (objSpSize.small / 8);

        for (int row = 0; row <= tiles.length - smallLen; row += smallLen) {
            for (int col = 0; col <= tiles[row].length - smallLen; col += smallLen) {
                BigTile smallTile = new BigTile(tiles, smallLen, row, col);

                if (smallTile.isEmpty()) {
                    continue;
                }

                smallTile.x = this.x + col;
                smallTile.y = this.y + row;
                smallTiles.add(smallTile);
            }
        }

        if (smallTiles.size() > objSpSize.large / objSpSize.small) {
            return emptyList();
        }

        return smallTiles;
    }

    Obj getObj(byte xOff, byte yOff, byte size) {
        if (isEmpty()) {
            return null;
        }

        // Try to reuse if this tile is redundant
        for (int row = 0; row <= tileset.length - tiles.length; row++) {
            for (int col = 0; col <= tileset[row].length - tiles[0].length; col++) {
                byte tile = (byte) (row * tileset[row].length + col);
                BigTile view = new BigTile(tileset, tiles.length, row, col);

                if (equals(view)) {
                    return new Obj(xOff, yOff, tile, size, (byte) 0);
                }

                if (isHMirrorOf(view)) {
                    return new Obj(xOff, yOff, tile, size, HORIZONTAL_MIRROR);
                }

                if (isVMirrorOf(view)) {
                    return new Obj(xOff, yOff, tile, size, VERTICAL_MIRROR);
                }

                if (isHVMirrorOf(view)) {
                    return new Obj(xOff, yOff, tile, size, (byte) (HORIZONTAL_MIRROR | VERTICAL_MIRROR));
                }
            }
        }

        //If didnt reuse, search for an empty slot to insert it
        for (int row = 0; row <= tileset.length - tiles.length; row++) {
            for (int col = 0; col <= tileset[row].length - tiles[0].length; col++) {
                if (tileset[row][col] == null || tileset[row][col].isEmpty()) {
                    for (Tile[] rowOfTiles : tiles) {
                        System.arraycopy(rowOfTiles, 0, tileset[row], col, tiles.length);
                        Arrays.fill(tileset[row], col + tiles.length, tileset[row].length, Tile.EMPTY);
                    }

                    return new Obj(xOff, yOff, (byte) (row * tileset[row].length + col), size, (byte) 0);
                }
            }
        }

        throw new IllegalStateException("BigTile does not fit in tileset");
    }
}

