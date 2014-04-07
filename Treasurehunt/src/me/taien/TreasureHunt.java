package me.taien;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import me.taien.config.Config;
import me.taien.config.THToolSettings;
import me.taien.config.THWorldList;
import me.taien.config.THWorldOpts;
import me.taien.listeners.THListener;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.palmergames.bukkit.towny.Towny;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class TreasureHunt extends JavaPlugin
{
  static String ptag;
  static String spawnedchest;
  static String playerclose;
  static String youareclosest;
  static String nolongerclosest;
  static String playerfound;
  static String moneyfound;
  static String foundchestfaded;
  static String unfoundchestfaded;
  static String alreadyclaimed;
  static String closestchest;
  static String nochests;
  static String offeritem;
  static String compasschange;
  static String compassnochange;
  static String directional;
  static String forwardtext;
  static String backwardtext;
  static String abovetext;
  static String belowtext;
  static String lefttext;
  static String righttext;
  private static THListener listener = null;
  private final THTimer timer = new THTimer();
  private int timerid = 0;
  public static boolean useperms = false;
  public static WorldGuardPlugin worldguard = null;
  public static Towny towny = null;
  public static Economy economy = null;
  public static Permission permission = null;
  public static String version = "0.10.2";
  public static Server server = Bukkit.getServer();

  public static int checksec = 5;
  public static int maxspawnattempts = 1000;
  public static int minplayers = 4;
  public static int foundchestfadetime = 5;
  public static int maxattemptspertick = 20;
  public static int uncommonlevel = 1500;
  public static int rarelevel = 2500;
  public static int legendarylevel = 3500;
  public static int epiclevel = 4500;
  public static boolean usecompass = false;
  public static boolean threedimensionaldistance = false;
  public static boolean directionaltext = false;
  public static boolean detaillogs = false;
  public static boolean protectbreak = true;
  public static boolean protectburn = true;
  public static boolean protectpiston = true;
  public static boolean protectexplode = true;

  public static Random rndGen = new Random();
  public static Map<String, THWorldList> worldlists = new HashMap();
  public static Map<String, Map<ItemStack, Integer>> customlists = new HashMap();
  public static List<Block> compassblocks = new LinkedList();
  public static Map<String, THPlayer> playerdata = new HashMap();

  public static List<THHunt> huntList = new LinkedList();
  public static List<THStationaryChest> stationaryList = new LinkedList();
  public static List<Player> nodetectlist = new LinkedList();
  public static Map<Player, THToolSettings> selections = new HashMap();
  public static Map<Player, Long> lastcheck = new HashMap();
  public static Map<String, THWorldOpts> worlds = new HashMap();
  public static Map<String, ItemStack> enchanted = new HashMap();

  private DecimalFormat ratiopercent = new DecimalFormat("#.###");

  public void onDisable()
  {
    for (THHunt h : huntList)
    {
      h.removeChest(false);
    }
    for (THStationaryChest c : stationaryList)
    {
      c.removeChest();
    }
    huntList.clear();
    saveProcedure();
    server.getScheduler().cancelTask(this.timerid);

    System.out.println("[TreasureHunt] Deactivated.");
  }

  public void onEnable()
  {
    System.out.println("[TreasureHunt] Activating...");
    try
    {
      RegisteredServiceProvider economyProvider = server.getServicesManager().getRegistration(Economy.class);
      if (economyProvider != null) economy = (Economy)economyProvider.getProvider(); else
        System.out.println("[TreasureHunt] Vault not found.  No money will be found in chests.");
    }
    catch (NoClassDefFoundError e)
    {
      System.out.println("[TreasureHunt] Vault not found.  No money will be found in chests.");
    }
    try
    {
      RegisteredServiceProvider permissionProvider = server.getServicesManager().getRegistration(Permission.class);
      if (permissionProvider != null) permission = (Permission)permissionProvider.getProvider(); else
        System.out.println("[TreasureHunt] Vault not found.  OP-only permissions will be used.");
    }
    catch (NoClassDefFoundError e)
    {
      System.out.println("[TreasureHunt] Vault not found.  OP-only permissions will be used.");
    }
    if (permission != null) useperms = true;

    PluginManager pm = Bukkit.getServer().getPluginManager();

    worldguard = (WorldGuardPlugin)pm.getPlugin("WorldGuard");
    if (worldguard != null) System.out.println("[TreasureHunt] Hooked into WorldGuard.");

    towny = (Towny)pm.getPlugin("Towny");
    if (towny != null)
    {
      System.out.println("[TreasureHunt] Hooked into Towny.");
    }

    loadProcedure();

    listener = new THListener(this);

    this.timerid = server.getScheduler().scheduleSyncRepeatingTask(this, this.timer, 200L, 1L);

    System.out.println("[TreasureHunt] Activated.");
  }

  public static Towny getTowny()
  {
    return towny;
  }

  public void loadProcedure()
  {
    Config messages = new Config(this, "messages.yml");
    Config config = new Config(this, "config.yml");
    Config players = new Config(this, "players.yml");

    System.out.println("[TreasureHunt] Loading messages...");
    ptag = messages.getConfig().getString("Options.PluginTag", "&e[&6TreasureHunt&e]");
    spawnedchest = messages.getConfig().getString("Messages.SpawnedChest", "<tag> &fA treasure chest of <rarity> &frarity appeared in &9<worldname>&f!");
    playerclose = messages.getConfig().getString("Messages.PlayerCloseToChest", "<tag> &fA player is very close to the <rarity> &fchest!");
    youareclosest = messages.getConfig().getString("Messages.YouAreClosest", "<tag> &aYou are now the closest player to the <rarity> &achest!");
    nolongerclosest = messages.getConfig().getString("Messages.NoLongerClosest", "<tag> &cYou are no longer the closest player to the <rarity> &cchest!");
    playerfound = messages.getConfig().getString("Messages.PlayerFoundChest", "<tag> &fThe chest of value &a<value> &fhas been found by &2<pname> &fat &a<location>&f!");
    moneyfound = messages.getConfig().getString("Messages.MoneyFound", "<tag> &aYou found <amount> in the chest!");
    foundchestfaded = messages.getConfig().getString("Messages.FoundChestFaded", "");
    unfoundchestfaded = messages.getConfig().getString("Messages.UnfoundChestFaded", "<tag> &fThe chest of value &a<value> &fhas &cfaded &fwithout being found!");
    alreadyclaimed = messages.getConfig().getString("Messages.AlreadyClaimed", "&7This chest has already been claimed by &a<pname>&7!");
    closestchest = messages.getConfig().getString("Messages.ClosestChest", "&7The closest chest (of <numhunts>) is currently &9<distance> &7blocks away.");
    nochests = messages.getConfig().getString("Messages.NoChests", "&7No hunts are currently active in this world!");
    offeritem = messages.getConfig().getString("Messages.OfferItem", "&7You offer the altar a &9<item>&7...");
    compasschange = messages.getConfig().getString("Messages.CompassChange", "&7...and your compass needle starts pointing madly in a certain direction!");
    compassnochange = messages.getConfig().getString("Messages.CompassNoChange", "&7...but your compass needle doesn't change.");
    directional = messages.getConfig().getString("Messages.Directional", "&7The chest seems to be somewhere <direction>.");
    forwardtext = messages.getConfig().getString("Directions.Forward", "ahead of you");
    backwardtext = messages.getConfig().getString("Directions.Backward", "behind you");
    abovetext = messages.getConfig().getString("Directions.Above", "above you");
    belowtext = messages.getConfig().getString("Directions.Below", "below you");
    lefttext = messages.getConfig().getString("Directions.Left", "to your left");
    righttext = messages.getConfig().getString("Directions.Right", "to your right");

    System.out.println("[TreasureHunt] Loading configuration...");

    customlists.clear();
    worldlists.clear();
    compassblocks.clear();
    nodetectlist.clear();
    enchanted.clear();
    stationaryList.clear();
    playerdata.clear();

    int i = 0;
    List stationarystrings = config.getConfig().getStringList("StationaryChests");

    checksec = config.getConfig().getInt("Options.SecondsBetweenChecks", 5);
    maxspawnattempts = config.getConfig().getInt("Options.MaxSpawnAttempts", 1000);
    minplayers = config.getConfig().getInt("Options.MinPlayersOnline", 4);
    foundchestfadetime = config.getConfig().getInt("Options.FoundChestFadeTime", 5);
    maxattemptspertick = config.getConfig().getInt("Options.MaxAttemptsPerTick", 20);
    usecompass = config.getConfig().getBoolean("Options.UseCompass", false);
    threedimensionaldistance = config.getConfig().getBoolean("Options.3DDistances", false);
    directionaltext = config.getConfig().getBoolean("Options.DirectionalText", false);
    detaillogs = config.getConfig().getBoolean("Options.DetailLogs", false);
    protectbreak = config.getConfig().getBoolean("Options.Protection.Break", true);
    protectburn = config.getConfig().getBoolean("Options.Protection.Burn", true);
    protectexplode = config.getConfig().getBoolean("Options.Protection.Explode", true);
    protectpiston = config.getConfig().getBoolean("Options.Protection.Piston", true);
    uncommonlevel = config.getConfig().getInt("Options.ChestLevels.Uncommon", 1500);
    rarelevel = config.getConfig().getInt("Options.ChestLevels.Rare", 2500);
    legendarylevel = config.getConfig().getInt("Options.ChestLevels.Legendary", 3500);
    epiclevel = config.getConfig().getInt("Options.ChestLevels.Epic", 4500);

    List incdata = new LinkedList();
    incdata = config.getConfig().getStringList("CompassBlocks");
    int cb = 0;
    String[] split;
    for (String s : incdata)
    {
      split = s.split(":");
      if (split.length < 4) { System.out.println("[TreasureHunt] Incorrect data value found in CompassBlocks(" + s + "), ignoring...");
      } else
      {
        String world = split[0];
        int x = Integer.parseInt(split[1]);
        int y = Integer.parseInt(split[2]);
        int z = Integer.parseInt(split[3]);
        World w = server.getWorld(world);
        Block block = null;
        if (w != null) block = w.getBlockAt(x, y, z);
        if (block != null)
        {
          cb++;
          compassblocks.add(block);
        } else {
          System.out.println("[TreasureHunt] Incorrect data value found in CompassBlocks(" + s + "), ignoring...");
        }
      }
    }
    System.out.println("[TreasureHunt] Loaded " + cb + " CompassBlocks.");

    if (!config.getConfig().isConfigurationSection("WorldOptions")) config.getConfig().createSection("WorldOptions");
    Set worldlist = config.getConfig().getConfigurationSection("WorldOptions").getKeys(false);
    int chance;
    int maxcompassdistance;
    int giweight;
    if (worldlist.size() == 0)
    {
      String w = ((World)server.getWorlds().get(0)).getName();
      worlds.put(w, new THWorldOpts());
      System.out.println("[TreasureHunt] No worlds found in WorldOptions.  Added world '" + w + "' by default.");
    }
    else
    {
      for (String w : worldlist)
      {
        String itemlist = config.getConfig().getString("WorldOptions." + w + ".ItemList", "Default");
        chance = config.getConfig().getInt("WorldOptions." + w + ".ChestChance", 100);
        int interval = config.getConfig().getInt("WorldOptions." + w + ".ChestInterval", 60);
        int duration = config.getConfig().getInt("WorldOptions." + w + ".ChestDuration", 60);
        int maxdistance = config.getConfig().getInt("WorldOptions." + w + ".MaxDistance", 3000);
        int mindistance = config.getConfig().getInt("WorldOptions." + w + ".MinDistance", 0);
        maxcompassdistance = config.getConfig().getInt("WorldOptions." + w + ".MaxCompassDistance", 1000);
        int centerx = config.getConfig().getInt("WorldOptions." + w + ".CenterX", 0);
        int centerz = config.getConfig().getInt("WorldOptions." + w + ".CenterZ", 0);
        int drawweight = config.getConfig().getInt("WorldOptions." + w + ".DrawWeight", 2);
        giweight = config.getConfig().getInt("WorldOptions." + w + ".GoodItemWeight", 2);
        int amountweight = config.getConfig().getInt("WorldOptions." + w + ".AmountWeight", 3);
        int maxvalue = config.getConfig().getInt("WorldOptions." + w + ".MaxValue", 5000);
        int minlight = config.getConfig().getInt("WorldOptions." + w + ".MinLightLevel", 0);
        int maxlight = config.getConfig().getInt("WorldOptions." + w + ".MaxLightLevel", 4);
        int maxelevation = config.getConfig().getInt("WorldOptions." + w + ".MaxElevation", 50);
        int maxelevationrare = config.getConfig().getInt("WorldOptions." + w + ".MaxElevationRare", 25);
        int minelevation = config.getConfig().getInt("WorldOptions." + w + ".MinElevation", 4);
        int consumechance = config.getConfig().getInt("WorldOptions." + w + ".ConsumeChance", 50);
        int minmoney = config.getConfig().getInt("WorldOptions." + w + ".MinMoney", 100);
        int offeramount = config.getConfig().getInt("WorldOptions." + w + ".OfferAmount", 1);
        int minchests = config.getConfig().getInt("WorldOptions." + w + ".MinChests", 0);
        long lastcheck = config.getConfig().getLong("WorldOptions." + w + ".LastCheck", 0L);
        double moneymultiplier = config.getConfig().getDouble("WorldOptions." + w + ".MoneyMultiplier", 1.0D);
        boolean usemarker = config.getConfig().getBoolean("WorldOptions." + w + ".UseMarker", true);
        boolean enabled = config.getConfig().getBoolean("WorldOptions." + w + ".Enabled", true);
        boolean strictitems = config.getConfig().getBoolean("WorldOptions." + w + ".StrictItems", false);
        boolean fadefoundchests = config.getConfig().getBoolean("WorldOptions." + w + ".FadeFoundChests", true);
        boolean overrideminplayers = config.getConfig().getBoolean("WorldOptions." + w + ".OverrideMinPlayers", false);
        String s = config.getConfig().getString("WorldOptions." + w + ".HuntTool", "ROTTEN_FLESH");
        Material hunttool = Material.ROTTEN_FLESH;
        if ((Material.getMaterial(s.toUpperCase()) == null) && (Material.getMaterial(Integer.parseInt(s)) == null)) System.out.println("'" + s + "' is not a valid item name or id. (HuntTool) Using default.");
        else if (Material.getMaterial(s.toUpperCase()) != null) hunttool = Material.getMaterial(s.toUpperCase()); else {
          hunttool = Material.getMaterial(Integer.parseInt(s));
        }
        s = config.getConfig().getString("WorldOptions." + w + ".OfferingTool", "BONE");
        Material offeringtool = Material.BONE;
        if ((Material.getMaterial(s.toUpperCase()) == null) && (Material.getMaterial(Integer.parseInt(s)) == null)) System.out.println("'" + s + "' is not a valid item name or id. (HuntTool) Using default.");
        else if (Material.getMaterial(s.toUpperCase()) != null) offeringtool = Material.getMaterial(s.toUpperCase()); else {
          offeringtool = Material.getMaterial(Integer.parseInt(s));
        }
        s = config.getConfig().getString("WorldOptions." + w + ".MarkerBlock", "GLOWSTONE");
        Material markerblock = Material.GLOWSTONE;
        if ((Material.getMaterial(s.toUpperCase()) == null) && (Material.getMaterial(Integer.parseInt(s)) == null)) System.out.println("'" + s + "' is not a valid item name or id. (MarkerBlock) Using default.");
        else if (Material.getMaterial(s.toUpperCase()) != null) markerblock = Material.getMaterial(s.toUpperCase()); else {
          markerblock = Material.getMaterial(Integer.parseInt(s));
        }
        s = config.getConfig().getString("WorldOptions." + w + ".FadeBlock", "SOUL_SAND");
        Material fadeblock = Material.SOUL_SAND;
        if ((Material.getMaterial(s.toUpperCase()) == null) && (!s.equalsIgnoreCase("RETURN")) && (Material.getMaterial(Integer.parseInt(s)) == null)) System.out.println("'" + s + "' is not a valid item name or id. (FadeBlock) Using default.");
        else if (Material.getMaterial(s.toUpperCase()) != null) fadeblock = Material.getMaterial(s.toUpperCase());
        else if (s.equalsIgnoreCase("RETURN")) fadeblock = null; else {
          fadeblock = Material.getMaterial(Integer.parseInt(s));
        }

        List spawnblocks = new LinkedList();
        spawnblocks = config.getConfig().getStringList("WorldOptions." + w + ".CanSpawnOn");
        List spawnableblocks = new LinkedList();
        if (spawnblocks.size() == 0)
        {
          System.out.println("[TreasureHunt] No spawning blocks found for world " + w + ".  Using default.");
          spawnableblocks.add(Material.STONE);
          spawnableblocks.add(Material.SMOOTH_BRICK);
          spawnableblocks.add(Material.MOSSY_COBBLESTONE);
          spawnableblocks.add(Material.OBSIDIAN);
        }
        else
        {
          for (String block : spawnblocks)
          {
            if ((Material.getMaterial(block.toUpperCase()) == null) && (Material.getMaterial(Integer.parseInt(block)) == null)) System.out.println("'" + block + "' is not a valid item name or id. (SpawnableBlocks, World " + w + ")");
            else if (Material.getMaterial(block.toUpperCase()) != null) spawnableblocks.add(Material.getMaterial(block.toUpperCase())); else {
              spawnableblocks.add(Material.getMaterial(Integer.parseInt(block)));
            }
          }
          if (spawnableblocks.size() == 0) {
            System.out.println("[TreasureHunt] No usable spawning blocks found for world " + w + ".  Using default.");
            spawnableblocks.add(Material.STONE);
            spawnableblocks.add(Material.SMOOTH_BRICK);
            spawnableblocks.add(Material.MOSSY_COBBLESTONE);
            spawnableblocks.add(Material.OBSIDIAN);
          }
        }
        World world = server.getWorld(w);
        if (world != null)
        {
          worlds.put(w, new THWorldOpts(itemlist, duration, interval, maxdistance, mindistance, maxcompassdistance, chance, maxvalue, minlight, maxlight, maxelevation, maxelevationrare, minelevation, centerx, centerz, drawweight, giweight, amountweight, consumechance, minmoney, offeramount, minchests, lastcheck, moneymultiplier, usemarker, enabled, strictitems, fadefoundchests, overrideminplayers, markerblock, hunttool, offeringtool, fadeblock, spawnableblocks));
          System.out.println("[TreasureHunt] Loaded world '" + w + "'");
        }
        else
        {
          System.out.println("[TreasureHunt] Failed to load world '" + w + "' - world does not appear to exist");
        }
      }
    }

    System.out.println("[TreasureHunt] Settings for " + worlds.size() + " worlds loaded.");

    int enchs = 0;
    if (!config.getConfig().isConfigurationSection("EnchantedItems")) config.getConfig().createSection("EnchantedItems");
    Set enchantedlist = config.getConfig().getConfigurationSection("EnchantedItems").getKeys(false);
    ItemStack item;
    boolean conflict;
    for (String s : enchantedlist)
    {
      int id = config.getConfig().getInt("EnchantedItems." + s + ".ID", 307);
      short damage = (short)config.getConfig().getInt("EnchantedItems." + s + ".Damage", 0);
      item = new ItemStack(id, 1, damage);
      if (!config.getConfig().isConfigurationSection("EnchantedItems." + s + ".Effects")) config.getConfig().createSection("EnchantedItems." + s + ".Effects");
      for (String effect : config.getConfig().getConfigurationSection("EnchantedItems." + s + ".Effects").getKeys(false))
      {
        Enchantment e = Enchantment.getByName(effect);
        if (e != null)
          if (!e.canEnchantItem(item))
          {
            System.out.println("[TreasureHunt] Cannot enchant " + item.getType().name() + " with " + e.getName() + "! Enchantment dropped.");
          }
          else {
            conflict = false;
            for (Enchantment ench : item.getEnchantments().keySet()) if (e.conflictsWith(ench))
              {
                System.out.println("[TreasureHunt] Enchant " + e.getName() + " conflicts with " + ench.getName() + " on " + item.getType().name() + "! Enchantment dropped.");
                conflict = true;
                break;
              }

            if (!conflict)
            {
              int magnitude = e.getMaxLevel() < config.getConfig().getInt("EnchantedItems." + s + ".Effects." + effect, 1) ? e.getMaxLevel() : config.getConfig().getInt("EnchantedItems." + s + ".Effects." + effect, 1);
              item.addEnchantment(e, magnitude);
            }
          }
      }
      if (item.getEnchantments().size() < 1)
      {
        System.out.println("[TreasureHunt] No valid enchants found for " + item.getType().name() + "! Item dropped from enchanted list.");
      }
      else
      {
        enchanted.put(s, item);
        enchs++;
      }
    }
    System.out.println("[TreasureHunt] Loaded " + enchs + " Enchanted Item Setups.");

    if (!config.getConfig().isConfigurationSection("WorldLists")) config.getConfig().createSection("WorldLists");
    Set worldstrings = config.getConfig().getConfigurationSection("WorldLists").getKeys(false);
    int listnum = 0;
    int totalitems = 0;
    Set commonlist;
    Set rarelist;
    for (String list : worldstrings)
    {
      listnum++;
      if (!config.getConfig().isConfigurationSection("WorldLists." + list + ".Common")) config.getConfig().createSection("WorldLists." + list + ".Common");
      commonlist = config.getConfig().getConfigurationSection("WorldLists." + list + ".Common").getKeys(false);
      Map commonitemlist = new HashMap();
      short itemdat;
      for (String s : commonlist)
      {
        String[] split = s.split(":");
        if (split.length < 2)
        {
          int intval = 0;
          try { intval = Integer.parseInt(s); } catch (NumberFormatException localNumberFormatException) {
          }
          if ((Material.getMaterial(s.toUpperCase()) == null) && (!enchanted.containsKey(s)) && ((intval == 0) || (Material.getMaterial(intval) == null))) { System.out.println("'" + s + "' is not a valid item name or id. (List '" + list + "' Commons)");
          } else
          {
            if (Material.getMaterial(s.toUpperCase()) != null) commonitemlist.put(new ItemStack(Material.getMaterial(s.toUpperCase())), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Common." + s, 1)));
            else if (enchanted.containsKey(s)) commonitemlist.put((ItemStack)enchanted.get(s), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Common." + s, 1))); else
              commonitemlist.put(new ItemStack(Material.getMaterial(intval)), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Common." + s, 1)));
            i++;
          }

        }
        else
        {
          itemdat = Short.parseShort(split[1]);
          int intval = 0;
          try { intval = Integer.parseInt(split[0]); } catch (NumberFormatException localNumberFormatException1) {
          }
          if ((Material.getMaterial(s.toUpperCase()) == null) && ((intval == 0) || (Material.getMaterial(intval) == null))) { System.out.println("'" + split[0] + "' is not a valid item name or id. (List '" + list + "' Commons)");
          } else
          {
            if (Material.getMaterial(split[0].toUpperCase()) != null) commonitemlist.put(new ItemStack(Material.getMaterial(split[0].toUpperCase()), 1, itemdat), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Common." + s, 1))); else
              commonitemlist.put(new ItemStack(Material.getMaterial(intval), 1, itemdat), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Common." + s, 1)));
            i++;
          }
        }
      }

      if (!config.getConfig().isConfigurationSection("WorldLists." + list + ".Uncommon")) config.getConfig().createSection("WorldLists." + list + ".Uncommon");
      Set uncommonlist = config.getConfig().getConfigurationSection("WorldLists." + list + ".Uncommon").getKeys(false);
      Map uncommonitemlist = new HashMap();
      short itemdat;
      for (String s : uncommonlist)
      {
        String[] split = s.split(":");
        if (split.length < 2)
        {
          int intval = 0;
          try { intval = Integer.parseInt(s); } catch (NumberFormatException localNumberFormatException2) {
          }
          if ((Material.getMaterial(s.toUpperCase()) == null) && (!enchanted.containsKey(s)) && ((intval == 0) || (Material.getMaterial(intval) == null))) { System.out.println("'" + s + "' is not a valid item name or id. (List '" + list + "' Uncommons)");
          } else
          {
            if (Material.getMaterial(s.toUpperCase()) != null) uncommonitemlist.put(new ItemStack(Material.getMaterial(s.toUpperCase())), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Uncommon." + s, 1)));
            else if (enchanted.containsKey(s)) uncommonitemlist.put((ItemStack)enchanted.get(s), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Uncommon." + s, 1))); else
              uncommonitemlist.put(new ItemStack(Material.getMaterial(intval)), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Uncommon." + s, 1)));
            i++;
          }

        }
        else
        {
          itemdat = Short.parseShort(split[1]);
          int intval = 0;
          try { intval = Integer.parseInt(split[0]); } catch (NumberFormatException localNumberFormatException3) {
          }
          if ((Material.getMaterial(s.toUpperCase()) == null) && ((intval == 0) || (Material.getMaterial(intval) == null))) { System.out.println("'" + split[0] + "' is not a valid item name or id. (List '" + list + "' Uncommons)");
          } else
          {
            if (Material.getMaterial(split[0].toUpperCase()) != null) uncommonitemlist.put(new ItemStack(Material.getMaterial(split[0].toUpperCase()), 1, itemdat), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Uncommon." + s, 1))); else
              uncommonitemlist.put(new ItemStack(Material.getMaterial(intval), 1, itemdat), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Uncommon." + s, 1)));
            i++;
          }
        }
      }

      if (!config.getConfig().isConfigurationSection("WorldLists." + list + ".Rare")) config.getConfig().createSection("WorldLists." + list + ".Rare");
      rarelist = config.getConfig().getConfigurationSection("WorldLists." + list + ".Rare").getKeys(false);
      Map rareitemlist = new HashMap();
      short itemdat;
      for (String s : rarelist)
      {
        String[] split = s.split(":");
        if (split.length < 2)
        {
          int intval = 0;
          try { intval = Integer.parseInt(s); } catch (NumberFormatException localNumberFormatException4) {
          }
          if ((Material.getMaterial(s.toUpperCase()) == null) && (!enchanted.containsKey(s)) && ((intval == 0) || (Material.getMaterial(intval) == null))) { System.out.println("'" + s + "' is not a valid item name or id. (List '" + list + "' Rares)");
          } else
          {
            if (Material.getMaterial(s.toUpperCase()) != null) rareitemlist.put(new ItemStack(Material.getMaterial(s.toUpperCase())), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Rare." + s, 1)));
            else if (enchanted.containsKey(s)) rareitemlist.put((ItemStack)enchanted.get(s), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Rare." + s, 1))); else
              rareitemlist.put(new ItemStack(Material.getMaterial(intval)), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Rare." + s, 1)));
            i++;
          }

        }
        else
        {
          itemdat = Short.parseShort(split[1]);
          int intval = 0;
          try { intval = Integer.parseInt(split[0]); } catch (NumberFormatException localNumberFormatException5) {
          }
          if ((Material.getMaterial(s.toUpperCase()) == null) && ((intval == 0) || (Material.getMaterial(intval) == null))) { System.out.println("'" + split[0] + "' is not a valid item name or id. (List '" + list + "' Rares)");
          } else
          {
            if (Material.getMaterial(split[0].toUpperCase()) != null) rareitemlist.put(new ItemStack(Material.getMaterial(split[0].toUpperCase()), 1, itemdat), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Rare." + s, 1))); else
              rareitemlist.put(new ItemStack(Material.getMaterial(intval), 1, itemdat), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Rare." + s, 1)));
            i++;
          }
        }
      }

      if (!config.getConfig().isConfigurationSection("WorldLists." + list + ".Legendary")) config.getConfig().createSection("WorldLists." + list + ".Legendary");
      Set legendarylist = config.getConfig().getConfigurationSection("WorldLists." + list + ".Legendary").getKeys(false);
      Map legendaryitemlist = new HashMap();
      short itemdat;
      for (String s : legendarylist)
      {
        String[] split = s.split(":");
        if (split.length < 2)
        {
          int intval = 0;
          try { intval = Integer.parseInt(s); } catch (NumberFormatException localNumberFormatException6) {
          }
          if ((Material.getMaterial(s.toUpperCase()) == null) && (!enchanted.containsKey(s)) && ((intval == 0) || (Material.getMaterial(intval) == null))) { System.out.println("'" + s + "' is not a valid item name or id. (List '" + list + "' Legendaries)");
          } else
          {
            if (Material.getMaterial(s.toUpperCase()) != null) legendaryitemlist.put(new ItemStack(Material.getMaterial(s.toUpperCase())), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Legendary." + s, 1)));
            else if (enchanted.containsKey(s)) legendaryitemlist.put((ItemStack)enchanted.get(s), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Legendary." + s, 1))); else
              legendaryitemlist.put(new ItemStack(Material.getMaterial(intval)), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Legendary." + s, 1)));
            i++;
          }

        }
        else
        {
          itemdat = Short.parseShort(split[1]);
          int intval = 0;
          try { intval = Integer.parseInt(split[0]); } catch (NumberFormatException localNumberFormatException7) {
          }
          if ((Material.getMaterial(s.toUpperCase()) == null) && ((intval == 0) || (Material.getMaterial(intval) == null))) { System.out.println("'" + split[0] + "' is not a valid item name or id. (List '" + list + "' Legendaries)");
          } else
          {
            if (Material.getMaterial(split[0].toUpperCase()) != null) legendaryitemlist.put(new ItemStack(Material.getMaterial(split[0].toUpperCase()), 1, itemdat), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Legendary." + s, 1))); else
              legendaryitemlist.put(new ItemStack(Material.getMaterial(intval), 1, itemdat), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Legendary." + s, 1)));
            i++;
          }
        }
      }

      if (!config.getConfig().isConfigurationSection("WorldLists." + list + ".Epic")) config.getConfig().createSection("WorldLists." + list + ".Epic");
      Set epiclist = config.getConfig().getConfigurationSection("WorldLists." + list + ".Epic").getKeys(false);
      Map epicitemlist = new HashMap();

      for (String s : epiclist)
      {
        String[] split = s.split(":");
        if (split.length < 2)
        {
          int intval = 0;
          try { intval = Integer.parseInt(s); } catch (NumberFormatException localNumberFormatException8) {
          }
          if ((Material.getMaterial(s.toUpperCase()) == null) && (!enchanted.containsKey(s)) && ((intval == 0) || (Material.getMaterial(intval) == null))) { System.out.println("'" + s + "' is not a valid item name or id. (List '" + list + "' Epics)");
          } else
          {
            if (Material.getMaterial(s.toUpperCase()) != null) epicitemlist.put(new ItemStack(Material.getMaterial(s.toUpperCase())), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Epic." + s, 1)));
            else if (enchanted.containsKey(s)) epicitemlist.put((ItemStack)enchanted.get(s), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Epic." + s, 1))); else
              epicitemlist.put(new ItemStack(Material.getMaterial(intval)), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Epic." + s, 1)));
            i++;
          }

        }
        else
        {
          short itemdat = Short.parseShort(split[1]);
          int intval = 0;
          try { intval = Integer.parseInt(split[0]); } catch (NumberFormatException localNumberFormatException9) {
          }
          if ((Material.getMaterial(s.toUpperCase()) == null) && ((intval == 0) || (Material.getMaterial(intval) == null))) { System.out.println("'" + s + "' is not a valid item name or id. (List '" + list + "' Epics)");
          } else
          {
            if (Material.getMaterial(split[0].toUpperCase()) != null) epicitemlist.put(new ItemStack(Material.getMaterial(split[0].toUpperCase()), 1, itemdat), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Epic." + s, 1))); else
              epicitemlist.put(new ItemStack(Material.getMaterial(intval), 1, itemdat), Integer.valueOf(config.getConfig().getInt("WorldLists." + list + ".Epic." + s, 1)));
            i++;
          }
        }
      }
      System.out.println("[TreasureHunt] Loaded " + i + " items successfully from list '" + list + "'.");
      if ((commonitemlist.size() == 0) || (uncommonitemlist.size() == 0) || (rareitemlist.size() == 0) || (legendaryitemlist.size() == 0) || (epicitemlist.size() == 0))
      {
        listnum--;
        System.out.println("[TreasureHunt] A subsection of WorldList '" + list + "' has no items!  As this would BREAK TreasureHunt, this list is being ignored!");
      }
      else
      {
        totalitems += i;
        worldlists.put(list, new THWorldList(commonitemlist, uncommonitemlist, rareitemlist, legendaryitemlist, epicitemlist));
      }
      i = 0;
    }
    System.out.println("[TreasureHunt] Loaded a total of " + totalitems + " items successfully from " + listnum + " WorldLists!");

    if (!config.getConfig().isConfigurationSection("CustomLists")) config.getConfig().createSection("CustomLists");
    Set customstrings = config.getConfig().getConfigurationSection("CustomLists").getKeys(false);
    listnum = 0;
    totalitems = 0;
    Set itemlist;
    for (String list : customstrings)
    {
      listnum++;
      itemlist = config.getConfig().getConfigurationSection("CustomLists." + list).getKeys(false);
      Map customitemlist = new HashMap();

      for (String s : itemlist)
      {
        String[] split = s.split(":");
        if (split.length < 2)
        {
          int intval = 0;
          try { intval = Integer.parseInt(s); } catch (NumberFormatException localNumberFormatException10) {
          }
          if ((Material.getMaterial(s.toUpperCase()) == null) && (!enchanted.containsKey(s)) && ((intval == 0) || (Material.getMaterial(intval) == null))) { System.out.println("'" + s + "' is not a valid item name or id. (CustomList '" + list + "')");
          } else
          {
            if (Material.getMaterial(s.toUpperCase()) != null) customitemlist.put(new ItemStack(Material.getMaterial(s.toUpperCase())), Integer.valueOf(config.getConfig().getInt("CustomLists." + list + "." + s, 1)));
            else if (enchanted.containsKey(s)) customitemlist.put((ItemStack)enchanted.get(s), Integer.valueOf(config.getConfig().getInt("CustomLists." + list + "." + s, 1))); else
              customitemlist.put(new ItemStack(Material.getMaterial(intval)), Integer.valueOf(config.getConfig().getInt("CustomLists." + list + "." + s, 1)));
            i++;
          }

        }
        else
        {
          short itemdat = Short.parseShort(split[1]);
          int intval = 0;
          try { intval = Integer.parseInt(split[0]); } catch (NumberFormatException localNumberFormatException11) {
          }
          if ((Material.getMaterial(s.toUpperCase()) == null) && ((intval == 0) || (Material.getMaterial(intval) == null))) { System.out.println("'" + split[0] + "' is not a valid item name or id. (CustomList '" + list + "')");
          } else
          {
            if (Material.getMaterial(split[0].toUpperCase()) != null) customitemlist.put(new ItemStack(Material.getMaterial(split[0].toUpperCase()), 1, itemdat), Integer.valueOf(config.getConfig().getInt("CustomLists." + list + "." + s, 1))); else
              customitemlist.put(new ItemStack(Material.getMaterial(intval), 1, itemdat), Integer.valueOf(config.getConfig().getInt("CustomLists." + list + "." + s, 1)));
            i++;
          }
        }
      }
      if (customitemlist.size() == 0)
      {
        listnum--;
        System.out.println("[TreasureHunt] CustomList '" + list + "' has no items!  The list is being ignored!");
      }
      else
      {
        totalitems += i;
        customlists.put(list, customitemlist);
      }
      i = 0;
    }
    System.out.println("[TreasureHunt] Loaded a total of " + totalitems + " items successfully from " + listnum + " CustomLists!");

    int stats = 0;
    World w;
    for (String s : stationarystrings)
    {
      String[] split = s.split(":");
      if (split.length >= 9) {
        w = server.getWorld(split[0]);
        if (w != null)
        {
          if (split.length == 9)
          {
            THStationaryChest c = new THStationaryChest("Default", Integer.parseInt(split[5]), Integer.parseInt(split[6]), Integer.parseInt(split[4]), Long.parseLong(split[7]), Integer.parseInt(split[8]), w.getBlockAt(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3])));
            stationaryList.add(c);
            stats++;
          }
          else if (split.length == 10)
          {
            THStationaryChest c = new THStationaryChest(split[9], Integer.parseInt(split[5]), Integer.parseInt(split[6]), Integer.parseInt(split[4]), Long.parseLong(split[7]), Integer.parseInt(split[8]), w.getBlockAt(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3])));
            stationaryList.add(c);
            stats++;
          }
        }
      }
    }
    System.out.println("[TreasureHunt] Loaded " + stats + " stationary chests.");

    for (String s : worlds.keySet())
    {
      THWorldOpts o = (THWorldOpts)worlds.get(s);
      if (!worldlists.containsKey(o.itemlist))
      {
        System.out.println("[TreasureHunt] World '" + s + "' has '" + o.itemlist + "' listed as its WorldList, but this WorldList doesn't seem to exist.");
        System.out.println("[TreasureHunt] Chest generation will not work in this world until this is resolved!");
      }
    }
    for (THStationaryChest c : stationaryList)
    {
      if ((!worldlists.containsKey(c.itemlist)) && (!customlists.containsKey(c.itemlist)))
      {
        System.out.println("[TreasureHunt] Stationary chest at " + c.chest.getX() + "," + c.chest.getY() + "," + c.chest.getZ() + " in world '" + c.chest.getWorld().getName() + "' has '" + c.itemlist + "' listed as its WorldList/CustomList, but this list doesn't seem to exist.");
        System.out.println("[TreasureHunt] This chest will not respawn properly until this is resolved!");
      }
    }

    int pcount = 0;
    if (!players.getConfig().isConfigurationSection("PlayerData")) players.getConfig().createSection("PlayerData");
    Set playerstrings = players.getConfig().getConfigurationSection("PlayerData").getKeys(false);
    for (String s : playerstrings)
    {
      String[] split = players.getConfig().getString("PlayerData." + s, "0:0").split(":");
      if (split.length >= 2)
      {
        pcount++;
        playerdata.put(s, new THPlayer(Integer.parseInt(split[0]), Integer.parseInt(split[1])));
      }
    }
    System.out.println("[TreasureHunt] Loaded data for " + pcount + " players.");

    config.saveDefaultConfig();
    players.saveDefaultConfig();
    messages.saveDefaultConfig();
  }

  public void saveProcedure()
  {
    System.out.println("[TreasureHunt] Saving data...");
    Config config = new Config(this, "config.yml");
    config.loadConfig();
    int i = 0;
    int wc = 0;
    int wl = 0;
    int cl = 0;

    config.getConfig().set("Options.SecondsBetweenChecks", Integer.valueOf(checksec));
    config.getConfig().set("Options.MaxSpawnAttempts", Integer.valueOf(maxspawnattempts));
    config.getConfig().set("Options.MaxAttemptsPerTick", Integer.valueOf(maxattemptspertick));
    config.getConfig().set("Options.MinPlayersOnline", Integer.valueOf(minplayers));
    config.getConfig().set("Options.UseCompass", Boolean.valueOf(usecompass));
    config.getConfig().set("Options.3DDistances", Boolean.valueOf(threedimensionaldistance));
    config.getConfig().set("Options.FoundChestFadeTime", Integer.valueOf(foundchestfadetime));
    config.getConfig().set("Options.DirectionalText", Boolean.valueOf(directionaltext));
    config.getConfig().set("Options.DetailLogs", Boolean.valueOf(detaillogs));
    config.getConfig().set("Options.Protection.Break", Boolean.valueOf(protectbreak));
    config.getConfig().set("Options.Protection.Burn", Boolean.valueOf(protectburn));
    config.getConfig().set("Options.Protection.Explode", Boolean.valueOf(protectexplode));
    config.getConfig().set("Options.Protection.Piston", Boolean.valueOf(protectpiston));
    config.getConfig().set("Options.ChestLevels.Uncommon", Integer.valueOf(uncommonlevel));
    config.getConfig().set("Options.ChestLevels.Rare", Integer.valueOf(rarelevel));
    config.getConfig().set("Options.ChestLevels.Legendary", Integer.valueOf(legendarylevel));
    config.getConfig().set("Options.ChestLevels.Epic", Integer.valueOf(epiclevel));

    List entries = new LinkedList();
    for (Block b : compassblocks)
    {
      String tosave = b.getWorld().getName() + ":" + b.getX() + ":" + b.getY() + ":" + b.getZ();
      entries.add(tosave);
    }
    config.getConfig().set("CompassBlocks", entries);
    THWorldOpts o;
    Iterator localIterator2;
    for (Map.Entry e : worlds.entrySet())
    {
      String w = (String)e.getKey();
      o = (THWorldOpts)e.getValue();
      config.getConfig().set("WorldOptions." + w + ".ItemList", o.itemlist);
      config.getConfig().set("WorldOptions." + w + ".HuntTool", o.hunttool.name());
      config.getConfig().set("WorldOptions." + w + ".OfferingTool", o.offeringtool.name());
      config.getConfig().set("WorldOptions." + w + ".MarkerBlock", o.markerblock.name());
      if (o.fadeblock == null) config.getConfig().set("WorldOptions." + w + ".FadeBlock", "RETURN"); else
        config.getConfig().set("WorldOptions." + w + ".FadeBlock", o.fadeblock.name());
      config.getConfig().set("WorldOptions." + w + ".ChestChance", Integer.valueOf(o.chance));
      config.getConfig().set("WorldOptions." + w + ".ChestInterval", Integer.valueOf(o.interval));
      config.getConfig().set("WorldOptions." + w + ".ChestDuration", Integer.valueOf(o.duration));
      config.getConfig().set("WorldOptions." + w + ".MaxDistance", Integer.valueOf(o.maxdistance));
      config.getConfig().set("WorldOptions." + w + ".MinDistance", Integer.valueOf(o.mindistance));
      config.getConfig().set("WorldOptions." + w + ".MaxCompassDistance", Integer.valueOf(o.maxcompassdistance));
      config.getConfig().set("WorldOptions." + w + ".CenterX", Integer.valueOf(o.centerx));
      config.getConfig().set("WorldOptions." + w + ".CenterZ", Integer.valueOf(o.centerz));
      config.getConfig().set("WorldOptions." + w + ".DrawWeight", Integer.valueOf(o.drawweight));
      config.getConfig().set("WorldOptions." + w + ".AmountWeight", Integer.valueOf(o.amountweight));
      config.getConfig().set("WorldOptions." + w + ".GoodItemWeight", Integer.valueOf(o.gooditemweight));
      config.getConfig().set("WorldOptions." + w + ".MinLightLevel", Integer.valueOf(o.minlight));
      config.getConfig().set("WorldOptions." + w + ".MaxLightLevel", Integer.valueOf(o.maxlight));
      config.getConfig().set("WorldOptions." + w + ".UseMarker", Boolean.valueOf(o.usemarker));
      config.getConfig().set("WorldOptions." + w + ".MinElevation", Integer.valueOf(o.minelevation));
      config.getConfig().set("WorldOptions." + w + ".MaxElevation", Integer.valueOf(o.maxelevation));
      config.getConfig().set("WorldOptions." + w + ".MaxElevationRare", Integer.valueOf(o.maxelevationrare));
      config.getConfig().set("WorldOptions." + w + ".ConsumeChance", Integer.valueOf(o.consumechance));
      config.getConfig().set("WorldOptions." + w + ".MinMoney", Integer.valueOf(o.minmoney));
      config.getConfig().set("WorldOptions." + w + ".MoneyMultiplier", Double.valueOf(o.moneymultiplier));
      config.getConfig().set("WorldOptions." + w + ".Enabled", Boolean.valueOf(o.enabled));
      config.getConfig().set("WorldOptions." + w + ".MaxValue", Integer.valueOf(o.maxvalue));
      config.getConfig().set("WorldOptions." + w + ".LastCheck", Long.valueOf(o.lastcheck));
      config.getConfig().set("WorldOptions." + w + ".OverrideMinPlayers", Boolean.valueOf(o.overrideminplayers));
      config.getConfig().set("WorldOptions." + w + ".FadeFoundChests", Boolean.valueOf(o.fadefoundchests));
      config.getConfig().set("WorldOptions." + w + ".MinChests", Integer.valueOf(o.minchests));
      config.getConfig().set("WorldOptions." + w + ".OfferAmount", Integer.valueOf(o.offeramount));
      config.getConfig().set("WorldOptions." + w + ".StrictItems", Boolean.valueOf(o.strictitems));

      List spawnmats = new LinkedList();
      Material m;
      for (localIterator2 = o.spawnableblocks.iterator(); localIterator2.hasNext(); spawnmats.add(m.name())) m = (Material)localIterator2.next();
      config.getConfig().set("WorldOptions." + w + ".CanSpawnOn", spawnmats);
      wc++;
    }

    i = 0;
    for (??? = worldlists.keySet().iterator(); ???.hasNext(); 
      o.hasNext())
    {
      String s = (String)???.next();

      wl++;
      for (Map.Entry e : ((THWorldList)worldlists.get(s)).common.entrySet())
      {
        ItemStack item = (ItemStack)e.getKey();
        if (enchanted.containsValue(item)) { for (Map.Entry en : enchanted.entrySet())
          {
            if (en.getValue() == item) config.getConfig().set("WorldLists." + s + ".Common." + (String)en.getKey(), e.getValue());
          }

        }
        else if (item.getDurability() != 0)
        {
          String itemdat = item.getType().name() + ":" + item.getDurability();
          config.getConfig().set("WorldLists." + s + ".Common." + itemdat, e.getValue());
        } else {
          config.getConfig().set("WorldLists." + s + ".Common." + item.getType().name(), e.getValue());
        }
        i++;
      }
      for (Map.Entry e : ((THWorldList)worldlists.get(s)).uncommon.entrySet())
      {
        ItemStack item = (ItemStack)e.getKey();
        if (enchanted.containsValue(item)) { for (Map.Entry en : enchanted.entrySet())
          {
            if (en.getValue() == item) config.getConfig().set("WorldLists." + s + ".Uncommon." + (String)en.getKey(), e.getValue());
          }

        }
        else if (item.getDurability() != 0)
        {
          String itemdat = item.getType().name() + ":" + item.getDurability();
          config.getConfig().set("WorldLists." + s + ".Uncommon." + itemdat, e.getValue());
        } else {
          config.getConfig().set("WorldLists." + s + ".Uncommon." + item.getType().name(), e.getValue());
        }
        i++;
      }
      for (Map.Entry e : ((THWorldList)worldlists.get(s)).rare.entrySet())
      {
        ItemStack item = (ItemStack)e.getKey();
        if (enchanted.containsValue(item)) { for (Map.Entry en : enchanted.entrySet())
          {
            if (en.getValue() == item) config.getConfig().set("WorldLists." + s + ".Rare." + (String)en.getKey(), e.getValue());
          }

        }
        else if (item.getDurability() != 0)
        {
          String itemdat = item.getType().name() + ":" + item.getDurability();
          config.getConfig().set("WorldLists." + s + ".Rare." + itemdat, e.getValue());
        } else {
          config.getConfig().set("WorldLists." + s + ".Rare." + item.getType().name(), e.getValue());
        }
        i++;
      }
      for (Map.Entry e : ((THWorldList)worldlists.get(s)).legendary.entrySet())
      {
        ItemStack item = (ItemStack)e.getKey();
        if (enchanted.containsValue(item)) { for (Map.Entry en : enchanted.entrySet())
          {
            if (en.getValue() == item) config.getConfig().set("WorldLists." + s + ".Legendary." + (String)en.getKey(), e.getValue());
          }

        }
        else if (item.getDurability() != 0)
        {
          String itemdat = item.getType().name() + ":" + item.getDurability();
          config.getConfig().set("WorldLists." + s + ".Legendary." + itemdat, e.getValue());
        } else {
          config.getConfig().set("WorldLists." + s + ".Legendary." + item.getType().name(), e.getValue());
        }
        i++;
      }
      o = ((THWorldList)worldlists.get(s)).epic.entrySet().iterator(); continue; Map.Entry e = (Map.Entry)o.next();

      ItemStack item = (ItemStack)e.getKey();
      if (enchanted.containsValue(item)) { for (Map.Entry en : enchanted.entrySet())
        {
          if (en.getValue() == item) config.getConfig().set("WorldLists." + s + ".Epic." + (String)en.getKey(), e.getValue());
        }

      }
      else if (item.getDurability() != 0)
      {
        String itemdat = item.getType().name() + ":" + item.getDurability();
        config.getConfig().set("WorldLists." + s + ".Epic." + itemdat, e.getValue());
      } else {
        config.getConfig().set("WorldLists." + s + ".Epic." + item.getType().name(), e.getValue());
      }
      i++;
    }
    ItemStack item;
    for (??? = customlists.keySet().iterator(); ???.hasNext(); 
      o.hasNext())
    {
      String s = (String)???.next();

      cl++;
      o = ((Map)customlists.get(s)).entrySet().iterator(); continue; e = (Map.Entry)o.next();

      item = (ItemStack)e.getKey();
      if (enchanted.containsValue(item)) { for (Map.Entry en : enchanted.entrySet())
        {
          if (en.getValue() == item) config.getConfig().set("CustomLists." + s + "." + (String)en.getKey(), e.getValue());
        }

      }
      else if (item.getDurability() != 0)
      {
        itemdat = item.getType().name() + ":" + item.getDurability();
        config.getConfig().set("CustomLists." + s + "." + itemdat, e.getValue());
      } else {
        config.getConfig().set("CustomLists." + s + "." + item.getType().name(), e.getValue());
      }
      i++;
    }

    int enchs = 0;
    for (Map.Entry e = enchanted.entrySet().iterator(); e.hasNext(); 
      item.hasNext())
    {
      Map.Entry e = (Map.Entry)e.next();

      enchs++;
      config.getConfig().set("EnchantedItems." + (String)e.getKey() + ".ID", Integer.valueOf(((ItemStack)e.getValue()).getTypeId()));
      config.getConfig().set("EnchantedItems." + (String)e.getKey() + ".Damage", Short.valueOf(((ItemStack)e.getValue()).getDurability()));
      item = ((ItemStack)e.getValue()).getEnchantments().entrySet().iterator(); continue; en = (Map.Entry)item.next(); config.getConfig().set("EnchantedItems." + (String)e.getKey() + ".Effects." + ((Enchantment)en.getKey()).getName(), en.getValue());
    }
    Object chestlist = new LinkedList();
    THStationaryChest c;
    for (Map.Entry en = stationaryList.iterator(); en.hasNext(); ((List)chestlist).add(c.chest.getWorld().getName() + ":" + c.chest.getX() + ":" + c.chest.getY() + ":" + c.chest.getZ() + ":" + c.value + ":" + c.respawnmintime + ":" + c.respawnmaxtime + ":" + c.lastrespawn + ":" + c.currentrespawntime + ":" + c.itemlist)) c = (THStationaryChest)en.next();
    config.getConfig().set("StationaryChests", chestlist);
    config.saveConfig();

    System.out.println("[TreasureHunt] Saved " + wc + " worlds.");
    System.out.println("[TreasureHunt] Saved " + i + " items in " + wl + " WorldLists and " + cl + "CustomLists.");
    System.out.println("[TreasureHunt] Saved " + enchs + " enchanted item setups.");

    Config messages = new Config(this, "messages.yml");
    messages.loadConfig();
    messages.getConfig().set("Options.PluginTag", ptag);
    messages.getConfig().set("Messages.SpawnedChest", spawnedchest);
    messages.getConfig().set("Messages.PlayerCloseToChest", playerclose);
    messages.getConfig().set("Messages.YouAreClosest", youareclosest);
    messages.getConfig().set("Messages.NoLongerClosest", nolongerclosest);
    messages.getConfig().set("Messages.PlayerFoundChest", playerfound);
    messages.getConfig().set("Messages.MoneyFound", moneyfound);
    messages.getConfig().set("Messages.FoundChestFaded", foundchestfaded);
    messages.getConfig().set("Messages.UnoundChestFaded", unfoundchestfaded);
    messages.getConfig().set("Messages.AlreadyClaimed", alreadyclaimed);
    messages.getConfig().set("Messages.ClosestChest", closestchest);
    messages.getConfig().set("Messages.NoChests", nochests);
    messages.getConfig().set("Messages.OfferItem", offeritem);
    messages.getConfig().set("Messages.CompassChange", compasschange);
    messages.getConfig().set("Messages.CompassNoChange", compassnochange);
    messages.getConfig().set("Messages.Directional", directional);
    messages.getConfig().set("Directions.Forward", forwardtext);
    messages.getConfig().set("Directions.Backward", backwardtext);
    messages.getConfig().set("Directions.Above", abovetext);
    messages.getConfig().set("Directions.Below", belowtext);
    messages.getConfig().set("Directions.Left", lefttext);
    messages.getConfig().set("Directions.Right", righttext);
    messages.saveConfig();

    System.out.println("[TreasureHunt] Saved messages.");

    Config players = new Config(this, "players.yml");
    players.loadConfig();
    String s;
    for (String itemdat = playerdata.keySet().iterator(); itemdat.hasNext(); players.getConfig().set("PlayerData." + s, ((THPlayer)playerdata.get(s)).getChestsFound() + ":" + ((THPlayer)playerdata.get(s)).getValueFound())) s = (String)itemdat.next();
    players.saveConfig();

    System.out.println("[TreasureHunt] Saved player data.");
  }

  public static void addFound(Player p, int value)
  {
    if (playerdata.containsKey(p.getName())) ((THPlayer)playerdata.get(p.getName())).foundChest(value); else
      playerdata.put(p.getName(), new THPlayer(1, value));
  }

  public static THHunt getCurrentHunt(Location location)
  {
    int x = location.getBlockX();
    int y = location.getBlockY();
    int z = location.getBlockZ();
    for (THHunt h : huntList)
    {
      Location hl = h.getLocation();
      if ((hl.getWorld().getName().equalsIgnoreCase(location.getWorld().getName())) && 
        (hl.getBlockX() == x) && 
        ((hl.getBlockY() == y) || (hl.getBlockY() == y + 1)) && 
        (hl.getBlockZ() == z))
        return h;
    }
    return null;
  }

  public static int getAmountInInventory(PlayerInventory p, Material m, short d)
  {
    int total = 0;
    for (int i = 0; i < p.getSize(); i++)
    {
      ItemStack pm = p.getItem(i);
      if ((pm != null) && (pm.getType() == m) && (pm.getDurability() == d)) total += pm.getAmount();
    }
    return total;
  }

  public static void takeItemFromPlayer(PlayerInventory p, Material m, short data, int amt)
  {
    int amounttotake = amt;
    do
    {
      ItemStack tstack = null;
      for (ItemStack i : p.getContents()) if ((i != null) && (i.getType() == m) && (i.getDurability() == data)) tstack = i;
      if (tstack != null)
      {
        int samt = tstack.getAmount();
        int pos = p.first(tstack);
        if (samt <= amounttotake)
        {
          p.clear(pos);
          amounttotake -= samt;
        }
        else if (samt > amounttotake)
        {
          p.setItem(pos, new ItemStack(m, samt - amounttotake, data));
          amounttotake = 0;
        }
      }
    }
    while (
      amounttotake > 0);
  }

  public static Set<THHunt> getHuntsInWorld(String world)
  {
    Set returnlist = new HashSet();
    for (THHunt h : huntList)
    {
      if (h.getWorld().equalsIgnoreCase(world)) returnlist.add(h);
    }
    return returnlist;
  }

  public static THStationaryChest getStationaryChest(Block block)
  {
    if ((block.getType() != Material.CHEST) && (block.getType() != Material.LOCKED_CHEST)) return null;

    for (THStationaryChest c : stationaryList)
    {
      if (c.chest.equals(block)) return c;
    }
    return null;
  }

  public static String colorize(String s)
  {
    if (s == null) return null;
    return s.replaceAll("&([0-9a-f])", "§$1");
  }

  public static String convertTags(String inc, Map<String, String> values)
  {
    if (inc == null) return null;
    String message = new String(inc);
    if (values.containsKey("pname")) message = message.replaceAll("<pname>", (String)values.get("pname"));
    if (values.containsKey("worldname")) message = message.replaceAll("<worldname>", (String)values.get("worldname"));
    if (values.containsKey("value")) message = message.replaceAll("<value>", (String)values.get("value"));
    if (values.containsKey("rarity")) message = message.replaceAll("<rarity>", (String)values.get("rarity"));
    if (values.containsKey("item")) message = message.replaceAll("<item>", (String)values.get("item"));
    if (values.containsKey("distance")) message = message.replaceAll("<distance>", (String)values.get("distance"));
    if (values.containsKey("numhunts")) message = message.replaceAll("<numhunts>", (String)values.get("numhunts"));
    if (values.containsKey("location")) message = message.replaceAll("<location>", (String)values.get("location"));
    if (values.containsKey("amount")) message = message.replaceAll("<amount>", (String)values.get("amount"));
    if (values.containsKey("timeleft")) message = message.replaceAll("<timeleft>", (String)values.get("timeleft"));
    if (values.containsKey("direction")) message = message.replaceAll("<direction>", (String)values.get("direction"));
    message = message.replaceAll("<tag>", ptag);
    return message;
  }

  public static THHunt getClosestHunt(Player p, boolean display)
  {
    int currdist = 100000;
    THHunt currhunt = null;
    Set hunts = getHuntsInWorld(p.getWorld().getName());
    for (THHunt h : hunts)
    {
      int distance = threedimensionaldistance ? h.get3DDistanceFrom(p.getLocation()) : h.getDistanceFrom(p.getLocation());
      if ((!h.isLocked()) && (distance < currdist))
      {
        currdist = distance;
        currhunt = h;
      }
    }
    if (display)
    {
      if (currhunt == null)
      {
        Map data = new HashMap();
        data.put("pname", p.getName());
        data.put("worldname", p.getWorld().getName());
        p.sendMessage(colorize(convertTags(nochests, data)));
      }
      else
      {
        String wname = p.getWorld().getName();
        int distance = currhunt.getDistanceFrom(p.getLocation());
        int numhunts = getHuntsInWorld(wname).size();
        Map data = new HashMap();
        data.put("pname", p.getName());
        data.put("worldname", wname);
        data.put("distance", Integer.toString(distance));
        data.put("numhunts", Integer.toString(numhunts));
        data.put("value", Integer.toString(currhunt.getValue()));
        data.put("rarity", currhunt.getRarityString());
        data.put("location", currhunt.getLocString());
        data.put("timeleft", currhunt.getMinutesLeft() + " minutes");
        p.sendMessage(colorize(convertTags(closestchest, data)));
        if (directionaltext)
        {
          Vector dir = new Vector(currhunt.getLocation().getBlockX() - p.getLocation().getBlockX(), currhunt.getLocation().getBlockY() - p.getLocation().getBlockY(), currhunt.getLocation().getBlockZ() - p.getLocation().getBlockZ());
          LookDirection direction = null;
          if ((Math.abs(dir.getX()) >= Math.abs(dir.getZ())) && (Math.abs(dir.getX()) >= Math.abs(dir.getY())))
          {
            if (dir.getX() >= 0.0D) direction = LookDirection.POSX; else
              direction = LookDirection.NEGX;
          }
          else if ((Math.abs(dir.getZ()) >= Math.abs(dir.getX())) && (Math.abs(dir.getZ()) >= Math.abs(dir.getY())))
          {
            if (dir.getZ() >= 0.0D) direction = LookDirection.POSZ; else {
              direction = LookDirection.NEGZ;
            }

          }
          else if (dir.getY() >= 0.0D) direction = LookDirection.UP; else {
            direction = LookDirection.DOWN;
          }
          if ((direction == LookDirection.UP) || (direction == LookDirection.DOWN))
          {
            if (direction == LookDirection.UP) data.put("direction", abovetext); else
              data.put("direction", belowtext);
            p.sendMessage(colorize(convertTags(directional, data)));
          }
          else
          {
            Vector pdir = p.getLocation().getDirection();
            LookDirection pdirection = null;
            if (Math.abs(pdir.getX()) >= Math.abs(pdir.getZ()))
            {
              if (pdir.getX() >= 0.0D) pdirection = LookDirection.POSX; else {
                pdirection = LookDirection.NEGX;
              }

            }
            else if (pdir.getZ() >= 0.0D) pdirection = LookDirection.POSZ; else {
              pdirection = LookDirection.NEGZ;
            }

            if (direction == pdirection)
            {
              data.put("direction", forwardtext);
              p.sendMessage(colorize(convertTags(directional, data)));
            }
            else if (((direction == LookDirection.POSX) && (pdirection == LookDirection.NEGX)) || 
              ((direction == LookDirection.NEGX) && (pdirection == LookDirection.POSX)) || 
              ((direction == LookDirection.POSZ) && (pdirection == LookDirection.NEGZ)) || (
              (direction == LookDirection.NEGZ) && (pdirection == LookDirection.POSZ)))
            {
              data.put("direction", backwardtext);
              p.sendMessage(colorize(convertTags(directional, data)));
            }
            else if (((direction == LookDirection.POSX) && (pdirection == LookDirection.POSZ)) || 
              ((direction == LookDirection.NEGZ) && (pdirection == LookDirection.POSX)) || 
              ((direction == LookDirection.NEGX) && (pdirection == LookDirection.NEGZ)) || (
              (direction == LookDirection.POSZ) && (pdirection == LookDirection.NEGX)))
            {
              data.put("direction", lefttext);
              p.sendMessage(colorize(convertTags(directional, data)));
            }
            else
            {
              data.put("direction", righttext);
              p.sendMessage(colorize(convertTags(directional, data)));
            }
          }
        }
        currhunt.showClosestPlayer();
      }
      return currhunt;
    }
    return currhunt;
  }

  public static void broadcast(String s)
  {
    for (Player p : server.getOnlinePlayers()) if ((!useperms) || ((useperms) && ((permission.has(p, "taien.th.notify.*")) || (permission.has(p, "taien.th.notify." + p.getWorld().getName()))))) p.sendMessage(s);
  }

  public static String getFirstWorldListName()
  {
    Iterator localIterator = worldlists.keySet().iterator(); if (localIterator.hasNext()) { String s = (String)localIterator.next(); return s; }
    return null;
  }

  public static String getFirstCustomListName()
  {
    Iterator localIterator = customlists.keySet().iterator(); if (localIterator.hasNext()) { String s = (String)localIterator.next(); return s; }
    return null;
  }

  public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
  {
    boolean isPlayer = sender instanceof Player;
    Player p = isPlayer ? (Player)sender : null;
    String currentp;
    String s;
    if (command.getName().equalsIgnoreCase("top"))
    {
      if (!isPlayer) return false;
      if ((args.length == 0) || ((args.length == 1) && (args[0].equalsIgnoreCase("chests"))))
      {
        p.sendMessage(ChatColor.DARK_PURPLE + "TOP HUNTERS" + ChatColor.GRAY + "-------------------------");
        List exclude = new LinkedList();
        for (Integer i = Integer.valueOf(1); i.intValue() < 11; i = Integer.valueOf(i.intValue() + 1))
        {
          int current = 0;
          int currentval = 0;
          String currentp = "";
          for (String s : playerdata.keySet())
          {
            THPlayer tp = (THPlayer)playerdata.get(s);
            if ((tp.getChestsFound() > current) && (!exclude.contains(s)))
            {
              current = tp.getChestsFound();
              currentp = s;
              currentval = tp.getValueFound();
            }
          }
          if (!currentp.equals(""))
          {
            exclude.add(currentp);
            p.sendMessage(ChatColor.GRAY + i.toString() + ". " + ChatColor.WHITE + currentp + ChatColor.DARK_GRAY + "......" + ChatColor.LIGHT_PURPLE + current + " chests of " + currentval + " TV");
          }
        }
        p.sendMessage(ChatColor.LIGHT_PURPLE + "/top values " + ChatColor.WHITE + "to order by value");
        return true;
      }
      if ((args.length == 1) && (args[0].equalsIgnoreCase("values")))
      {
        p.sendMessage(ChatColor.DARK_PURPLE + "TOP HUNTERS" + ChatColor.GRAY + "-------------------------");
        List exclude = new LinkedList();
        for (Integer i = Integer.valueOf(1); i.intValue() < 11; i = Integer.valueOf(i.intValue() + 1))
        {
          int current = 0;
          int currentchests = 0;
          currentp = "";
          for (??? = playerdata.keySet().iterator(); ???.hasNext(); ) { s = (String)???.next();

            THPlayer tp = (THPlayer)playerdata.get(s);
            if ((tp.getValueFound() > current) && (!exclude.contains(s)))
            {
              current = tp.getValueFound();
              currentp = s;
              currentchests = tp.getChestsFound();
            }
          }
          if (!currentp.equals(""))
          {
            exclude.add(currentp);
            p.sendMessage(ChatColor.GRAY + i.toString() + ". " + ChatColor.WHITE + currentp + ChatColor.DARK_GRAY + "......" + ChatColor.LIGHT_PURPLE + current + " TV in " + currentchests + " chests");
          }
        }
        p.sendMessage(ChatColor.LIGHT_PURPLE + "/top " + ChatColor.WHITE + "to order by chests");
        return true;
      }
      if ((args.length == 1) && (args[0].equalsIgnoreCase("reset")))
      {
        if (((!useperms) && (p.isOp())) || ((useperms) && (permission.has(p, "taien.th.admin"))))
        {
          playerdata.clear();
          p.sendMessage(ChatColor.DARK_PURPLE + "All top hunter rankings have been reset.");
          return true;
        }
        return false;
      }
      return false;
    }
    if (command.getName().equalsIgnoreCase("stattool"))
    {
      if (isPlayer)
      {
        if (((!useperms) && (p.isOp())) || ((useperms) && ((permission.has(p, "taien.th.admin")) || (permission.has(p, "taien.th.stattool.*")) || (permission.has(p, "taien.th.stattool." + p.getWorld().getName())))))
        {
          if ((args.length == 1) && (args[0].equalsIgnoreCase("off")))
          {
            if (selections.containsKey(p))
            {
              selections.remove(p);
              p.sendMessage(ChatColor.DARK_PURPLE + "TreasureHunt Stationary Chest tool turned off.");
              return true;
            }
            return false;
          }
          if (args.length < 4)
          {
            p.sendMessage(ChatColor.DARK_RED + "Incorrect entry.  Correct format(s) for this command:");
            p.sendMessage(ChatColor.RED + "/stattool <value> <minrespawnminutes> <maxrespawnminutes> <itemlist>");
            p.sendMessage(ChatColor.RED + "/stattool off");
            return true;
          }
          if (args.length == 4)
          {
            try
            {
              value = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException e)
            {
              int value;
              p.sendMessage(ChatColor.DARK_RED + "Argument (" + args[0] + ") is not an integer valid for value.");
              return true;
            }int value;
            try { minrespawntime = Integer.parseInt(args[1]); }
            catch (NumberFormatException e)
            {
              int minrespawntime;
              p.sendMessage(ChatColor.DARK_RED + "Argument (" + args[1] + ") is not an integer valid for minrespawnminutes.");
              return true;
            }int minrespawntime;
            try { maxrespawntime = Integer.parseInt(args[2]); }
            catch (NumberFormatException e)
            {
              int maxrespawntime;
              p.sendMessage(ChatColor.DARK_RED + "Argument (" + args[2] + ") is not an integer valid for maxrespawnminutes.");
              return true;
            }
            int maxrespawntime;
            if (value < 1) value = 1;
            if (minrespawntime > maxrespawntime) minrespawntime = maxrespawntime - 1;
            if (minrespawntime < 0) minrespawntime = 0;
            if (maxrespawntime < minrespawntime) maxrespawntime = minrespawntime;
            if (maxrespawntime < 0) maxrespawntime = 0;
            if ((!worldlists.containsKey(args[3])) || (!customlists.containsKey(args[3])))
            {
              p.sendMessage(ChatColor.DARK_RED + "Argument (" + args[3] + ") is not a valid WorldList/CustomList.");
              return true;
            }
            p.sendMessage(ChatColor.DARK_PURPLE + "TreasureHunt Stationary Chest tool set to Value: " + value + " MinMinutes: " + minrespawntime + " MaxMinutes: " + maxrespawntime + " ItemList: " + args[3]);
            selections.put(p, new THToolSettings(minrespawntime, maxrespawntime, value, args[3]));
            return true;
          }
        }
        return true;
      }
      return false;
    }
    World worldtouse;
    if (command.getName().equalsIgnoreCase("starthunt"))
    {
      if (args.length == 0)
      {
        if (isPlayer)
        {
          if (((!useperms) && (p.isOp())) || ((useperms) && ((permission.has(p, "taien.th.admin")) || (permission.has(p, "taien.th.starthunt.*")) || (permission.has(p, "taien.th.starthunt." + p.getWorld().getName())))))
          {
            String[] worldarray = new String[worlds.size()];
            worldarray = (String[])worlds.keySet().toArray(worldarray);
            World worldtouse = null;

            if (worlds.size() == 1) worldtouse = server.getWorld(worldarray[0]); else
              worldtouse = server.getWorld(worldarray[rndGen.nextInt(worlds.size())]);
            THChestGenerator.startHunt(worldtouse, -1, null, false, null);
            return true;
          }

          p.sendMessage(ChatColor.DARK_RED + "You are not allowed to do that.");
          return true;
        }

        String[] worldarray = new String[worlds.size()];
        worldarray = (String[])worlds.keySet().toArray(worldarray);
        World worldtouse = null;

        if (worlds.size() == 1) worldtouse = server.getWorld(worldarray[0]); else
          worldtouse = server.getWorld(worldarray[rndGen.nextInt(worlds.size())]);
        THChestGenerator.startHunt(worldtouse, -1, null, false, null);
        return true;
      }

      if (args.length == 1)
      {
        int value = 0;
        try { value = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
          sender.sendMessage(ChatColor.DARK_RED + "You must enter an integer (for value).");
          return true;
        }
        if (isPlayer)
        {
          if (((!useperms) && (p.isOp())) || ((useperms) && ((permission.has(p, "taien.th.admin")) || (permission.has(p, "taien.th.starthunt.*")) || (permission.has(p, "taien.th.starthunt." + p.getWorld().getName())))))
          {
            World world = p.getWorld();
            THWorldOpts o = (THWorldOpts)worlds.get(world.getName());
            if (o == null)
            {
              o = new THWorldOpts();
              worlds.put(world.getName(), o);
            }

            THChestGenerator.startHunt(world, value, null, false, null);
            return true;
          }

          p.sendMessage(ChatColor.DARK_RED + "You are not allowed to do that.");
          return true;
        }

        if (worlds.size() == 0)
        {
          System.out.println("[TreasureHunt] Unable to start hunt!  No worlds set!");
          return true;
        }

        World[] worldarray = new World[worlds.size()];
        worldarray = (World[])worlds.keySet().toArray(worldarray);
        worldtouse = null;

        if (worlds.size() == 1) worldtouse = worldarray[0]; else {
          worldtouse = worldarray[rndGen.nextInt(worlds.size())];
        }
        THChestGenerator.startHunt(worldtouse, value, null, false, null);
        return true;
      }

      if ((args.length == 2) && (args[0].equalsIgnoreCase("here")))
      {
        int value = 0;
        try { value = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
          sender.sendMessage(ChatColor.DARK_RED + "You must enter an integer (for value).");
          return true;
        }
        if (isPlayer)
        {
          if (((!useperms) && (p.isOp())) || ((useperms) && ((permission.has(p, "taien.th.admin")) || (permission.has(p, "taien.th.starthunt.*")) || (permission.has(p, "taien.th.starthunt." + p.getWorld().getName())))))
          {
            Block b = p.getTargetBlock(null, 20);
            THChestGenerator.startHunt(b.getWorld(), value, b, false, null);
            return true;
          }

          p.sendMessage(ChatColor.DARK_RED + "You are not allowed to do that.");
          return true;
        }

        sender.sendMessage(ChatColor.DARK_RED + "You cannot /starthunt here when you are not ingame.");
        return true;
      }

      return false;
    }
    if ((command.getName().equalsIgnoreCase("hunt")) || ((command.getName().equalsIgnoreCase("th")) && ((sender instanceof Player))))
    {
      if (!isPlayer)
      {
        sender.sendMessage("Sorry, that command is not yet available from console.");
        return true;
      }
      String wn = p.getWorld().getName();

      if ((isPlayer) && (args.length == 0) && ((!useperms) || ((useperms) && ((permission.has(p, "taien.th.hunt." + p.getWorld().getName())) || (permission.has(p, "taien.th.hunt.*"))))))
      {
        if ((!lastcheck.containsKey(p)) || (((Long)lastcheck.get(p)).longValue() < System.currentTimeMillis() - 1000 * checksec))
        {
          getClosestHunt(p, true);
          lastcheck.put(p, Long.valueOf(System.currentTimeMillis()));
          return true;
        }

        p.sendMessage(ChatColor.DARK_RED + "You can only check for the closest chest once every " + checksec + " seconds.");
        return true;
      }

      if ((args.length == 1) && (((!useperms) && (sender.isOp())) || ((useperms) && (permission.has(p, "taien.th.admin")))))
      {
        if ((args[0].equalsIgnoreCase("help")) || (args[0].equalsIgnoreCase("?")))
        {
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + "-----------------------------------");
          p.sendMessage(ChatColor.YELLOW + "/hunt reload|load" + ChatColor.GRAY + " - reload from config, overwriting settings.");
          p.sendMessage(ChatColor.YELLOW + "/hunt save" + ChatColor.GRAY + " - save settings to the config file.");
          p.sendMessage(ChatColor.YELLOW + "/hunt settings" + ChatColor.GRAY + " - view current chest settings");
          p.sendMessage(ChatColor.YELLOW + "/hunt list" + ChatColor.GRAY + " - list info on all active hunts");
          p.sendMessage(ChatColor.YELLOW + "/hunt center" + ChatColor.GRAY + " - set current loc as the center of spawning.");
          p.sendMessage(ChatColor.YELLOW + "/hunt addworld" + ChatColor.GRAY + " - add your current world as a chest world.");
          p.sendMessage(ChatColor.YELLOW + "/hunt removeworld" + ChatColor.GRAY + " - remove your current world as a chest world.");
          p.sendMessage(ChatColor.YELLOW + "/hunt maxdist <int>" + ChatColor.GRAY + " - set max dist of chests from center.");
          p.sendMessage(ChatColor.YELLOW + "/hunt mindist <int>" + ChatColor.GRAY + " - set min dist of chests from center.");
          p.sendMessage(ChatColor.YELLOW + "/hunt duration <int>" + ChatColor.GRAY + " - set time in mins until chests fade.");
          p.sendMessage(ChatColor.YELLOW + "/hunt chance <int>" + ChatColor.GRAY + " - set spawn chance to 1 in <int>.");
          p.sendMessage(ChatColor.YELLOW + "/hunt weight <int>" + ChatColor.GRAY + " - set weight of chest draws(more = lower val).");
          p.sendMessage(ChatColor.YELLOW + "/hunt interval <int>" + ChatColor.GRAY + " - set interval in secs between spawn checks.");
          p.sendMessage(ChatColor.GREEN + "/hunt help 2 or /hunt ? 2 for more help");
          return true;
        }
        if ((args[0].equalsIgnoreCase("reload")) || (args[0].equalsIgnoreCase("load")))
        {
          loadProcedure();
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Reloaded items/options.");
          return true;
        }
        if (args[0].equalsIgnoreCase("save"))
        {
          saveProcedure();
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Saved items/options.");
          return true;
        }
        if (args[0].equalsIgnoreCase("list"))
        {
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + "-----------------------------------");
          for (THHunt h : huntList)
          {
            Location l = h.getLocation();
            if (h.isLocked()) p.sendMessage(ChatColor.YELLOW + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + "(" + l.getWorld().getName() + ")" + ChatColor.WHITE + " - " + ChatColor.DARK_RED + "FOUND(" + h.getMinutesLeft() + " mins to fade)"); else
              p.sendMessage(ChatColor.YELLOW + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + "(" + l.getWorld().getName() + ")" + ChatColor.WHITE + " - " + ChatColor.GREEN + "Value " + h.getValue() + ChatColor.WHITE + " - " + ChatColor.BLUE + h.getMinutesLeft() + " mins");
          }
          return true;
        }
        if (args[0].equalsIgnoreCase("settings"))
        {
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "] " + ChatColor.WHITE + "GENERAL OPTIONS--------------------");
          String w = "";
          THWorldOpts o = null;
          String s;
          for (currentp = worlds.keySet().iterator(); currentp.hasNext(); w = w + s + " ") s = (String)currentp.next();
          p.sendMessage(ChatColor.DARK_AQUA + "Worlds: " + ChatColor.WHITE + w);
          p.sendMessage(ChatColor.DARK_AQUA + "Protections: " + ChatColor.AQUA + "Break: " + ChatColor.WHITE + protectbreak + ChatColor.AQUA + " Burn: " + ChatColor.WHITE + protectburn + ChatColor.AQUA + " Explode: " + ChatColor.WHITE + protectexplode + ChatColor.AQUA + " Piston: " + ChatColor.WHITE + protectpiston);
          p.sendMessage(ChatColor.AQUA + "MinPlayers: " + ChatColor.WHITE + minplayers + " online" + ChatColor.AQUA + " | CompassBlocks: " + ChatColor.WHITE + usecompass);
          p.sendMessage(ChatColor.AQUA + "MaxSpawnAttempts: " + ChatColor.WHITE + maxspawnattempts + ChatColor.AQUA + " | DetailLogs: " + ChatColor.WHITE + detaillogs);
          p.sendMessage(ChatColor.AQUA + "FoundChestFadeTime: " + ChatColor.WHITE + foundchestfadetime + " min" + ChatColor.AQUA + " | ThreeDimensionalDistance: " + ChatColor.WHITE + threedimensionaldistance);
          p.sendMessage(ChatColor.AQUA + "CheckSec: " + ChatColor.WHITE + checksec + " sec" + ChatColor.AQUA + " | ItemsLists: " + ChatColor.WHITE + (worldlists.size() + customlists.size()) + ChatColor.AQUA + " | Enchants: " + ChatColor.WHITE + enchanted.size());
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "Current World" + ChatColor.YELLOW + "] " + ChatColor.WHITE + p.getWorld().getName().toUpperCase() + "--------------------");
          if (worlds.containsKey(wn)) { o = (THWorldOpts)worlds.get(wn);
          } else
          {
            p.sendMessage(ChatColor.YELLOW + "Current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          if (worldlists.get(o.itemlist) == null) { p.sendMessage(ChatColor.DARK_AQUA + " ** ItemList: " + ChatColor.WHITE + o.itemlist + " (ERROR:  List doesn't exist)");
          } else
          {
            THWorldList wl = (THWorldList)worldlists.get(o.itemlist);
            p.sendMessage(ChatColor.DARK_AQUA + " ** ItemList: " + ChatColor.WHITE + o.itemlist + " (" + (wl.common.size() + wl.uncommon.size() + wl.rare.size() + wl.legendary.size() + wl.epic.size()) + " items in list)");
          }
          p.sendMessage(ChatColor.DARK_AQUA + " ** Enabled: " + ChatColor.WHITE + o.enabled + ChatColor.DARK_AQUA + " | Strict Items: " + ChatColor.WHITE + o.strictitems + ChatColor.DARK_AQUA + " | MinChests: " + ChatColor.WHITE + o.minchests);
          p.sendMessage(ChatColor.DARK_AQUA + "WEIGHTS: " + ChatColor.AQUA + "Value: " + ChatColor.WHITE + o.drawweight + " draws" + ChatColor.AQUA + " Items: " + ChatColor.WHITE + o.gooditemweight + " draws" + ChatColor.AQUA + " Amounts: " + ChatColor.WHITE + o.amountweight + " draws");
          p.sendMessage(ChatColor.AQUA + "Center: " + ChatColor.WHITE + o.centerx + "," + o.centerz + ChatColor.AQUA + " | Distance: " + ChatColor.WHITE + o.mindistance + "-" + o.maxdistance + ChatColor.AQUA + " | Duration: " + ChatColor.WHITE + o.duration + " mins");
          p.sendMessage(ChatColor.AQUA + "Chance: " + ChatColor.WHITE + "1:" + o.chance + ChatColor.AQUA + " per " + ChatColor.WHITE + o.interval + " sec" + ChatColor.AQUA + " | Min/Max Light: " + ChatColor.WHITE + o.minlight + "-" + o.maxlight);
          p.sendMessage(ChatColor.AQUA + "Min/Max Elevation: " + ChatColor.WHITE + o.minelevation + "-" + o.maxelevation + ChatColor.AQUA + "   Max Elevation(Rares): " + ChatColor.WHITE + o.maxelevationrare);
          p.sendMessage(ChatColor.AQUA + "Min Money: " + ChatColor.WHITE + o.minmoney + ChatColor.AQUA + " | Money Multiplier: " + ChatColor.WHITE + "x" + this.ratiopercent.format(o.moneymultiplier) + ChatColor.AQUA + " | Max Compass Dist: " + ChatColor.WHITE + o.maxcompassdistance);
          String s = "";
          Material m;
          for (s = o.spawnableblocks.iterator(); s.hasNext(); s = s + m.name() + " ") m = (Material)s.next();
          p.sendMessage(ChatColor.AQUA + "Spawns on: " + ChatColor.WHITE + s);
          if (o.fadeblock == null) p.sendMessage(ChatColor.AQUA + "Hunt Tool: " + ChatColor.WHITE + o.hunttool.name() + ChatColor.AQUA + " | Marker Block: " + ChatColor.WHITE + o.markerblock.name() + ChatColor.AQUA + " | Fade Block: " + ChatColor.WHITE + "RETURN"); else
            p.sendMessage(ChatColor.AQUA + "Hunt Tool: " + ChatColor.WHITE + o.hunttool.name() + ChatColor.AQUA + " | Marker Block: " + ChatColor.WHITE + o.markerblock.name() + ChatColor.AQUA + " | Fade Block: " + ChatColor.WHITE + o.fadeblock.name());
          p.sendMessage(ChatColor.AQUA + "Use Marker Block: " + ChatColor.WHITE + o.usemarker + ChatColor.AQUA + " | Override MinPlayers: " + ChatColor.WHITE + o.overrideminplayers);
          return true;
        }
        if (args[0].equalsIgnoreCase("cb"))
        {
          Block b = p.getTargetBlock(null, 10);
          boolean adding = false;
          if (compassblocks.contains(b)) { compassblocks.remove(b);
          } else
          {
            compassblocks.add(b);
            adding = true;
          }
          if (adding) p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Added " + ChatColor.YELLOW + b.getX() + ", " + b.getY() + ", " + b.getZ() + ChatColor.WHITE + " in " + ChatColor.YELLOW + b.getWorld().getName() + ChatColor.WHITE + " as a compass block."); else
            p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Removed " + ChatColor.YELLOW + b.getX() + ", " + b.getY() + ", " + b.getZ() + ChatColor.WHITE + " in " + ChatColor.YELLOW + b.getWorld().getName() + ChatColor.WHITE + " as a compass block.");
          return true;
        }
        if (args[0].equalsIgnoreCase("usecb"))
        {
          usecompass = !usecompass;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Compass use toggled to " + ChatColor.YELLOW + usecompass + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("center"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          o.centerx = p.getLocation().getBlockX();
          o.centerz = p.getLocation().getBlockZ();
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Center of treasure generation set to " + ChatColor.YELLOW + o.centerx + ", " + o.centerz + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("tool"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          if ((p.getItemInHand() == null) || (p.getItemInHand().getType() == Material.AIR))
          {
            p.sendMessage(ChatColor.DARK_RED + "You are not holding an item!");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          o.hunttool = p.getItemInHand().getType();
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Hunt tool set to " + ChatColor.YELLOW + o.hunttool.name() + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("offeringtool"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          if ((p.getItemInHand() == null) || (p.getItemInHand().getType() == Material.AIR))
          {
            p.sendMessage(ChatColor.DARK_RED + "You are not holding an item!");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          o.offeringtool = p.getItemInHand().getType();
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Offering tool set to " + ChatColor.YELLOW + o.hunttool.name() + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("marker"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          if ((p.getItemInHand() == null) || (p.getItemInHand().getType() == Material.AIR))
          {
            p.sendMessage(ChatColor.DARK_RED + "You are not holding an item!");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          o.markerblock = p.getItemInHand().getType();
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Marker block set to " + ChatColor.YELLOW + o.markerblock.name() + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("addworld"))
        {
          String world = wn;
          if (worlds.containsKey(world))
          {
            p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " This world is already set up.");
          }
          else
          {
            worlds.put(world, new THWorldOpts());
            p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Treasure world " + ChatColor.YELLOW + world + ChatColor.WHITE + " added.");
          }
          return true;
        }
        if (args[0].equalsIgnoreCase("removeworld"))
        {
          World world = p.getWorld();
          if (worlds.containsKey(world))
          {
            worlds.remove(world);
            p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Treasure world " + ChatColor.YELLOW + world.getName() + ChatColor.WHITE + " removed.");
          }
          else
          {
            p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " This world is already not a treasure world.");
          }
          return true;
        }
        return false;
      }
      if ((args.length == 2) && (((!useperms) && (sender.isOp())) || ((useperms) && (permission.has(p, "taien.th.admin")))))
      {
        if ((args[0].equalsIgnoreCase("help")) || (args[0].equalsIgnoreCase("?")))
        {
          if (args[1].equalsIgnoreCase("2"))
          {
            p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + "-----------------------------------");
            p.sendMessage(ChatColor.YELLOW + "/hunt minmoney <int>" + ChatColor.GRAY + " - set minimum money found in chests.");
            p.sendMessage(ChatColor.YELLOW + "/hunt consumechance <int>" + ChatColor.GRAY + " - set chance of tool being consumed.");
            p.sendMessage(ChatColor.YELLOW + "/hunt maxcompassdist <int>" + ChatColor.GRAY + " - set max distance compass blocks work.");
            p.sendMessage(ChatColor.YELLOW + "/hunt moneymultiplier <decimal>" + ChatColor.GRAY + " - set money multiplier (X value).");
            p.sendMessage(ChatColor.YELLOW + "/hunt itemweight <int>" + ChatColor.GRAY + " - set good item weight.");
            p.sendMessage(ChatColor.YELLOW + "/hunt tool" + ChatColor.GRAY + " - set hunt tool to held item.");
            p.sendMessage(ChatColor.YELLOW + "/hunt cb" + ChatColor.GRAY + " - toggle block you're looking at as compass block.");
            p.sendMessage(ChatColor.YELLOW + "/hunt usecb" + ChatColor.GRAY + " - toggle whether to allow compass use or not.");
            p.sendMessage(ChatColor.YELLOW + "/hunt maxvalue <int>" + ChatColor.GRAY + " - set max value of chests.");
            p.sendMessage(ChatColor.YELLOW + "/hunt marker" + ChatColor.GRAY + " - set marker under chests to held item.");
            p.sendMessage(ChatColor.YELLOW + "/hunt usemarker <true/false>" + ChatColor.GRAY + " - use chest marker.");
            p.sendMessage(ChatColor.YELLOW + "/hunt enable <true/false>" + ChatColor.GRAY + " - enable/disable this world.");
            p.sendMessage(ChatColor.YELLOW + "/hunt copyworld <world>" + ChatColor.GRAY + " - copy <world> settings to this world.");
            p.sendMessage(ChatColor.GREEN + "/hunt help 3 or /hunt ? 3 for more help");
            return true;
          }
          if (args[1].equalsIgnoreCase("3"))
          {
            p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + "-----------------------------------");
            p.sendMessage(ChatColor.YELLOW + "/hunt minchests <int>" + ChatColor.GRAY + " - set minimum chests in world.");
            p.sendMessage(ChatColor.YELLOW + "/hunt overrideminplayers <true/false>" + ChatColor.GRAY + " - override minplayers to reach minchests.");
            p.sendMessage(ChatColor.YELLOW + "/hunt offeramount <int>" + ChatColor.GRAY + " - set amount of offer item needed to set compass.");
            p.sendMessage(ChatColor.YELLOW + "/hunt fadefoundchests <true/false>" + ChatColor.GRAY + " - remove chests that have been found.");
            p.sendMessage(ChatColor.YELLOW + "/hunt strictitems <true/false>" + ChatColor.GRAY + " - set rarity-level strictness.");
            p.sendMessage(ChatColor.YELLOW + "/hunt offeringtool" + ChatColor.GRAY + " - set offering tool to item in hand.");
            p.sendMessage(ChatColor.YELLOW + "/hunt addenchant <string>" + ChatColor.GRAY + " - add an enchantment setup using the held item.");
            p.sendMessage(ChatColor.GREEN + "* TreasureHunt v" + version + " by " + ChatColor.DARK_PURPLE + "Taien");
            p.sendMessage(ChatColor.GREEN + "* taienverdain@gmail.com for paypal donations/suggestions/feedback!");
            return true;
          }
          return false;
        }
        if (args[0].equalsIgnoreCase("addenchant"))
        {
          String name = args[1];
          if ((name.contains(":")) || (name.contains("'")) || (name.contains(".")) || (enchanted.containsKey(args[1])))
          {
            p.sendMessage(ChatColor.DARK_RED + "That is an invalid name for an enchant, or it already exists.");
            return true;
          }
          ItemStack item = p.getItemInHand();
          if ((item == null) || (item.getType() == Material.AIR))
          {
            p.sendMessage(ChatColor.DARK_RED + "You are not holding anything!");
            return true;
          }
          if (item.getEnchantments().size() == 0)
          {
            p.sendMessage(ChatColor.DARK_RED + "You are not holding an enchanted item!");
            return true;
          }
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Added enchantment setup '" + ChatColor.YELLOW + name + ChatColor.WHITE + "'.");
          enchanted.put(name, item);
          return true;
        }
        if (args[0].equalsIgnoreCase("usemarker"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          boolean val = Boolean.parseBoolean(args[1]);
          o.usemarker = val;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " This world's marker use set to " + ChatColor.YELLOW + val + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("enable"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          boolean val = Boolean.parseBoolean(args[1]);
          o.enabled = val;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " This world's random treasure generation set to " + ChatColor.YELLOW + val + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("strictitems"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          boolean val = Boolean.parseBoolean(args[1]);
          o.strictitems = val;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " This world's item strictness set to " + ChatColor.YELLOW + val + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("overrideminplayers"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          boolean val = Boolean.parseBoolean(args[1]);
          o.overrideminplayers = val;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Override MinPlayers to reach MinChests set to " + ChatColor.YELLOW + val + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("fadefoundchests"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          boolean val = Boolean.parseBoolean(args[1]);
          o.fadefoundchests = val;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Fading of found chests set to " + ChatColor.YELLOW + val + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("copyworld"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          World w = null;
          w = server.getWorld(args[1]);
          if (w == null)
          {
            p.sendMessage(ChatColor.DARK_RED + "That world doesn't appear to exist.");
            return true;
          }
          if (!worlds.containsKey(w))
          {
            p.sendMessage(ChatColor.DARK_RED + "The world you're trying to copy from is not set up for chest generation.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          THWorldOpts oo = (THWorldOpts)worlds.get(w);
          o.chance = oo.chance;
          o.consumechance = oo.consumechance;
          o.drawweight = oo.drawweight;
          o.gooditemweight = oo.gooditemweight;
          o.amountweight = oo.amountweight;
          o.duration = oo.duration;
          o.enabled = oo.enabled;
          o.hunttool = oo.hunttool;
          o.interval = oo.interval;
          o.lastcheck = oo.lastcheck;
          o.markerblock = oo.markerblock;
          o.maxelevation = oo.maxelevation;
          o.maxlight = oo.maxlight;
          o.maxelevationrare = oo.maxelevationrare;
          o.maxvalue = oo.maxvalue;
          o.minelevation = oo.minelevation;
          o.minlight = oo.minlight;
          o.minmoney = oo.minmoney;
          o.moneymultiplier = oo.moneymultiplier;
          o.spawnableblocks = new LinkedList(oo.spawnableblocks);
          o.usemarker = oo.usemarker;
          o.strictitems = oo.strictitems;
          o.maxcompassdistance = oo.maxcompassdistance;
          o.fadefoundchests = oo.fadefoundchests;
          o.minchests = oo.minchests;
          o.offeramount = oo.offeramount;
          o.offeringtool = oo.offeringtool;
          o.overrideminplayers = oo.overrideminplayers;

          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " All settings except maxdist, mindist, centerx, and centerz have been copied from world " + args[1] + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("maxvalue"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          int val = Integer.parseInt(args[1]);
          if (val < 0) val = 0;
          else if (val > 5000) val = 5000;
          o.maxvalue = val;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Max value of treasure set to " + ChatColor.YELLOW + val + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("minlight"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          int val = Integer.parseInt(args[1]);
          if (val < 0) val = 0;
          if (val > 14) val = 14;
          if (val > o.maxlight) val = o.maxlight;
          o.minlight = val;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Min lightlevel set to " + ChatColor.YELLOW + val + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("maxlight"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          int val = Integer.parseInt(args[1]);
          if (val > 14) val = 14;
          if (val < 0) val = 0;
          if (val < o.minlight) val = o.minlight;
          o.maxlight = val;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Max lightlevel set to " + ChatColor.YELLOW + val + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("minchests"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          int val = Integer.parseInt(args[1]);
          if (val < 0) val = 0;
          o.minchests = val;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Minimum amount of chests set to " + ChatColor.YELLOW + val + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("offeramount"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          int val = Integer.parseInt(args[1]);
          if (val < 1) val = 1;
          o.offeramount = val;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Amount of items needed for offering set to " + ChatColor.YELLOW + val + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("amountweight"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          int val = Integer.parseInt(args[1]);
          if (val < 1) val = 1;
          else if (val > 5000) val = 100;
          o.amountweight = val;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Amount weight of treasure set to " + ChatColor.YELLOW + val + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("maxdist"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          int dist = Integer.parseInt(args[1]);
          if (dist < 0) dist = 0;
          o.maxdistance = dist;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Max distance of treasure from center set to " + ChatColor.YELLOW + dist + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("maxcompassdist"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          int dist = Integer.parseInt(args[1]);
          if (dist < 0) dist = 0;
          o.maxcompassdistance = dist;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Max compass block reach distance set to " + ChatColor.YELLOW + dist + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("mindist"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          int dist = Integer.parseInt(args[1]);
          if (dist < 0) dist = 0;
          o.mindistance = dist;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Min distance of treasure from center set to " + ChatColor.YELLOW + dist + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("minmoney"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          int val = Integer.parseInt(args[1]);
          if (val < 0) val = 0;
          o.minmoney = val;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Min money in a chest set to " + ChatColor.YELLOW + val + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("consumechance"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          int val = Integer.parseInt(args[1]);
          if (val < 0) val = 0;
          if (val > 100) val = 100;
          o.consumechance = val;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Chance of hunt tool consumption set to " + ChatColor.YELLOW + val + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("moneymultiplier"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          double val = Double.parseDouble(args[1]);
          if (val < 0.0D) val = 0.0D;
          o.moneymultiplier = val;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Max money multiplier set to " + ChatColor.YELLOW + val + ChatColor.WHITE + "x chest value.");
          return true;
        }
        if (args[0].equalsIgnoreCase("duration"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          int dur = Integer.parseInt(args[1]);
          if (dur < 5) dur = 5;
          o.duration = dur;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Duration of new hunts set to " + ChatColor.YELLOW + dur + ChatColor.WHITE + " minutes.");
          return true;
        }
        if (args[0].equalsIgnoreCase("chance"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          int cha = Integer.parseInt(args[1]);
          if (cha < 1) cha = 1;
          o.chance = cha;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Chance of chests spawning set to 1 in " + ChatColor.YELLOW + cha + ChatColor.WHITE + ".");
          return true;
        }
        if (args[0].equalsIgnoreCase("weight"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          int wei = Integer.parseInt(args[1]);
          if (wei < 1) wei = 1;
          o.drawweight = wei;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Draw weight of chests set to " + ChatColor.YELLOW + wei + ChatColor.WHITE + " draws.");
          return true;
        }
        if (args[0].equalsIgnoreCase("itemweight"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          int wei = Integer.parseInt(args[1]);
          if (wei < 1) wei = 1;
          o.gooditemweight = wei;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Weight of good items in chests set to " + ChatColor.YELLOW + wei + ChatColor.WHITE + " draws.");
          return true;
        }
        if (args[0].equalsIgnoreCase("interval"))
        {
          if (!worlds.containsKey(wn))
          {
            p.sendMessage(ChatColor.DARK_RED + "Your current world is not set up for chest generation.  Use /hunt addworld.");
            return true;
          }
          THWorldOpts o = (THWorldOpts)worlds.get(wn);
          int i = Integer.parseInt(args[1]);
          if (i < 5) i = 5;
          o.interval = i;
          p.sendMessage(ChatColor.YELLOW + "[" + ChatColor.GOLD + "TreasureHunt" + ChatColor.YELLOW + "]" + ChatColor.WHITE + " Interval of chest draws set to " + ChatColor.YELLOW + i + ChatColor.WHITE + " seconds.");
          return true;
        }
        return false;
      }
      return false;
    }
    return false;
  }

  public static enum LookDirection
  {
    POSX, 
    POSZ, 
    NEGX, 
    NEGZ, 
    UP, 
    DOWN;
  }
}