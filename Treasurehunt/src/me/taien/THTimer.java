package me.taien;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import me.taien.config.THWorldOpts;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;

public class THTimer
  implements Runnable
{
  public void run()
  {
    if (TreasureHunt.worlds.size() == 0)
    {
      return;
    }

    long curtime = System.currentTimeMillis();
    String wn;
    for (Iterator localIterator = TreasureHunt.worlds.entrySet().iterator(); localIterator.hasNext(); ) { e = (Map.Entry)localIterator.next();

      wn = (String)e.getKey();
      THWorldOpts o = (THWorldOpts)e.getValue();
      if (o.enabled)
      {
        Block target;
        if (o.searchingloc)
        {
          World w = Bukkit.getServer().getWorld(wn);
          target = THChestGenerator.findLocation(w, o, TreasureHunt.maxattemptspertick);
          o.searchingattempts += TreasureHunt.maxattemptspertick;

          if (target != null)
          {
            if (TreasureHunt.detaillogs) System.out.println("[THDetails] Location selection took about " + o.searchingattempts + " runs.");
            THChestGenerator.startHunt(w, o.searchingvalue, target, false, null);
            o.searchingloc = false;
            o.searchingattempts = 0;
            o.searchingvalue = 0;
          }
          else if ((target == null) && (o.searchingattempts >= TreasureHunt.maxspawnattempts))
          {
            System.out.println("[TreasureHunt] Failed to find suitable location in world '" + wn + "' after " + TreasureHunt.maxspawnattempts + " runs.");
            o.searchingloc = false;
            o.searchingattempts = 0;
            o.searchingvalue = 0;
          }

        }
        else if (curtime >= o.lastcheck + o.interval * 1000)
        {
          if (TreasureHunt.getHuntsInWorld(wn).size() < o.minchests)
          {
            if ((o.overrideminplayers) || (TreasureHunt.server.getOnlinePlayers().length >= TreasureHunt.minplayers))
            {
              o.searchingloc = true;
              o.searchingvalue = THChestGenerator.findValue(o);
              o.searchingattempts = 0;
            }
          }

          if (o.chance < 1)
          {
            System.out.println("[TreasureHunt] Settings for world '" + (String)e.getKey() + "' are incorrect:  ChestChance cannot be less than 1.");
          }
          else if ((o.chance == 1) || (TreasureHunt.rndGen.nextInt(o.chance) == 0))
          {
            if (TreasureHunt.server.getOnlinePlayers().length >= TreasureHunt.minplayers)
            {
              o.searchingloc = true;
              o.searchingvalue = THChestGenerator.findValue(o);
              o.searchingattempts = 0;
            }
            else if (TreasureHunt.detaillogs) { System.out.println("[THDetails] Chest would have spawned, but insufficient players online."); }

          }
          THStationaryChest c;
          for (target = TreasureHunt.stationaryList.iterator(); target.hasNext(); c.tick(curtime)) c = (THStationaryChest)target.next();
          o.lastcheck = curtime;
        }
      }

    }

    THHunt[] hunts = new THHunt[TreasureHunt.huntList.size()];
    hunts = (THHunt[])TreasureHunt.huntList.toArray(hunts);
    Map.Entry localEntry1 = (wn = hunts).length; for (Map.Entry e = 0; e < localEntry1; e++) { THHunt h = wn[e];

      if (h.isExpired())
      {
        if ((h.isLocked()) && (!((THWorldOpts)TreasureHunt.worlds.get(h.getWorld())).fadefoundchests))
        {
          TreasureHunt.huntList.remove(h);
        }
        else
        {
          h.removeChest(false);
          TreasureHunt.huntList.remove(h);
        }
      }
    }
  }
}