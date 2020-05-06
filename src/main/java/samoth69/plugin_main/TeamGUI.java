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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeamGUI implements InventoryHolder, Listener {
    private Inventory inv;

    public TeamGUI(/*int numberOfTeams*/) {
        // Create a new inventory, with "this" owner for comparison with other inventories, a size of nine, called example
        inv = Bukkit.createInventory(this, 45, "Choisie ton équipe");
    }

    @Override
    public Inventory getInventory()
    {
        return inv;
    }

    // Nice little method to create a gui item with a custom name, and description
    protected ItemStack createGuiItem(final Material material, final String name, final short couleur, final ArrayList<String> lore)
    {
        final ItemStack item = new ItemStack(material, 1, couleur);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.setDisplayName(name);

        // Set the lore of the item
        meta.setLore(lore);

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        item.setItemMeta(meta);

        return item;
    }

    public void updateTeams(List<Equipe> equipes) {
        //inv.clear();
        int index = 0;
        if (equipes.size() != 0) {
            for (Equipe e : equipes) {
                ArrayList<String> lore = new ArrayList<>();
                lore.add(ChatColor.RESET + "Joueurs dans cette équipe:");
                if (e.getJoueurs().size() > 0) {
                    for (Joueur j: e.getJoueurs()) {
                        lore.add(e.getChatColor() + "- " + j.getPseudo());
                    }
                } else {
                    lore.add(ChatColor.ITALIC + "Aucun joueur dans cette équipe");
                }
                inv.setItem(index, createGuiItem(Material.WOOL, "Equipe " + e.getChatColor() + e.getTeamName(), e.getCouleurEquipe(), lore));
                index++;
            }
        }
    }

    // You can open the inventory with this
    public void openInventory(final HumanEntity ent)
    {
        ent.openInventory(inv);
    }
}
