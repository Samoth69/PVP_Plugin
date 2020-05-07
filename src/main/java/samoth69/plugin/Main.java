package samoth69.plugin;

import com.comphenix.packetwrapper.WrapperPlayServerPlayerInfo;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.connorlinfoot.titleapi.TitleAPI;
import com.google.common.collect.Lists;
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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getServer;

public class Main implements Listener, CommandExecutor {

    private FileConfiguration config;
    private Position SpawnLocation;
    private static HashMap<UUID, Joueur> joueurs = new HashMap<>();
    private boolean SpawnStructureGenerated = false;
    //private boolean GameStarted = false;
    private GameStatus gameStatus = GameStatus.SERVER_STARTED;
    private enum GameStatus {
        SERVER_STARTED, //status par défaut
        BEFORE_START, //status avant que les joueurs sois larguer au sol
        GAME_STARTED, //game en cours. le chrono démare lorsque le serveur passe dans ce mode
        GAME_FINISHED; //lorsque la partie est fini
    }
    private JavaPlugin jp;
    private ScoreboardManager sm = Bukkit.getScoreboardManager();
    private Scoreboard sb = sm.getNewScoreboard();
    //private Objective objective = sb.registerNewObjective("PVPObjective", "dummy");
    //private Team scoreboardTeam;


    private TeamGUI teamGUI = new TeamGUI();
    private ArrayList<Equipe> equipes = new ArrayList<>();
    private ArrayList<String> scoreboardTextBuffer = new ArrayList<>(); //la première ligne est le titre.
    public static final String dateDuJour = getDate();
    public static final String startText = ChatColor.DARK_GRAY + "≫ " + ChatColor.RESET;
    private BukkitRunnable startCounter;

    private ProtocolManager protocolManager;

    Main(FileConfiguration config, JavaPlugin jp)
    {
        this.config = config;
        this.SpawnLocation = (Position)this.config.get("SpawnCoord");
        this.jp = jp;

        startCounter = new BukkitRunnable() {
            private Calendar cal = Calendar.getInstance(); //temps de démarage
            private int counter = 5;

            @Override
            public void run() {
                for (Map.Entry<UUID, Joueur> j : joueurs.entrySet()) {
                    TitleAPI.sendTitle(j.getValue().getJoueur(), 0, 20, 5, ChatColor.RED + String.valueOf(counter), "Démarrage");
                    counter--;
                    if (counter <= 0) {
                        this.cancel();
                    }
                }
            }
        };

        protocolManager = ProtocolLibrary.getProtocolManager();

        protocolManager.addPacketListener(new PacketMgnt(jp, joueurs, PacketType.Play.Server.PLAYER_INFO));

        //this.scoreboardTeam = this.sb.registerNewTeam("ha");
        //this.scoreboardTeam.setPrefix("§4PREFIX");
    }

