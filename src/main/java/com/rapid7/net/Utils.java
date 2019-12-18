package com.rapid7.net;

final class Utils {
  // private constructor
  private Utils (){}
  
  /**
   * Check if the contained string is <code>null</code> or empty. Trimming is NOT applied.
   * @param s The String to be checked.
   * @return <code>true</code> if the passed String is <code>null</code> or empty.
   */
  public static boolean isNullOrEmpty (String s){
    return s == null || s.length () == 0;
  }
}
