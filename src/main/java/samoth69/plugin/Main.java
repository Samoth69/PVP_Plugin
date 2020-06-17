package samoth69.plugin;

import com.connorlinfoot.titleapi.TitleAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getServer;

public class Main implements Listener, CommandExecutor {

    private FileConfiguration config;
    private Position SpawnLocation;
    private HashMap<UUID, Joueur> joueurs = new HashMap<>();
    private HashMap<UUID, Joueur> taupeJoueurs = new HashMap<>();
    private boolean SpawnStructureGenerated = false;
    private GameStatus gameStatus = GameStatus.SERVER_STARTED;
    public enum GameStatus {
        SERVER_STARTED, //status par défaut
        INIT_GAME, //compte à rebour avant début téléportation
        TELEPORT_PLAYER, //téléportation des joueurs en cours, le serveur peux laguer un peu pendant cette période
        GAME_STARTED, //game en cours. le chrono démare lorsque le serveur passe dans ce mode
        GAME_FINISHED; //lorsque la partie est fini
    }
    private JavaPlugin jp;
    private ScoreboardManager sm = Bukkit.getScoreboardManager();
//    private Scoreboard sb = sm.getNewScoreboard();
//    private Objective listObjective;
//    private Objective belowNameObjective;

    private TeamGUI teamGUI = new TeamGUI();
    private ArrayList<Equipe> equipes = new ArrayList<>();
    private ArrayList<String> scoreboardTextBuffer = new ArrayList<>(); //la première ligne est le titre.
    public static final String dateDuJour = getDate();
    public static final String startText = ChatColor.DARK_GRAY + "≫ " + ChatColor.RESET;

    private BukkitRunnable startCounter;
    private BukkitTask startProcedure, gameStarted;

    public GameSettings gameSettings = new GameSettings(this);
    public GameRunningProcedure gameRunningProcedure =  new GameRunningProcedure(this);

    Main(FileConfiguration config, JavaPlugin jp)
    {
        this.config = config;
        this.SpawnLocation = (Position)this.config.get("SpawnCoord");
        this.jp = jp;

//        this.listObjective = sb.registerNewObjective("health", "health");
//        this.listObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
//        this.belowNameObjective = sb.registerNewObjective("blhealth", "health");
//        this.belowNameObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);

        startCounter = new BukkitRunnable() {
            private Calendar cal = Calendar.getInstance(); //temps de démarage
            private int counter = 5;

            @Override
            public void run() {
                for (Map.Entry<UUID, Joueur> j : joueurs.entrySet()) {
                    TitleAPI.sendTitle(j.getValue().getJoueur(), 0, 15, 3, ChatColor.RED + String.valueOf(counter), "Démarrage");
                    counter--;
                    if (counter <= 0) {
                        //startProcedure.runTaskLater(jp, 1);
                        this.cancel();
                        GameStatusChangedEvent sige = new GameStatusChangedEvent(GameStatus.TELEPORT_PLAYER);
                        Bukkit.getPluginManager().callEvent(sige);;
                    }
                }
            }
        };
    }

