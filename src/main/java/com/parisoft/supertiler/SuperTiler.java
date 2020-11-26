package com.parisoft.supertiler;

import com.parisoft.supertiler.cmd.Cmd;
import com.parisoft.supertiler.cmd.SpriteCmd;
import com.parisoft.supertiler.pojo.SpriteSize;
import com.parisoft.supertiler.pojo.Tile;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@SuppressWarnings({"Duplicates", "WeakerAccess"})
public class SuperTiler {

    public static final String ARG_INPUT = "input";
    public static final String ARG_TILESET = "tileset";
    public static final String ARG_PALETTE = "palette";
    public static final String ARG_METATILE = "metatile";
    public static final String ARG_MODE = "mode";
    public static final String ARG_BPP = "bpp";
    public static final String ARG_METAW = "metaw";
    public static final String ARG_METAH = "metah";
    public static final String ARG_TILESIZE = "tilesize";
    public static final String ARG_APPLYSMALL = "applysmall";
    public static final String ARG_APPLYLARGE = "applylarge";
    public static final String ARG_PALNUM = "palnum";
    public static final String ARG_PRIORITY = "priority";
    public static final String ARG_XOFF = "xoff";
    public static final String ARG_YOFF = "yoff";
    public static final String ARG_TILEOFF = "tileoff";
    public static final String ARG_TILESETNUM = "tilesetnum";

    public static BufferedImage input;
    public static String mode;
    public static byte bpp;
    public static byte objPalNum;
    public static byte objPriority;
    public static byte objTilesetNum;
    public static byte objXOff;
    public static byte objYOff;
    public static SpriteSize objSpSize;
    public static byte objTileOff;
    public static boolean applySmall;
    public static boolean applyLarge;
    public static byte metatileWidth;
    public static byte metatileHeight;
    public static Namespace namespace;
    public static Tile[][] tileset = new Tile[16][16];

    public static void main(String[] args) throws IOException {
        ArgumentParser parser = ArgumentParsers.newFor("SuperTiler v1.0")
                .build()
                .defaultHelp(true)
                .description("Tile generation toolkit for NES and Super NES");
        Subparser sprite = parser.addSubparsers()
                .addParser("sprite", true)
                .description("Generates " + ARG_TILESET + ", " + ARG_PALETTE + "s, and " + ARG_METATILE + "s from image")
                .setDefault("cmd", new SpriteCmd());
        sprite.addArgument("-i", "--" + ARG_INPUT).nargs("?").required(true).type(String.class).help("Input indexed PNG image");
        sprite.addArgument("-t", "--" + ARG_TILESET).nargs("?").required(false).type(String.class).help("Output " + ARG_TILESET + " file");
        sprite.addArgument("-p", "--" + ARG_PALETTE).nargs("?").required(false).type(String.class).help("Output " + ARG_PALETTE + " file");
        sprite.addArgument("-m", "--" + ARG_METATILE).nargs("?").required(false).type(String.class).help("Prefix for " + ARG_METATILE + " files");
        sprite.addArgument("-M", "--" + ARG_MODE).nargs("?").required(false).type(String.class).choices("snes", "nes").setDefault("snes").help("Mode for target console");
        sprite.addArgument("-B", "--" + ARG_BPP).nargs("?").required(false).type(Integer.class).choices(2, 4, 8).setDefault(4).help("Depth or number of colors per pixel");
        sprite.addArgument("-W", "--" + ARG_METAW).nargs("?").required(false).type(Integer.class).setDefault(8).help("Metatile width in pixels");
        sprite.addArgument("-H", "--" + ARG_METAH).nargs("?").required(false).type(Integer.class).setDefault(8).help("Metatile height in pixels");
        sprite.addArgument("-s", "--" + ARG_TILESIZE).nargs("?").required(false).type(Integer.class).choices(0, 1, 2, 3, 4, 5).setDefault(0)
                .help("Size of the sprites (SNES only):" + System.lineSeparator()
                              + "0 =  8x8  and 16x16 sprites" + System.lineSeparator()
                              + "1 =  8x8  and 32x32 sprites" + System.lineSeparator()
                              + "2 =  8x8  and 64x64 sprites" + System.lineSeparator()
                              + "3 = 16x16 and 32x32 sprites" + System.lineSeparator()
                              + "4 = 16x16 and 64x64 sprites" + System.lineSeparator()
                              + "5 = 32x32 and 64x64 sprites");
        sprite.addArgument("-S", "--" + ARG_APPLYSMALL).nargs("?").required(false).type(Boolean.class).setDefault(true)
                .help("If " + ARG_APPLYLARGE + " is not set, all tiles size are the one define in tileize. See applylarge when both are set. (SNES only)");
        sprite.addArgument("-L", "--" + ARG_APPLYLARGE).nargs("?").required(false).type(Boolean.class).setDefault(false)
                .help("When " + ARG_MODE + " is snes, SuperTiler will first scan for large tiles defined in " + ARG_TILESIZE + " then, if " + ARG_APPLYSMALL + " is set, it will replace the large tile for N small tiles if N <= large/small" + System.lineSeparator()
                              + "When " + ARG_MODE + " is nes, tiles are 8x16");
        sprite.addArgument("--" + ARG_PALNUM).nargs("?").required(false).type(Integer.class).choices(0, 1, 2, 3, 4, 5, 6, 7).setDefault(0).help("Object " + ARG_PALETTE + " number");
        sprite.addArgument("--" + ARG_PRIORITY).nargs("?").required(false).type(Integer.class).choices(0, 1, 2, 3).setDefault(2).help("Object " + ARG_PRIORITY + " (SNES only)");
        sprite.addArgument("--" + ARG_TILESETNUM).nargs("?").required(false).type(Integer.class).choices(0, 1).setDefault(0).help("Object " + ARG_TILESET + " number (SNES only)");
        sprite.addArgument("-X", "--" + ARG_XOFF).nargs("?").required(false).type(Integer.class).setDefault(0).help("Object X offset");
        sprite.addArgument("-Y", "--" + ARG_YOFF).nargs("?").required(false).type(Integer.class).setDefault(0).help("Object Y offset");
        sprite.addArgument("-T", "--" + ARG_TILEOFF).nargs("?").required(false).type(Integer.class).setDefault(0).help("Object tile offset");

        namespace = parser.parseArgsOrFail(args);
        mode = namespace.getString(ARG_MODE);
        bpp = namespace.getInt(ARG_BPP).byteValue();
        objPalNum = namespace.getInt(ARG_PALNUM).byteValue();
        objPriority = namespace.getInt(ARG_PRIORITY).byteValue();
        objTilesetNum = namespace.getInt(ARG_TILESETNUM).byteValue();
        objXOff = namespace.getInt(ARG_XOFF).byteValue();
        objYOff = namespace.getInt(ARG_YOFF).byteValue();
        objSpSize = SpriteSize.valueOf(namespace.getInt(ARG_TILESIZE));
        objTileOff = namespace.getInt(ARG_TILEOFF).byteValue();
        applySmall = namespace.getBoolean(ARG_APPLYSMALL);
        applyLarge = namespace.getBoolean(ARG_APPLYLARGE);
        metatileWidth = namespace.getInt(ARG_METAW).byteValue();
        metatileHeight = namespace.getInt(ARG_METAH).byteValue();
        input = ImageIO.read(new File(namespace.getString(ARG_INPUT)));

        try {
            ((Cmd)namespace.get("cmd")).execute();
        } catch (Exception e) {
            System.err.println("ERROR: ".concat(e.getMessage()));
        }

        System.out.println("Bye");
    }
}
