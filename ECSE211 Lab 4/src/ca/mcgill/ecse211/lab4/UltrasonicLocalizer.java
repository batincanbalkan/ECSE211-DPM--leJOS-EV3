

/**
 * This class implements the first step in the localization process, that is to say ultrasonic
 * localization. Essentially, it implements the ultrasonic sensor to filter distances through either
 * the falling edge or the raising edge process. Then, once the filtering is done, two angles (alpha
 * and beta), and the robot uses those to orient its heading in the direction of increasing y.
 * 
 * @author1 Cristian Ciungu
 * @author2 Hao Shu
 * @version 12-02-2019
 * 
 * 
 */

// import package
package ca.mcgill.ecse211.lab4;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import ca.mcgill.ecse211.odometer.*;
import java.util.Arrays;
import ca.mcgill.ecse211.lab4.*;

public class UltrasonicLocalizer {


  /**
   * speed used to move the robot as it localizes
   */
  public static int ROTATION_SPEED = 75;
  private Odometer odometer;
  private float[] usData;
  private EV3LargeRegulatedMotor leftMotor, rightMotor;
  private SampleProvider usDistance;

  /**
   * distance value to select appropriate readings from the ultrasonic sensor once it detects
   * abnormally large values
   */
  private double THRESHOLD = 22.00;// the value of d
  /**
   * distance value to select appropriate readings from the ultrasonic sensor once it detects
   * abnormally small values
   */
  private double MARGIN = 2.00;// the value of k
  private double alpha, beta, theta;// the angle recorded when the sensor detects the wall
  private double angleToTurn;
  private double risingAngle = 180;// offset for the turning angle

  /**
   * Constructor to initialize variables
   * 
   * @param Odometer
   * @param EV3LargeRegulatedMotor
   * @param EV3LargeRegulatedMotor
   * @param SampleProvider
   */
  public UltrasonicLocalizer(Odometer odo, EV3LargeRegulatedMotor leftMotor,
      EV3LargeRegulatedMotor rightMotor, SampleProvider usDistance) {
    this.odometer = odo;
    this.leftMotor = leftMotor;
    this.rightMotor = rightMotor;
    this.usDistance = usDistance;
    this.usData = new float[usDistance.sampleSize()];

    leftMotor.setSpeed(ROTATION_SPEED);
    rightMotor.setSpeed(ROTATION_SPEED);
  }



  /**
   * A method to localize position using the falling edge
   * 
   */
  public void fallingEdge() {


    // Rotate to open space
    while (medianFilter() < THRESHOLD + MARGIN) {
      leftMotor.backward();
      rightMotor.forward();
    }
    // Rotate to the first wall
    while (medianFilter() > THRESHOLD) {
      leftMotor.backward();
      rightMotor.forward();
    }

    // record angle
    alpha = odometer.getXYT()[2];
    // make a sound so that we can know the angle is recorded
    Sound.beep();
    leftMotor.stop(true);
    rightMotor.stop();
    // rotate out of the wall
    while (medianFilter() < THRESHOLD + MARGIN) {
      leftMotor.forward();
      rightMotor.backward();
    }

    // rotate to the second wall
    while (medianFilter() > THRESHOLD) {
      leftMotor.forward();
      rightMotor.backward();
    }
    Sound.beep();
    beta = odometer.getXYT()[2];

    leftMotor.stop(true);
    rightMotor.stop();

    // calculate angle of rotation by using the formula given in the tutorial
    theta = calculateTheta(alpha, beta);

    if (Math.abs(alpha - beta) <= 100) { // when the robot is facing the wall, it will turn around
      angleToTurn = theta + odometer.getXYT()[2] + risingAngle;
    } else {// when the robot is not facing the wall
      angleToTurn = theta + odometer.getXYT()[2];

    }
    // rotate robot to the theta = 0.0
    // introduce a fix error correction
    leftMotor.rotate(-convertAngle(Lab4.WHEEL_RAD, Lab4.TRACK, angleToTurn), true);
    rightMotor.rotate(convertAngle(Lab4.WHEEL_RAD, Lab4.TRACK, angleToTurn), false);

    // set odometer to x, y,theta = 0
    odometer.setXYT(0.0, 0.0, 0.0);

  }

