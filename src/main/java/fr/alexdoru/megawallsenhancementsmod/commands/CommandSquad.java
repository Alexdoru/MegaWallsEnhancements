package fr.alexdoru.megawallsenhancementsmod.commands;

import fr.alexdoru.fkcountermod.utils.ScoreboardUtils;
import fr.alexdoru.megawallsenhancementsmod.events.SquadEvent;
import fr.alexdoru.megawallsenhancementsmod.utils.TabCompletionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import static fr.alexdoru.megawallsenhancementsmod.utils.ChatUtil.*;

public class CommandSquad extends CommandBase {

    @Override
    public String getCommandName() {
        return "squad";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/squad <add|disband|list|remove>";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

        Minecraft mc = Minecraft.getMinecraft();
        String title = null;

        if (mc.theWorld != null) {
            Scoreboard scoreboard = mc.theWorld.getScoreboard();
            if (scoreboard != null) {
                title = ScoreboardUtils.getUnformattedSidebarTitle(scoreboard);
            }
        }

        if (title != null && title.contains("THE HYPIXEL PIT")) {
            mc.thePlayer.sendChatMessage("/squad " + buildString(args, 0));
            return;
        }

        if (args.length < 1) {
            addChatMessage(getCommandHelp());
            return;
        }

        if (args[0].equalsIgnoreCase("add")) {

            if (args.length < 2) {
                addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Usage : /squad <add> <playername>"));
                return;
            }

            if (args.length >= 4 && args[2].equalsIgnoreCase("as")) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 3; i < args.length; i++) {
                    stringBuilder.append(args[i]).append((i == args.length - 1) ? "" : " ");
                }
                String alias = stringBuilder.toString();
                SquadEvent.addPlayer(args[1], alias);
                addChatMessage(new ChatComponentText(getTagMW() +
                        EnumChatFormatting.GREEN + "Added " + EnumChatFormatting.GOLD + args[1] + EnumChatFormatting.GREEN + " as " +
                        EnumChatFormatting.GOLD + alias + EnumChatFormatting.GREEN + " to the squad."));
                return;
            }

            for (int i = 1; i < args.length; i++) {
                SquadEvent.addPlayer(args[i]);
                addChatMessage(new ChatComponentText(getTagMW() +
                        EnumChatFormatting.GREEN + "Added " + EnumChatFormatting.GOLD + args[i] + EnumChatFormatting.GREEN + " to the squad."));
            }

        } else if (args[0].equalsIgnoreCase("disband")) {

            SquadEvent.clearSquad();
            addChatMessage(new ChatComponentText(getTagMW() + EnumChatFormatting.GREEN + "Removed all players from the squad."));

        } else if (args[0].equalsIgnoreCase("l") || args[0].equalsIgnoreCase("list")) {

            HashMap<String, String> squad = SquadEvent.getSquad();

            if (squad.isEmpty()) {
                addChatMessage(new ChatComponentText(getTagMW() + EnumChatFormatting.RED + "No one in the squad right now."));
                return;
            }

            IChatComponent imsg = new ChatComponentText(getTagMW() + EnumChatFormatting.GREEN + "Players in your squad : \n");

            for (Entry<String, String> entry : squad.entrySet()) {
                String displayname = entry.getKey();
                String squadname = entry.getValue();
                imsg.appendSibling(new ChatComponentText(EnumChatFormatting.DARK_GRAY + "- " + EnumChatFormatting.GOLD + displayname
                        + (displayname.equals(squadname) ? "" : EnumChatFormatting.GREEN + " renamed as : " + EnumChatFormatting.GOLD + entry.getValue()) + "\n"));
            }

            addChatMessage(imsg);

        } else if (args[0].equalsIgnoreCase("remove")) {

            if (args.length < 2) {
                addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Usage : /squad <remove> <playername>"));
                return;
            }

            for (int i = 1; i < args.length; i++) {
                if (SquadEvent.removePlayer(args[i])) {
                    addChatMessage(new ChatComponentText(getTagMW() +
                            EnumChatFormatting.GREEN + "Removed " + EnumChatFormatting.GOLD + args[i] + EnumChatFormatting.GREEN + " from the squad."));
                } else {
                    addChatMessage(new ChatComponentText(getTagMW() +
                            EnumChatFormatting.GOLD + args[i] + EnumChatFormatting.RED + " isn't in the squad."));
                }
            }

        } else {
            addChatMessage(getCommandHelp());
        }

    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        String[] args1 = {"add", "disband", "list", "remove"};
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, args1) : args.length >= 2 ? getListOfStringsMatchingLastWord(args, TabCompletionUtil.getOnlinePlayersByName()) : null;
    }

    private IChatComponent getCommandHelp() {

        return new ChatComponentText(EnumChatFormatting.GREEN + bar() + "\n"
                + centerLine(EnumChatFormatting.GOLD + "Squad Help\n\n")
                + EnumChatFormatting.YELLOW + "/squad add <player>" + EnumChatFormatting.GRAY + " - " + EnumChatFormatting.AQUA + "add a player to the squad\n"
                + EnumChatFormatting.YELLOW + "/squad add <player> as Nickname" + EnumChatFormatting.GRAY + " - " + EnumChatFormatting.AQUA + "add a player to the squad and change their name\n"
                + EnumChatFormatting.YELLOW + "/squad remove <player>" + EnumChatFormatting.GRAY + " - " + EnumChatFormatting.AQUA + "remove a player from the squad\n"
                + EnumChatFormatting.YELLOW + "/squad list" + EnumChatFormatting.GRAY + " - " + EnumChatFormatting.AQUA + "list players in the squad\n"
                + EnumChatFormatting.YELLOW + "/squad disband" + EnumChatFormatting.GRAY + " - " + EnumChatFormatting.AQUA + "disband the squad\n"
                + EnumChatFormatting.GREEN + bar()
        );

    }

}
