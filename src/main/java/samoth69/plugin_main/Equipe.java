package samoth69.plugin_main;

import org.bukkit.ChatColor;

import java.util.ArrayList;

public class Equipe {
    private ArrayList<Joueur> Joueurs = new ArrayList<>();
    private String nom;
    private ChatColor couleurEquipe;

    public Equipe(String nom, ChatColor couleurEquipe) {
        this.nom = nom;
        this.couleurEquipe = couleurEquipe;
    };

    public String getNom() {
        return nom;
    }

    public ChatColor getCouleurEquipe() {
        return couleurEquipe;
    }

}
