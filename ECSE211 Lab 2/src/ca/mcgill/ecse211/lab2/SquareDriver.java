

 package ca.mcgill.ecse211.lab2;



import lejos.hardware.motor.EV3LargeRegulatedMotor;



/**

 * This class is used to drive the robot on the demo floor.
*
*@author1 Cristian Ciungu
*@author2 Hao Shu
*@version 2019-01-29
*
 */

public class SquareDriver {

  private static final int FORWARD_SPEED = 200;

  private static final int ROTATE_SPEED = 120;

  private static final double TILE_SIZE = 30.48;



  /**

   * This method is meant to drive the robot in a square of size 3x3 Tiles. It is to run in parallel

   * with the odometer and Odometer correcton classes allow testing their functionality.

   * 

   * @param leftMotor

   * @param rightMotor

   * @param leftRadius

   * @param rightRadius

   * @param width

   */

  public static void drive(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,

      double leftRadius, double rightRadius, double track) {

    // reset the motors

    for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {leftMotor, rightMotor}) {

      motor.stop();

      motor.setAcceleration(3000);

    }



    // Sleep for 2 seconds

    try {

      Thread.sleep(2000);

    } catch (InterruptedException e) {

      // There is nothing to be done here

    }



    for (int i = 0; i < 4; i++) {

      // drive forward two tiles

      leftMotor.setSpeed(FORWARD_SPEED-1);

      rightMotor.setSpeed(FORWARD_SPEED-1);



      leftMotor.rotate(convertDistance(leftRadius, 2.9 * TILE_SIZE), true);

      rightMotor.rotate(convertDistance(rightRadius, 2.9 * TILE_SIZE), false);



      // turn 90 degrees clockwise

      leftMotor.setSpeed(ROTATE_SPEED);

      rightMotor.setSpeed(ROTATE_SPEED);



      leftMotor.rotate(convertAngle(leftRadius, track, 90.0), true);

      rightMotor.rotate(-convertAngle(rightRadius, track, 90.0), false);

    }

  }



  /**

   * This method allows the conversion of a distance to the total rotation of each wheel need to

   * cover that distance.

   * 

   * @param radius

   * @param distance

   * @return

   */

  private static int convertDistance(double radius, double distance) {

    return (int) ((180.0 * distance) / (Math.PI * radius));

  }//number of wheel turns, to make it easier to calculate theta



  private static int convertAngle(double radius, double width, double angle) {

    return convertDistance(radius, Math.PI * width * angle / 360.0);// convert the angle unit from radian

  }
}