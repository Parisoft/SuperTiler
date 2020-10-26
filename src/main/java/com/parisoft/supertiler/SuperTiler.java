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

    static String mode;
    static byte bpp;
    static byte objPalNum;
    static byte objPriority;
    static byte objTilesetNum;
    static byte objXOff;
    static byte objYOff;
    static SpriteSize objSpSize;
    static boolean applySmall;
    static boolean applyLarge;
    static byte frameWidth;
    static byte frameHeight;
    static Tile[][] tileset = new Tile[16][16];

    public static void main(String[] args) throws IOException {
        ArgumentParser parser = ArgumentParsers.newFor("SuperTiler v1.0")
                .build()
                .defaultHelp(true)
                .description("Tools for tiles generation for NES and Super NES");
        ArgumentGroup sprite = parser.addArgumentGroup("sprite").description("Generates tileset, palettes, and metasprites from image");
        sprite.addArgument("-i", "--input").nargs("?").required(true).type(String.class).help("Input indexed PNG image");
        sprite.addArgument("-t", "--tileset").nargs("?").required(false).type(String.class).help("Output tileset file");
        sprite.addArgument("-p", "--palette").nargs("?").required(false).type(String.class).help("Output palette file");
        sprite.addArgument("-m", "--metasprite").nargs("?").required(false).type(String.class).help("Prefix for metasprite files");
        sprite.addArgument("-M", "--mode").nargs("?").required(false).type(String.class).choices("snes", "nes").help("Mode for target console");
        sprite.addArgument("-B", "--bpp").nargs("?").required(false).choices(2, 4, 8).setDefault(4).help("Depth or number of colors per pixel");
        sprite.addArgument("-W", "--frame-width").nargs("?").required(false).setDefault(8).help("Width of each frame from image");
        sprite.addArgument("-H", "--frame-height").nargs("?").required(false).setDefault(8).help("Height if each frame from image");
        sprite.addArgument("-s", "--tile-size").nargs("?").required(false).setDefault(0).help("Size of the sprites (SNES only)");
        sprite.addArgument("-S", "--apply-small").nargs("?").required(false).setDefault(true)
                .help("If apply-large is not set, all tiles size are the one define in tile-size. See apply-large when both are set. (SNES only)");
        sprite.addArgument("-L", "--apply-large").nargs("?").required(false).setDefault(false)
                .help("When mode is snes, SuperTiler will first scan for large tiles defined in tile-size then, if apply-small is set, it will replace the large tile for N small tiles if N <= large/small" + System.lineSeparator()
                              + "When mode is nes, tiles are 8x16");
        sprite.addArgument("--pal-num").nargs("?").required(false).choices(0, 1, 2, 3, 4, 5, 6, 7).setDefault(0).help("Object palette number");
        sprite.addArgument("--priority").nargs("?").required(false).choices(0, 1, 2, 3).setDefault(2).help("Object priority (SNES only)");
        sprite.addArgument("--tileset-num").nargs("?").required(false).choices(0, 1).setDefault(0).help("Object tileset number (SNES only)");
        sprite.addArgument("-X", "--xoff").nargs("?").required(false).setDefault(0).help("Object X offset");
        sprite.addArgument("-Y", "--yoff").nargs("?").required(false).setDefault(0).help("Object Y offset");

        Namespace namespace = null;

        try {
            namespace = parser.parseArgs(args);
            mode = namespace.getString("mode");
            bpp = namespace.getByte("bpp");
            objPalNum = namespace.getByte("pal-num");
            objPriority = namespace.getByte("priority");
            objTilesetNum = namespace.getByte("tileset-num");
            objXOff = namespace.getByte("xoff");
            objYOff = namespace.getByte("yoff");
            objSpSize = SpriteSize.valueOf(namespace.getInt("tile-size"));
            applySmall = namespace.getBoolean("apply-small");
            applyLarge = namespace.getBoolean("apply-large");
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
