package samoth69.plugin;

import com.comphenix.packetwrapper.WrapperPlayServerPlayerInfo;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

public class Joueur implements Listener {
    private Player player;
    private java.util.UUID UUID;
    private Equipe equipe;
    private String pseudo;
    private JavaPlugin jp;
    private Main main;
    private boolean alive = true;
    private int numberOfKills = 0;
    private double totalDamage = 0;

    private ScoreboardManager sm;
    private Scoreboard board;
    private Objective sidebarObjective;

    private boolean isTaupe = false;
    private boolean kitClaimed = false;

    Joueur(Main main, Player player, ScoreboardManager sm) {
        this.main = main;
        this.jp = main.getJp();
        this.player = player;
        this.UUID = player.getUniqueId();
        this.pseudo = player.getName();
        this.sm = sm;
        this.board = sm.getNewScoreboard();
        this.player.setCompassTarget(new Location(Bukkit.getWorlds().get(0),0,0,0)); //on dit au compass de pointer en 0 0
    }

    public Player getJoueur() {
        return player;
    }

    public UUID getUUID() {
        return UUID;
    }

    public Equipe getEquipe() {return equipe;}

    public void addKill() {
        this.numberOfKills++;
    }

    public int getNumberOfKills() {return this.numberOfKills;}

    public boolean setEquipe(Equipe equipe) {
        if (this.equipe == null || equipe == null || !equipe.containJoueur(this))
        {
            if (this.equipe != null)
                this.equipe.removeJoueur(this);

            this.equipe = equipe;
            if (equipe != null) {
                this.equipe.addJoueur(this);
                this.player.setScoreboard(board);
                this.player.setDisplayName(this.equipe.getChatColor() + this.equipe.getNomShort() + " " + this.player.getName() + ChatColor.RESET);
            } else {
                this.player.setScoreboard(null);
                this.player.setDisplayName(this.player.getName());
            }

            PlayerInfoData pid = new PlayerInfoData(WrappedGameProfile.fromPlayer(this.player), 1,
                    EnumWrappers.NativeGameMode.SURVIVAL,
                    WrappedChatComponent.fromText(this.getPseudoWithTeam()));
            WrapperPlayServerPlayerInfo wpspi = new WrapperPlayServerPlayerInfo();
            wpspi.setAction(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
            wpspi.setData(Collections.singletonList(pid));
            for(Player p : Bukkit.getOnlinePlayers())
            {
                if(p.equals(this.player))
                {
                    continue;
                }
                p.hidePlayer(this.player);
                wpspi.sendPacket(p);
            }

            ProtocolLibrary.getProtocolManager().addPacketListener(
                    new PacketAdapter(main.getJp(), PacketType.Play.Server.PLAYER_INFO)
                    {

                        @Override
                        public void onPacketSending(PacketEvent event)
                        {

                            if(event.getPacket().getPlayerInfoAction().read(0) != EnumWrappers.PlayerInfoAction.ADD_PLAYER)
                            {
                                return;
                            }

                            PlayerInfoData pid = event.getPacket().getPlayerInfoDataLists().read(0).get(0);

                            if(!pid.getProfile().getUUID().equals(UUID)) // Here you can do something to ensure you're changing the name of the correct guy
                            {
                                return;
                            }

                            String pseudo = getPseudoWithTeamAndColor();
                            if (pseudo.length() >= 16) {
                                pseudo = pseudo.substring(0, 16);
                            }

                            PlayerInfoData newPid = new PlayerInfoData(pid.getProfile().withName(pseudo), pid.getPing(), pid.getGameMode(),
                                    WrappedChatComponent.fromText(pseudo));
                            event.getPacket().getPlayerInfoDataLists().write(0, Collections.singletonList(newPid));
                        }
                    }
            );

            this.player.setPlayerListName(getPseudoWithTeamAndColor());

            for(Player p : Bukkit.getOnlinePlayers())
            {
                if(p.equals(this.player))
                {
                    continue;
                }
                p.showPlayer(this.player);
            }

            return true;
        } else {
            return false;
        }
    }

    public void removeTeam() {
        if (this.equipe != null) {
            this.equipe.removeJoueur(this);
            this.equipe = null;
        }
    }

    public String getPseudo() {return pseudo;}

    public String getPseudoWithTeam() {
        if (this.equipe != null) {
            return this.equipe.getNomShort() + " " + pseudo;
        } else {
            return pseudo;
        }

    }

    public String getPseudoWithTeamAndColor() {
        if (this.equipe != null) {
            return this.equipe.getChatColor() + this.getPseudoWithTeam();
        } else {
            return this.getPseudoWithTeam();
        }

    }

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
        this.equipe.notifyPlayerDead();
    }

    public void updateScoreboard(final ArrayList<String> globalText, int index) {
        ArrayList<String> text = new ArrayList<>(globalText);

        if (this.sidebarObjective != null)
            this.sidebarObjective.unregister();
        this.sidebarObjective = this.board.registerNewObjective("sidebar", "dummy");

        this.sidebarObjective.setDisplayName(text.get(0)); //limite: 32
        text.remove(0);

        text.add(index, Main.startText + ChatColor.GRAY + "Tués: " + ChatColor.RED + numberOfKills);
        text.set(index + 2, Main.startText + ChatColor.GRAY + "Centre: " + ChatColor.YELLOW + getCentreValue());

        int counter = 15;
        for (String s : text) {
            Score score = this.sidebarObjective.getScore(s);
            score.setScore(counter);
            counter--;
        }

        this.player.setScoreboard(board);
        this.sidebarObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    private String getCentreValue() {
        double x = this.player.getLocation().getX();
        double z = this.player.getLocation().getZ();
        final String[] arrows = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖" };
        int dist = (int)Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2)); //théorème de Pytagore, calcul de la distance sur un plan, on ignore la hauteur 'y' ici.
        /*
        double angleSpawnJoueur = 0;

        if (x == 0.0) {
            x = 0.1; //on évite la division par 0 en créant une légère érreur.
        }

        if (x >= 0 && z >= 0) {
            angleSpawnJoueur = Math.tan(z / x);
        } else if (x >= 0 && z <= 0) {
            angleSpawnJoueur = Math.tan(z / x);
        }
        */
        return String.valueOf(dist);
    }

    public boolean isInTeam() {return this.equipe != null;}

    public boolean isTaupe() {
        return isTaupe;
    }

    public void setTaupe(boolean taupe) {
        isTaupe = taupe;
    }

    public boolean isKitClaimed() {
        return kitClaimed;
    }

    public void setKitClaimed(boolean kitClaimed) {
        this.kitClaimed = kitClaimed;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getDistanceFromMid() {
        return (int)Math.sqrt(Math.pow(this.player.getLocation().getBlockX(), 2) + Math.pow(this.player.getLocation().getBlockZ(), 2));
    }

    public double getTotalDamage() {
        return totalDamage;
    }

    public void addTotalDamage(double dmg) {
        Bukkit.getLogger().info(this.pseudo + " dmg " + dmg);
        this.totalDamage += dmg;
    }

    public static Comparator<Joueur> compTopDmg() {
        return new Comparator<Joueur>() {
            @Override
            public int compare(Joueur j1, Joueur j2) {
                return Double.compare(j2.getTotalDamage(), j1.getTotalDamage());
            }
        };
    }

    public static Comparator<Joueur> compKillCount() {
        return new Comparator<Joueur>() {
            @Override
            public int compare(Joueur j1, Joueur j2) {
                return Double.compare(j2.getNumberOfKills(), j1.getNumberOfKills());
            }
        };
    }

}
