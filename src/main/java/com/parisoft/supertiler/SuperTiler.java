package com.parisoft.supertiler;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import javax.imageio.ImageIO;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Duplicates")
public class SuperTiler {

    static byte bpp;
    static byte palNum;
    static byte priority;
    static byte tilesetNum;
    static byte xOff;
    static byte yOff;
    static byte smallTilePixels;
    static byte largeTilePixels;
    static byte frameWidth;
    static byte frameHeight;
    static Tile[][] tileset = new Tile[16][16];

    public static void main(String[] args) throws IOException {
        ArgumentParser parser = ArgumentParsers.newFor("supertiler")
                .build()
                .defaultHelp(true)
                .description("Tools for tiles generation for NES and Super NES");
        ArgumentGroup sprite = parser.addArgumentGroup("sprite").description("Generates tileset and metasprites from image");
        sprite.addArgument("-i", "--input").nargs("?").required(true).type(String.class).help("Input indexed PNG image");
        sprite.addArgument("-t", "--tileset").nargs("?").required(false).type(String.class).help("Output tileset file");
        sprite.addArgument("-p", "--palette").nargs("?").required(false).type(String.class).help("Output palette file");
        sprite.addArgument("-m", "--metasprite").nargs("?").required(false).type(String.class).help("Prefix for metasprite files");
        sprite.addArgument("-B", "--bpp").nargs("?").required(false).choices(2, 4, 8).setDefault(4).help("Depth or number of colors per pixel");
        sprite.addArgument("-W", "--frame-width").nargs("?").required(false).setDefault(8).help("Width of each frame from image");
        sprite.addArgument("-H", "--frame-height").nargs("?").required(false).setDefault(8).help("Height if each frame from image");
        sprite.addArgument("-s", "--tile-small").nargs("?").required(false).setDefault(8).help("Size for small sprites");
        sprite.addArgument("-S", "--tile-large").nargs("?").required(false).setDefault(16).help("Size for large sprites");
        sprite.addArgument("--pal-num").nargs("?").required(false).setDefault(0).help("Palette number used by sprites");
        sprite.addArgument("--priority").nargs("?").required(false).setDefault(2).help("Sprites priority");
        sprite.addArgument("--tileset-num").nargs("?").required(false).setDefault(0).help("Tileset number");
        sprite.addArgument("-X", "--xoff").nargs("?").required(false).setDefault(0).help("Object X offset");
        sprite.addArgument("-Y", "--yoff").nargs("?").required(false).setDefault(0).help("Object Y offset");

        Namespace namespace = null;

        try {
            namespace = parser.parseArgs(args);
            bpp = namespace.getByte("bpp");
            palNum = namespace.getByte("pal-num");
            priority = namespace.getByte("priority");
            tilesetNum = namespace.getByte("tileset-num");
            xOff = namespace.getByte("xoff");
            yOff = namespace.getByte("yoff");
            smallTilePixels = namespace.getByte("tile-small");
            largeTilePixels = namespace.getByte("tile-large");
            frameWidth = namespace.getByte("frame-width");
            frameHeight = namespace.getByte("frame-height");
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        WritableRaster img = ImageIO.read(new File(namespace.getString("input"))).getRaster();
        List<Frame> frames = new ArrayList<>();

        for (int y = 0; y < img.getHeight(); y += frameHeight) {
            for (int x = 0; x < img.getWidth(); x += frameWidth) {
                Frame frame = new Frame(img, x, y);

                if (frame.isEmpty()) {
                    continue;
                }

                frames.add(frame);
            }
        }

        if (frames.isEmpty()) {
            System.err.println("No frames");
            return;
        }

        frames.forEach(Frame::createLargeMetasprites);
        frames.forEach(Frame::createSmallMetasprites);

        String tilesetPath = namespace.getString("tileset");

        if (tilesetPath != null) {
            try (FileOutputStream output = new FileOutputStream(new File(tilesetPath))) {
                exit:
                for (Tile[] row : SuperTiler.tileset) {
                    for (Tile tile : row) {
                        if (tile == null) {
                            break exit;
                        }

                        tile.write(output);
                    }
                }
            }
        }

        String metasprite = namespace.getString("metasprite");

        if (metasprite != null) {
            for (int i = 0; i < frames.size(); i++) {
                try (FileOutputStream output = new FileOutputStream(new File(metasprite + i + ".bin"))) {
                    frames.get(i).write(output);
                }
            }
        }

        System.out.printf("Frames=%d%n", frames.size());
        System.out.println("Bye");
    }

}
