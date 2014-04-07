package me.taien.config;

import java.util.LinkedList;
import java.util.List;
import org.bukkit.Material;

public class THWorldOpts
{
  public String itemlist = "Default";
  public int duration = 60;
  public int interval = 60;
  public int maxdistance = 3000;
  public int mindistance = 0;
  public int maxcompassdistance = 1000;
  public int chance = 100;
  public int maxvalue = 5000;
  public int minlight = 0;
  public int maxlight = 4;
  public int maxelevation = 50;
  public int maxelevationrare = 25;
  public int minelevation = 4;
  public int centerx = 0;
  public int centerz = 0;
  public int drawweight = 2;
  public int gooditemweight = 2;
  public int consumechance = 50;
  public int minmoney = 100;
  public int amountweight = 3;
  public int minchests = 0;
  public int offeramount = 1;
  public int searchingattempts = 0;
  public int searchingvalue = 0;
  public long lastcheck = 0L;
  public double moneymultiplier = 1.0D;
  public boolean usemarker = true;
  public boolean enabled = false;
  public boolean strictitems = false;
  public boolean fadefoundchests = true;
  public boolean overrideminplayers = false;
  public boolean searchingloc = false;
  public Material markerblock = Material.GLOWSTONE;
  public Material hunttool = Material.ROTTEN_FLESH;
  public Material offeringtool = Material.BONE;
  public Material fadeblock = Material.SOUL_SAND;
  public List<Material> spawnableblocks = new LinkedList();

  public THWorldOpts()
  {
    this.spawnableblocks.add(Material.STONE);
    this.spawnableblocks.add(Material.SMOOTH_BRICK);
    this.spawnableblocks.add(Material.MOSSY_COBBLESTONE);
    this.spawnableblocks.add(Material.OBSIDIAN);
  }

  public THWorldOpts(String itemlist, int dur, int interval, int maxdist, int mindist, int maxcompassdistance, int chance, int maxvalue, int minlight, int maxlight, int maxelev, int maxelevrare, int minelev, int centerx, int centerz, int weight, int gooditemweight, int amountweight, int consumechance, int minmoney, int offeramount, int minchests, long lastcheck, double moneymult, boolean usemarker, boolean enabled, boolean strictitems, boolean fadefoundchests, boolean overrideminplayers, Material markerblock, Material hunttool, Material offeringtool, Material fadeblock, List<Material> spawnableblocks)
  {
    this.itemlist = itemlist;
    this.duration = dur;
    this.interval = interval;
    this.maxdistance = maxdist;
    this.mindistance = mindist;
    this.maxcompassdistance = maxcompassdistance;
    this.chance = chance;
    this.maxvalue = maxvalue;
    this.minlight = minlight;
    this.maxlight = maxlight;
    this.maxelevation = maxelev;
    this.maxelevationrare = maxelevrare;
    this.minelevation = minelev;
    this.centerx = centerx;
    this.centerz = centerz;
    this.drawweight = weight;
    this.offeramount = offeramount;
    this.gooditemweight = gooditemweight;
    this.amountweight = amountweight;
    this.strictitems = strictitems;
    this.fadefoundchests = fadefoundchests;
    this.overrideminplayers = overrideminplayers;
    this.consumechance = consumechance;
    this.minmoney = minmoney;
    this.minchests = minchests;
    this.lastcheck = lastcheck;
    this.moneymultiplier = moneymult;
    this.usemarker = usemarker;
    this.enabled = enabled;
    this.markerblock = markerblock;
    this.hunttool = hunttool;
    this.offeringtool = offeringtool;
    this.fadeblock = fadeblock;
    this.spawnableblocks = spawnableblocks;
  }
}