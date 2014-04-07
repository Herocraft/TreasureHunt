package me.taien;

import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import me.taien.config.THWorldList;
import me.taien.config.THWorldOpts;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class THChestGenerator
{
  public static int findValue(THWorldOpts o)
  {
    int value = o.maxvalue;
    int current = value;
    for (int i = 1; i <= o.drawweight; i++)
    {
      value = TreasureHunt.rndGen.nextInt(o.maxvalue);
      current = value < current ? value : current;
    }
    return current;
  }

  public static Block findLocation(World w, THWorldOpts o, int amtofruns)
  {
    Block target = null;
    boolean found = false;
    int attempt = 0;
    int x = 0;
    int y = 0;
    int z = 0;
    int minxpos = o.centerx + o.mindistance;
    int minxneg = o.centerx - o.mindistance;
    int minzpos = o.centerz + o.mindistance;
    int minzneg = o.centerz - o.mindistance;
    do
    {
      attempt++;
      x = TreasureHunt.rndGen.nextInt(o.maxdistance * 2) - o.maxdistance + o.centerx;
      z = TreasureHunt.rndGen.nextInt(o.maxdistance * 2) - o.maxdistance + o.centerz;
      do
      {
        x = TreasureHunt.rndGen.nextInt(o.maxdistance * 2) - o.maxdistance + o.centerx;
        z = TreasureHunt.rndGen.nextInt(o.maxdistance * 2) - o.maxdistance + o.centerz;
      }
      while (((x < minxpos) && (x > minxneg)) || ((z < minzpos) && (
        z > minzneg)));
      do
      {
        if (o.searchingvalue < 2500) y = TreasureHunt.rndGen.nextInt(o.maxelevation); else
          y = TreasureHunt.rndGen.nextInt(o.maxelevationrare);
      }
      while (
        y < o.minelevation);
      target = w.getBlockAt(x, y, z);
      if ((target.getType() == Material.AIR) && (target.getLightLevel() <= o.maxlight) && (target.getLightLevel() >= o.minlight)) {
        target = w.getBlockAt(x, y - 1, z);
        if (o.spawnableblocks.contains(target.getType()))
        {
          if (TreasureHunt.towny != null)
          {
            if (!TownyUniverse.isWilderness(target))
            {
              System.out.println("[TreasureHuntDebug] Potential location denied due to Towny block.");
              continue;
            }
          }
          if (TreasureHunt.worldguard != null)
          {
            THFakePlayer p = new THFakePlayer();
            LocalPlayer lp = TreasureHunt.worldguard.wrapPlayer(p);
            ApplicableRegionSet rs = TreasureHunt.worldguard.getRegionManager(w).getApplicableRegions(BukkitUtil.toVector(target));
            if ((!rs.canBuild(lp)) && (!rs.allows(DefaultFlag.CHEST_ACCESS, lp)))
            {
              System.out.println("[TreasureHuntDebug] Potential location denied due to WorldGuard.");
              continue;
            }
          }
          found = true;
          target = w.getBlockAt(x, y, z);
        }
      }
    }
    while ((!found) && (
      attempt <= amtofruns));
    if (found) return target;
    return null;
  }

  public static int populateItems(Inventory contents, THWorldOpts o, int current, boolean stationary, String stationaryitemlist) {
    int generatedvalue = 0;

    Map all = new HashMap();
    Map allvals = new HashMap();
    int i = 0;
    if (!stationary)
    {
      if (TreasureHunt.worldlists.get(o.itemlist) == null)
      {
        System.out.println("[TreasureHunt] Couldn't fill chest!  WorldList '" + o.itemlist + "' doesn't exist!");
        return -1;
      }
      if (!o.strictitems)
      {
        if (current >= TreasureHunt.epiclevel) for (Map.Entry e : ((THWorldList)TreasureHunt.worldlists.get(o.itemlist)).epic.entrySet())
          {
            all.put(Integer.valueOf(i), (ItemStack)e.getKey());
            allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
            i++;
          }
        if (current >= TreasureHunt.legendarylevel) for (Map.Entry e : ((THWorldList)TreasureHunt.worldlists.get(o.itemlist)).legendary.entrySet())
          {
            all.put(Integer.valueOf(i), (ItemStack)e.getKey());
            allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
            i++;
          }
        if (current >= TreasureHunt.rarelevel) for (Map.Entry e : ((THWorldList)TreasureHunt.worldlists.get(o.itemlist)).rare.entrySet())
          {
            all.put(Integer.valueOf(i), (ItemStack)e.getKey());
            allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
            i++;
          }
        if (current >= TreasureHunt.uncommonlevel) for (Map.Entry e : ((THWorldList)TreasureHunt.worldlists.get(o.itemlist)).uncommon.entrySet())
          {
            all.put(Integer.valueOf(i), (ItemStack)e.getKey());
            allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
            i++;
          }
        for (Map.Entry e : ((THWorldList)TreasureHunt.worldlists.get(o.itemlist)).common.entrySet())
        {
          all.put(Integer.valueOf(i), (ItemStack)e.getKey());
          allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
          i++;
        }

      }
      else if (current >= TreasureHunt.epiclevel) { for (Map.Entry e : ((THWorldList)TreasureHunt.worldlists.get(o.itemlist)).epic.entrySet())
        {
          all.put(Integer.valueOf(i), (ItemStack)e.getKey());
          allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
          i++;
        }
      } else if (current >= TreasureHunt.legendarylevel) { for (Map.Entry e : ((THWorldList)TreasureHunt.worldlists.get(o.itemlist)).legendary.entrySet())
        {
          all.put(Integer.valueOf(i), (ItemStack)e.getKey());
          allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
          i++;
        }
      } else if (current >= TreasureHunt.rarelevel) { for (Map.Entry e : ((THWorldList)TreasureHunt.worldlists.get(o.itemlist)).rare.entrySet())
        {
          all.put(Integer.valueOf(i), (ItemStack)e.getKey());
          allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
          i++;
        }
      } else if (current >= TreasureHunt.uncommonlevel) { for (Map.Entry e : ((THWorldList)TreasureHunt.worldlists.get(o.itemlist)).uncommon.entrySet())
        {
          all.put(Integer.valueOf(i), (ItemStack)e.getKey());
          allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
          i++;
        } } else {
        for (Map.Entry e : ((THWorldList)TreasureHunt.worldlists.get(o.itemlist)).common.entrySet())
        {
          all.put(Integer.valueOf(i), (ItemStack)e.getKey());
          allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
          i++;
        }
      }
    }
    else
    {
      boolean customlist = true;
      if (TreasureHunt.customlists.get(stationaryitemlist) == null) customlist = false;
      if ((!customlist) && (TreasureHunt.worldlists.get(stationaryitemlist) == null))
      {
        System.out.println("[TreasureHunt] Couldn't fill stationary chest!  WorldList/CustomList '" + stationaryitemlist + "' doesn't exist!");
        return -1;
      }
      if (customlist)
      {
        for (Map.Entry e : ((Map)TreasureHunt.customlists.get(stationaryitemlist)).entrySet())
        {
          all.put(Integer.valueOf(i), (ItemStack)e.getKey());
          allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
          i++;
        }

      }
      else if (!o.strictitems)
      {
        if (current >= TreasureHunt.epiclevel) for (Map.Entry e : ((THWorldList)TreasureHunt.worldlists.get(o.itemlist)).epic.entrySet())
          {
            all.put(Integer.valueOf(i), (ItemStack)e.getKey());
            allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
            i++;
          }
        if (current >= TreasureHunt.legendarylevel) for (Map.Entry e : ((THWorldList)TreasureHunt.worldlists.get(o.itemlist)).legendary.entrySet())
          {
            all.put(Integer.valueOf(i), (ItemStack)e.getKey());
            allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
            i++;
          }
        if (current >= TreasureHunt.rarelevel) for (Map.Entry e : ((THWorldList)TreasureHunt.worldlists.get(o.itemlist)).rare.entrySet())
          {
            all.put(Integer.valueOf(i), (ItemStack)e.getKey());
            allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
            i++;
          }
        if (current >= TreasureHunt.uncommonlevel) for (Map.Entry e : ((THWorldList)TreasureHunt.worldlists.get(o.itemlist)).uncommon.entrySet())
          {
            all.put(Integer.valueOf(i), (ItemStack)e.getKey());
            allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
            i++;
          }
        for (Map.Entry e : ((THWorldList)TreasureHunt.worldlists.get(o.itemlist)).common.entrySet())
        {
          all.put(Integer.valueOf(i), (ItemStack)e.getKey());
          allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
          i++;
        }

      }
      else if (current >= TreasureHunt.epiclevel) { for (Map.Entry e : ((THWorldList)TreasureHunt.worldlists.get(o.itemlist)).epic.entrySet())
        {
          all.put(Integer.valueOf(i), (ItemStack)e.getKey());
          allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
          i++;
        }
      } else if (current >= TreasureHunt.legendarylevel) { for (Map.Entry e : ((THWorldList)TreasureHunt.worldlists.get(o.itemlist)).legendary.entrySet())
        {
          all.put(Integer.valueOf(i), (ItemStack)e.getKey());
          allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
          i++;
        }
      } else if (current >= TreasureHunt.rarelevel) { for (Map.Entry e : ((THWorldList)TreasureHunt.worldlists.get(o.itemlist)).rare.entrySet())
        {
          all.put(Integer.valueOf(i), (ItemStack)e.getKey());
          allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
          i++;
        }
      } else if (current >= TreasureHunt.uncommonlevel) { for (Map.Entry e : ((THWorldList)TreasureHunt.worldlists.get(o.itemlist)).uncommon.entrySet())
        {
          all.put(Integer.valueOf(i), (ItemStack)e.getKey());
          allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
          i++;
        } } else {
        for (Map.Entry e : ((THWorldList)TreasureHunt.worldlists.get(o.itemlist)).common.entrySet())
        {
          all.put(Integer.valueOf(i), (ItemStack)e.getKey());
          allvals.put((ItemStack)e.getKey(), (Integer)e.getValue());
          i++;
        }

      }

    }

    do
    {
      int nbr = 0;
      int curnbr = all.size();
      if (all.size() < 1)
      {
        System.out.println("[TreasureHunt] There appear to be no items in the selection list...chest spawn failed.");
        return -1;
      }
      for (int draw = 1; draw <= o.gooditemweight; draw++)
      {
        nbr = TreasureHunt.rndGen.nextInt(all.size());
        curnbr = nbr < curnbr ? nbr : curnbr;
      }
      ItemStack item = (ItemStack)all.get(Integer.valueOf(curnbr));
      if (item.getMaxStackSize() == 1)
      {
        contents.addItem(new ItemStack[] { item });
        generatedvalue += ((Integer)allvals.get(item)).intValue();
      }
      else
      {
        int maxamt = (current - generatedvalue) / ((Integer)allvals.get(item)).intValue();
        if (maxamt <= 0)
        {
          if (item.getDurability() == 0) contents.addItem(new ItemStack[] { new ItemStack(item.getType(), 1) }); else
            contents.addItem(new ItemStack[] { new ItemStack(item.getType(), 1, item.getDurability()) });
          generatedvalue += ((Integer)allvals.get(item)).intValue();
        }
        else
        {
          int amt = TreasureHunt.rndGen.nextInt(maxamt) + 1;
          for (int ii = 1; ii < o.amountweight; ii++)
          {
            int newamt = TreasureHunt.rndGen.nextInt(maxamt) + 1;
            if (newamt < amt) amt = newamt;
          }
          if (item.getDurability() == 0) contents.addItem(new ItemStack[] { new ItemStack(item.getType(), amt) }); else
            contents.addItem(new ItemStack[] { new ItemStack(item.getType(), amt, item.getDurability()) });
          generatedvalue += ((Integer)allvals.get(item)).intValue() * amt;
        }
      }
    }
    while ((generatedvalue < current) && (
      contents.firstEmpty() >= 0));
    current = generatedvalue;

    return current;
  }

  public static THHunt startHunt(World worldtouse, int setvalue, Block block, boolean stationary, String stationaryitemlist)
  {
    if (TreasureHunt.worlds.size() == 0)
    {
      System.out.println("[TreasureHunt] Unable to start hunt!  No worlds set!");
      return null;
    }
    THWorldOpts o = (THWorldOpts)TreasureHunt.worlds.get(worldtouse.getName());
    if (o == null)
    {
      System.out.println("[TreasureHunt] Unable to start hunt!  World not set up!");
      return null;
    }
    int maxvalue = o.maxvalue + 1;
    int value = maxvalue;
    int current = value;
    int x = 0;
    int y = 0;
    int z = 0;
    Inventory contents = null;
    Material oldblock = null;

    if (setvalue == -1)
    {
      value = findValue(o);
      current = value;
    }
    else
    {
      value = setvalue;
      current = setvalue;
    }

    if (block == null)
    {
      o.searchingloc = true;
      o.searchingattempts = 0;
      o.searchingvalue = current;
      return null;
    }

    x = block.getX();
    y = block.getY() - 1;
    z = block.getZ();
    if (o.usemarker) {
      block = worldtouse.getBlockAt(x, y, z);
      oldblock = block.getType();
      block.setType(o.markerblock);
    }
    y++;
    block = worldtouse.getBlockAt(x, y, z);
    block.setType(Material.CHEST);
    InventoryHolder h = (InventoryHolder)block.getState();
    contents = h.getInventory();

    Location location = new Location(worldtouse, x, y, z);

    if (stationary) current = populateItems(contents, o, current, true, stationaryitemlist); else
      current = populateItems(contents, o, current, false, null);
    if (current == -1) return null;
    THHunt hunt = new THHunt(System.currentTimeMillis(), current, o.duration, location, contents, oldblock);

    if (!stationary)
    {
      TreasureHunt.huntList.add(hunt);
      Map data = new HashMap();
      data.put("worldname", worldtouse.getName());
      data.put("value", Integer.toString(current));
      data.put("rarity", hunt.getRarityString());
      data.put("timeleft", o.duration + " minutes");
      data.put("numhunts", Integer.toString(TreasureHunt.getHuntsInWorld(worldtouse.getName()).size()));
      data.put("location", hunt.getLocString());
      if (TreasureHunt.spawnedchest.length() > 0) TreasureHunt.broadcast(TreasureHunt.colorize(TreasureHunt.convertTags(TreasureHunt.spawnedchest, data)));
      if (TreasureHunt.detaillogs) System.out.println("[THDetails] Hunt started in world " + worldtouse.getName() + " at " + hunt.getLocString() + " - Value: " + hunt.getValue());

    }
    else if (TreasureHunt.detaillogs) { System.out.println("[THDetails] Stationary chest respawned (in " + worldtouse.getName() + ")!  Loc: " + hunt.getLocString() + " - Val: " + hunt.getValue()); }


    return hunt;
  }
}