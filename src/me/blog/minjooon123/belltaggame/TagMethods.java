package me.blog.minjooon123.belltaggame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.NameTagVisibility;

public class TagMethods {
	public static boolean isTagger(String tagger) {
		for(String s: BellTagGame.taggers) {
			if(s.equalsIgnoreCase(tagger)) return true;
		}
		return false;
	}
	
	public static void giveTaggerItems(Player tagger) {
		if(!BellTagGame.isSwordGiven) return;
		switch(BellTagGame.swordType) {
			case 1: tagger.getInventory().addItem(new ItemStack(Material.WOOD_SWORD, 1)); break;
			case 2: tagger.getInventory().addItem(new ItemStack(Material.STONE_SWORD, 1)); break;
			case 3: tagger.getInventory().addItem(new ItemStack(Material.IRON_SWORD, 1)); break;
			case 4: tagger.getInventory().addItem(new ItemStack(Material.GOLD_SWORD, 1)); break;
			case 5: tagger.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD, 1)); break;
			default: break;
		}
	}
	
	public static void playBellSound(Plugin plugin, final Player player, final float volume, final float pitch) {
		player.getWorld().playSound(player.getLocation(), Sound.ORB_PICKUP, volume, pitch);
		Bukkit.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				player.getWorld().playSound(player.getLocation(), Sound.ORB_PICKUP, volume, pitch);
			}
		}, 2);
	}

	public static void setTeams() {

		for(Player pl: Bukkit.getOnlinePlayers()) {
			pl.setScoreboard(BellTagGame.board);
			if(BellTagGame.taggers.contains(pl.getName())) BellTagGame.taggerTeam.addPlayer(pl);
			else BellTagGame.runnerTeam.addPlayer(pl);
		}

		for(Player pl: Bukkit.getOnlinePlayers()) {
			boolean isTagger = false;
			for(String s: BellTagGame.taggers) {
				if(s.equalsIgnoreCase(pl.getName())) {
					isTagger = true;
					BellTagGame.taggerTeam.addPlayer(pl);
					break;
				}
			}
			if(!isTagger) BellTagGame.runnerTeam.addPlayer(pl);
		}
	}
}
