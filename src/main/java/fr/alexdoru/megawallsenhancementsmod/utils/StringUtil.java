package fr.alexdoru.megawallsenhancementsmod.utils;

import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import javax.annotation.Nonnull;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)" + '\u00a7' + "[0-9A-FK-OR]");
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("(?i)" + '\u00a7' + "[0-9A-F]");

    /**
     * On hypixel the chat messages sent by players follow the pattern:
     * <p>
     * Alexdoru: the message sent by the player
     * <p>
     * With a bunch of color codes everywhere
     * This method adds injectedText after the name of the sender and before the ": ".
     * In addition, it can add text after the ": " which is the start of the players message.
     * In addition, it can clean all the formatting codes in the rest of the message.
     * <p>
     * Example:
     * sample text : "§6[MVP§8++§6] Kyotone§f§r§f: §r§eAlexdoru§r§f is bhopping§r"
     * after first split : "§6[MVP§8++§6] " + "Kyotone" + "§f§r§f: §r§eAlexdoru§r§f is bhopping§r"
     * after second split :"§6[MVP§8++§6] " + "Kyotone" + "§f§r§f" + ": " + "§r§eAlexdoru§r§f is bhopping§r"
     */
    public static String insertAfterName(String message, String messageSender, @Nonnull String injectedText, String injectAtMsgStart, boolean cleanEnd) {
        String[] split = message.split(messageSender, 2);
        if (split.length != 2) {
            return message;
        }
        String[] secondSplit = split[1].split(": ", 2);
        if (secondSplit.length != 2) {
            return split[0] + messageSender + injectedText + (cleanEnd ? EnumChatFormatting.getTextWithoutFormattingCodes(split[1]) : split[1]);
        }
        return split[0] + messageSender + injectedText + secondSplit[0] + ": " + injectAtMsgStart + (cleanEnd ? EnumChatFormatting.getTextWithoutFormattingCodes(secondSplit[1]) : secondSplit[1]);
    }

    /**
     * Changes the color of the target inside a message while keeping the original color after that
     */
    public static String changeColorOf(String message, String target, EnumChatFormatting color) {
        String[] split = message.split(target, 2);
        if (split.length != 2) {
            return message;
        }
        return split[0] + color + target + getLastFormattingCodeOf(split[0]) + split[1];
    }

    /**
     * Self-explanatory, returns "" if it can't find the last formatting code
     */
    public static String getLastFormattingCodeOf(String text) {
        final Matcher matcher = FORMATTING_CODE_PATTERN.matcher(text);
        String s = "";
        while (matcher.find()) {
            s = matcher.group();
        }
        return s;
    }

    public static String getLastFormattingCodeBefore(String message, String target) {
        String[] split = message.split(target, 2);
        if (split.length != 2) {
            return "";
        }
        return getLastFormattingCodeOf(split[0]);
    }

    /**
     * Returns only color codes, Self-explanatory, returns "" if it can't find the last color code
     */
    public static String getLastColorCodeOf(String text) {
        final Matcher matcher = COLOR_CODE_PATTERN.matcher(text);
        String s = "";
        while (matcher.find()) {
            s = matcher.group();
        }
        return s;
    }

    public static String getLastColorCodeBefore(String message, String target) {
        String[] split = message.split(target, 2);
        if (split.length != 2) {
            return "";
        }
        return getLastColorCodeOf(split[0]);
    }

    public static IChatComponent censorChatMessage(String message, String messageSender) {
        String[] split = message.split(messageSender, 2);
        if (split.length != 2) {
            return new ChatComponentText(message);
        }
        String[] secondSplit = split[1].split(": ", 2);
        if (secondSplit.length != 2) {
            return new ChatComponentText(message);
        }
        return (new ChatComponentText(split[0] + messageSender + secondSplit[0] + ": "))
                .appendSibling(new ChatComponentText(EnumChatFormatting.DARK_GRAY + "Censored")
                        .setChatStyle(new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(secondSplit[1])))));
    }

    //private static final Pattern FORMATTING_CODE_END_OF_STRING_PATTERN = Pattern.compile('\u00a7' + "[0-9A-FK-OR]$");
    ///* NEEDS TO BE TESTED
    // * Removes all formatting codes located directly before the target string
    // * Only does it for the first occurence of that string
    // */
    //public static String removeFormattingCodesBefore(String message, String target) {
    //    String[] split = message.split(target, 2);
    //    if (split.length != 2) {
    //        return message;
    //    }
    //    Matcher matcher = FORMATTING_CODE_END_OF_STRING_PATTERN.matcher(split[0]);
    //    while (matcher.matches()) {
    //        split[0] = split[0].substring(0, split[0].length() - 2);
    //        matcher = FORMATTING_CODE_END_OF_STRING_PATTERN.matcher(split[0]);
    //    }
    //    return split[0] + target + split[1];
    //}

}