    @EventHandler
    public void gameStatusChanged(GameStatusChangedEvent e) {
        Bukkit.getLogger().info(e.toString());
        switch (e.getGameStatus()) {
            case SERVER_STARTED:
                this.gameStatus = GameStatus.SERVER_STARTED;
                break;
            case INIT_GAME:
                this.gameStatus = GameStatus.INIT_GAME;
                for (Equipe eq : this.equipes) {
                    eq.tirerTaupe();
                }
                for (Map.Entry<UUID, Joueur> j : joueurs.entrySet()) {
                    if (j.getValue().isTaupe()) {
                        this.taupeJoueurs.put(j.getKey(), j.getValue());
                    }
                }
                this.startCounter.runTaskTimer(this.jp, 0, 20);
                break;
            case TELEPORT_PLAYER:
                this.gameStatus = GameStatus.TELEPORT_PLAYER;
                if (this.startProcedure == null)
                    this.startProcedure = new StartProcedure(this).runTaskTimer(jp, 0, 20);
                break;
            case GAME_STARTED:
                this.gameStatus = GameStatus.GAME_STARTED;
                this.gameStarted = this.gameRunningProcedure.runTaskTimer(jp, 0, 10);
                genSpawnStructure(SpawnLocation, (short)15, true);
                this.gameRunningProcedure.startWatch();
                break;
            case GAME_FINISHED:
                for (Map.Entry<UUID, Joueur> j : joueurs.entrySet()) {
                    TitleAPI.sendTitle(j.getValue().getJoueur(), 2, 30, 2, "Partie fini", "BRAVO à TOUS !");
                }
                //-------------------------------------------TOP DAMAGE-----------------------------------------------------
                ArrayList<Joueur> topDmg = new ArrayList<>(this.getJoueurs().values());
                topDmg.sort(Joueur.compTopDmg());

                Bukkit.getLogger().info("TOP DAMAGE");
                for (Joueur j : topDmg) {
                    Bukkit.getLogger().info(j.getPseudo() + ":" + j.getTotalDamage());
                }

                StringBuilder sb = new StringBuilder();
                sb.append(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "------------TOP DAMAGE:------------\n");
                for (int i = 0; i < 6 && i < topDmg.size(); i++) {
                    sb.append(ChatColor.GRAY + "N°" +
                            ChatColor.GOLD + (i + 1) +
                            ChatColor.DARK_GRAY + ": " + topDmg.get(i).getPseudoWithTeamAndColor() +
                            ChatColor.DARK_GRAY + " (" +
                            ChatColor.GRAY + topDmg.get(i).getTotalDamage() +
                            ChatColor.DARK_GRAY + ")\n");
                }
                Bukkit.broadcastMessage(sb.toString());

                //-------------------------------------------TOP KILLS-----------------------------------------------------
                ArrayList<Joueur> topKill = new ArrayList<>(this.getJoueurs().values());
                topKill.sort(Joueur.compKillCount());

                Bukkit.getLogger().info("TOP KILL");
                for (Joueur j : topKill) {
                    Bukkit.getLogger().info(j.getPseudo() + ":" + j.getNumberOfKills());
                }

                sb = new StringBuilder();

                sb.append(ChatColor.DARK_BLUE + "" + ChatColor.BOLD + "------------TOP KILLS:------------\n");
                for (int i = 0; i < 6 && i < topKill.size(); i++) {
                    sb.append(ChatColor.GRAY + "N°" +
                            ChatColor.GOLD + (i + 1) +
                            ChatColor.DARK_GRAY + ": " + topDmg.get(i).getPseudoWithTeamAndColor() +
                            ChatColor.DARK_GRAY + " (" +
                            ChatColor.GRAY + topDmg.get(i).getNumberOfKills() +
                            ChatColor.DARK_GRAY + ")\n");
                }
                Bukkit.broadcastMessage(sb.toString());
                break;
        }
    }

