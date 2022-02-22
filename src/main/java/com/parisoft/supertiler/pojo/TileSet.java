package com.parisoft.supertiler.pojo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static com.parisoft.supertiler.SuperTiler.ARG_TILESET;
import static com.parisoft.supertiler.SuperTiler.discardFlip;
import static com.parisoft.supertiler.SuperTiler.discardRedundant;
import static com.parisoft.supertiler.SuperTiler.mode;
import static com.parisoft.supertiler.SuperTiler.namespace;
import static java.util.stream.Collectors.toList;

public class TileSet extends AbstractSet<BigTile> {

    private List<BigTile> tiles = new ArrayList<>();
    private Tile[] array;

    public void addAll(MetaTile metaTile) {
        metaTile.largeTiles.forEach(this::add);
        metaTile.smallTiles.forEach(this::add);
    }

    @Override
    public Iterator<BigTile> iterator() {
        return tiles.iterator();
    }

    @Override
    public int size() {
        return tiles.size();
    }

    public boolean add(BigTile bigTile) {
        Optional<BigTile> clone;

        if (discardRedundant && (clone = tiles.stream().filter(bigTile::equals).findFirst()).isPresent()) {
            bigTile.index = clone.get().index;
            return false;
        }

        if (discardFlip && (clone = tiles.stream().filter(bigTile::isHFlipOf).findFirst()).isPresent()) {
            bigTile.index = clone.get().index;
            bigTile.hFlip = true;
            return false;
        }

        if (discardFlip && (clone = tiles.stream().filter(bigTile::isVFlipOf).findFirst()).isPresent()) {
            bigTile.index = clone.get().index;
            bigTile.vFlip = true;
            return false;
        }

        if (discardFlip && (clone = tiles.stream().filter(bigTile::isHVFlipOf).findFirst()).isPresent()) {
            bigTile.index = clone.get().index;
            bigTile.hFlip = true;
            bigTile.vFlip = true;
            return false;
        }

        return tiles.add(bigTile);
    }

    public void build() {
        if (mode == Mode.SNES) {//2D arrangement
            List<List<Tile>> tileset = new ArrayList<>();
            int row;

            for (BigTile bigTile : tiles) {
                // search row in tileset to add tile
                for (row = 0; row < tileset.size(); row++) {
                    if (16 - tileset.get(row).size() >= bigTile.getWidth() / 8) {
                        break;
                    }
                }

                // compute tile index
                if (tileset.size() <= row) {
                    bigTile.index.set(row * 16);
                } else {
                    bigTile.index.set(row * 16 + tileset.get(row).size());
                }

                // add empty row to tileset
                for (int i = 0; i < bigTile.getHeight() / 8; i++) {
                    if (row + i >= tileset.size()) {
                        tileset.add(new ArrayList<>(16));
                    }
                }

                // add tile to tileset
                for (Tile[] tile : bigTile.tiles) {
                    tileset.get(row++).addAll(Arrays.asList(tile));
                }
            }

            // fill empty spaces
            for (int r = 0; r < tileset.size() - 1; r++) {
                for (int c = tileset.get(r).size(); c < 16; c++) {
                    tileset.get(r).add(Tile.EMPTY);
                }
            }

            array = tileset.stream().flatMap(List::stream).toArray(Tile[]::new);
        } else {//1D arrangement (linear)
            int index = 0;

            for (BigTile tile : tiles) {
                tile.index.set(index);
                index += tile.tiles.length * tile.tiles[0].length;
            }

            array = tiles.stream()
                    .map(bigTile -> Arrays.stream(bigTile.tiles)
                            .flatMap(Arrays::stream)
                            .collect(toList()))
                    .flatMap(List::stream)
                    .toArray(Tile[]::new);
        }
    }

    public void write() throws IOException {
        String tilesetPath = namespace.getString(ARG_TILESET);

        if (tilesetPath != null) {
            try (FileOutputStream output = new FileOutputStream(new File(tilesetPath))) {
                for (Tile tile : array) {
                    tile.write(output);
                }
            }
        }
    }
}
