package samoth69.plugin_main;

import org.bukkit.entity.Player;

import java.util.UUID;

public class Joueur {
    private Player player;
    private java.util.UUID UUID;
    private Equipe equipe;

    Joueur(Player player) {
        this.player = player;
        this.UUID = player.getUniqueId();
    }

    public Player getJoueur() {
        return player;
    }

    public UUID getUUID() {
        return UUID;
    }

    public Equipe getEquipe() {return equipe;}

    public void setEquipe(Equipe equipe) {this.equipe = equipe;}

}
