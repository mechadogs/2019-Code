/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.InvertType;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.sensors.PigeonIMU;
import com.ctre.phoenix.sensors.PigeonIMU.FusionStatus;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.command.InstantCommand;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
/*
*FRC 2344/5123 - Practice Robot(s) Code.
*This supports the Driver & Autonomous Robot
*/
public class Robot extends TimedRobot {

 WPI_TalonSRX leftMaster, leftPrimarySlave, rightMaster, rightPrimarySlave;
 PigeonIMU gyro;
 DifferentialDrive drive;
 Joystick driver, operator;
 boolean auto_bot = false;
 int _printLoops = 0;
 int jPOV;
 Timer autoTimer;
 JoystickButton turn;
 PigeonIMU.FusionStatus fusionStatus;
 enum Angle{
   LEFT,
   RIGHT
 }

 enum AutoSteps{
   ONE,
   TWO,
   THREE,
   FOUR
 }

 AutoSteps test;
 @Override
 public void robotInit() {
  /* What's going on?
   * Depending on the robot, we will be assigning the talon's to the robot's respective ID.
   * I was sort of lazy switching id's via tuner so i just left it. We will also reset to factory default
   * as well as let the system know which are slaves, which aren't, which slaves follow which master and also
   * letting the motors on coast mode.
   */
   driver = new Joystick(0);

     leftMaster = new WPI_TalonSRX(1);
     leftPrimarySlave = new WPI_TalonSRX(2); // has magencoder
     rightMaster = new WPI_TalonSRX(4); //has magencoder
     rightPrimarySlave = new WPI_TalonSRX(5);
     //Gyro
  
   //Left Master
     leftMaster.setNeutralMode(NeutralMode.Coast);
     leftMaster.setInverted(false);

   //Right Master
     rightMaster.setNeutralMode(NeutralMode.Coast);
     rightMaster.setInverted(false);
     rightMaster.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, // Feedback
     0,                      // PID ID
     30);
    //Left Primary Slave
     leftPrimarySlave.setNeutralMode(NeutralMode.Coast);
     leftPrimarySlave.follow(this.leftMaster);
     leftPrimarySlave.setInverted(InvertType.FollowMaster);

   //Right Primary Slave
     rightPrimarySlave.setNeutralMode(NeutralMode.Coast);
     rightPrimarySlave.follow(this.rightMaster);
     rightPrimarySlave.setInverted(InvertType.FollowMaster);
     //Magnetic Encoder
    

   autoTimer = new Timer();

   //PigeonIMU
    gyro = new PigeonIMU(rightPrimarySlave);
   final int kTimeoutMs = 30;
   gyro.setFusedHeading(0.0, kTimeoutMs);

 //Drive
 this.drive = new DifferentialDrive(leftMaster, rightMaster);
 drive.setRightSideInverted(false); //this is to confirm it being inverted.
 turn = new JoystickButton(driver, 8);
 fusionStatus = new PigeonIMU.FusionStatus();
 }

 @Override
 public void robotPeriodic() {
   //ShuffleBoard Stuff
   gyro.getFusedHeading(fusionStatus);
   drive.setSafetyEnabled(false);
   double currentAngle = fusionStatus.heading;
   SmartDashboard.putNumber("Pigeon Angle", currentAngle);

   SmartDashboard.putNumber("Positon", this.getSensorPosition());
 }




 /*
    SANDSTORM PERIOD
   Duration: 15 Seconds
 */

 @Override
 public void autonomousInit() {

   autoTimer.reset();
   autoTimer.start();
   rightMaster.getSensorCollection().setQuadraturePosition(0, 0);
   gyro.setFusedHeading(0.0, 30);
   test = AutoSteps.ONE;

 }

 @Override
 public void autonomousPeriodic() {

   double left = driver.getRawAxis(1);
   double right = driver.getRawAxis(3);
   drive.curvatureDrive(right, left, true);


  /* if(getSensorPosition() < Sensor2Feet(5)){
     drive.tankDrive(-.25, .25, false);
   }
   else{
     */
     if(getSensorPosition() >= Sensor2Feet(8) && test == AutoSteps.ONE){
       if(fusionStatus.heading > angle(Angle.LEFT, 45)){
         drive.tankDrive(0.5, 0.5, false);
       } else{
         drive.tankDrive(-left, right);
         rightMaster.getSensorCollection().setQuadraturePosition(0, 0);
         gyro.setFusedHeading(0.0, 30);
         test = AutoSteps.TWO;
       }
     } else{

     }






   //}
  
  

 }

 /*

   Teleop Period
   Duration: 2 minutes 15 seconds.

 */

 @Override
 public void teleopInit() {
 }

 @Override
 public void teleopPeriodic() {
  
   double left = driver.getRawAxis(1);
   double right = driver.getRawAxis(2);
   
   jPOV = driver.getPOV();
   boolean cBut = driver.getRawButton(1);

    if (cBut){
      align(jPOV);
    }
    else {
      drive.curvatureDrive(right, left, true);
    }

 }

  @Override
 public void testPeriodic() {
   Scheduler.getInstance().run();


 }

 public double getSensorPosition(){
   return rightMaster.getSelectedSensorPosition(0);
 }
  public double Sensor2Feet(int feet){
   int one_rev = 1375, overflow = 4125;
   double answer;

   answer = one_rev * feet;
   answer = answer - overflow;
   return answer;
 }



 public double angle(Angle x, double a){

   if(x == Angle.LEFT){
     return -a;
   }
   else if(x == Angle.RIGHT){
     return a;
   }

   return 0;


 }

 public void align(int inputAngle){

  double targetAngle, angle = -fusionStatus.heading;

  if (inputAngle == -1){
    targetAngle = 0;
  }

  else if ((inputAngle - angle) > 180){
    targetAngle = inputAngle - 360;
  }
  else {
     targetAngle = inputAngle;
  }

  double angleDif = targetAngle - angle;

  if(targetAngle > angle){
    drive.arcadeDrive(0.6, 0);
  }
  else if (targetAngle < angle){
    drive.arcadeDrive(-0.6, 0);
  }
 
  /*  if (angleDif > 10){
    drive.arcadeDrive(0, 0.5);
  }
  else if (angleDif < -10){
    drive.arcadeDrive(0, -0.5);
  }
  else drive.arcadeDrive(0, 0);
*/
}
}

