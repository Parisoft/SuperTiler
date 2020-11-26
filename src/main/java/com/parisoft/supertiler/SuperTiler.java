package com.parisoft.supertiler;

import com.parisoft.supertiler.cmd.BackgroundCmd;
import com.parisoft.supertiler.cmd.Cmd;
import com.parisoft.supertiler.cmd.SpriteCmd;
import com.parisoft.supertiler.pojo.SpriteSize;
import com.parisoft.supertiler.pojo.Tile;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@SuppressWarnings({"Duplicates", "WeakerAccess"})
public class SuperTiler {

    public static final String VERSION = "1.0";

    public static final String ARG_INPUT = "input";
    public static final String ARG_TILESET = "tileset";
    public static final String ARG_PALETTE = "palette";
    public static final String ARG_METATILE = "metatile";
    public static final String ARG_TILEMAP = "tilemap";
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
    public static Tile[][] tileset;

    public static void main(String[] args) throws IOException {
        ArgumentParser parser = ArgumentParsers.newFor("supertiler-" + VERSION + ".jar")
                .build()
                .defaultHelp(true)
                .description("Tile generation toolkit for NES and Super NES");
        SpriteCmd.create(parser);
        BackgroundCmd.create(parser);

        namespace = parser.parseArgsOrFail(args);
        mode = namespace.getString(ARG_MODE);
        bpp = getByte(ARG_BPP);
        objPalNum = getByte(ARG_PALNUM);
        objPriority = getByte(ARG_PRIORITY);
        objTilesetNum = getByte(ARG_TILESETNUM);
        objXOff = getByte(ARG_XOFF);
        objYOff = getByte(ARG_YOFF);
        objSpSize = SpriteSize.valueOf(namespace.getInt(ARG_TILESIZE));
        objTileOff = getByte(ARG_TILEOFF);
        applySmall = getBool(ARG_APPLYSMALL);
        applyLarge = getBool(ARG_APPLYLARGE);
        metatileWidth = getByte(ARG_METAW);
        metatileHeight = getByte(ARG_METAH);
        input = ImageIO.read(new File(namespace.getString(ARG_INPUT)));

        try {
            ((Cmd) namespace.get("cmd")).execute();
        } catch (Exception e) {
            System.err.println("ERROR: ".concat(e.getMessage()));
        }

        System.out.println("Bye");
    }

    private static Boolean getBool(String arg) {
        Boolean bool = namespace.getBoolean(arg);

        if (bool == null) {
            return false;
        }

        return bool;
    }

    private static byte getByte(String arg) {
        Integer anInt = namespace.getInt(arg);

        if (anInt == null) {
            return 0;
        }

        return anInt.byteValue();
    }
}
