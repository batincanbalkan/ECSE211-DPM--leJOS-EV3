/**
 * This class extends Exception. It is used to handle errors generated by the Odometer and the
 * OdometerData classes.
 * 
 * @author1 Cristian Ciungu
 * @author2 Hao Shu
 * @version 12-02-2019
 *
 */

// import package

package ca.mcgill.ecse211.odometer;

public class OdometerExceptions extends Exception {

  /**
   * The constructor calls the superclass Exception to handle errors from the Odometer and the
   * OdometerData classes.
   * 
   * @param Error
   */

  public OdometerExceptions(String Error) {

    super(Error);

  } // end OdometerExceptions()

} // end OdometerExceptions
