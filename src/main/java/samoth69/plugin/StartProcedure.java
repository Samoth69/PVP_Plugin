package samoth69.plugin;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class StartProcedure extends BukkitRunnable {
    private Main main;
    private int counter = 0;
    private ArrayList<Position> spawnSpot = new ArrayList<>();

    public StartProcedure(Main main) {
        this.main = main;
    }

    private void genSpawnSpot() {
        Random r = new Random();
        for (int i = 0; i < main.getNumberOfTeams(); i++) {
            Position p = new Position(r.nextInt(this.main.gameSettings.getTailleBordure() / 2), 120, r.nextInt(this.main.gameSettings.getTailleBordure() / 2));
            spawnSpot.add(p);
            main.genSpawnStructure(p, (short)4, false, Material.STAINED_GLASS, Material.BARRIER);
        }
    }

    @Override
    public void run() {
        if (spawnSpot.size() <= 0) {
            sendActionBarMsg(ChatColor.GOLD + "Génération des plateformes de spawn");
            genSpawnSpot();
        }
        sendActionBarMsg(ChatColor.GOLD + "Téléportation des équipes " + ChatColor.DARK_GRAY + "(" + ChatColor.YELLOW + (counter + 1)  + ChatColor.GRAY + "/" + ChatColor.YELLOW + main.getNumberOfTeams() + ChatColor.DARK_GRAY + ")");
        Equipe e = main.getEquipes().get(counter);
        Position l = spawnSpot.get(counter);
        main.getJp().getLogger().info("Teleporting " + e.getNomShort() + " to " + l.getX() + " " + l.getY() + " " + l.getZ());
        for (Joueur j : e.getJoueurs()) {
            j.getJoueur().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1, false, false));
            j.getJoueur().teleport(new Location(main.getWorld(), l.getX(), l.getY(), l.getZ()));
        }
        counter++;

        //main.getJp().getLogger().info(counter + " " + main.getEquipes().size());
        if (counter >= main.getEquipes().size()) {
            for (Position p : spawnSpot) {
                main.genSpawnStructure(p, (short)4, true);
            }
            for (Map.Entry<UUID, Joueur> j : main.getJoueurs().entrySet()) {
                for (PotionEffect effect : j.getValue().getJoueur().getActivePotionEffects()) {
                    j.getValue().getJoueur().removePotionEffect(effect.getType());
                }
                j.getValue().getJoueur().getInventory().clear();
            }
            this.cancel();
            GameStatusChangedEvent sige = new GameStatusChangedEvent(Main.GameStatus.GAME_STARTED);
            Bukkit.getPluginManager().callEvent(sige);
        }
    }

    private void sendActionBarMsg(String text) {
        for (Map.Entry<UUID, Joueur> j : main.getJoueurs().entrySet()) {
            ActionBarAPI.sendActionBar(j.getValue().getJoueur(), text);
        }
    }
}
