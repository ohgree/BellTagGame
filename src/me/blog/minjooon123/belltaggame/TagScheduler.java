/*
 *  DetectiveGame - A bukkit plugin for Detective Games
 *  Copyright (C) 2015  InfectedDuck
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Contact: minjooon123@naver.com | Skype: minjooon123
 */

package me.blog.minjooon123.belltaggame;

import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.UUID;

/**
 * Created by InfectedDuck on 2015. 12. 13..
 */
public class TagScheduler {
    private static BukkitRunnable markerTask;
    public static void cancelMarkerTask() {
        markerTask.cancel();
    }
    public static void setMarkerTask(int delayInMinutes) {
        markerTask = new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player: Bukkit.getOnlinePlayers()) {
                    if(BellTagGame.taggers.contains(player.getName())) continue;
                    Location loc = player.getLocation();
                    final ArmorStand marker = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
                    marker.setSmall(true);

                    ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
                    SkullMeta meta = (SkullMeta) skull.getItemMeta();
                    meta.setOwner(player.getName());
                    skull.setItemMeta(meta);

                    marker.setHelmet(skull);
                    marker.setCustomNameVisible(true);
                    marker.setCustomName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "[ "
                            + ChatColor.YELLOW + "" + ChatColor.BOLD + player.getName()
                            + ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + " ]");
                    marker.setMetadata("BTmarker", new FixedMetadataValue(BellTagGame.instance, "1"));

                    BukkitRunnable markerTask = new BukkitRunnable() {
                        public void run() {
                            marker.remove();
                        }
                    };
                    markerTask.runTaskLater(BellTagGame.instance, 20 * 20L);
                }
                //Bukkit.broadcastMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "[" + ChatColor.BLUE + "공지" + ChatColor.AQUA + "] "
                //       + ChatColor.YELLOW + "생존 플레이어들의 위치가 표시되었습니다. 마커는 20초 후 사라집니다.");
            }
        };
        markerTask.runTaskTimer(BellTagGame.instance, delayInMinutes * 60 * 20L, delayInMinutes * 60 * 20L);
    }

    public static void sidebarCountDownWithSound(final int count) {
        Scoreboard sb = BellTagGame.board;
        final Objective o = sb.registerNewObjective(UUID.randomUUID().toString().substring(0, 15), "dummy");
        o.setDisplayName(ChatColor.GOLD + "남은시간");
        o.setDisplaySlot(DisplaySlot.SIDEBAR);
        final Score score = o.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + ""));

        BukkitRunnable task = new BukkitRunnable() {
            int countdown = count * 60;
            public void run() {
                score.setScore(countdown);
                if(countdown < 11) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (countdown == 0) p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 0.7f);
                        else p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1, 0.8f);
                    }
                }

                if (countdown-- == 0) { //If countdown == 0.
                    Bukkit.broadcastMessage(ChatColor.BLUE + " " + ChatColor.BOLD + "정해진 시간이 지났습니다");
                    o.unregister();
                    this.cancel();
                }
            }
        };
        task.runTaskTimer(BellTagGame.instance, 0L, 20L);
    }
}
