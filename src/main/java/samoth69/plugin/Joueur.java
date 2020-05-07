package samoth69.plugin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.UUID;

public class Joueur implements Listener {
    private Player player;
    private java.util.UUID UUID;
    private Equipe equipe;
    private String pseudo;
    private JavaPlugin jp;
    private boolean alive = true;
    private int numberOfKills = 0;
    private int totalDamage = 0;

    private ScoreboardManager sm;
    private Scoreboard board;
    private Objective sidebarObjective;
    private Objective listObjective;
    private Objective belowNameObjective;

    Joueur(JavaPlugin jp, Player player, ScoreboardManager sm) {
        this.jp = jp;
        this.player = player;
        this.UUID = player.getUniqueId();
        this.pseudo = player.getName();
        this.sm = sm;
        this.board = sm.getNewScoreboard();
        this.listObjective = board.registerNewObjective("team", "dummy");
        this.listObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);


    }

    public Player getJoueur() {
        return player;
    }

    public UUID getUUID() {
        return UUID;
    }

    public Equipe getEquipe() {return equipe;}

    public boolean setEquipe(Equipe equipe) {
        if (this.equipe == null || !equipe.containJoueur(this))
        {
            if (this.equipe != null)
                this.equipe.removeJoueur(this);

            this.equipe = equipe;
            this.equipe.addJoueur(this);

            this.player.setScoreboard(board);

            //this.scoreboardTeam.setPrefix(equipe.getChatColor() + "");
            //this.scoreboardTeam.setAllowFriendlyFire(true);
            //this.scoreboardTeam.setCanSeeFriendlyInvisibles(true);

            this.player.setDisplayName(this.equipe.getChatColor() + this.equipe.getNomShort() + " " + this.player.getName() + ChatColor.RESET);
            //this.aboveHeadPseudo = this.player.getDisplayName();
            //this.player.setPlayerListName(this.player.getDisplayName());
            //this.scoreboardTeam.setPrefix(this.equipe.getChatColor() + this.equipe.getNomShort());

            //this.listObjective.setDisplayName(equipe.getChatColor() + equipe.getNomShort() + player.getName());

            return true;
        } else {
            return false;
        }
    }

    public void removeTeam() {
        this.equipe.removeJoueur(this);
        this.equipe = null;
    }

    public String getPseudo() {return pseudo;}

    public String getPseudoWithTeam() {return this.equipe.getNomShort() + " " + pseudo;}

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Joueur) {
            return ((Joueur) obj).getUUID().equals(this.getUUID());
        } else if (obj instanceof Player) {
            return ((Player) obj).getUniqueId().equals(this.getUUID());
        }
        return false;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void updateScoreboard(final ArrayList<String> globalText) {
        ArrayList<String> text = new ArrayList<>();
        text.addAll(globalText);

        if (this.sidebarObjective != null)
            this.sidebarObjective.unregister();
        this.sidebarObjective = this.board.registerNewObjective("sidebar", "dummy");

        this.sidebarObjective.setDisplayName(text.get(0)); //limite: 32
        text.remove(0);

        text.add(2, Main.startText + ChatColor.GRAY + "Tués: " + ChatColor.RED + numberOfKills);

        text.add(player.getDisplayName());

        int counter = 15;
        for (String s : text) {
            Score score = this.sidebarObjective.getScore(s);
            score.setScore(counter);
            counter--;
        }

        this.player.setScoreboard(board);
        this.sidebarObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public boolean isInTeam() {return this.equipe != null;}

}
