package me.taien.listeners;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import me.taien.THHunt;
import me.taien.THStationaryChest;
import me.taien.TreasureHunt;
import me.taien.config.THToolSettings;
import me.taien.config.THWorldOpts;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;

public class THListener
  implements Listener
{
  public THListener(TreasureHunt plugin)
  {
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event)
  {
    Player p = event.getPlayer();
    if ((TreasureHunt.useperms) && ((TreasureHunt.permission.has(p, "taien.th.nodetect." + p.getWorld().getName())) || (TreasureHunt.permission.has(p, "taien.th.nodetect.*"))))
      TreasureHunt.nodetectlist.add(p);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event)
  {
    Player p = event.getPlayer();
    if (TreasureHunt.nodetectlist.contains(p)) TreasureHunt.nodetectlist.remove(p);
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event)
  {
    if (!TreasureHunt.protectbreak) return;
    Block eb = event.getBlock();
    for (THHunt h : TreasureHunt.huntList) if (h.isChestBlock(eb))
      {
        event.getPlayer().sendMessage(ChatColor.DARK_RED + "You can't break treasure chests or the blocks under them!");
        event.setCancelled(true);
        return;
      }
    for (THStationaryChest c : TreasureHunt.stationaryList) if ((c.hunt != null) && (c.hunt.isChestBlock(eb)))
      {
        event.getPlayer().sendMessage(ChatColor.DARK_RED + "You can't break treasure chests or the blocks under them!");
        event.setCancelled(true);
        return;
      }
    for (Block b : TreasureHunt.compassblocks)
    {
      if (b.equals(eb))
      {
        event.getPlayer().sendMessage(ChatColor.DARK_RED + "You can't break compass blocks!");
        event.setCancelled(true);
        return;
      }
    }
  }

  @EventHandler
  public void onBlockBurn(BlockBurnEvent e)
  {
    if (!TreasureHunt.protectburn) return;
    Block b = e.getBlock();
    for (THHunt h : TreasureHunt.huntList) if (h.isChestBlock(b))
      {
        e.setCancelled(true);
        return;
      }
    for (THStationaryChest c : TreasureHunt.stationaryList) if ((c.hunt != null) && (c.hunt.isChestBlock(b)))
      {
        e.setCancelled(true);
        return;
      }
    for (Block bb : TreasureHunt.compassblocks)
    {
      if (b.equals(bb))
      {
        e.setCancelled(true);
        return;
      }
    }
  }

  @EventHandler
  public void onEntityExplodeEvent(EntityExplodeEvent e)
  {
    if (!TreasureHunt.protectexplode)
      return;
    Iterator localIterator2;
    for (Iterator localIterator1 = TreasureHunt.huntList.iterator(); localIterator1.hasNext(); localIterator2.hasNext()) { THHunt h = (THHunt)localIterator1.next(); localIterator2 = e.blockList().iterator(); continue; Block b = (Block)localIterator2.next(); if (h.isChestBlock(b))
      {
        e.setCancelled(true);
        return;
      } }
    for (localIterator1 = TreasureHunt.stationaryList.iterator(); localIterator1.hasNext(); localIterator2.hasNext()) { THStationaryChest c = (THStationaryChest)localIterator1.next(); localIterator2 = e.blockList().iterator(); continue; Block b = (Block)localIterator2.next(); if ((c.hunt != null) && (c.hunt.isChestBlock(b)))
      {
        e.setCancelled(true);
        return;
      } }
    for (Block bb : TreasureHunt.compassblocks)
    {
      if (e.blockList().contains(bb))
      {
        e.setCancelled(true);
        return;
      }
    }
  }

  @EventHandler
  public void onBlockPistonExtendEvent(BlockPistonExtendEvent e)
  {
    if (!TreasureHunt.protectpiston)
      return;
    Iterator localIterator2;
    for (Iterator localIterator1 = TreasureHunt.huntList.iterator(); localIterator1.hasNext(); localIterator2.hasNext()) { THHunt h = (THHunt)localIterator1.next(); localIterator2 = e.getBlocks().iterator(); continue; Block b = (Block)localIterator2.next(); if (h.isChestBlock(b))
      {
        e.setCancelled(true);
        return;
      } }
    for (localIterator1 = TreasureHunt.stationaryList.iterator(); localIterator1.hasNext(); localIterator2.hasNext()) { THStationaryChest c = (THStationaryChest)localIterator1.next(); localIterator2 = e.getBlocks().iterator(); continue; Block b = (Block)localIterator2.next(); if ((c.hunt != null) && (c.hunt.isChestBlock(b)))
      {
        e.setCancelled(true);
        return;
      } }
    for (Block bb : TreasureHunt.compassblocks)
    {
      if (e.getBlocks().contains(bb))
      {
        e.setCancelled(true);
        return;
      }
    }
  }

  @EventHandler
  public void onBlockPistonRetractEvent(BlockPistonRetractEvent e)
  {
    if (!TreasureHunt.protectpiston) return;
    Block b = e.getBlock();
    for (THHunt h : TreasureHunt.huntList) if (h.isChestBlock(b))
      {
        e.setCancelled(true);
        return;
      }
    for (THStationaryChest c : TreasureHunt.stationaryList) if ((c.hunt != null) && (c.hunt.isChestBlock(b)))
      {
        e.setCancelled(true);
        return;
      }
    for (Block bb : TreasureHunt.compassblocks)
    {
      if (b.equals(bb))
      {
        e.setCancelled(true);
        return;
      }
    }
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event)
  {
    Block eb = event.getBlock();
    Player p = event.getPlayer();
    if ((TreasureHunt.selections.containsKey(p)) && (event.getBlockPlaced().getType() == Material.OBSIDIAN))
    {
      THToolSettings ts = (THToolSettings)TreasureHunt.selections.get(p);
      THStationaryChest removal = null;
      for (THStationaryChest c : TreasureHunt.stationaryList)
      {
        if (c.chest.equals(eb))
        {
          if (c.hunt != null) c.removeChest();
          removal = c;
          p.sendMessage(ChatColor.DARK_PURPLE + "Stationary chest location removed.");
          break;
        }
      }
      if (removal != null)
      {
        TreasureHunt.stationaryList.remove(removal);
        event.setCancelled(true);
        return;
      }

      TreasureHunt.stationaryList.add(new THStationaryChest(ts.itemlist, ts.minminutes, ts.maxminutes, ts.value, eb));
      p.sendMessage(ChatColor.DARK_PURPLE + "Created stationary chest at Loc: " + eb.getX() + "," + eb.getY() + "," + eb.getZ() + " Val: " + ts.value + " MinMinutes: " + ts.minminutes + " MaxMinutes: " + ts.maxminutes);
      event.setCancelled(true);
      return;
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event)
  {
    Player p = event.getPlayer();
    THWorldOpts o = null;
    if (TreasureHunt.worlds.containsKey(p.getWorld().getName())) o = (THWorldOpts)TreasureHunt.worlds.get(p.getWorld().getName()); else {
      return;
    }
    if ((event.getAction() == Action.RIGHT_CLICK_BLOCK) && (event.getClickedBlock().getType() == Material.CHEST))
    {
      THHunt target = TreasureHunt.getCurrentHunt(event.getClickedBlock().getLocation());
      if (target != null)
      {
        if (target.isLocked())
        {
          if (p == target.getPlayerFound()) return;

          Map data = new HashMap();
          data.put("rarity", target.getRarityString());
          data.put("value", Integer.toString(target.getValue()));
          data.put("location", target.getLocString());
          data.put("worldname", p.getWorld().getName());
          data.put("numhunts", Integer.toString(TreasureHunt.getHuntsInWorld(p.getWorld().getName()).size()));
          data.put("timeleft", target.getMinutesLeft() + " minutes");
          data.put("pname", target.getPlayerFound().getName());
          p.sendMessage(TreasureHunt.colorize(TreasureHunt.convertTags(TreasureHunt.alreadyclaimed, data)));
          event.setCancelled(true);
          return;
        }

        if ((!TreasureHunt.useperms) || ((TreasureHunt.useperms) && ((TreasureHunt.permission.has(p, "taien.th.claim." + p.getWorld().getName())) || (TreasureHunt.permission.has(p, "taien.th.claim.*")))))
        {
          target.chestFoundBy(p, false);
          return;
        }

        p.sendMessage(ChatColor.DARK_RED + "You are not allowed to claim chests!");
        event.setCancelled(true);
        return;
      }

      THStationaryChest stattarget = TreasureHunt.getStationaryChest(event.getClickedBlock());
      if ((stattarget != null) && (stattarget.hunt != null))
      {
        if (stattarget.hunt.isLocked())
        {
          if (p == stattarget.hunt.getPlayerFound()) return;

          Map data = new HashMap();
          data.put("rarity", stattarget.hunt.getRarityString());
          data.put("value", Integer.toString(stattarget.hunt.getValue()));
          data.put("location", stattarget.hunt.getLocString());
          data.put("worldname", p.getWorld().getName());
          data.put("numhunts", Integer.toString(TreasureHunt.getHuntsInWorld(p.getWorld().getName()).size()));
          data.put("timeleft", stattarget.hunt.getMinutesLeft() + " minutes");
          data.put("pname", stattarget.hunt.getPlayerFound().getName());
          p.sendMessage(TreasureHunt.colorize(TreasureHunt.convertTags(TreasureHunt.alreadyclaimed, data)));
          event.setCancelled(true);
          return;
        }

        if ((!TreasureHunt.useperms) || ((TreasureHunt.useperms) && ((TreasureHunt.permission.has(p, "taien.th.claimstat." + p.getWorld().getName())) || (TreasureHunt.permission.has(p, "taien.th.claimstat.*")))))
        {
          stattarget.hunt.chestFoundBy(p, true);
          return;
        }

        p.sendMessage(ChatColor.DARK_RED + "You are not allowed to claim stationary chests!");
        event.setCancelled(true);
        return;
      }
      return;
    }
    ItemStack i;
    if (((event.getAction() == Action.LEFT_CLICK_AIR) || (event.getAction() == Action.LEFT_CLICK_BLOCK)) && (event.hasItem()) && (event.getItem().getType() == o.hunttool))
    {
      if ((!TreasureHunt.useperms) || ((TreasureHunt.useperms) && ((TreasureHunt.permission.has(p, "taien.th.tool." + p.getWorld().getName())) || (TreasureHunt.permission.has(p, "taien.th.tool.*")))))
      {
        if ((!TreasureHunt.lastcheck.containsKey(p)) || (((Long)TreasureHunt.lastcheck.get(p)).longValue() < System.currentTimeMillis() - 1000 * TreasureHunt.checksec))
        {
          int cc = o.consumechance;
          if (cc > 0)
          {
            if ((cc >= 100) || (TreasureHunt.rndGen.nextInt(100) < cc))
            {
              if ((!TreasureHunt.useperms) || ((!TreasureHunt.permission.has(p, "taien.th.noconsume." + p.getWorld().getName())) && (!TreasureHunt.permission.has(p, "taien.th.noconsume.*"))))
              {
                ItemStack tstack = null;
                for (i : p.getInventory().getContents())
                {
                  if ((i != null) && (i.getType() == o.hunttool)) tstack = i;
                }
                if (tstack != null)
                {
                  if (tstack.getAmount() == 1) p.getInventory().clear(p.getInventory().first(tstack)); else
                    p.getInventory().setItem(p.getInventory().first(tstack), new ItemStack(o.hunttool, tstack.getAmount() - 1)); 
                }
              }
            }
          }
          TreasureHunt.getClosestHunt(p, true);
          TreasureHunt.lastcheck.put(p, Long.valueOf(System.currentTimeMillis()));
          return;
        }

        p.sendMessage(ChatColor.DARK_RED + "You can only check for the closest chest once every " + TreasureHunt.checksec + " seconds.");
        return;
      }

      return;
    }
    if ((event.getAction() == Action.LEFT_CLICK_BLOCK) && (TreasureHunt.usecompass) && (event.hasItem()) && (event.getItem().getType() == o.offeringtool))
    {
      Block b = event.getClickedBlock();

      for (Block cb : TreasureHunt.compassblocks)
      {
        if ((b.getWorld().getName().equalsIgnoreCase(cb.getWorld().getName())) && (b.getX() == cb.getX()) && (b.getY() == cb.getY()) && (b.getZ() == cb.getZ()))
        {
          event.setCancelled(true);
          if ((TreasureHunt.useperms) && (!TreasureHunt.permission.has(p, "taien.th.compass." + p.getWorld().getName())) && (!TreasureHunt.permission.has(p, "taien.th.compass.*")))
          {
            p.sendMessage(ChatColor.DARK_RED + "You aren't allowed to use compass blocks!");
            return;
          }
          if (TreasureHunt.getAmountInInventory(p.getInventory(), o.offeringtool, (short)0) < o.offeramount)
          {
            p.sendMessage(ChatColor.DARK_RED + "You don't have enough " + o.offeringtool.name() + " to make an offering!");
            return;
          }
          Object data = new HashMap();
          ((Map)data).put("pname", p.getName());
          ((Map)data).put("worldname", p.getWorld().getName());
          ((Map)data).put("numhunts", Integer.toString(TreasureHunt.getHuntsInWorld(p.getWorld().getName()).size()));
          ((Map)data).put("item", o.offeringtool.name());
          p.sendMessage(TreasureHunt.colorize(TreasureHunt.convertTags(TreasureHunt.offeritem, (Map)data)));

          TreasureHunt.takeItemFromPlayer(p.getInventory(), o.offeringtool, (short)0, o.offeramount);

          THHunt h = TreasureHunt.getClosestHunt(p, false);
          if (h == null) { p.sendMessage(TreasureHunt.colorize(TreasureHunt.convertTags(TreasureHunt.compassnochange, (Map)data))); break;
          }

          int distance = TreasureHunt.threedimensionaldistance ? h.get3DDistanceFrom(p.getLocation()) : h.getDistanceFrom(p.getLocation());
          if (distance > o.maxcompassdistance) { p.sendMessage(TreasureHunt.colorize(TreasureHunt.convertTags(TreasureHunt.compassnochange, (Map)data))); break;
          }

          ((Map)data).put("distance", Integer.toString(distance));
          ((Map)data).put("rarity", h.getRarityString());
          ((Map)data).put("value", Integer.toString(h.getValue()));
          ((Map)data).put("location", h.getLocString());
          ((Map)data).put("timeleft", h.getMinutesLeft() + " minutes");
          p.setCompassTarget(h.getLocation());
          p.sendMessage(TreasureHunt.colorize(TreasureHunt.convertTags(TreasureHunt.compasschange, (Map)data)));

          break;
        }
      }
      return;
    }
  }
}