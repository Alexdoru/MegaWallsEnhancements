package fr.alexdoru.megawallsenhancementsmod.events;

import fr.alexdoru.fkcountermod.FKCounterMod;
import fr.alexdoru.fkcountermod.events.KillCounter;
import fr.alexdoru.fkcountermod.events.MwGameEvent;
import fr.alexdoru.fkcountermod.utils.DelayedTask;
import fr.alexdoru.megawallsenhancementsmod.asm.hooks.NetHandlerPlayClientHook;
import fr.alexdoru.megawallsenhancementsmod.config.ConfigHandler;
import fr.alexdoru.megawallsenhancementsmod.gui.ArrowHitGui;
import fr.alexdoru.megawallsenhancementsmod.gui.HunterStrengthGui;
import fr.alexdoru.megawallsenhancementsmod.gui.KillCooldownGui;
import fr.alexdoru.megawallsenhancementsmod.utils.ChatUtil;
import fr.alexdoru.megawallsenhancementsmod.utils.HypixelApiKeyUtil;
import fr.alexdoru.megawallsenhancementsmod.utils.PartyDetection;
import fr.alexdoru.megawallsenhancementsmod.utils.StringUtil;
import fr.alexdoru.nocheatersmod.data.WDR;
import fr.alexdoru.nocheatersmod.data.WdredPlayers;
import fr.alexdoru.nocheatersmod.events.GameInfoGrabber;
import fr.alexdoru.nocheatersmod.events.ReportQueue;
import fr.alexdoru.nocheatersmod.util.ReportSuggestionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatEvents {

    private static final Minecraft mc = Minecraft.getMinecraft();
    public static final ResourceLocation STRENGTH_SOUND = new ResourceLocation("item.fireCharge.use");
    private static final String BAN_MESSAGE = "A player has been removed from your game.";
    private static final String HUNTER_STRENGTH_MESSAGE = "Your Force of Nature gave you a 5 second Strength I buff.";
    private static final String GENERAL_START_MESSAGE = "The game starts in 1 second!";
    private static final String OWN_WITHER_DEATH_MESSAGE = "Your wither has died. You can no longer respawn!";
    private static final String PREP_PHASE = "Prepare your defenses!";
    private static final Pattern API_KEY_PATTERN = Pattern.compile("^Your new API key is ([a-zA-Z0-9-]+)");
    private static final Pattern COINS_PATTERN = Pattern.compile("^\\+\\d+ coins!( \\((?:Triple Coins \\+ EXP, |)(?:Active Booster, |)\\w+'s Network Booster\\)).*");
    private static final Pattern DREADLORD_STRENGTH_PATTERN = Pattern.compile("\u00a74\u00a7lSOUL SIPHON \u00a7c\u00a7l85% ([0-9])s");
    private static final Pattern HEROBRINE_STRENGTH_PATTERN = Pattern.compile("\u00a7e\u00a7lPOWER \u00a7c\u00a7l85% ([0-9])s");
    private static final Pattern HUNTER_PRE_STRENGTH_PATTERN = Pattern.compile("\u00a7a\u00a7lF\\.O\\.N\\. \u00a77\\(\u00a7l\u00a7c\u00a7lStrength\u00a77\\) \u00a7e\u00a7l([0-9]+)");
    private static final Pattern PLAYER_JOIN_PATTERN = Pattern.compile("^(\\w{1,16}) has joined \\([0-9]{1,3}/[0-9]{1,3}\\)!");
    private static final Pattern ZOMBIE_STRENGTH_PATTERN = Pattern.compile("\u00a72\u00a7lBERSERK \u00a7c\u00a7l75% ([0-9])s");
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("^(?:|\\[SHOUT\\] |\\[SPECTATOR\\] )(?:|\\[[A-Z]{3,6}\\] )(?:|\\[((?:MV|VI)P\\+?\\+?)\\] )(\\w{2,16}):.*");
    private static long lastStrength = 0;
    private static final HashSet<String> MW_REPETITVE_MSG = new HashSet<>();

    static {
        MW_REPETITVE_MSG.add("You broke your protected chest");
        MW_REPETITVE_MSG.add("You broke your protected trapped chest");
        MW_REPETITVE_MSG.add("Get to the middle to stop the hunger!");
        MW_REPETITVE_MSG.add("Your Salvaging skill returned your arrow to you!");
        MW_REPETITVE_MSG.add("Your Efficiency skill got you an extra drop!");
        MW_REPETITVE_MSG.add("Your Soothing Moo Skill is ready!");
        MW_REPETITVE_MSG.add("Click your sword or bow to activate your skill!");
        MW_REPETITVE_MSG.add("Your Eagles Eye Skill is ready!");
        MW_REPETITVE_MSG.add("Left Click with any bow to activate your skill!");
        MW_REPETITVE_MSG.add("Your From the Depths Skill is ready!");
    }

    @SubscribeEvent
    public void onMWGameStart(MwGameEvent event) {
        if (event.getType() == MwGameEvent.EventType.GAME_START || event.getType() == MwGameEvent.EventType.GAME_END ) {
            ReportSuggestionHandler.clearReportSuggestionHistory();
            ReportQueue.INSTANCE.clearPlayersReportedThisGame();
        }
    }

    @SubscribeEvent
    public void onChatMessage(ClientChatReceivedEvent event) {

        /*normal chat messages*/
        if (event.type == 0) {

            String msg = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());
            String fmsg = event.message.getFormattedText();

            if (FKCounterMod.isInMwGame && ConfigHandler.hideRepetitiveMWChatMsg && MW_REPETITVE_MSG.contains(msg)) {
                event.setCanceled(true);
                return;
            }

            if (msg.equals(GENERAL_START_MESSAGE)) {
                GameInfoGrabber.saveinfoOnGameStart();
                SquadEvent.formSquad();
                return;
            }

            if (msg.equals(OWN_WITHER_DEATH_MESSAGE)) {
                KillCooldownGui.hideGUI();
                return;
            }

            /*
             * shortens the coins messages removing the booster info
             */
            if (ConfigHandler.shortencoinmessage) {
                Matcher matchercoins = COINS_PATTERN.matcher(msg);
                if (matchercoins.matches()) {
                    event.message = new ChatComponentText(fmsg.replace(matchercoins.group(1), ""));
                    return;
                }
            }

            if (msg.equals(PREP_PHASE)) {
                MinecraftForge.EVENT_BUS.post(new MwGameEvent(MwGameEvent.EventType.GAME_START));
                return;
            }

            if (KillCounter.processMessage(fmsg, msg)) {
                event.setCanceled(true);
                return;
            }

            if (ConfigHandler.show_ArrowHitHUD && ArrowHitGui.processMessage(msg, fmsg)) {
                event.setCanceled(true);
                return;
            }

            Matcher matcher = MESSAGE_PATTERN.matcher(msg);
            String senderRank = null;
            String messageSender = null;
            String squadname = null;
            if (matcher.matches()) {
                senderRank = matcher.group(1);
                messageSender = matcher.group(2);
                squadname = SquadEvent.getSquad().get(messageSender);
            }

            if (ReportSuggestionHandler.parseReportMessage(senderRank, messageSender, squadname, msg, fmsg)) {
                event.setCanceled(true);
                return;
            }

            // Chat Censoring
            if (ConfigHandler.censorCheaterChatMsg && messageSender != null) {
                final NetworkPlayerInfo networkPlayerInfo = NetHandlerPlayClientHook.playerInfoMap.get(messageSender);
                if (networkPlayerInfo != null) {
                    final String uuid = networkPlayerInfo.getGameProfile().getId().toString().replace("-", "");
                    final WDR wdr = WdredPlayers.getPlayer(uuid, messageSender);
                    if (wdr != null && wdr.hasValidCheats()) {
                        if (!ConfigHandler.deleteCheaterChatMsg) {
                            ChatUtil.addChatMessage(StringUtil.censorChatMessage(fmsg, messageSender));
                        }
                        event.setCanceled(true);
                        return;
                    }
                }
            }

            if (squadname != null && messageSender != null) {
                event.message = new ChatComponentText(fmsg.replaceFirst(messageSender, squadname));
            }

            if (MWGameStatsEvent.processMessage(msg)) {
                return;
            }

            if (ConfigHandler.strengthHUD && msg.equals(HUNTER_STRENGTH_MESSAGE)) {
                HunterStrengthGui.instance.setStrengthRenderStart(5000L);
                return;
            }

            if (parseAPIKey(msg)) {
                return;
            }

            if (FKCounterMod.preGameLobby) {
                Matcher playerJoinMatcher = PLAYER_JOIN_PATTERN.matcher(msg);
                if (playerJoinMatcher.matches()) {
                    PartyDetection.onPlayerJoin(playerJoinMatcher.group(1), System.currentTimeMillis());
                    return;
                }
            }

            if (msg.equals(BAN_MESSAGE)) {
                new DelayedTask(NetHandlerPlayClientHook::printDisconnectedPlayers, 20);
                //return;
            }

            /*Status messages*/
        } else if (ConfigHandler.strengthHUD && event.type == 2) {

            String fmsg = event.message.getFormattedText();

            Matcher dreadStrenghtMatcher = DREADLORD_STRENGTH_PATTERN.matcher(fmsg);
            if (dreadStrenghtMatcher.find()) {
                HunterStrengthGui.instance.setStrengthRenderStart(Long.parseLong(dreadStrenghtMatcher.group(1)) * 1000L);
                return;
            }

            Matcher preStrengthMatcher = HUNTER_PRE_STRENGTH_PATTERN.matcher(fmsg);
            if (preStrengthMatcher.find()) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastStrength > 11000L) {
                    lastStrength = currentTime;
                    mc.getSoundHandler().playSound(PositionedSoundRecord.create(STRENGTH_SOUND, 0.0F));
                }
                String preStrengthTimer = preStrengthMatcher.group(1);
                HunterStrengthGui.instance.setPreStrengthTime(preStrengthTimer, currentTime);
                return;
            }

            Matcher herobrineStrenghtMatcher = HEROBRINE_STRENGTH_PATTERN.matcher(fmsg);
            if (herobrineStrenghtMatcher.find()) {
                HunterStrengthGui.instance.setStrengthRenderStart(Long.parseLong(herobrineStrenghtMatcher.group(1)) * 1000L);
                return;
            }

            Matcher zombieStrenghtMatcher = ZOMBIE_STRENGTH_PATTERN.matcher(fmsg);
            if (zombieStrenghtMatcher.find()) {
                HunterStrengthGui.instance.setStrengthRenderStart(Long.parseLong(zombieStrenghtMatcher.group(1)) * 1000L);
            }

        }

    }

    /*
     * automatically sets up the api key on hypixel when you type /api new
     */
    private boolean parseAPIKey(String msg) {
        Matcher matcherapikey = API_KEY_PATTERN.matcher(msg);
        if (matcherapikey.matches()) {
            HypixelApiKeyUtil.setApiKey(matcherapikey.group(1));
            return true;
        }
        return false;
    }

}
