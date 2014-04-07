package me.taien.config;

public class THToolSettings
{
  public int minminutes;
  public int maxminutes;
  public int value;
  public String itemlist;

  public THToolSettings(int minmins, int maxmins, int value, String itemlist)
  {
    this.minminutes = minmins;
    this.maxminutes = maxmins;
    this.value = value;
    this.itemlist = itemlist;
  }
}