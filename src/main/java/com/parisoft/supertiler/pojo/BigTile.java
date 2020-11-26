package com.parisoft.supertiler.pojo;

import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.parisoft.supertiler.pojo.Obj.HORIZONTAL_MIRROR;
import static com.parisoft.supertiler.pojo.Obj.VERTICAL_MIRROR;
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

                smallTile.x = this.x + col * 8;
                smallTile.y = this.y + row * 8;
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

        // Search for a clone tile in tileset
        for (int row = 0; row <= tileset.length - tiles.length; row++) {
            for (int col = 0; col <= tileset[row].length - tiles[0].length; col++) {
                byte tile = (byte) (row * tileset[row].length + col);
                BigTile view = new BigTile(tileset, tiles.length, row, col);

                if (view.isEmpty()) {
                    continue;
                }

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

        //If didnt find, search for an empty slot to insert it
        for (int row = 0; row <= tileset.length - tiles.length; row++) {
            for (int col = 0; col <= tileset[row].length - tiles[0].length; col++) {
                if (Tile.isNullOrEmpty(tileset[row][col])) {
                    //FIXME save on Tile from tileset which size it is, so small tiles dont overlap larger ones
//                    if (tiles.length == 1 || new BigTile(tileset, tiles.length, row, col).isEmpty()) {
                    byte tile = (byte) (row * tileset[row].length + col);

                    for (Tile[] rowOfTiles : tiles) {
                        System.arraycopy(rowOfTiles, 0, tileset[row], col, rowOfTiles.length);
                        Arrays.fill(tileset[row], col + rowOfTiles.length, tileset[row].length, Tile.EMPTY);
                        row++;
                    }

                    return new Obj(xOff, yOff, tile, size, (byte) 0);
//                    }
                }
            }
        }

        throw new IllegalStateException("BigTile does not fit in tileset");
    }

    BG getBG() {
        if (isNull()) {
            return null;
        }

        // Search for a clone tile in tileset
        for (int row = 0; row <= tileset.length - tiles.length; row++) {
            for (int col = 0; col <= tileset[row].length - tiles[0].length; col++) {
                int tile = row * tileset[row].length + col;
                BigTile view = new BigTile(tileset, tiles.length, row, col);

                if (view.isNull()) {
                    continue;
                }

                if (equals(view)) {
                    return new BG(tile, false, false);
                }

                if (isHMirrorOf(view)) {
                    return new BG(tile, true, false);
                }

                if (isVMirrorOf(view)) {
                    return new BG(tile, false, true);
                }

                if (isHVMirrorOf(view)) {
                    return new BG(tile, true, true);
                }
            }
        }

        //If didnt find, search for an empty slot to insert it
        for (int row = 0; row <= tileset.length - tiles.length; row++) {
            for (int col = 0; col <= tileset[row].length - tiles[0].length; col++) {
                if (tileset[row][col] == null) {
                    //FIXME save on Tile from tileset which size it is, so small tiles dont overlap larger ones
//                    if (tiles.length == 1 || new BigTile(tileset, tiles.length, row, col).isEmpty()) {
                    int tile = row * tileset[row].length + col;

                    for (Tile[] rowOfTiles : tiles) {
                        System.arraycopy(rowOfTiles, 0, tileset[row], col, rowOfTiles.length);
                        row++;
                    }

                    return new BG(tile, false, false);
//                    }
                }
            }
        }

        throw new IllegalStateException("BigTile does not fit in tileset");
    }
}

