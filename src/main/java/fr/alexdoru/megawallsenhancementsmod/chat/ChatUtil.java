package fr.alexdoru.megawallsenhancementsmod.chat;

import fr.alexdoru.megawallsenhancementsmod.asm.accessors.ChatComponentTextAccessor;
import fr.alexdoru.megawallsenhancementsmod.asm.accessors.NetworkPlayerInfoAccessor_ChatHeads;
import fr.alexdoru.megawallsenhancementsmod.asm.hooks.NetHandlerPlayClientHook;
import fr.alexdoru.megawallsenhancementsmod.scoreboard.ScoreboardTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.*;

public class ChatUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static String getTagMW() {
        return EnumChatFormatting.GOLD + "[" + EnumChatFormatting.DARK_GRAY + "MWE" + EnumChatFormatting.GOLD + "] ";
    }

    public static String getTagNoCheaters() {
        return EnumChatFormatting.GOLD + "[" + EnumChatFormatting.DARK_GRAY + "NoCheaters" + EnumChatFormatting.GOLD + "] ";
    }

    public static String getTagHitboxes() {
        return EnumChatFormatting.BLUE + "[Hitbox] ";
    }

    public static void addChatMessage(String msg) {
        addChatMessage(new ChatComponentText(msg));
    }

    public static void addChatMessage(String msg, String playername) {
        addChatMessage(new ChatComponentText(msg), playername);
    }

    public static void addChatMessage(IChatComponent component, String playername) {
        addSkinToComponent(component, playername);
        addChatMessage(component);
    }

    public static void addChatMessage(IChatComponent msg) {
        addChatMessage(msg, mc.isCallingFromMinecraftThread());
    }

    private static void addChatMessage(IChatComponent msg, boolean isCallingFromMinecraftThread) {
        if (isCallingFromMinecraftThread) {
            if (mc.theWorld != null && mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(msg);
            }
        } else {
            mc.addScheduledTask(() -> {
                if (mc.theWorld != null && mc.thePlayer != null) {
                    mc.thePlayer.addChatMessage(msg);
                }
            });
        }
    }

    public static void addSkinToComponent(IChatComponent msg, String playername) {
        if (msg instanceof ChatComponentTextAccessor && ((ChatComponentTextAccessor) msg).getSkinChatHead() == null) {
            tryAddSkinToComponent(msg, playername);
        }
    }

    public static boolean tryAddSkinToComponent(IChatComponent msg, String playername) {
        final NetworkPlayerInfo netInfo = NetHandlerPlayClientHook.getPlayerInfo(playername);
        if (netInfo instanceof NetworkPlayerInfoAccessor_ChatHeads) {
            final SkinChatHead skin = new SkinChatHead(netInfo.getLocationSkin());
            ((ChatComponentTextAccessor) msg).setSkinChatHead(skin);
            ((NetworkPlayerInfoAccessor_ChatHeads) netInfo).setSkinChatHead(skin);
            return true;
        } else {
            final ResourceLocation resourceLocation = NetHandlerPlayClientHook.getPlayerSkin(playername);
            if (resourceLocation != null) {
                ((ChatComponentTextAccessor) msg).setSkinChatHead(new SkinChatHead(resourceLocation));
                return true;
            }
        }
        return false;
    }

    public static void printIChatList(String listtitle, IChatComponent imessagebody, int displaypage, int nbpage, String command, EnumChatFormatting barColor, IChatComponent titleHoverText, String titleURL) {
        final IChatComponent titleLine = getListTitleLine(listtitle, displaypage, nbpage, command, titleHoverText, titleURL);
        addChatMessage(new ChatComponentText(barColor + bar() + "\n")
                .appendSibling(titleLine)
                .appendText("\n")
                .appendSibling(imessagebody)
                .appendText(barColor + bar())
        );
    }

    public static IChatComponent getListTitleLine(String listtitle, int displaypage, int nbpage, String command, IChatComponent titleHoverText, String titleURL) {
        final IChatComponent titleLine = new ChatComponentText("             ");
        if (displaypage > 1) {
            titleLine.appendSibling(new ChatComponentText(EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + " <<")
                    .setChatStyle(new ChatStyle()
                            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.YELLOW + "Click to view page " + (displaypage - 1))))
                            .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command + " " + (displaypage - 1)))));
        } else {
            titleLine.appendText("   ");
        }
        final IChatComponent titleComponent = new ChatComponentText(EnumChatFormatting.GOLD + " " + listtitle + " (Page " + displaypage + " of " + nbpage + ")");
        if (titleHoverText != null && titleURL != null) {
            titleComponent.setChatStyle(new ChatStyle()
                    .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, titleHoverText))
                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, titleURL)));
        }
        titleLine.appendSibling(titleComponent);
        if (displaypage < nbpage) {
            titleLine.appendSibling(new ChatComponentText(EnumChatFormatting.YELLOW + "" + EnumChatFormatting.BOLD + " >>")
                    .setChatStyle(new ChatStyle()
                            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.YELLOW + "Click to view page " + (displaypage + 1))))
                            .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command + " " + (displaypage + 1)))));
        }
        return titleLine;
    }

    public static void printApikeySetupInfo() {
        addChatMessage(getTagMW() + EnumChatFormatting.RED + "You didn't set up your Api key. If you have an Api Key, use "
                + EnumChatFormatting.YELLOW + "\"/mwe setapikey <key>\""
                + EnumChatFormatting.RED + " to use it with the mod.");
    }

    public static String inexistantMinecraftNameMsg(String playername) {
        return EnumChatFormatting.RED + "The name " + EnumChatFormatting.YELLOW + playername + EnumChatFormatting.RED + " doesn't exist, it might be a nick.";
    }

    public static String invalidMinecraftNameMsg(String playername) {
        return EnumChatFormatting.RED + "The name " + EnumChatFormatting.YELLOW + playername + EnumChatFormatting.RED + " isn't a valid Minecraft username.";
    }

    /**
     * Draws a bar that takes the width of the chat window
     */
    public static String bar() {
        final char separator = '-';
        final int chatWidth = mc.ingameGUI.getChatGUI().getChatWidth();
        final int separatorWidth = mc.fontRendererObj.getCharWidth(separator);
        return EnumChatFormatting.STRIKETHROUGH + new String(new char[chatWidth / separatorWidth]).replace("\0", "-");
    }

    /**
     * Returns the message with spaces at the start to make the message centered in the chat box
     */
    public static String centerLine(String message) {
        return getSeparatorToCenter(message) + message;
    }

    /**
     * Returns the amounts of spaces needed to make a message centered
     */
    public static String getSeparatorToCenter(String message) {
        final char space = ' ';
        final int chatWidth = mc.ingameGUI.getChatGUI().getChatWidth();
        final int separatorWidth = mc.fontRendererObj.getCharWidth(space);
        final int messageWidth = mc.fontRendererObj.getStringWidth(message);
        if (messageWidth >= chatWidth) {
            return "";
        }
        return new String(new char[(chatWidth - messageWidth) / (2 * separatorWidth)]).replace("\0", " ");
    }

    /**
     * Returns a formatted message, the input matrix needs to be square.
     * If the message cannot be formatted (chat box too small for instance) the unformatted message is returned
     */
    public static String alignText(String[][] messagematrix) {

        final char separator = ' ';
        final int chatWidth = mc.ingameGUI.getChatGUI().getChatWidth();
        final int separatorWidth = mc.fontRendererObj.getCharWidth(separator);
        int columnWidth = 0;
        int maxLineWidth = 0;

        for (final String[] line : messagematrix) {
            final StringBuilder linemessage = new StringBuilder();
            for (final String msg : line) {
                linemessage.append(msg);
                columnWidth = Math.max(columnWidth, mc.fontRendererObj.getStringWidth(msg));
            }
            maxLineWidth = Math.max(maxLineWidth, mc.fontRendererObj.getStringWidth(linemessage.toString()));
        }

        String leftSeparatorText = "";

        if (chatWidth > maxLineWidth) {
            leftSeparatorText = new String(new char[(chatWidth - maxLineWidth) / (2 * separatorWidth)]).replace("\0", String.valueOf(separator));
        }

        final StringBuilder message = new StringBuilder();

        for (final String[] strings : messagematrix) { // lines
            for (int j = 0; j < strings.length; j++) { // columns

                if (j == 0) { // first element on the left

                    final int messageWidth = mc.fontRendererObj.getStringWidth(strings[j]);
                    message.append(leftSeparatorText).append(strings[j]).append(new String(new char[(columnWidth - messageWidth) / (separatorWidth)]).replace("\0", String.valueOf(separator)));

                } else if (j == strings.length - 1) { // last element on the right

                    message.append(strings[j]).append("\n");

                } else { // element in the middle

                    final int messageWidth = mc.fontRendererObj.getStringWidth(strings[j]);
                    message.append(strings[j]).append(new String(new char[(columnWidth - messageWidth) / (separatorWidth)]).replace("\0", String.valueOf(separator)));

                }

            }
        }

        return message.toString();

    }

    /**
     * Returns the integer as a String with a space for thousands delimiter
     */
    public static String formatInt(int number) {
        return formatLong(number);
    }

    /**
     * Returns the integer as a String with a space for thousands delimiter
     */
    public static String formatLong(long number) {
        final String str = String.valueOf(number);
        final char separator = ' ';
        int iterator = 1;
        final StringBuilder msg = new StringBuilder();
        for (int i = str.length() - 1; i >= 0; i--) {
            msg.insert(0, ((iterator == 3 && i != 0) ? String.valueOf(separator) : "") + str.charAt(i));
            if (iterator == 3) {
                iterator = 1;
            } else {
                iterator++;
            }
        }
        return msg.toString();
    }

    /**
     * Converts int from 1 to 5 to roman
     */
    public static String intToRoman(int number) {
        switch (number) {
            case (1):
                return "I";
            case (2):
                return "II";
            case (3):
                return "III";
            case (4):
                return "IV";
            case (5):
                return "V";
            default:
                return String.valueOf(number);
        }
    }

    public static IChatComponent PlanckeHeaderText(String formattedname, String playername, String titletext) {
        return new ChatComponentText(getSeparatorToCenter(formattedname + EnumChatFormatting.GOLD + titletext))
                .appendSibling(new ChatComponentText(formattedname)
                        .setChatStyle(new ChatStyle()
                                .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/names " + playername))
                                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.YELLOW + "Click for name history")))))

                .appendSibling(new ChatComponentText(EnumChatFormatting.GOLD + titletext)
                        .setChatStyle(new ChatStyle()
                                .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://plancke.io/hypixel/player/stats/" + playername))
                                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.YELLOW + "Click to open Plancke in browser")))));
    }

    public static IChatComponent getReportButtons(String playername, String cheatReport, String cheatWDR, ClickEvent.Action actionreport, ClickEvent.Action actionwdr) {
        return getReportButton(playername, cheatReport, actionreport).appendSibling(getWDRButton(playername, cheatWDR, actionwdr));
    }

    public static IChatComponent getReportButton(String playername, String cheatReport, ClickEvent.Action actionreport) {
        return new ChatComponentText(EnumChatFormatting.DARK_GREEN + " [Report]")
                .setChatStyle(new ChatStyle()
                        .setChatClickEvent(new ClickEvent(actionreport, "/report " + playername + " " + cheatReport))
                        .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ChatComponentText(EnumChatFormatting.GREEN + "Click this message to report this player" + "\n"
                                        + EnumChatFormatting.YELLOW + "Command : " + EnumChatFormatting.RED + "/report " + playername + " " + cheatReport + "\n"
                                        + EnumChatFormatting.GRAY + "Using the report option won't save the cheater's name in the mod NoCheaters"
                                        + getReportingAdvice()))));
    }

    public static IChatComponent getWDRButton(String playername, String cheatWDR, ClickEvent.Action actionwdr) {
        return new ChatComponentText(EnumChatFormatting.DARK_PURPLE + " [WDR]")
                .setChatStyle(new ChatStyle()
                        .setChatClickEvent(new ClickEvent(actionwdr, "/wdr " + playername + " " + cheatWDR))
                        .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ChatComponentText(EnumChatFormatting.GREEN + "Click this message to report this player" + "\n"
                                        + EnumChatFormatting.YELLOW + "Command : " + EnumChatFormatting.RED + "/wdr " + playername + " " + cheatWDR + "\n"
                                        + EnumChatFormatting.GRAY + "Using the wdr option will give you warnings about this player ingame\n"
                                        + EnumChatFormatting.GRAY + "You can use " + EnumChatFormatting.YELLOW + "/unwdr " + playername + EnumChatFormatting.GRAY + " to remove them from your report list"
                                        + getReportingAdvice()))));
    }

    public static IChatComponent getUnIgnoreButton(String uuid, String playername) {
        return new ChatComponentText(EnumChatFormatting.YELLOW + " [Un-Ignore]")
                .setChatStyle(new ChatStyle()
                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nocheaters ignoreremove " + uuid + " " + playername))
                        .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ChatComponentText(EnumChatFormatting.GREEN + "Click this message to remove that player from your ignore list,\n"
                                        + EnumChatFormatting.GREEN + "you will receive all future report suggestions comming from them"))));
    }

    public static IChatComponent getIgnoreButton(String playername) {
        return new ChatComponentText(EnumChatFormatting.YELLOW + " [Ignore]")
                .setChatStyle(new ChatStyle()
                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nocheaters ignore " + playername))
                        .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ChatComponentText(EnumChatFormatting.GREEN + "Click this message to cancel the report from this player\n"
                                        + EnumChatFormatting.GREEN + "and ignore all future report suggestions comming from them\n"
                                        + EnumChatFormatting.YELLOW + "Command : " + EnumChatFormatting.RED + "/nocheaters ignore " + playername + "\n"
                                        + EnumChatFormatting.GRAY + "You can un-ignore them by opening the ignore list with\n"
                                        + EnumChatFormatting.YELLOW + "/nocheaters ignorelist"))));
    }

    public static IChatComponent getCancelButton(String playername) {
        return new ChatComponentText(EnumChatFormatting.RED + " [Cancel]")
                .setChatStyle(new ChatStyle()
                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nocheaters cancelreport " + playername))
                        .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ChatComponentText(EnumChatFormatting.GREEN + "Click this message to cancel the report for this player"))));
    }

    public static IChatComponent getCancelAllReportsButton() {
        return new ChatComponentText(EnumChatFormatting.RED + " [Cancel All Reports]")
                .setChatStyle(new ChatStyle()
                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nocheaters clearreportqueue"))
                        .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ChatComponentText(EnumChatFormatting.GREEN + "Click this message to cancel all report suggestions about to be sent"))));
    }

    public static String getReportingAdvice() {
        final String s = "\n\n" + EnumChatFormatting.RED + "To make reporting efficient, be sure to report" + EnumChatFormatting.DARK_RED + " when you are ingame with\n"
                + EnumChatFormatting.DARK_RED + "the cheater" + EnumChatFormatting.RED + " and not before the game starts or in the lobby.";
        return ScoreboardTracker.isPreGameLobby ? s : "";
    }

    public static void printReportingAdvice() {
        addChatMessage(getTagNoCheaters() + EnumChatFormatting.RED + "To make reporting efficient, be sure to report" + EnumChatFormatting.DARK_RED + " when you are ingame with the cheater " + EnumChatFormatting.RED + "and not before the game starts or in the lobby. This way a replay can be attached to the report and it will have a higher chance to be reviewed.");
    }

    public static IChatComponent formattedNameWithReportButton(String playername, String formattedName) {
        return new ChatComponentText(formattedName).setChatStyle(new ChatStyle()
                .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/report " + playername + " cheating"))
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ChatComponentText(EnumChatFormatting.GREEN + "Click this message to report this player" + "\n"
                                + EnumChatFormatting.YELLOW + "Command : " + EnumChatFormatting.RED + "/report " + playername + " cheating" + "\n"
                                + EnumChatFormatting.GRAY + "Using the report option won't save the cheater's name in the mod NoCheaters"))));
    }

    public static void debug(String msg) {
        addChatMessage(EnumChatFormatting.AQUA + "[Debug]: " + EnumChatFormatting.RESET + msg);
    }

}
