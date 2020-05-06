package samoth69.plugin_main;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.UUID;

public class Joueur implements Listener {
    private Player player;
    private java.util.UUID UUID;
    private Equipe equipe;
    private String pseudo;

    Joueur(Player player) {
        this.player = player;
        this.UUID = player.getUniqueId();
        this.pseudo = player.getDisplayName();
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

            this.player.setScoreboard(equipe.getScoreboard());
            this.player.setDisplayName(equipe.getChatColor() + this.player.getName() + ChatColor.RESET);

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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Joueur) {
            return ((Joueur) obj).getUUID().equals(this.getUUID());
        } else if (obj instanceof Player) {
            return ((Player) obj).getUniqueId().equals(this.getUUID());
        }
        return false;
    }

}
