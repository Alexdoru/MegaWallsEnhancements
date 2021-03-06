package fr.alexdoru.fkcountermod.utils;

import fr.alexdoru.megawallsenhancementsmod.gui.LastWitherHPGui;
import fr.alexdoru.megawallsenhancementsmod.utils.StringUtil;
import net.minecraft.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScoreboardParser {

    private static final Pattern GAME_ID_PATTERN = Pattern.compile("\\s*\\d+/\\d+/\\d+\\s+([\\d\\w]+)\\s*", Pattern.CASE_INSENSITIVE);
    private static final Pattern MW_TITLE_PATTERN = Pattern.compile("\\s*MEGA\\sWALLS\\s*", Pattern.CASE_INSENSITIVE);
    private static final Pattern MW_INGAME_PATTERN = Pattern.compile("[0-9]+\\sFinals?\\s[0-9]+\\sF\\.\\sAssists?");
    private static final Pattern PREGAME_LOBBY_PATTERN = Pattern.compile("Players:\\s*[0-9]+/[0-9]+");
    private static final Pattern WITHER_ALIVE_PATTERN = Pattern.compile("\\s*\\[.\\] Wither HP: ?(\\d+).*", Pattern.CASE_INSENSITIVE);
    private static final Pattern WITHER_ALIVE_HEART_PATTERN = Pattern.compile("\\s*\\[.\\] Wither [\u2764\u2665]: ?(\\d+).*", Pattern.CASE_INSENSITIVE);

    private final ArrayList<String> aliveWithers = new ArrayList<>();
    private String gameId = null;
    private boolean isInMwGame = false;
    private boolean isMWEnvironement = false;
    private boolean preGameLobby = false;
    private boolean isitPrepPhase = false;
    private boolean hasgameended = false;

    /* This is run on every tick to parse the scoreboard data */
    public ScoreboardParser(Scoreboard scoreboard) {
        if (scoreboard == null) {
            return;
        }

        String title = ScoreboardUtils.getUnformattedSidebarTitle(scoreboard);
        if (!MW_TITLE_PATTERN.matcher(title).matches()) {
            return;
        }
        isMWEnvironement = true;
        List<String> formattedSidebarLines = ScoreboardUtils.getFormattedSidebarText(scoreboard);
        List<String> unformattedSidebarLines = ScoreboardUtils.stripControlCodes(formattedSidebarLines);

        if (unformattedSidebarLines.size() == 0) {
            return;
        }

        Matcher matcher = GAME_ID_PATTERN.matcher(unformattedSidebarLines.get(0));
        if (!matcher.matches()) {
            for (String line : unformattedSidebarLines) {
                if (PREGAME_LOBBY_PATTERN.matcher(line).find()) {
                    gameId = null;
                    preGameLobby = true;
                    isInMwGame = false;
                    return;
                }
            }
            return;
        }

        gameId = matcher.group(1);

        for (String line : unformattedSidebarLines) {
            if (MW_INGAME_PATTERN.matcher(line).find()) {
                isInMwGame = true;
                continue;
            }
            if (PREGAME_LOBBY_PATTERN.matcher(line).find()) {
                gameId = null;
                preGameLobby = true;
                isInMwGame = false;
                return;
            }
        }

        if (unformattedSidebarLines.size() < 7) {
            return;
        }

        if (unformattedSidebarLines.get(1).contains("Walls Fall:") || unformattedSidebarLines.get(1).contains("Gates Open:")) {
            isitPrepPhase = true;
        }

        int eliminated_teams = 0;
        int witherHP = 1000;

        for (int i = 3; i < Math.min(unformattedSidebarLines.size(), 7); i++) {

            String line = unformattedSidebarLines.get(i);
            /*Wither alive detection*/
            final Matcher matcher1 = WITHER_ALIVE_PATTERN.matcher(line);
            String colorCode;
            if (matcher1.matches()) {
                String formattedLine = formattedSidebarLines.get(i);
                colorCode = StringUtil.getLastColorCodeBefore(formattedLine, "\\[").replace("\u00a7", "");
                aliveWithers.add(colorCode);
                witherHP = Integer.parseInt(matcher1.group(1));
            } else {
                final Matcher matcher2 = WITHER_ALIVE_HEART_PATTERN.matcher(line);
                if (matcher2.matches()) {
                    String formattedLine = formattedSidebarLines.get(i);
                    colorCode = StringUtil.getLastColorCodeBefore(formattedLine, "\\[").replace("\u00a7", "");
                    aliveWithers.add(colorCode);
                    witherHP = 2 * Integer.parseInt(matcher2.group(1));
                }
            }

            if (line.contains("eliminated!")) {
                eliminated_teams++;
            }

        }

        if (eliminated_teams == 3 || unformattedSidebarLines.get(1).contains("None!:")) {
            hasgameended = true;
        }

        if (isOnlyOneWitherAlive()) {
            LastWitherHPGui.instance.updateWitherHP(witherHP);
        }

    }

    public boolean isWitherAlive(String colorCode) {
        return aliveWithers.contains(colorCode);
    }

    public boolean areAllWithersAlive() {
        return aliveWithers.size() == 4;
    }

    public String getGameId() {
        return gameId;
    }

    public List<String> getAliveWithers() {
        return aliveWithers;
    }

    public boolean hasGameEnded() {
        return hasgameended;
    }

    public boolean isitPrepPhase() {
        return isitPrepPhase;
    }

    public boolean isOnlyOneWitherAlive() {
        return aliveWithers.size() == 1;
    }

    public boolean isMWEnvironement() {
        return isMWEnvironement;
    }

    public boolean isInMwGame() {
        return isInMwGame;
    }

    public boolean isPreGameLobby() {
        return preGameLobby;
    }
}
