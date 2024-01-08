package knockknockp.siegeplugin.Siege;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;

public final class SiegeManager {
    private final ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
    public final Scoreboard scoreboard;
    {
        assert (scoreboardManager != null);
        scoreboard = scoreboardManager.getNewScoreboard();
    }

    public Map<Player, TeamPlayer> players = new HashMap<Player, TeamPlayer>();
    public final Map<Teams, SiegeTeam> teams = new HashMap<Teams, SiegeTeam>();

    private final String redString = Teams.RED.toString(), blueString = Teams.BLUE.toString();
    private final Objective scoreObjective = scoreboard.registerNewObjective("scoreObjective", Criteria.DUMMY, "Score");
    private final Score redScore = scoreObjective.getScore(redString), blueScore = scoreObjective.getScore(blueString);

    public boolean isGameRunning = false;

    public SiegeManager() {
        SiegeTeam redTeam = new SiegeTeam(scoreboard.registerNewTeam(redString)), blueTeam = new SiegeTeam(scoreboard.registerNewTeam(blueString));
        redTeam.team.setPrefix(ChatColor.RED + redString + ChatColor.WHITE + " ");
        blueTeam.team.setPrefix(ChatColor.BLUE + blueString + ChatColor.WHITE + " ");

        redTeam.team.setAllowFriendlyFire(false);
        blueTeam.team.setAllowFriendlyFire(false);

        teams.put(Teams.RED, redTeam);
        teams.put(Teams.BLUE, blueTeam);

        scoreObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        reset();
    }

    public void setBase(Teams team, Location location1, Location location2) {
        SiegeTeam siegeTeam = teams.get(team);
        siegeTeam.base[0] = location1;
        siegeTeam.base[1] = location2;
    }

    private void reset() {
        redScore.setScore(0);
        blueScore.setScore(0);
    }

    public void start() {
        isGameRunning = true;
        reset();
    }

    public void incrementScore(Teams team) {
        if (team == Teams.RED) {
            redScore.setScore(redScore.getScore() + 1);
        } else {
            blueScore.setScore(blueScore.getScore() + 1);
        }

        if (redScore.getScore() == 3) {
            win(Teams.RED);
        } else if (blueScore.getScore() == 3) {
            win(Teams.BLUE);
        }
    }

    private void win(Teams team) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            int red = redScore.getScore(), blue = blueScore.getScore();

            ChatColor color = ChatColor.RED;
            if (team == Teams.BLUE) {
                color = ChatColor.BLUE;
            }

            String teamName = "빨강팀";
            if (team == Teams.BLUE) {
                teamName = "청팀";
            }
            player.sendTitle(color + teamName + " 승리", String.format(ChatColor.YELLOW + "%d - %d", Math.max(red, blue), Math.min(red, blue)), 2, 70, 10);
        }
    }
}