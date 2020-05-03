package samoth69.plugin_main;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.*;

public class MyListener implements Listener {

    private FileConfiguration config;
    private Position SpawnLocation;
    private HashMap<UUID, Joueur> Joueurs = new HashMap<UUID, Joueur>();
    private boolean SpawnStructureGenerated = false;
    private boolean GameStarted = false;
    private JavaPlugin jp;
    private ScoreboardManager sm = Bukkit.getScoreboardManager();
    private Scoreboard sb = sm.getNewScoreboard();
    private Objective objective = sb.registerNewObjective("PVPObjective", "dummy");
    private ArrayList<Score> scores = new ArrayList<>();
    private short nombreDeTeams = 4;
    private TeamGUI teamGUI = new TeamGUI(nombreDeTeams);

    MyListener(FileConfiguration config, JavaPlugin jp)
    {
        this.config = config;
        this.SpawnLocation = (Position)this.config.get("SpawnCoord");
        this.jp = jp;
        this.jp.getServer().getPluginManager().registerEvents(teamGUI, jp);
    }

    // This method checks for incoming players and sends them a message
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!Joueurs.containsKey(player.getUniqueId())) {
            Joueurs.put(player.getUniqueId(), new Joueur(player));
            if (config.getBoolean("TeleportOnConnect")) {
                if (!SpawnStructureGenerated)
                {
                    genSpawnStructure(SpawnLocation);
                    SpawnStructureGenerated = true;
                    initScoreboard();
                    player.setScoreboard(sb);
                }
            }
        }

        if (!GameStarted){
            Location l = new Location(Bukkit.getWorlds().get(0), SpawnLocation.getX(), SpawnLocation.getY(), SpawnLocation.getZ());
            player.teleport(l);
            player.getInventory().clear();
            ItemStack is = new ItemStack(Material.WOOL, 1);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName("Team");
            player.getInventory().setItem(4, is); //4: milieu de la hotbar
        }
    }

    private void initScoreboard() {
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.RED + "          UHC          ");

        Score score = objective.getScore(ChatColor.WHITE + "Joueurs: " + ChatColor.GOLD + jp.getServer().getOnlinePlayers().size());
        score.setScore(15);
        scores.add(score);

        score = objective.getScore(ChatColor.WHITE + "Teams: " + ChatColor.GOLD + "3");
        score.setScore(14);
        scores.add(score);
    }

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent e) {
        if (!GameStarted && e.isBlockInHand()) {
            teamGUI.openInventory(e.getPlayer());
        }
    }

    @EventHandler
    public void playerDropItem(PlayerDropItemEvent e) {
        if (!GameStarted) {
            e.setCancelled(true);
        }
    }

    //génération auto du spawn pour la partie (sol en stained glass de couleur aléatoire avec des murs en stained glass pane de couleur aléatoire)
    //center: position centrale de la plateforme.
    private void genSpawnStructure(Position Center) {
        jp.getLogger().info("Generating spawn structure");

        //SOL
        for (int x = -10; x < 10; x++) {
            for (int z = -10; z < 10; z++) {
                setSpawnBlockAt(new Position(SpawnLocation.getX() + x, SpawnLocation.getY() - 1, SpawnLocation.getZ() + z), Material.STAINED_GLASS);
            }
        }

        for (int x = -10; x < 10; x++) {
            for (int y = 0; y < 3; y++) {
                setSpawnBlockAt(new Position(SpawnLocation.getX() + x, SpawnLocation.getY() + y, SpawnLocation.getZ() - 10), Material.STAINED_GLASS_PANE);
                setSpawnBlockAt(new Position(SpawnLocation.getX() + x, SpawnLocation.getY() + y, SpawnLocation.getZ() + 9), Material.STAINED_GLASS_PANE);
            }
        }

        for (int z = -10; z < 10; z++) {
            for (int y = 0; y < 3; y++) {
                setSpawnBlockAt(new Position(SpawnLocation.getX() - 10, SpawnLocation.getY() + y, SpawnLocation.getZ() + z), Material.STAINED_GLASS_PANE);
                setSpawnBlockAt(new Position(SpawnLocation.getX() + 9, SpawnLocation.getY() + y, SpawnLocation.getZ() + z), Material.STAINED_GLASS_PANE);
            }
        }
    }

    //pose un block au coordonnée donnée avec une position.
    //pos: position du block
    //mat: block à placer (une métadata avec un nombre aléatoire entre 0 et 15 sera appliqué)
    private void setSpawnBlockAt(Position pos, Material mat){
        //Random rand = new Random();
        Location l = new Location(Bukkit.getWorlds().get(0), pos.getX(), pos.getY(), pos.getZ());
        Block b = l.getBlock();

        //String s = Utils.getRandomGlassColor();
        //b.setMetadata("color", new FixedMetadataValue(jp, s));

        b.setData((byte)new Random().nextInt(16)); //obsolète mais pas mieux :(
        b.setType(mat);
    }

    //Stores data for damage events
    @EventHandler
    public void EntityDamageEvent(EntityDamageEvent e) {
        if (config.getBoolean("TeleportOnConnect"))
        {
            if (e.getEntity() instanceof Player)
            {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void BlockBreakEvent(BlockBreakEvent e) {
        if (!GameStarted) {
            e.setCancelled(true);
        }
    }
}
