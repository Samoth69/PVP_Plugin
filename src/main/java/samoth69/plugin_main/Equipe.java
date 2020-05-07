package samoth69.plugin_main;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.*;

import java.util.*;

public class Equipe implements Listener {
    //private ArrayList<Joueur> joueurs = new ArrayList<>();
    private HashMap<UUID, Joueur> joueurs = new HashMap<>();

    private final String nomShort;
    private final String nomComplet;
    private final short id;
    private final short couleurEquipe;

    private static Main main;

    private ItemStack teamWool;
    private ScoreboardManager sm;
    private Scoreboard board;
    //private Objective objective;
    //private Team scoreboardTeam;

    private boolean teamIsAlive = true;

    public Equipe(String nomShort, String nomComplet, short id, short couleurEquipe, ScoreboardManager sm, Main m) {
        this.nomShort = nomShort;
        this.nomComplet = nomComplet;
        this.id = id;
        this.couleurEquipe = couleurEquipe;

        //Main.numberOfTeams++;
        //Main.numberOfAliveTeam++;

        main = m;
        this.teamWool = new ItemStack(Material.WOOL, 1, (short)couleurEquipe);

        this.sm = sm;
        this.board = sm.getNewScoreboard();
    };

    public void updateScoreboard(final ArrayList<String> globalText) {
        ArrayList<String> text = new ArrayList<>();
        text.addAll(globalText);

        text.set(0, globalText.get(0) + this.getChatColor() + this.nomComplet);

        for (Map.Entry<UUID, Joueur> j : joueurs.entrySet()) {
            j.getValue().updateScoreboard(text);
        }

        /*if (this.objective != null)
            this.objective.unregister();
        this.objective = this.board.registerNewObjective("objT" + nomShort, "dummy");

        this.objective.setDisplayName(text.get(0)); //limite: 32
        text.remove(0);

        int counter = 15;
        for (String s : text) {
            Score score = this.objective.getScore(s);
            score.setScore(counter);
            counter--;
        }

        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);*/
    }

    public String getTeamName() {
        return nomShort;
    }

    public short getCouleurEquipe() {
        return couleurEquipe;
    }

    public ItemStack getTeamWool() {
        return teamWool;
    }

    public ChatColor getChatColor() {
        return Utils.getChatColorFromInt(couleurEquipe);
    }

    public HashMap<UUID, Joueur> getJoueursHash() {return joueurs;}

    public Collection<Joueur> getJoueurs() {return joueurs.values();}

    public void addJoueur(Joueur j) {
        if (!this.joueurs.containsKey(j.getUUID())) {
            this.joueurs.put(j.getUUID(), j);
            //this.scoreboardTeam.addEntry(j.getPseudo());
            j.setEquipe(this);
        }
    }

    public void removeJoueur(Joueur j) {
        this.joueurs.remove(j.getUUID());
        //this.scoreboardTeam.removeEntry(j.getPseudo());
    }

    public boolean containJoueur(Joueur j) {
        return this.joueurs.containsKey(j.getUUID());
    }

    public Scoreboard getScoreboard() {
        return board;
    }

    public boolean isTeamAlive() {
        return teamIsAlive;
    }

    public String getNomShort() {
        return nomShort;
    }
}
