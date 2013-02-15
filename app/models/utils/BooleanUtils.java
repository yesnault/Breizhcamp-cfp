package models.utils;

/**
 *
  */
public class BooleanUtils {
    
    // Méthodes pour éviter l'import de la lib Apache Common Lang
    
    public static boolean isFalse(Boolean bool) {
        if (bool == null) {
            return false;
        }
        return bool.booleanValue() ? false : true; 
    }
    
    public static boolean isNotFalse(Boolean bool) {
        return !isNotFalse(bool);
    }

    public static boolean isTrue(Boolean bool) {
        if (bool == null) {
            return false;
        }
        return bool.booleanValue() ? true : false;
    }
    
    public static boolean isNotTrue(Boolean bool) {
        return !isTrue(bool);
    }
}
