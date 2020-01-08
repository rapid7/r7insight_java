package com.rapid7.net;

/**
 * Utility class to avoid including external dependencies.
 * 
 * @since 3.0.1
 */
final class Utils {
  // private constructor
  private Utils (){}
  
  /**
   * Check if the contained string is <code>null</code> or empty. Trimming is NOT applied.
   * @param s The String to be checked. May be <code>null</code>.
   * @return <code>true</code> if the passed String is <code>null</code> or empty.
   */
  public static boolean isNullOrEmpty (final String s){
    return s == null || s.length () == 0;
  }
}
