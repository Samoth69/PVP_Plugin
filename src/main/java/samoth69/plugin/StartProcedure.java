package samoth69.plugin;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
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
            Biome b;
            Position p;
            do {
                //taille de la zone de spawn par rapport à la taille de la bordure.
                //on prend 80% de cette taille
                int sizeSpawnPoint = this.main.gameSettings.getTailleBordure() * 80 / 100;
                //on obtient un nombre aléatoire sur X et sur Z.
                //on détermine la limite du nombre
                p = new Position(r.nextInt(sizeSpawnPoint) - sizeSpawnPoint / 2, 130, r.nextInt(sizeSpawnPoint) - sizeSpawnPoint / 2);
                b = Bukkit.getWorlds().get(0).getBiome(p.getX(), p.getZ());
            } while (b == Biome.DEEP_OCEAN || b == Biome.OCEAN || b == Biome.FROZEN_OCEAN);
            spawnSpot.add(p);
            main.genSpawnStructure(p, (short) 2, false, Material.STAINED_GLASS, Material.BARRIER);
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
        for (Joueur j : e.getJoueurs()) {
            main.getJp().getLogger().info("Teleporting " + j.getPseudoWithTeamAndColor() + " to " + l.getX() + " " + l.getY() + " " + l.getZ());
            j.getJoueur().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1, false, false));
            j.getJoueur().teleport(new Location(main.getWorld(), l.getX(), l.getY(), l.getZ()));
        }
        counter++;

        //main.getJp().getLogger().info(counter + " " + main.getEquipes().size());
        if (counter >= main.getEquipes().size()) {
            for (Position p : spawnSpot) {
                main.genSpawnStructure(p, (short)2, true);
            }
            for (Map.Entry<UUID, Joueur> j : main.getJoueurs().entrySet()) {
                for (PotionEffect effect : j.getValue().getJoueur().getActivePotionEffects()) {
                    j.getValue().getJoueur().removePotionEffect(effect.getType());
                }
                j.getValue().getJoueur().getInventory().clear();
                //j.getValue().getJoueur().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 20, 20, false, false));
                j.getValue().getJoueur().addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 20, 20, false, false));
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
