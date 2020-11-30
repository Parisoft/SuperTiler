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
import static com.parisoft.supertiler.SuperTiler.namespace;
import static java.util.stream.Collectors.toList;

public class TileSet extends AbstractSet<BigTile> {

    private List<BigTile> tiles = new ArrayList<>();
    private List<Tile> flat;

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
        throw new UnsupportedOperationException();
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
        List<List<Tile>> tileset = new ArrayList<>();
        int row;

        for (BigTile bigTile : tiles) {
            for (row = 0; row < tileset.size(); row++) {
                if (16 - tileset.get(row).size() >= bigTile.getWidth() / 8) {
                    break;
                }
            }

            bigTile.index.set(row * 16 + tileset.size() > row ? tileset.size() : 0);

            for (Tile[] tile : bigTile.tiles) {
                if (tileset.size() <= row) {
                    tileset.add(new ArrayList<>(16));
                }

                tileset.get(row).addAll(Arrays.asList(tile));
            }
        }

        flat = tileset.stream().flatMap(List::stream).collect(toList());
    }

    public void write() throws IOException {
        String tilesetPath = namespace.getString(ARG_TILESET);

        if (tilesetPath != null) {
            try (FileOutputStream output = new FileOutputStream(new File(tilesetPath))) {
                for (Tile tile : flat) {
                    tile.write(output);
                }
            }
        }
    }
}
