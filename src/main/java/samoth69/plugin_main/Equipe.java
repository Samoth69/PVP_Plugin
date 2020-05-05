package samoth69.plugin_main;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;

public class Equipe {
    private ArrayList<Joueur> joueurs = new ArrayList<>();
    private final String nom;
    private final short couleurEquipe;
    private ItemStack teamWool;
    private Scoreboard board;
    private Objective objective;
    private Team teamScoreboard;

    public Equipe(String nom, short couleurEquipe, Scoreboard board, Objective obj) {
        this.nom = nom;
        this.couleurEquipe = couleurEquipe;

        this.teamWool = new ItemStack(Material.WOOL, 1, (short)couleurEquipe);
        this.board = board;
        this.objective = obj;
        this.teamScoreboard = this.board.registerNewTeam(nom);
        teamScoreboard.addEntry("ae: " + nom);
    };

    public String getTeamName() {
        return nom;
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

    public ArrayList<Joueur> getJoueurs() {return joueurs;}

}
