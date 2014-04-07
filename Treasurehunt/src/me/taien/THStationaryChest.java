package me.taien;

import java.io.PrintStream;
import java.util.Random;
import org.bukkit.block.Block;

public class THStationaryChest
{
  public THHunt hunt;
  public String itemlist;
  public int respawnmintime;
  public int respawnmaxtime;
  public int value;
  public int currentrespawntime;
  public long lastrespawn;
  public Block chest;

  public THStationaryChest(String itemlist, int respawnmintime, int respawnmaxtime, int value, long lastrespawn, int currentrespawntime, Block chest)
  {
    this.itemlist = itemlist;
    this.respawnmintime = respawnmintime;
    this.respawnmaxtime = respawnmaxtime;
    this.value = value;
    this.lastrespawn = lastrespawn;
    this.currentrespawntime = currentrespawntime;
    this.chest = chest;
    this.hunt = null;
  }

  public THStationaryChest(String itemlist, int respawnmintime, int respawnmaxtime, int value, Block chest)
  {
    this.itemlist = itemlist;
    this.respawnmintime = respawnmintime;
    this.respawnmaxtime = respawnmaxtime;
    this.value = value;
    this.lastrespawn = 0L;
    this.currentrespawntime = 0;
    this.chest = chest;
    this.hunt = null;
  }

  public void tick(long curtime)
  {
    if ((this.hunt == null) && (curtime >= this.lastrespawn + this.currentrespawntime * 60000))
    {
      this.hunt = THChestGenerator.startHunt(this.chest.getWorld(), this.value, this.chest, true, this.itemlist);
      if (this.hunt == null)
      {
        System.out.println("[TreasureHunt] Unable to spawn stationary chest!");
      }
    }
    else if ((this.hunt != null) && (this.hunt.isLocked()) && (this.hunt.isExpired()))
    {
      removeChest();
    }
  }

  public void removeChest()
  {
    if (this.hunt != null)
    {
      this.hunt.removeChest(true);
      this.currentrespawntime = (this.respawnmintime - 1);
      do
      {
        this.currentrespawntime = (TreasureHunt.rndGen.nextInt(this.respawnmaxtime) + 1);
      }
      while (
        this.currentrespawntime <= this.respawnmintime);
      this.hunt = null;
      this.lastrespawn = System.currentTimeMillis();
    }
  }
}