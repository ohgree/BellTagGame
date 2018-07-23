package me.blog.minjooon123.belltaggame;


import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class TagListener implements Listener{
	public static BellTagGame plugin;
	public TagListener(BellTagGame instance) {
		plugin = instance;
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerMoveBlockEvent(PlayerMoveEvent event) {
		if(!BellTagGame.isEnabled) return;
		if((int)event.getFrom().getX() != (int)event.getTo().getX()
				//|| (int)event.getFrom().getY() != (int)event.getTo().getY()
				|| (int)event.getFrom().getZ() != (int)event.getTo().getZ()) {
			if(!BellTagGame.taggers.contains(event.getPlayer().getName())) return;
			if(event.getPlayer().isSneaking()) return;
			if(BellTagGame.playerBellCount.containsKey(event.getPlayer().getName())) {
				if(BellTagGame.playerBellCount.get(event.getPlayer().getName()) < 1) {
					BellTagGame.playerBellCount.put(event.getPlayer().getName(),
							BellTagGame.playerBellCount.get(event.getPlayer().getName())+1);
					return;
				}
				BellTagGame.playerBellCount.put(event.getPlayer().getName(), 0);
				if(event.getPlayer().isSprinting()) {
					TagMethods.playBellSound(plugin, event.getPlayer(),
							BellTagGame.sprintVolume, BellTagGame.pitch);
				} else {
					TagMethods.playBellSound(plugin, event.getPlayer(),
							BellTagGame.walkVolume, BellTagGame.pitch);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if(!BellTagGame.isEnabled) return;
		if(!BellTagGame.taggerContagious) return;
		BellTagGame.taggers.add(event.getEntity().getName());
		BellTagGame.taggerTeam.addPlayer(event.getEntity());
		TagMethods.giveTaggerItems(event.getEntity());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		player.setScoreboard(plugin.board);
		for(OfflinePlayer pl: plugin.taggerTeam.getPlayers()) {
			if(pl.getName().equalsIgnoreCase(player.getName())) {
				plugin.taggerTeam.removePlayer(pl);
				plugin.taggerTeam.addPlayer(player);
				break;
			}
		}
		for(OfflinePlayer pl: plugin.runnerTeam.getPlayers()) {
			if(pl.getName().equalsIgnoreCase(player.getName())) {
				plugin.runnerTeam.removePlayer(pl);
				plugin.runnerTeam.addPlayer(player);
				break;
			}
		}
	}
}
