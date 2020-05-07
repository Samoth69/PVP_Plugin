package samoth69.plugin;

import com.comphenix.packetwrapper.WrapperPlayServerPlayerInfo;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PacketMgnt extends PacketAdapter implements Listener{

    private HashMap<UUID, Joueur> joueurs;

    public PacketMgnt(Plugin plugin, HashMap<UUID, Joueur>  joueurs, PacketType... types) {
        super(plugin, types);
        this.joueurs = joueurs;
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        WrapperPlayServerPlayerInfo wrapper = new WrapperPlayServerPlayerInfo(event.getPacket());
        Player target = event.getPlayer();

        List<PlayerInfoData> playerInfoDataList = wrapper.getData();

        if (wrapper.getAction() != EnumWrappers.PlayerInfoAction.ADD_PLAYER) {
            return;
        }

        List<PlayerInfoData> newPlayerInfoDataList = Lists.newArrayList();

        for (PlayerInfoData playerInfoData : playerInfoDataList) {
            Player player;

            if (playerInfoData == null || playerInfoData.getProfile() == null || (player = Bukkit.getPlayer(playerInfoData.getProfile().getUUID())) == null || !player.isOnline()) {
                newPlayerInfoDataList.add(playerInfoData);
                continue;
            }

            WrappedGameProfile profile = playerInfoData.getProfile();
            Joueur j = this.joueurs.get(player.getUniqueId());
            String newNick;

            if (j != null) {
                if (j.isInTeam()) {
                    newNick = j.getPseudoWithTeam();
                } else {
                    newNick = j.getPseudo();
                }
            } else {
                newNick = player.getName();
            }

            WrappedGameProfile newProfile = profile.withName(newNick);
            newProfile.getProperties().putAll(profile.getProperties());

            PlayerInfoData newPlayerInfoData = new PlayerInfoData(newProfile, playerInfoData.getPing(), playerInfoData.getGameMode(), playerInfoData.getDisplayName());
            newPlayerInfoDataList.add(newPlayerInfoData);
        }

        wrapper.setData(newPlayerInfoDataList);
    }
}
