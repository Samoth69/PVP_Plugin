package samoth69.plugin_main;

import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.*;

public class Main implements Listener, CommandExecutor {

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
    private TeamGUI teamGUI = new TeamGUI();
    private ArrayList<Equipe> equipes = new ArrayList<>();

    Main(FileConfiguration config, JavaPlugin jp)
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
                    genSpawnStructure(SpawnLocation, (short)15);
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

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length <= 0) {
            sender.sendMessage("No Argument given, showing help");
            sender.sendMessage(getCommandHelp());
        } else {
            jp.getLogger().info("dbg:");
            for (String s: args)
                jp.getLogger().info(s);
            if (args[0].equals("help")) {
                sender.sendMessage(getCommandHelp());
            } else if(args[0].toLowerCase().equals("setteamnumber")) {
                if (StringUtils.isNumeric(args[1])) {
                    updateNumberOfTeams(Integer.parseInt(args[1]));
                } else {
                    sender.sendMessage(ChatColor.RED + "This is not a valid number");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid argument (not reconized)");
            }
        }
        return true;
    }

    private String getCommandHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.AQUA + "-----------PVP Plugin Help:-----------" + ChatColor.RESET + "\n");
        sb.append(ChatColor.GOLD + "/pvp help" + ChatColor.RESET + ": Show this help\n");
        sb.append(ChatColor.GOLD + "/pvp setTeamNumber [number of teams]" + ChatColor.RESET + ": set the number of teams\n");
        sb.append(ChatColor.AQUA + "--------------------------------------" + ChatColor.RESET  + "\n");
        return sb.toString();
    }

    private void initScoreboard() {
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.RED + "          UHC          ");

        Score score = objective.getScore(ChatColor.WHITE + "Joueurs: " + ChatColor.GOLD + jp.getServer().getOnlinePlayers().size());
        score.setScore(15);
        scores.add(score);

        score = objective.getScore(ChatColor.WHITE + "Equipes: " + ChatColor.GOLD + equipes.size());
        score.setScore(14);
        scores.add(score);
    }

    private void updateScoreboard() {
        //TODO
    }

    private void updateNumberOfTeams(int numberOfTeams) {
        equipes.clear();
        for (int i = 0; i < numberOfTeams; i++) {
            //jp.getLogger().info(Utils.getRandomChatColor().toString());
            equipes.add(new Equipe("Equipe " + i, (short)i, sb, objective));

        }
    }

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent e) {
        if (!GameStarted && e.isBlockInHand()) {
            teamGUI.updateTeams(equipes);
            teamGUI.openInventory(e.getPlayer());
        }
    }

    @EventHandler
    public void playerDropItem(PlayerDropItemEvent e) {
        if (!GameStarted) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e)
    {
        if (!(e.getInventory().getHolder() instanceof TeamGUI)) return;

        //e.setCancelled(false);

        final ItemStack clickedItem = e.getCurrentItem();

        // verify current item is not null
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        final Player p = (Player) e.getWhoClicked();

        // Using slots click is a best option for your inventory click's
        p.sendMessage("You clicked at slot " + e.getRawSlot());
    }

    //génération auto du spawn pour la partie (sol en stained glass de couleur aléatoire avec des murs en stained glass pane de couleur aléatoire)
    //center: position centrale de la plateforme.
    private void genSpawnStructure(Position Center, short size) {
        jp.getLogger().info("Generating spawn structure");

        //SOL
        for (int x = -size; x < size; x++) {
            for (int z = -size; z < size; z++) {
                setSpawnBlockAt(new Position(SpawnLocation.getX() + x, SpawnLocation.getY() - 1, SpawnLocation.getZ() + z), Material.STAINED_GLASS);
            }
        }

        for (int x = -size; x < size; x++) {
            for (int y = 0; y < 3; y++) {
                setSpawnBlockAt(new Position(SpawnLocation.getX() + x, SpawnLocation.getY() + y, SpawnLocation.getZ() - size), Material.STAINED_GLASS_PANE);
                setSpawnBlockAt(new Position(SpawnLocation.getX() + x, SpawnLocation.getY() + y, SpawnLocation.getZ() + size - 1), Material.STAINED_GLASS_PANE);
            }
        }

        for (int z = -size; z < size; z++) {
            for (int y = 0; y < 3; y++) {
                setSpawnBlockAt(new Position(SpawnLocation.getX() - size, SpawnLocation.getY() + y, SpawnLocation.getZ() + z), Material.STAINED_GLASS_PANE);
                setSpawnBlockAt(new Position(SpawnLocation.getX() + size - 1, SpawnLocation.getY() + y, SpawnLocation.getZ() + z), Material.STAINED_GLASS_PANE);
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
        b.setType(mat);
        b.setData((byte)new Random().nextInt(16)); //obsolète mais pas mieux :(
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

    @EventHandler
    public void BlockPlaceEvent(BlockPlaceEvent e) {
        if (!GameStarted) {
            e.setCancelled(true);
        }
    }
}