    // This method checks for incoming players and sends them a message
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!joueurs.containsKey(player.getUniqueId())) {
            joueurs.put(player.getUniqueId(), new Joueur(jp, player, sm));
            if (config.getBoolean("TeleportOnConnect")) {
                if (!SpawnStructureGenerated)
                {
                    genSpawnStructure(SpawnLocation, (short)15);
                    SpawnStructureGenerated = true;
                }
            }
        }

        if (gameStatus == GameStatus.SERVER_STARTED){
            Location l = new Location(Bukkit.getWorlds().get(0), SpawnLocation.getX(), SpawnLocation.getY(), SpawnLocation.getZ());
            player.teleport(l);
            player.getInventory().clear();
            ItemStack is = new ItemStack(Material.WOOL, 1);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName("Team");
            player.getInventory().setItem(4, is); //4: milieu de la hotbar
            getServer().getPluginManager().registerEvents(joueurs.get(player.getUniqueId()), jp);
            //scoreboardTeam.addEntry(player.getName());
            updateScoreboard();

        } else {
            if (!joueurs.containsKey(player.getUniqueId())) {
                player.kickPlayer("Game already started !");
            } else {
                //Bukkit.broadcastMessage("Reconnexion de " + player.getDisplayName());
            }
        }
    }

    @EventHandler
    public void onPlayerLeft(PlayerQuitEvent e) {
        if (gameStatus == GameStatus.SERVER_STARTED) {
            Joueur j = joueurs.get(e.getPlayer().getUniqueId());
            j.removeTeam();
            joueurs.remove(e.getPlayer().getUniqueId());
            //scoreboardTeam.removeEntry(j.getJoueur().getName());
            updateScoreboard();
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
                if (args[1] != null) {
                    if (StringUtils.isNumeric(args[1])) {
                        updateNumberOfTeams(Integer.parseInt(args[1]));
                    } else {
                        sender.sendMessage(ChatColor.RED + "This is not a valid number");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Expected a number after setTeamNumber");
                }
            } else if(args[0].toLowerCase().equals("debug")) {
                Logger l = jp.getLogger();
                l.info("Teams:");
                for (Equipe e : equipes) {
                    l.info("Teamname: " + e.getTeamName());
                    if (e.getJoueurs().size() > 0) {
                        for (Joueur j : e.getJoueurs()) {
                            l.info("TeamUser: " + j.getPseudo());
                            l.info("TeamUserTeam:" + j.getEquipe().getTeamName());
                        }
                    } else {
                        l.info("not player on this team");
                    }
                    l.info("---");
                }
                l.info("----------------------");
                l.info("Joueurs:");
                for (Map.Entry<UUID, Joueur> j : joueurs.entrySet()) {
                    l.info("pseudo: " + j.getValue().getPseudo());
                    l.info("object UUID: " + j.getValue().getUUID());
                    l.info("hash UUID: " + j.getKey());
                    if (j.getValue().getEquipe() != null)
                        l.info("team: " + j.getValue().getEquipe().getTeamName());
                    else
                        l.info("team: none");
                }
            } else if(args[0].toLowerCase().equals("start")) {
                startGame(sender);
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid argument (not reconized)");
            }
        }
        return true;
    }

    //démarre la partie
    private void startGame(CommandSender sender) {
        if (equipes.size() <= 0) {
            sender.sendMessage(ChatColor.RED + "Aucune équipe, impossible de lancer la game");
        } else {
            if (gameStatus == GameStatus.SERVER_STARTED) {
                sender.sendMessage("Starting Game");
                startCounter.runTaskTimer(this.jp, 0, 20);
            } else {
                sender.sendMessage(ChatColor.RED + "La partie est déjà lancé ou terminé");
            }
        }
    }

    //renvoie le nombre de joueur dans la game (mort ou en vie)
    public int getNumberOfPlayer() {
        return joueurs.size();
    }

    //renvoie le nombre de joueur encore en vie
    public int getNumberOfTeamsAlive() {
        int counter = 0;
        for (Equipe e: equipes) {
            if (e.isTeamAlive())
                counter++;
        }
        return counter;
    }

    public int getNumberOfAlivePlayers() {
        int counter = 0;
        for (Map.Entry<UUID, Joueur> j: joueurs.entrySet()) {
            if (j.getValue().isAlive())
                counter++;
        }
        return counter;
    }

    private String getCommandHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.AQUA + "-----------PVP Plugin Help:-----------" + ChatColor.RESET + "\n");
        sb.append(ChatColor.GOLD + "/pvp help" + ChatColor.RESET + ": Show this help\n");
        sb.append(ChatColor.GOLD + "/pvp setTeamNumber [number of teams]" + ChatColor.RESET + ": set the number of teams\n");
        sb.append(ChatColor.AQUA + "--------------------------------------" + ChatColor.RESET  + "\n");
        return sb.toString();
    }

    private void updateScoreboard() {
        scoreboardTextBuffer.clear();
        scoreboardTextBuffer.add(ChatColor.YELLOW + "UHC " + ChatColor.DARK_GRAY + "| ");
        scoreboardTextBuffer.add(startText + ChatColor.DARK_GRAY + dateDuJour);
        scoreboardTextBuffer.add("");
        if (gameStatus != GameStatus.SERVER_STARTED) {
            scoreboardTextBuffer.add(startText + ChatColor.GOLD + "Démarrage: ");
            scoreboardTextBuffer.add("");
        }
        scoreboardTextBuffer.add(startText + ChatColor.GRAY + "équipes: " + ChatColor.GOLD + getNumberOfTeamsAlive() + ChatColor.DARK_GRAY + " (" + ChatColor.GRAY + getNumberOfAlivePlayers() + ChatColor.DARK_GRAY + ")"); //ligne de l'équipe

        for (Equipe e: equipes) {
            e.updateScoreboard(scoreboardTextBuffer);
        }
    }

    final String[] caractere = {"♥", "♦", "♣", "♠"};

    private void updateNumberOfTeams(int numberOfTeams) {
        equipes.clear();
        short color = 0;
        short prefixCounter = 0;
        for (int i = 0; i < numberOfTeams; i++) {
            equipes.add(new Equipe(caractere[prefixCounter], "Equipe " + caractere[prefixCounter], (short)i, color, sm, this));
            color++;
            if (color >= 16) { //on exclue la couleur noir car pas super lisible. si on dépasse les 16 équipes, on repart à 0
                color = 0;
                prefixCounter++;
            }
        }
    }

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent e) {
        if ((gameStatus == GameStatus.SERVER_STARTED || gameStatus == GameStatus.BEFORE_START) && e.isBlockInHand()) {
            teamGUI.updateTeams(equipes);
            teamGUI.openInventory(e.getPlayer());
        }
    }

    @EventHandler
    public void playerDropItem(PlayerDropItemEvent e) {
        if ((gameStatus == GameStatus.SERVER_STARTED || gameStatus == GameStatus.BEFORE_START)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e)
    {
        if (!(e.getInventory().getHolder() instanceof TeamGUI)) return;

        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();

        // verify current item is not null
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        final Player p = (Player) e.getWhoClicked();
        final Joueur j = joueurs.get(p.getUniqueId());
        final Equipe eq = equipes.get(e.getRawSlot());

        // Using slots click is a best option for your inventory click's
        //p.sendMessage("You clicked at slot " + e.getRawSlot());
        if (j.setEquipe(eq)) {
            p.sendMessage("Vous avez rejoints " + j.getEquipe().getChatColor() + j.getEquipe().getTeamName());
        } else {
            p.sendMessage(ChatColor.RED + "Vous déjà dans cette équipe");
        }

        teamGUI.updateTeams(equipes);
        updateScoreboard();
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
        if ((gameStatus == GameStatus.SERVER_STARTED || gameStatus == GameStatus.BEFORE_START)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void BlockPlaceEvent(BlockPlaceEvent e) {
        if ((gameStatus == GameStatus.SERVER_STARTED || gameStatus == GameStatus.BEFORE_START)) {
            e.setCancelled(true);
        }
    }

    private static String getDate() {
        // Choose time zone in which you want to interpret your Date
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"));
        StringBuilder sb = new StringBuilder();
        if (cal.get(Calendar.DAY_OF_MONTH) < 10) {
            sb.append("0");
        }
        sb.append(cal.get(Calendar.DAY_OF_MONTH));

        sb.append("/");

        if (cal.get(Calendar.MONTH) < 10) {
            sb.append("0");
        }
        sb.append(cal.get(Calendar.MONTH));

        sb.append("/");

        sb.append(cal.get(Calendar.YEAR));

        return sb.toString();
    }
}
