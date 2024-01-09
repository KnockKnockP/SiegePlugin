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

    public Location deposit = null, spawn = null;

    public SiegeTeam(Team team) {
        this.team = team;
    }

    public String[] buildDescription() {
        return new String[] {
            String.format("Base: %s %s", LocationExtensions.toBlockTriple(base[0]),
                LocationExtensions.toBlockTriple(base[1])),
            String.format("Wool 0: %s", LocationExtensions.toBlockTriple(wools[0])),
            String.format("Wool 1: %s", LocationExtensions.toBlockTriple(wools[1])),
            String.format("Wool 2: %s", LocationExtensions.toBlockTriple(wools[2])),
            String.format("Deposit: %s", LocationExtensions.toBlockTriple(deposit)),
            String.format("Spawn: %s", LocationExtensions.toBlockTriple(spawn))
        };
    }

    public boolean validate() {
        return !((base[0] == null) ||
            (base[1] == null) ||
            (wools[0] == null) ||
            (wools[1] == null) ||
            (wools[2] == null) ||
            (deposit == null) ||
            (spawn == null));
    }
}