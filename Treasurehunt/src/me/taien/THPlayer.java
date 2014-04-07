package me.taien;

public class THPlayer
{
  private int chestsfound;
  private int valuefound;

  public THPlayer()
  {
    this.chestsfound = 0;
    this.valuefound = 0;
  }

  public THPlayer(int chestsfound, int valuefound)
  {
    this.chestsfound = chestsfound;
    this.valuefound = valuefound;
  }

  public int getChestsFound()
  {
    return this.chestsfound;
  }

  public int getValueFound()
  {
    return this.valuefound;
  }

  public void foundChest(int value)
  {
    this.chestsfound += 1;
    this.valuefound += value;
  }

  public void reset()
  {
    this.chestsfound = 0;
    this.valuefound = 0;
  }
}