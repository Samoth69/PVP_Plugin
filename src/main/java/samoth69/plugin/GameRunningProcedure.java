package samoth69.plugin;

import org.apache.commons.lang.time.StopWatch;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalTime;
import java.util.*;

public class GameRunningProcedure extends BukkitRunnable {
    private Main main;
    private StopWatch stopWatch = new StopWatch();
    private ArrayList<String> text = new ArrayList<>();

    private boolean taupeRevealed = false;
    private boolean borderIsReducing = false;
    //private boolean gameEnded = false;

    private CoolDownTimer invinsibiliteCoolDown = new CoolDownTimer(new int[]{60, 30, 10, 5, 4, 3, 2, 1, 0});
    private CoolDownTimer pvpCoolDown = new CoolDownTimer(new int[]{300, 120, 60, 30, 10, 5, 4, 3, 2, 1, 0});

    public GameRunningProcedure(Main main) {
        this.main = main;
    }

    public void startWatch() {
        this.stopWatch.start();
    }

    @Override
    public void run() {
        //FastDateFormat fdfChrono = FastDateFormat.getInstance("HH:mm:ss");
        long stopWatchSecondes = stopWatch.getTime() / 1000;
        long difInvisibilite = getDifInvinsibilite();
        long difPVP = getDifPVP();
        long difTaupe = getDifTaupe();
        long difBordure = getDifBordure();

        text.clear();

        text.add(Main.startText + ChatColor.GOLD + "Durée: " + ChatColor.YELLOW + formatSeconds(stopWatchSecondes));
        //if (difInvisibilite >= 0)
            //text.add(Main.startText + ChatColor.GOLD + "Invisibilité: " + ChatColor.YELLOW + formatSeconds(difInvisibilite));
        if (difPVP >= 0)
            text.add(Main.startText + ChatColor.GOLD + "PVP: " + ChatColor.YELLOW + formatSeconds(difPVP));
        if (main.gameSettings.isEnableTaupe()) {
            if (difTaupe >= 0)
                text.add(Main.startText + ChatColor.GOLD + "Taupe: " + ChatColor.YELLOW + formatSeconds(difTaupe));
        }
        if (difBordure >= 0)
            text.add(Main.startText + ChatColor.GOLD + "Bordure: " + ChatColor.YELLOW + formatSeconds(difBordure));

        this.main.updateScoreboard(text);

        if (invinsibiliteCoolDown.doReveal(difInvisibilite)) {
            if (difInvisibilite > 0) {
                Bukkit.broadcastMessage(ChatColor.YELLOW + "Fin de l'invinsibilité dans " + ChatColor.RED + ChatColor.BOLD + difInvisibilite +
                        ChatColor.RESET + ChatColor.YELLOW + " secondes");
            } else {
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Vous prenez à présent des dégats");
            }
        }

        if (pvpCoolDown.doReveal(difPVP)) {
            if (difPVP > 60) {
                Bukkit.broadcastMessage(ChatColor.YELLOW + "Activation du " +
                        ChatColor.GOLD + "PVP" +
                        ChatColor.YELLOW + " dans " +
                        ChatColor.RED + ChatColor.BOLD + (int)(difPVP / 60) +
                        ChatColor.RESET + ChatColor.YELLOW + " minutes");
            } else if (difPVP > 0){
                Bukkit.broadcastMessage(ChatColor.YELLOW + "Activation du " +
                        ChatColor.GOLD + "PVP" +
                        ChatColor.YELLOW + " dans " +
                        ChatColor.RED + ChatColor.BOLD + difPVP +
                        ChatColor.RESET + ChatColor.YELLOW + " secondes");
            } else {
                Bukkit.broadcastMessage(ChatColor.RED + "" + ChatColor.BOLD + "Le PVP est à présent actif");
            }
        }

        if (this.main.gameSettings.isEnableTaupe() && difTaupe < 0 && !taupeRevealed) {
            for (Map.Entry<UUID, Joueur> j : this.main.getTaupeJoueurs().entrySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append(ChatColor.GOLD + "-----------------------------------------------------\n");
                sb.append(ChatColor.RED + "Vous êtes une taupe.\n");
                sb.append(ChatColor.RED + "Vous devez trahir votre équipe afin de gagner la partie avec votre équipe de taupe.\n");
                sb.append(ChatColor.RED + "Vous pouvez communiquer dans un chat privé avec la commande ‘/t’.\n");
                sb.append(ChatColor.RED + "Vous disposez aussi d’un kit spécial que vous pouvez récupérer avec la commande ‘/claim’.\n");
                sb.append(ChatColor.RED + "Bonne chance soldat, et n'oubliez pas, " + ChatColor.ITALIC + "\"Nous sommes en guerre\"\n");
                sb.append(ChatColor.GOLD + "-----------------------------------------------------\n");
                j.getValue().getJoueur().sendMessage(sb.toString());
            }
            taupeRevealed = true;
        }

        if (difBordure < 0 && !borderIsReducing) {
            Bukkit.broadcastMessage(ChatColor.RED + "La bordure commence à ce réduire");
            this.main.getWorldBorder().setSize(200, 1800);
            borderIsReducing = true;
        }
    }

    public static String formatSeconds(long secondes) {
        LocalTime lt = LocalTime.ofSecondOfDay(secondes);

        StringBuilder sb = new StringBuilder();
        if (lt.getHour() > 0) {
            if (lt.getHour() <= 9) {
                sb.append("0");
            }
            sb.append(lt.getHour());
            sb.append(":");
        }

        if (lt.getMinute() <= 9) {
            sb.append("0");
        }
        sb.append(lt.getMinute());
        sb.append(":");

        if (lt.getSecond() <= 9) {
            sb.append("0");
        }
        sb.append(lt.getSecond());

        return sb.toString();
    }

    private long getDifInvinsibilite() {
        return main.gameSettings.getTpsInvincibilite() - getStopWatchSecondes();
    }

    private long getDifPVP() {
        return main.gameSettings.getTpsPVP() - getStopWatchSecondes();
    }

    private long getDifTaupe() {
        return main.gameSettings.getTpsTaupe() - getStopWatchSecondes();
    }

    private long getDifBordure() {
        return main.gameSettings.getTpsBordure() - getStopWatchSecondes();
    }

    public long getStopWatchSecondes() {
        return stopWatch.getTime() / 1000;
    }

    //indique si la période d'invisibilité est actif
    public boolean isInvinsibiliteActif() {
        return getDifInvinsibilite() > 0;
    }

    //indique si le pvp est actif
    public boolean isPVPActif() {
        return getDifPVP() < 0;
    }

    //indique si les taupes on été révélé ou pas
    //revoie toujours false si les taupes sont désactivé sur cette partie
    public boolean isTaupeActif() {
        if (main.gameSettings.isEnableTaupe()) {
            return getDifTaupe() < 0;
        } else {
            return false;
        }
    }

    //indique si la bordure ce réduit
    public boolean isBordureActif() {
        return getDifBordure() > 0;
    }
}
