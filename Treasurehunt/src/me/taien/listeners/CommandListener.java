package me.taien.listeners;

import java.util.LinkedList;
import java.util.List;

import me.taien.THPlayer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class CommandListener implements Listener{

	
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
}
