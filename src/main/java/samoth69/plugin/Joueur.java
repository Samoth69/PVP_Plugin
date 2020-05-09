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
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.Collections;
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
    private int totalDamage = 0;

    private ScoreboardManager sm;
    private Scoreboard board;
    private Objective sidebarObjective;
    private Objective listObjective;
    private Objective belowNameObjective;

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

    public void addKill() {
        this.numberOfKills++;
    }

    public boolean setEquipe(Equipe equipe) {
        if (this.equipe == null || !equipe.containJoueur(this))
        {
            if (this.equipe != null)
                this.equipe.removeJoueur(this);

            this.equipe = equipe;
            this.equipe.addJoueur(this);

            this.player.setScoreboard(board);

            this.player.setDisplayName(this.equipe.getChatColor() + this.equipe.getNomShort() + " " + this.player.getName() + ChatColor.RESET);

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

                            PlayerInfoData newPid = new PlayerInfoData(pid.getProfile().withName(getPseudoWithTeamAndColor()), pid.getPing(), pid.getGameMode(),
                                    WrappedChatComponent.fromText(getPseudoWithTeamAndColor()));
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
        this.equipe.removeJoueur(this);
        this.equipe = null;
    }

    public String getPseudo() {return pseudo;}

    public String getPseudoWithTeam() {return this.equipe.getNomShort() + " " + pseudo;}

    public String getPseudoWithTeamAndColor() {return this.equipe.getChatColor() + this.getPseudoWithTeam();}

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
        ArrayList<String> text = new ArrayList<>();
        text.addAll(globalText);

        if (this.sidebarObjective != null)
            this.sidebarObjective.unregister();
        this.sidebarObjective = this.board.registerNewObjective("sidebar", "dummy");

        this.sidebarObjective.setDisplayName(text.get(0)); //limite: 32
        text.remove(0);

        text.add(index, Main.startText + ChatColor.GRAY + "Tués: " + ChatColor.RED + numberOfKills);

        //text.add(player.getDisplayName());

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

}