  /**
   * A method to localize position using the rising edge
   * 
   * @param alpha
   * @param beta
   * @param angleToTurn
   */
  public void risingEdge() {


    // Rotate to the wall
    while (medianFilter() > THRESHOLD) {
      leftMotor.backward();
      rightMotor.forward();
    }
    // Rotate until it sees the open space
    while (medianFilter() < THRESHOLD + MARGIN) {
      leftMotor.backward();
      rightMotor.forward();
    }
    Sound.beep();
    // record angle
    alpha = odometer.getXYT()[2];
    // make a sound so that we can know the angle is recorded
    Sound.beep();

    // rotate the other way until it sees the wall
    while (medianFilter() > THRESHOLD) {
      leftMotor.forward();
      rightMotor.backward();
    }

    // rotate until it sees open space
    while (medianFilter() < THRESHOLD + MARGIN) {
      leftMotor.forward();
      rightMotor.backward();
    }

    beta = odometer.getXYT()[2];
    Sound.beep();
    leftMotor.stop(true);
    rightMotor.stop();

    theta = calculateTheta(alpha, beta) + risingAngle;

    if (Math.abs(alpha - beta) > 100) { // when the robot is not facing the wall, it will turn around
      angleToTurn = theta + odometer.getXYT()[2] + risingAngle;
    } else {// when the robot is  facing the wall
      angleToTurn = theta + odometer.getXYT()[2];

    }



    // rotate robot to theta = 0.0 using turning angle
    // introduce a fix error correction
    leftMotor.rotate(-convertAngle(Lab4.WHEEL_RAD, Lab4.TRACK, angleToTurn), true);
    rightMotor.rotate(convertAngle(Lab4.WHEEL_RAD, Lab4.TRACK, angleToTurn), false);

    // set x,y,theta = 0.0
    odometer.setXYT(0.0, 0.0, 0.0);
  }



  /**
   * This method use a median filter to filter the data collected by the sensor and toss out the
   * invalid sample
   * 
   * This method can help avoid the effect caused by invalid readings.
   * 
   * @param void
   * @return median value
   * 
   */
  private double medianFilter() {
    double[] filterData = new double[5]; // use an array to store readings
    for (int i = 0; i < 5; i++) { // take 5 readings
      usDistance.fetchSample(usData, 0);
      filterData[i] = usData[0] * 100.0; // put the readings in array and amplify them
    }
    Arrays.sort(filterData); // sort the array
    return filterData[2]; // take median value
  }


  /**
   * This method is used to calculate the angle to be added to the heading reported by the odometer
   * based on the value of alpha and beta
   * 
   * @param alpha
   * @param beta
   * @return theta
   */


  private double calculateTheta(double alpha, double beta) {// calculate the value of theta
    double theta = 0;
    if (alpha < beta) {
      theta = 45 - (alpha + beta) / 2;

    } else if (alpha > beta) {
      theta = 225 - (alpha + beta) / 2;
    }

    return theta;

  }


  /**
   * This method implement the conversion of a distance to rotation of each wheel need to cover the
   * distance.
   * 
   * @param radius
   * @param distance
   * @return
   */
  public static int convertDistance(double radius, double distance) {
    return (int) ((180.0 * distance) / (Math.PI * radius));
  }

  /**
   * This method implement the conversion of a angle to rotation of each wheel need to cover the
   * distance.
   * 
   * @param radius
   * @param distance
   * @param angle
   * @return
   */
  public static int convertAngle(double radius, double width, double angle) {
    return convertDistance(radius, Math.PI * width * angle / 360.0);
  }

}// end UltrasonicLocalizer