    // This method checks for incoming players and sends them a message
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!joueurs.containsKey(player.getUniqueId())) {
            joueurs.put(player.getUniqueId(), new Joueur(this, player, sm));
            if (config.getBoolean("TeleportOnConnect")) {
                if (!SpawnStructureGenerated)
                {
                    genSpawnStructure(SpawnLocation, (short)15);
                    SpawnStructureGenerated = true;

                    Bukkit.getWorlds().get(0).setFullTime(0);
                    Bukkit.getWorlds().get(0).setGameRuleValue("doDaylightCycle", "false");
                    Bukkit.getWorlds().get(1).setGameRuleValue("doDaylightCycle", "false");
                    Bukkit.getWorlds().get(2).setGameRuleValue("doDaylightCycle", "false");

                    Bukkit.getWorlds().get(0).setGameRuleValue("naturalRegeneration", "false");
                    Bukkit.getWorlds().get(1).setGameRuleValue("naturalRegeneration", "false");
                    Bukkit.getWorlds().get(2).setGameRuleValue("naturalRegeneration", "false");
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
                this.joueurs.get(player.getUniqueId()).setPlayer(player);
            }
        }
    }

    @EventHandler
    public void onPlayerLeft(PlayerQuitEvent e) {
        if (gameStatus == GameStatus.SERVER_STARTED) {
            if (joueurs.containsKey(e.getPlayer().getUniqueId())) {
                Joueur j = joueurs.get(e.getPlayer().getUniqueId());
                j.removeTeam();
                joueurs.remove(e.getPlayer().getUniqueId());
                //scoreboardTeam.removeEntry(j.getJoueur().getName());
                updateScoreboard();
            }
        }
    }

    // This method is called, when somebody uses our command
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player)sender;
        switch (command.getName()) {
            case "t":
                if (this.taupeJoueurs.containsKey(p.getUniqueId()) && this.gameSettings.isEnableTaupe() && this.gameRunningProcedure.isTaupeActif()) {
                    if (args.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (String s : args) {
                            sb.append(s);
                            sb.append(" ");
                        }
                        for (Map.Entry<UUID, Joueur> t : this.taupeJoueurs.entrySet()) {
                            if (t.getValue().isAlive()) {
                                t.getValue().getJoueur().sendMessage(ChatColor.RED + "[" + p.getName() + "] " + sb.toString());
                            }
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Votre message est vide");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Tu n'est pas une taupe !");
                }
                return true;
            case "claim":
                if (this.taupeJoueurs.containsKey(p.getUniqueId()) && this.gameSettings.isEnableTaupe()) {
                    Joueur j = this.taupeJoueurs.get(p.getUniqueId());
                    if (!j.isKitClaimed()) {
                        ItemStack dropItem = new ItemStack(Material.IRON_SWORD, 1);
                        dropItem.addEnchantment(Enchantment.DAMAGE_ALL, 2);
                        dropItem.addEnchantment(Enchantment.FIRE_ASPECT, 1);
                        p.getWorld().dropItemNaturally(p.getLocation(), dropItem);
                        j.setKitClaimed(true);
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Tu n'est pas une taupe !");
                }
                return true;
                //break;
            case "pvp":
                if (!sender.isOp()) {
                    sender.sendMessage("You need to be op to use this command");
                    return true;
                }
                if (args.length <= 0) {
                    sender.sendMessage("No Argument given, showing help");
                    sender.sendMessage(getCommandHelp());
                } else {
                    jp.getLogger().info("dbg:");
                    for (String s : args)
                        jp.getLogger().info(s);
                    switch (args[0].toLowerCase()) {
                        case "help":
                            if (args[1].toLowerCase().equals("set")) {
                                sender.sendMessage(getSetCommandHelp());
                            } else {
                                sender.sendMessage(getCommandHelp());
                            }
                            break;
                        case "debug":
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
                            break;
                        case "start":
                            startGame(sender);
                            break;
                        case "set":
                            if (args[1] != null) {
                                switch (args[1].toLowerCase()) {
                                    case "teamnumber":
                                        if (StringUtils.isNumeric(args[2])) {
                                            int num = Integer.parseInt(args[2]);
                                            if (num > 45) {
                                                sender.sendMessage(ChatColor.RED + "Number of team should be below or equal to 45");
                                            } else {
                                                updateNumberOfTeams(num);
                                                sender.sendMessage(ChatColor.AQUA + "Number of team updated to " + ChatColor.GOLD + num);
                                            }
                                        } else {
                                            sender.sendMessage(ChatColor.RED + "This is not a valid number");
                                        }
                                        break;
                                    case "wbsize":
                                        if (StringUtils.isNumeric(args[2])) {
                                            this.gameSettings.setTailleBordure(Integer.parseInt(args[2]));
                                            this.updateScoreboard();
                                            sender.sendMessage(ChatColor.AQUA + "World border size updated to " + ChatColor.GOLD + args[2]);
                                        } else {
                                            sender.sendMessage(ChatColor.RED + "This is not a valid number");
                                        }
                                        break;
                                    case "tpsdmg":
                                        if (StringUtils.isNumeric(args[2])) {
                                            this.gameSettings.setTpsInvincibilite(Integer.parseInt(args[2]));
                                            sender.sendMessage(ChatColor.AQUA + "Time before player will take damage is now " + ChatColor.GOLD + args[2] + ChatColor.AQUA + " seconds");
                                        } else {
                                            sender.sendMessage(ChatColor.RED + "This is not a valid number");
                                        }
                                        break;
                                    case "tpspvp":
                                        if (StringUtils.isNumeric(args[2])) {
                                            this.gameSettings.setTpsPVP(Integer.parseInt(args[2]));
                                            sender.sendMessage(ChatColor.AQUA + "Time before PVP is enabled is now " + ChatColor.GOLD + args[2] + ChatColor.AQUA + " seconds");
                                        } else {
                                            sender.sendMessage(ChatColor.RED + "This is not a valid number");
                                        }
                                        break;
                                    case "tpsborder":
                                        if (StringUtils.isNumeric(args[2])) {
                                            this.gameSettings.setTpsBordure(Integer.parseInt(args[2]));
                                            sender.sendMessage(ChatColor.AQUA + "Time before world border will shrink is now " + ChatColor.GOLD + args[2] + ChatColor.AQUA + " seconds");
                                        } else {
                                            sender.sendMessage(ChatColor.RED + "This is not a valid number");
                                        }
                                        break;
                                    case "muleenable":
                                        this.gameSettings.setEnableTaupe(Boolean.parseBoolean(args[2]));
                                        sender.sendMessage(ChatColor.AQUA + "Mule are now set to " + ChatColor.GOLD + args[2]);
                                        break;
                                    case "tpsmule":
                                        if (StringUtils.isNumeric(args[2])) {
                                            this.gameSettings.setTpsTaupe(Integer.parseInt(args[2]));
                                            sender.sendMessage(ChatColor.AQUA + "Mule will now be revealed at " + ChatColor.GOLD + args[2] + ChatColor.AQUA + " seconds");
                                        } else {
                                            sender.sendMessage(ChatColor.RED + "This is not a valid number");
                                        }
                                        break;
                                    default:
                                        sender.sendMessage(ChatColor.RED + "Argument not reconized");
                                        sender.sendMessage(this.getSetCommandHelp());
                                        break;
                                }
                            } else {
                                sender.sendMessage(ChatColor.RED + "missing argument");
                                sender.sendMessage(this.getSetCommandHelp());
                            }
                            break;
                        case "showconfig":
                            StringBuilder sb = new StringBuilder();
                            sb.append(ChatColor.AQUA + "-----------Current Game Config-----------" + ChatColor.RESET + "\n");
                            sb.append(ChatColor.GOLD + "teamnumber " + ChatColor.YELLOW + this.getNumberOfTeams() + "\n");
                            sb.append(ChatColor.GOLD + "wbsize     " + ChatColor.YELLOW + this.gameSettings.getTailleBordureTextFormatted() + "\n");
                            sb.append(ChatColor.GOLD + "tpsdmg     " + ChatColor.YELLOW + GameRunningProcedure.formatSeconds(this.gameSettings.getTpsInvincibilite()) + "\n");
                            sb.append(ChatColor.GOLD + "tpspvp     " + ChatColor.YELLOW + GameRunningProcedure.formatSeconds(this.gameSettings.getTpsPVP()) + "\n");
                            sb.append(ChatColor.GOLD + "tpsborder  " + ChatColor.YELLOW + GameRunningProcedure.formatSeconds(this.gameSettings.getTpsBordure()) + "\n");
                            sb.append(ChatColor.GOLD + "muleenable " + ChatColor.YELLOW + this.gameSettings.isEnableTaupe() + "\n");
                            sb.append(ChatColor.GOLD + "tpsmule    " + ChatColor.YELLOW + GameRunningProcedure.formatSeconds(this.gameSettings.getTpsTaupe()) + "\n");
                            sb.append(ChatColor.AQUA + "-----------------------------------------" + ChatColor.RESET + "\n");
                            sender.sendMessage(sb.toString());
                            break;
                        default:
                            sender.sendMessage(ChatColor.RED + "Invalid argument (not reconized)");
                            break;
                    }
                }
                break;
        }
        return true;
    }

    public JavaPlugin getJp() {
        return this.jp;
    }

    public ArrayList<Equipe> getEquipes() {
        return equipes;
    }

    public HashMap<UUID, Joueur> getJoueurs() {
        return joueurs;
    }

    public int getNumberOfTeams() {
        return equipes.size();
    }

    public World getWorld() {
        return Bukkit.getWorlds().get(0);
    }

    //démarre la partie
    private void startGame(CommandSender sender) {
        for (Equipe e : this.equipes) {
            if (e.getJoueurs().size() <= 0) {
                sender.sendMessage(ChatColor.RED + "Au moins une équipe est vide, impossible de démarré la partie.");
                return;
            }
        }

        if (equipes.size() <= 0) {
            sender.sendMessage(ChatColor.RED + "Aucune équipe, impossible de lancer la game");
        } else {
            if (gameStatus == GameStatus.SERVER_STARTED) {
                sender.sendMessage("Starting Game");
                updateGameStatus(GameStatus.INIT_GAME);
            } else {
                sender.sendMessage(ChatColor.RED + "La partie est déjà lancé ou terminé");
            }
        }
    }

    private void updateGameStatus(GameStatus g) {
        GameStatusChangedEvent gsc = new GameStatusChangedEvent(g);
        Bukkit.getPluginManager().callEvent(gsc);
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
        sb.append(ChatColor.GOLD + "/pvp help set" + ChatColor.RESET + ": Show '/pvp set' command help\n");
        sb.append(ChatColor.GOLD + "/pvp start" + ChatColor.RESET + ": start the game\n");
        sb.append(ChatColor.GOLD + "/pvp showconfig" + ChatColor.RESET + ": Show game config\n");
        sb.append(ChatColor.GOLD + "/pvp set [param] [optionnal arg 1]..." + ChatColor.RESET + ": Show this help\n");
        sb.append(ChatColor.GOLD + "/pvp debug" + ChatColor.RESET + ": internal, for dev only\n");
        sb.append(ChatColor.AQUA + "--------------------------------------" + ChatColor.RESET  + "\n");
        return sb.toString();
    }

    private String getSetCommandHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.AQUA + "-----------Set Command Help:-----------" + ChatColor.RESET + "\n");
        sb.append(ChatColor.AQUA + "pattern:" + ChatColor.GOLD+" /pvp set [param] [val]");
        sb.append(ChatColor.GOLD + "param =\n");
        sb.append(ChatColor.GOLD + "       teamnumber [number of teams]" + ChatColor.RESET + ": set the number of team\n");
        sb.append(ChatColor.GOLD + "       wbsize [wb size in blocks]" + ChatColor.RESET + ": set the size of the world border\n");
        sb.append(ChatColor.GOLD + "       tpsdmg [SECONDS]" + ChatColor.RESET + ": set time before player will take damage\n");
        sb.append(ChatColor.GOLD + "       tpspvp [SECONDS]" + ChatColor.RESET + ": set time before player can hit each other\n");
        sb.append(ChatColor.GOLD + "       tpsborder [SECONDS]" + ChatColor.RESET + ": set time before the world board will start to shrink\n");
        sb.append(ChatColor.GOLD + "       muleenable  [true/false]" + ChatColor.RESET + ": enable / disable mule for this party\n");
        sb.append(ChatColor.GOLD + "       tpsmule [SECONDS]" + ChatColor.RESET + ": set time before mule will be chosen at random\n");
        sb.append(ChatColor.AQUA + "---------------------------------------" + ChatColor.RESET  + "\n");
        return sb.toString();
    }

    public void updateScoreboard(ArrayList<String> text) {
        int index = 0;

        scoreboardTextBuffer.clear();
        scoreboardTextBuffer.add(ChatColor.YELLOW + "UHC " + ChatColor.DARK_GRAY + "| ");
        scoreboardTextBuffer.add(startText + ChatColor.DARK_GRAY + dateDuJour);
        scoreboardTextBuffer.add(" ");
        if (text != null) {
            scoreboardTextBuffer.addAll(text);
            scoreboardTextBuffer.add(2, " ");
            scoreboardTextBuffer.add(" ");
        }
        index = scoreboardTextBuffer.size();
        scoreboardTextBuffer.add(startText + ChatColor.GRAY + "équipes: " + ChatColor.GOLD + getNumberOfTeamsAlive() + ChatColor.DARK_GRAY + " (" + ChatColor.GRAY + getNumberOfAlivePlayers() + ChatColor.DARK_GRAY + ")"); //ligne de l'équipe
        scoreboardTextBuffer.add("");
        scoreboardTextBuffer.add(startText + ChatColor.GRAY + "Centre: ");
        scoreboardTextBuffer.add(startText + ChatColor.GRAY + "Bordure: " + ChatColor.YELLOW + gameSettings.getTailleBordureTextFormatted());

        for (Equipe e: equipes) {
            e.updateScoreboard(scoreboardTextBuffer, index);
        }
    }

    public void updateScoreboard() {
        updateScoreboard(null);
    }

    final String[] caractere = {"♥", "♦", "♣", "♠"};

    private void updateNumberOfTeams(int numberOfTeams) {
        equipes.clear();
        for (Map.Entry<UUID, Joueur> j : this.joueurs.entrySet()) {
            j.getValue().setEquipe(null);
        }
        short color = 0;
        short prefixCounter = 0;
        for (int i = 0; i < numberOfTeams; i++) {
            equipes.add(new Equipe(caractere[prefixCounter], "Equipe " + caractere[prefixCounter], (short)Utils.getIntFromChatColor(Utils.getChatColorFromInt(prefixCounter)), color, sm, this));
            color++;
            if (color >= 6) { //on exclue la couleur noir car pas super lisible. si on dépasse les 16 équipes, on repart à 0
                color = 0;
                prefixCounter++;
            }
        }
        this.updateScoreboard();
    }

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent e) {
        if ((gameStatus == GameStatus.SERVER_STARTED || gameStatus == GameStatus.INIT_GAME || gameStatus == GameStatus.TELEPORT_PLAYER) && e.isBlockInHand()) {
            teamGUI.updateTeams(equipes);
            teamGUI.openInventory(e.getPlayer());
        }
    }

    @EventHandler
    public void playerDropItem(PlayerDropItemEvent e) {
        if ((gameStatus == GameStatus.SERVER_STARTED || gameStatus == GameStatus.INIT_GAME || gameStatus == GameStatus.TELEPORT_PLAYER)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent e) {
        if (gameStatus == GameStatus.GAME_STARTED) {
            e.getEntity().setGameMode(GameMode.SPECTATOR);
            this.joueurs.get(e.getEntity().getUniqueId()).setAlive(false);
            if (e.getEntity().getKiller() != null) {
                Bukkit.getLogger().info("added kill point for " + e.getEntity().getName() + " to " + e.getEntity().getKiller().getName());
                this.joueurs.get(e.getEntity().getKiller().getUniqueId()).addKill();
            } else {
                Bukkit.getLogger().info("Didn't found a killer for " + e.getEntity().getName());
            }

            if (getNumberOfTeamsAlive() <= 1) {
                this.gameRunningProcedure.cancel();
                this.gameStatusChanged(new GameStatusChangedEvent(GameStatus.GAME_FINISHED));
            }
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e)
    {
        if (gameStatus == GameStatus.SERVER_STARTED) {
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
    }

    public void genSpawnStructure(Position center, short size) {
        genSpawnStructure(center, size, false, Material.STAINED_GLASS, Material.STAINED_GLASS_PANE);
    }

    public void genSpawnStructure(Position center, short size, boolean destroy) {
        genSpawnStructure(center, size, true, null, null);
    }

    public void genSpawnStructure(Position center, short size, boolean destroy, Material sol, Material mur) {
        if (destroy) {
            sol = Material.AIR;
            mur = Material.AIR;
            jp.getLogger().info("Destroying spawn structure");
        } else {
            jp.getLogger().info("Generating spawn structure");
        }

        //SOL
        for (int x = -size; x < size; x++) {
            for (int z = -size; z < size; z++) {
                setSpawnBlockAt(new Position(center.getX() + x, center.getY() - 1, center.getZ() + z), sol);
            }
        }

        for (int x = -size; x < size; x++) {
            for (int y = 0; y < 3; y++) {
                setSpawnBlockAt(new Position(center.getX() + x, center.getY() + y, center.getZ() - size), mur);
                setSpawnBlockAt(new Position(center.getX() + x, center.getY() + y, center.getZ() + size - 1), mur);
            }
        }

        for (int z = -size; z < size; z++) {
            for (int y = 0; y < 3; y++) {
                setSpawnBlockAt(new Position(center.getX() - size, center.getY() + y, center.getZ() + z), mur);
                setSpawnBlockAt(new Position(center.getX() + size - 1, center.getY() + y, center.getZ() + z), mur);
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
        if (mat == Material.STAINED_GLASS || mat == Material.STAINED_GLASS_PANE)
            b.setData((byte)new Random().nextInt(16)); //obsolète mais pas mieux :(
    }

    //Stores data for damage events
    @EventHandler
    public void EntityDamageEvent(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            if ((gameStatus == GameStatus.SERVER_STARTED || gameStatus == GameStatus.INIT_GAME || gameStatus == GameStatus.TELEPORT_PLAYER)) {
                e.setCancelled(true);
            } else if (gameStatus == GameStatus.GAME_STARTED) {
                if (this.gameRunningProcedure.isInvinsibiliteActif()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void EntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            if (!this.gameRunningProcedure.isPVPActif()) {
                e.getDamager().sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Le pvp n'est pas encore actif !");
                Bukkit.getLogger().info(e.getDamager().getName() + " tried to hurt " + e.getEntity().getName());
                e.setCancelled(true);
            } else {
                Joueur j = this.joueurs.get(e.getDamager().getUniqueId());
                j.addTotalDamage(e.getFinalDamage());
            }
        }
    }

    @EventHandler
    public void BlockBreakEvent(BlockBreakEvent e) {
        if ((gameStatus == GameStatus.SERVER_STARTED || gameStatus == GameStatus.INIT_GAME || gameStatus == GameStatus.TELEPORT_PLAYER)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void BlockPlaceEvent(BlockPlaceEvent e) {
        if ((gameStatus == GameStatus.SERVER_STARTED || gameStatus == GameStatus.INIT_GAME || gameStatus == GameStatus.TELEPORT_PLAYER)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void FoodLevelChangeEvent(FoodLevelChangeEvent e) {
        if ((gameStatus == GameStatus.SERVER_STARTED || gameStatus == GameStatus.INIT_GAME || gameStatus == GameStatus.TELEPORT_PLAYER)) {
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

        if (cal.get(Calendar.MONTH) + 1 < 10) {
            sb.append("0");
        }
        sb.append(cal.get(Calendar.MONTH) + 1);

        sb.append("/");

        sb.append(cal.get(Calendar.YEAR));

        return sb.toString();
    }

    public HashMap<UUID, Joueur> getTaupeJoueurs() {
        return this.taupeJoueurs;
    }

    public WorldBorder getWorldBorder() {
        return Bukkit.getWorlds().get(0).getWorldBorder();
    }
}
