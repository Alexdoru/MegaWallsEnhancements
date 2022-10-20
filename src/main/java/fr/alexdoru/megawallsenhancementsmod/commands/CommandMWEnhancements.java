package fr.alexdoru.megawallsenhancementsmod.commands;

import fr.alexdoru.megawallsenhancementsmod.MegaWallsEnhancementsMod;
import fr.alexdoru.megawallsenhancementsmod.api.apikey.HypixelApiKeyUtil;
import fr.alexdoru.megawallsenhancementsmod.chat.ChatUtil;
import fr.alexdoru.megawallsenhancementsmod.config.ConfigHandler;
import fr.alexdoru.megawallsenhancementsmod.data.PrestigeVCache;
import fr.alexdoru.megawallsenhancementsmod.gui.guiscreens.GeneralConfigGuiScreen;
import fr.alexdoru.megawallsenhancementsmod.scoreboard.ScoreboardUtils;
import fr.alexdoru.megawallsenhancementsmod.utils.DelayedTask;
import fr.alexdoru.megawallsenhancementsmod.utils.NameUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collections;
import java.util.List;

public class CommandMWEnhancements extends CommandBase {

    @Override
    public String getCommandName() {
        return "mwenhancements";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/mwenhancements";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("refreshconfig")) {
            ConfigHandler.preinit(MegaWallsEnhancementsMod.configurationFile);
            ChatUtil.addChatMessage(ChatUtil.getTagMW() + EnumChatFormatting.GREEN + "Reloaded values from the config file.");
            return;
        } else if (args.length >= 1 && args[0].equalsIgnoreCase("setapikey")) {
            if (args.length != 2) {
                ChatUtil.addChatMessage(EnumChatFormatting.RED + "Usage : " + getCommandUsage(sender) + " setapikey <key>" + "\n"
                        + EnumChatFormatting.RED + "Connect on Hypixel and type \"/api new\" to get an Api key");
            } else {
                HypixelApiKeyUtil.setApiKey(args[1]);
            }
            return;
        } else if (args.length >= 1 && args[0].equalsIgnoreCase("clearcache")) {
            PrestigeVCache.clearCache();
            ChatUtil.addChatMessage(ChatUtil.getTagMW() + EnumChatFormatting.GREEN + "Cleared " + EnumChatFormatting.GOLD + "Prestige V" + EnumChatFormatting.GREEN + " data Cache");
            NameUtil.refreshAllNamesInWorld();
            return;
        } else if (args.length >= 1 && args[0].equalsIgnoreCase("howplaygame")) {
            final String title = ScoreboardUtils.getUnformattedSidebarTitle();
            if (title != null && title.contains("MEGA WALLS")) {
                final String msg1 = "During the first 6 minutes you have to mine iron, make armor and store everything in your enderchest";
                final String msg2 = "Once the walls fall down you can go to mid and fight other players, each class has unique abilities";
                final String msg3 = "Every team has a wither, you have to protect yours and kill the withers from the other teams";
                final String msg4 = "Once a wither is dead the players from that team can't respawn, be the last team standing to win";
                final String msg5 = "More informations about the game: https://hypixel.net/threads/the-complete-mega-walls-guide.3489088/";
                sendChatMessage(msg1);
                new DelayedTask(() -> sendChatMessage(msg2), 80);
                new DelayedTask(() -> sendChatMessage(msg3), 155);
                new DelayedTask(() -> sendChatMessage(msg4), 240);
                new DelayedTask(() -> sendChatMessage(msg5), 320);
            } else {
                ChatUtil.addChatMessage(ChatUtil.getTagMW() + EnumChatFormatting.RED + "Command only works in Mega Walls");
            }
            return;
        }
        new DelayedTask(() -> Minecraft.getMinecraft().displayGuiScreen(new GeneralConfigGuiScreen()), 1);
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        final String[] possibilities = {"clearcache", "howplaygame", "refreshconfig", "setapikey"};
        return getListOfStringsMatchingLastWord(args, possibilities);
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("megawallsenhancements");
    }

    private void sendChatMessage(String msg) {
        if (Minecraft.getMinecraft().thePlayer != null) {
            Minecraft.getMinecraft().thePlayer.sendChatMessage(msg);
        }
    }

}
