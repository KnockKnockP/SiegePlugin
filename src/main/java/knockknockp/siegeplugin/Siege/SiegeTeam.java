package knockknockp.siegeplugin.Siege;

import org.bukkit.Location;
import org.bukkit.scoreboard.Team;

public final class SiegeTeam {
    public Team team;
    public Location[] base = new Location[2];
    {
        base[0] = null;
        base[1] = null;
    }

    public Location[] wools = new Location[3];
    {
        wools[0] = null;
        wools[1] = null;
        wools[2] = null;
    }

    public Location deposit = null;

    public SiegeTeam(Team team) {
        this.team = team;
    }
}