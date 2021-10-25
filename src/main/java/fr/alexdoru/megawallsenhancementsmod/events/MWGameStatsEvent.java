package fr.alexdoru.megawallsenhancementsmod.events;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.alexdoru.fkcountermod.events.MwGameEvent;
import fr.alexdoru.fkcountermod.utils.DelayedTask;
import fr.alexdoru.megawallsenhancementsmod.api.exceptions.ApiException;
import fr.alexdoru.megawallsenhancementsmod.api.hypixelplayerdataparser.LoginData;
import fr.alexdoru.megawallsenhancementsmod.api.hypixelplayerdataparser.MegaWallsClassSkinData;
import fr.alexdoru.megawallsenhancementsmod.api.hypixelplayerdataparser.MegaWallsClassStats;
import fr.alexdoru.megawallsenhancementsmod.api.requests.HypixelPlayerData;
import fr.alexdoru.megawallsenhancementsmod.commands.CommandScanGame;
import fr.alexdoru.megawallsenhancementsmod.utils.ChatUtil;
import fr.alexdoru.megawallsenhancementsmod.utils.HypixelApiKeyUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MWGameStatsEvent {

	private static String chosen_class;
	private static boolean isRandom = false;
	/*Data downloaded at the start of the game*/
	private static MegaWallsClassStats MWclassStats;
	/*Stats of the last game*/
	private static MegaWallsClassStats gameStats;
	private static String formattedname;
	
	private static final Pattern RANDOM_CLASS_PATTERN = Pattern.compile("^Random class: (\\w+)*");

	@SubscribeEvent
	public void onMwGame(MwGameEvent event) {

		if(event.getType() == MwGameEvent.EventType.GAME_START) {
			CommandScanGame.onGameStart();
			onGameStart();
		}

		if(event.getType() == MwGameEvent.EventType.GAME_END) {
			CommandScanGame.clearScanGameData();
			new DelayedTask(() -> onGameEnd(), 300);
		}		

	}

	private static void onGameStart() {
		if(!HypixelApiKeyUtil.isApiKeySetup()) {return;}

		(new Thread(() -> {
			String uuid = Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replace("-", "");
			try {			
				HypixelPlayerData playerdata = new HypixelPlayerData(uuid, HypixelApiKeyUtil.getApiKey());			
				if(formattedname == null) {
					LoginData logindata = new LoginData(playerdata.getPlayerData());
					formattedname = logindata.getFormattedName();
				}
				if(!isRandom) {
					MegaWallsClassSkinData mwclassskindata = new MegaWallsClassSkinData(playerdata.getPlayerData());
					chosen_class = mwclassskindata.getCurrentmwclass().toLowerCase();
				}
				MWclassStats = new MegaWallsClassStats(playerdata.getPlayerData(), chosen_class);
			} catch (ApiException e) {
				e.printStackTrace();
				ChatUtil.addChatMessage((IChatComponent)new ChatComponentText(ChatUtil.getTagMW() + EnumChatFormatting.RED + e.getMessage()));
			}
			isRandom = false;
		})).start();
	}

	private static void onGameEnd() {
		if(!HypixelApiKeyUtil.isApiKeySetup()) {return;}

		(new Thread(() -> {
			String uuid = Minecraft.getMinecraft().thePlayer.getUniqueID().toString().replace("-", "");
			try {
				HypixelPlayerData playerdata = new HypixelPlayerData(uuid, HypixelApiKeyUtil.getApiKey());
				gameStats = new MegaWallsClassStats(playerdata.getPlayerData(), chosen_class);
				if(MWclassStats == null || gameStats == null) {return;}
				gameStats.minus(MWclassStats);
				ChatUtil.addChatMessage((IChatComponent)new ChatComponentText(ChatUtil.getTagMW() + EnumChatFormatting.YELLOW + "Click to view the stats of your " + EnumChatFormatting.AQUA + "Mega Walls " + EnumChatFormatting.YELLOW + "game!")
						.setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mwgamestats"))));
			} catch (ApiException e) {
				e.printStackTrace();
				ChatUtil.addChatMessage((IChatComponent)new ChatComponentText(ChatUtil.getTagMW() + EnumChatFormatting.RED + e.getMessage()));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		})).start();
	}

	public static MegaWallsClassStats getGameStats() {
		return gameStats;
	}

	public static String getFormattedname() {
		return formattedname;
	}

	public static boolean processMessage(String msg) {
		Matcher matcher = RANDOM_CLASS_PATTERN.matcher(msg);		
		if(matcher.matches()) {
			chosen_class = matcher.group(1).toLowerCase();
			isRandom = true;
			return true;
		}
		return false;		
	}
}
