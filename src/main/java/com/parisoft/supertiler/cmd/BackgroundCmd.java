package com.parisoft.supertiler.cmd;

import com.parisoft.supertiler.pojo.TileMap;
import com.parisoft.supertiler.util.Palettes;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Subparser;

import static com.parisoft.supertiler.SuperTiler.ARG_APPLYLARGE;
import static com.parisoft.supertiler.SuperTiler.ARG_BPP;
import static com.parisoft.supertiler.SuperTiler.ARG_INPUT;
import static com.parisoft.supertiler.SuperTiler.ARG_MODE;
import static com.parisoft.supertiler.SuperTiler.ARG_NO_DISCARD_FLIP;
import static com.parisoft.supertiler.SuperTiler.ARG_NO_DISCARD_REDUNDANT;
import static com.parisoft.supertiler.SuperTiler.ARG_PALETTE;
import static com.parisoft.supertiler.SuperTiler.ARG_PALNUM;
import static com.parisoft.supertiler.SuperTiler.ARG_PRIORITY;
import static com.parisoft.supertiler.SuperTiler.ARG_TILEMAP;
import static com.parisoft.supertiler.SuperTiler.ARG_TILEOFF;
import static com.parisoft.supertiler.SuperTiler.ARG_TILESET;
import static com.parisoft.supertiler.SuperTiler.ARG_VERBOSE;

public class BackgroundCmd implements Cmd {

    @Override
    public void execute() throws Exception {
        TileMap tileMap = new TileMap();

        if (tileMap.isEmpty()) {
            throw new IllegalStateException("No tiles found");
        }

        tileMap.write();
        Palettes.write();
    }

    @SuppressWarnings("Duplicates")
    public static void create(ArgumentParser parser) {
        Subparser bg = parser.addSubparsers()
                .addParser("background", true)
                .aliases("bg")
                .description("Generates " + ARG_TILESET + ", " + ARG_PALETTE + "s, and " + ARG_TILEMAP + "s from image")
                .defaultHelp(true)
                .setDefault("cmd", new BackgroundCmd());
        bg.addArgument("-i", "--" + ARG_INPUT).nargs("?").required(true).type(String.class).help("Input indexed PNG image");
        bg.addArgument("-t", "--" + ARG_TILESET).nargs("?").required(false).type(String.class).help("Output " + ARG_TILESET + " file");
        bg.addArgument("-p", "--" + ARG_PALETTE).nargs("?").required(false).type(String.class).help("Output " + ARG_PALETTE + " file");
        bg.addArgument("-m", "--" + ARG_TILEMAP).nargs("?").required(false).type(String.class).help("Output " + ARG_TILEMAP + " file");
        bg.addArgument("-M", "--" + ARG_MODE).nargs("?").required(false).type(String.class).choices("snes", "nes").setDefault("snes").help("Mode for target console");
        bg.addArgument("-B", "--" + ARG_BPP).nargs("?").required(false).type(Integer.class).choices(2, 4, 8).setDefault(4).help("Depth or number of colors per pixel");
        bg.addArgument("-L", "--" + ARG_APPLYLARGE).nargs("?").required(false).type(Boolean.class).setDefault(false).setConst(true).help("Set tile size to 16x16 pixels");
        bg.addArgument("-D", "--"+ARG_NO_DISCARD_REDUNDANT).nargs("?").required(false).type(Boolean.class).setDefault(false).setConst(true).help("Don't discard redundant tiles");
        bg.addArgument("-F", "--"+ARG_NO_DISCARD_FLIP).nargs("?").required(false).type(Boolean.class).setDefault(false).setConst(true).help("Don't discard using tile flipping");
        bg.addArgument("--" + ARG_PALNUM).nargs("?").required(false).type(Integer.class).choices(0, 1, 2, 3, 4, 5, 6, 7).setDefault(0).help("Background " + ARG_PALETTE + " number");
        bg.addArgument("--" + ARG_PRIORITY).nargs("?").required(false).type(Integer.class).choices(0, 1).setDefault(0).help("Background " + ARG_PRIORITY + " (SNES only)");
        bg.addArgument("-T", "--" + ARG_TILEOFF).nargs("?").required(false).type(Integer.class).setDefault(0).help("Background tile offset");
        bg.addArgument("-v", "--"+ARG_VERBOSE).nargs("?").required(false).type(Boolean.class).setDefault(false).setConst(true).help("Enable verbose error output");
    }
}
