package me.taien.config;

import java.util.Map;
import org.bukkit.inventory.ItemStack;

public class THWorldList
{
  public Map<ItemStack, Integer> common;
  public Map<ItemStack, Integer> uncommon;
  public Map<ItemStack, Integer> rare;
  public Map<ItemStack, Integer> legendary;
  public Map<ItemStack, Integer> epic;

  public THWorldList(Map<ItemStack, Integer> common, Map<ItemStack, Integer> uncommon, Map<ItemStack, Integer> rare, Map<ItemStack, Integer> legendary, Map<ItemStack, Integer> epic)
  {
    this.common = common;
    this.uncommon = uncommon;
    this.rare = rare;
    this.legendary = legendary;
    this.epic = epic;
  }
}