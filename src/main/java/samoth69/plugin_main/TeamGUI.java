package samoth69.plugin_main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class TeamGUI implements InventoryHolder, Listener {
    private Inventory inv;
    //private int numberOfTeams;

    public TeamGUI(/*int numberOfTeams*/) {
        // Create a new inventory, with "this" owner for comparison with other inventories, a size of nine, called example
        inv = Bukkit.createInventory(this, 45, "Choisie ton équipe");

        //this.numberOfTeams = numberOfTeams;

        // Put the items into the inventory
        //initializeItems();
    }

    @Override
    public Inventory getInventory()
    {
        return inv;
    }

    // You can call this whenever you want to put the items in
    /*public void initializeItems()
    {
        //inv.addItem(createGuiItem(Material.DIAMOND_SWORD, "Example Sword", "§aFirst line of the lore", "§bSecond line of the lore"));
        //inv.addItem(createGuiItem(Material.IRON_HELMET, "§bExample Helmet", "§aFirst line of the lore", "§bSecond line of the lore"));
        inv.addItem(createGuiItem(Material.WOOL, "Equipe "+ ChatColor.BLUE + "Bleu", (short)3));
    }*/

    // Nice little method to create a gui item with a custom name, and description
    protected ItemStack createGuiItem(final Material material, final String name, final short couleur, final String... lore)
    {
        final ItemStack item = new ItemStack(material, 1, couleur);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.setDisplayName(name);

        // Set the lore of the item
        meta.setLore(Arrays.asList(lore));

        item.setItemMeta(meta);

        return item;
    }

    public void updateTeams(List<Equipe> equipes) {
        inv.clear();
        if (equipes.size() != 0) {
            for (Equipe e : equipes) {
                StringBuilder sb = new StringBuilder();
                sb.append(ChatColor.RESET);
                sb.append("Joueurs dans cette équipe:\r\n");
                if (e.getJoueurs().size() > 0) {
                    for (Joueur j: e.getJoueurs()) {
                        sb.append(e.getChatColor());
                        sb.append("- ");
                        sb.append(j.getPseudo());
                        sb.append("\n\n");
                    }
                } else {
                    sb.append(ChatColor.ITALIC);
                    sb.append("Aucun joueur dans cette équipe");
                }
                inv.addItem(createGuiItem(Material.WOOL, "Equipe " + e.getChatColor() + e.getTeamName(), e.getCouleurEquipe(), sb.toString()));
            }
        }
    }

    // You can open the inventory with this
    public void openInventory(final HumanEntity ent)
    {
        ent.openInventory(inv);
    }
}