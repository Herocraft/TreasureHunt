package me.taien;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import me.taien.config.THWorldOpts;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class THHunt
{
  private long timestart;
  private int duration;
  private int value;
  private boolean locked;
  private Inventory contents;
  private Location location;
  private Player playerfound;
  private Player closestplayer;
  private Material oldblock;

  public THHunt(long timestart, int value, int duration, Location loc, Inventory contents, Material oldblock)
  {
    this.locked = false;
    this.duration = duration;
    this.timestart = timestart;
    this.value = value;
    this.contents = contents;
    this.location = loc;
    this.playerfound = null;
    this.closestplayer = null;
    this.oldblock = null;
  }

  public int getMinutesLeft()
  {
    return (int)((this.timestart + this.duration * 60000 - System.currentTimeMillis()) / 60000L);
  }

  public Location getLocation()
  {
    return this.location;
  }

  public String getWorld()
  {
    return this.location.getWorld().getName();
  }

  public boolean isChestBlock(Block b)
  {
    Block cb = TreasureHunt.server.getWorld(getWorld()).getBlockAt(this.location.getBlockX(), this.location.getBlockY(), this.location.getBlockZ());
    Block mb = TreasureHunt.server.getWorld(getWorld()).getBlockAt(this.location.getBlockX(), this.location.getBlockY() - 1, this.location.getBlockZ());
    if ((b.equals(cb)) || (b.equals(mb))) return true;
    return false;
  }

  public int getDistanceFrom(Location location)
  {
    int xdiff = Math.abs(this.location.getBlockX() - location.getBlockX());
    int zdiff = Math.abs(this.location.getBlockZ() - location.getBlockZ());
    return (int)Math.sqrt(Math.pow(xdiff, 2.0D) + Math.pow(zdiff, 2.0D));
  }

  public int get3DDistanceFrom(Location location)
  {
    int xdiff = Math.abs(this.location.getBlockX() - location.getBlockX());
    int ydiff = Math.abs(this.location.getBlockY() - location.getBlockY());
    int zdiff = Math.abs(this.location.getBlockZ() - location.getBlockZ());
    return (int)Math.sqrt(Math.pow(xdiff, 2.0D) + Math.pow(ydiff, 2.0D) + Math.pow(zdiff, 2.0D));
  }

  public int getValue()
  {
    return this.value;
  }

  public String getRarityString()
  {
    if (this.value < TreasureHunt.uncommonlevel) return "&fCommon";
    if (this.value < TreasureHunt.rarelevel) return "&eUncommon";
    if (this.value < TreasureHunt.legendarylevel) return "&aRare";
    if (this.value < TreasureHunt.epiclevel) return "&9Legendary";
    return "&5EPIC";
  }

  public Player getPlayerFound()
  {
    return this.playerfound;
  }

  public void showClosestPlayer()
  {
    if (this.locked) return;
    Player current = null;
    int currdist = 200;
    World w = this.location.getWorld();
    for (Player p : Bukkit.getServer().getOnlinePlayers())
    {
      if ((p.getWorld() == w) && (!TreasureHunt.nodetectlist.contains(p))) {
        int i = TreasureHunt.threedimensionaldistance ? get3DDistanceFrom(p.getLocation()) : getDistanceFrom(p.getLocation());
        if (i < currdist)
        {
          currdist = i;
          current = p;
        }
      }
    }
    if ((current == null) && (this.closestplayer != null))
    {
      this.closestplayer = null;
    }
    else if (current != this.closestplayer)
    {
      Map data = new HashMap();
      data.put("rarity", getRarityString());
      data.put("value", Integer.toString(this.value));
      data.put("location", getLocString());
      data.put("pname", current.getName());
      data.put("worldname", this.location.getWorld().getName());
      data.put("distance", Integer.toString(currdist));
      data.put("numhunts", Integer.toString(TreasureHunt.getHuntsInWorld(this.location.getWorld().getName()).size()));
      data.put("timeleft", getMinutesLeft() + " minutes");
      if (this.closestplayer == null)
      {
        TreasureHunt.broadcast(TreasureHunt.colorize(TreasureHunt.convertTags(TreasureHunt.playerclose, data)));
      }
      else
      {
        this.closestplayer.sendMessage(TreasureHunt.colorize(TreasureHunt.convertTags(TreasureHunt.nolongerclosest, data)));
      }
      this.closestplayer = current;
      if (this.closestplayer != null) this.closestplayer.sendMessage(TreasureHunt.colorize(TreasureHunt.convertTags(TreasureHunt.youareclosest, data)));
    }
    if ((current != null) && (getMinutesLeft() < 10)) this.timestart = (System.currentTimeMillis() - (this.duration - 10) * 60000);
  }

  public String getLocString()
  {
    return this.location.getBlockX() + "," + this.location.getBlockY() + "," + this.location.getBlockZ();
  }

  public boolean isLocked()
  {
    return this.locked;
  }

  public void chestFoundBy(Player p, boolean stationary)
  {
    Map data = new HashMap();
    data.put("rarity", getRarityString());
    data.put("value", Integer.toString(this.value));
    data.put("location", getLocString());
    data.put("pname", p.getName());
    data.put("worldname", this.location.getWorld().getName());
    data.put("amount", Integer.toString(getMinutesLeft()));
    data.put("numhunts", Integer.toString(TreasureHunt.getHuntsInWorld(this.location.getWorld().getName()).size()));
    data.put("timeleft", getMinutesLeft() + " minutes");
    this.timestart = (System.currentTimeMillis() - (this.duration - TreasureHunt.foundchestfadetime) * 60000);
    int moneyamount = 0;
    THWorldOpts o = (THWorldOpts)TreasureHunt.worlds.get(this.location.getWorld().getName());
    double mult = o.moneymultiplier;
    if (mult != 0.0D)
    {
      if (TreasureHunt.economy != null)
      {
        int maxmoney = (int)(this.value * mult);
        moneyamount = TreasureHunt.rndGen.nextInt(maxmoney + 1);
        if (moneyamount < o.minmoney) moneyamount = o.minmoney;
        data.put("amount", moneyamount + " " + TreasureHunt.economy.currencyNamePlural());
        TreasureHunt.economy.depositPlayer(p.getName(), moneyamount);
      }
    }
    if (moneyamount > 0)
    {
      p.sendMessage(TreasureHunt.colorize(TreasureHunt.convertTags(TreasureHunt.moneyfound, data)));
    }
    if (!stationary)
    {
      TreasureHunt.broadcast(TreasureHunt.colorize(TreasureHunt.convertTags(TreasureHunt.playerfound, data)));
      TreasureHunt.addFound(p, this.value);
    }
    if (TreasureHunt.detaillogs)
    {
      if (TreasureHunt.economy != null) System.out.println("[THDetails] " + p.getName() + " found chest: Value " + this.value + " at " + getLocString() + ". Gained " + moneyamount + " " + TreasureHunt.economy.currencyNamePlural() + "."); else
        System.out.println("[THDetails] " + p.getName() + " found chest: Value " + this.value + " at " + getLocString() + ".");
    }
    this.locked = true;
    this.playerfound = p;
  }

  public boolean isExpired()
  {
    if (System.currentTimeMillis() >= this.timestart + this.duration * 60000) return true;
    return false;
  }

  public void removeChest(boolean stationary)
  {
    this.contents.clear();
    Map data = new HashMap();
    data.put("rarity", getRarityString());
    data.put("value", Integer.toString(this.value));
    data.put("location", getLocString());
    data.put("worldname", this.location.getWorld().getName());
    data.put("numhunts", Integer.toString(TreasureHunt.getHuntsInWorld(this.location.getWorld().getName()).size() - 1));
    THWorldOpts o = (THWorldOpts)TreasureHunt.worlds.get(this.location.getWorld().getName());
    Block b = this.location.getBlock();
    if (b.getChunk().isLoaded())
    {
      b.setType(Material.AIR);
      if (o.usemarker)
      {
        if (o.fadeblock == null)
        {
          this.location.getWorld().getBlockAt(b.getX(), b.getY() - 1, b.getZ()).setType(this.oldblock);
        }
        else this.location.getWorld().getBlockAt(b.getX(), b.getY() - 1, b.getZ()).setType(o.fadeblock);
      }
    }
    else
    {
      b.getChunk().load();
      b.setType(Material.AIR);
      if (o.usemarker)
      {
        if (o.fadeblock == null)
        {
          this.location.getWorld().getBlockAt(b.getX(), b.getY() - 1, b.getZ()).setType(this.oldblock);
        }
        else this.location.getWorld().getBlockAt(b.getX(), b.getY() - 1, b.getZ()).setType(o.fadeblock);
      }
      b.getChunk().unload();
    }
    this.timestart = 0L;

    if (!stationary)
    {
      if (!this.locked) TreasureHunt.broadcast(TreasureHunt.colorize(TreasureHunt.convertTags(TreasureHunt.unfoundchestfaded, data)));
      else if (TreasureHunt.foundchestfaded.length() > 0) TreasureHunt.broadcast(TreasureHunt.colorize(TreasureHunt.convertTags(TreasureHunt.foundchestfaded, data)));
    }

    if (TreasureHunt.detaillogs) System.out.println("[THDetails] Chest despawned at " + (String)data.get("location") + ". " + (this.locked ? "(Claimed)" : "(Unclaimed)"));
  }
}