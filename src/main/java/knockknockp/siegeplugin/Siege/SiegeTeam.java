package knockknockp.siegeplugin.Siege;

import org.bukkit.Location;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public final class SiegeTeam {
    public Team team;
    public Location[] base = new Location[2];
    {
        base[0] = null;
        base[1] = null;
    }

    public List<Location> wools = new ArrayList<>();

    public Location deposit = null, spawn = null;

    public SiegeTeam(Team team) {
        this.team = team;
    }

    public String[] buildDescription() {
        List<String> strings = new ArrayList<>();
        strings.add(String.format("Base: %s %s", LocationExtensions.toBlockTriple(base[0]),
            LocationExtensions.toBlockTriple(base[1])));
        strings.add(String.format("Deposit: %s", LocationExtensions.toBlockTriple(deposit)));
        strings.add(String.format("Spawn: %s", LocationExtensions.toBlockTriple(spawn)));

        int woolsSize = wools.size();
        for (int i = 0; i < woolsSize; ++i) {
            strings.add(String.format("Wool %d: %s", i, LocationExtensions.toBlockTriple(wools.get(i))));
        }
        return strings.toArray(new String[woolsSize]);
    }

    public boolean validate() {
        return !((base[0] == null) ||
            (base[1] == null) ||
            (wools.isEmpty()) ||
            (deposit == null) ||
            (spawn == null));
    }
}