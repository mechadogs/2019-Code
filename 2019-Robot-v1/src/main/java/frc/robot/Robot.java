/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.GyroBase;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.Sendable;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.Spark;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends IterativeRobot {
  
  //Auto
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  enum AutoStage {
    ONE, TWO, THREE, FOUR
  }
  AutoStage currentStage;
 

  //Sensors
  private GyroBase gyro = new ADXRS450_Gyro();
    //private PIDController gyroMagic = new PIDController(I, D, E, K, gyro.pidGet(), gyroMagic);
  private CameraServer cam0 = CameraServer.getInstance();

  //Motors
  private Spark mFlipper = new Spark(2);
  private Spark mArm = new Spark(3);

  //Drive
  private DifferentialDrive driveTrain = new DifferentialDrive(new Spark(0), new Spark(1));
  private Joystick ps1 = new Joystick(0);
  private Joystick ps2 = new Joystick(1);

  //Data Variables
  double lYAxis, rYAxis, lXAxis, rXAxis, rTrigger, lTrigger, armVal, angle;
  boolean xBut, cBut, sBut, tBut, r1But, r2But, l1But, l2But;
  int jPOV;

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
    gyro.reset();
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {

    //THIS IS SO COOL OMFG

    angle = gyro.getAngle();
    SmartDashboard.putNumber("Angle", angle);

  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {

    currentStage = AutoStage.ONE;

    m_autoSelected = m_chooser.getSelected();
    // autoSelected = SmartDashboard.getString("Auto Selector",
    // defaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      
      case kCustomAuto:
        // Put custom auto code here
      switch (currentStage){
        
        case ONE:

        if(false){

        }

        else{
        currentStage = AutoStage.TWO;
        }

        break;
        
        case TWO:

        break;

        case THREE:

        break;

        case FOUR:
        
        break;

      }

        break;
      
        case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {

    lYAxis = - ps1.getRawAxis(1); //check these
    lXAxis = ps1.getRawAxis(0);
    rYAxis = - ps1.getRawAxis(5);
    rXAxis = ps1.getRawAxis(2);
        
    cBut = ps1.getRawButton(3);
    tBut = ps1.getRawButton(4);
    r1But = ps1.getRawButton(5);
    l1But = ps1.getRawButton(6);
    jPOV = ps1.getPOV();

    

    if (cBut){
      align(jPOV);
    }
    else {
      driveTrain.curvatureDrive(lYAxis, rXAxis, l1But);
    }

    flipper();
    arm();

  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }

  public void flipper(){
    
    xBut = ps1.getRawButton(2);
    sBut = ps1.getRawButton(1);
    
    if (xBut){
      mFlipper.set(0.45);
    }
    else if (sBut){
      mFlipper.set(-0.35);
    }
    else {
      mFlipper.set(0);
    }
  }

  public void arm(){

    lTrigger = ps1.getRawAxis(3);
    rTrigger = ps1.getRawAxis(4);
    
    armVal = lTrigger - rTrigger;

    if (armVal < 0.25 && armVal > -0.25){
      mArm.set(0);
    }
    else mArm.set(armVal);

  }

  public void align(int inputAngle){

    double targetAngle;

    if ((inputAngle - angle) > 180){
      targetAngle = inputAngle - 360;
    }
    else {
       targetAngle = inputAngle;
    }

    double angleDif = targetAngle - angle;
   
    if (angleDif > 10){
      driveTrain.arcadeDrive(0, 0.5);
    }
    else if (angleDif > 5){
      driveTrain.arcadeDrive(0, 0.4);
    }

    if (angleDif < -10){
      driveTrain.arcadeDrive(0, -0.5);
    }
    else if (angleDif < -5){
      driveTrain.arcadeDrive(0, -0.4);
    }
   
    /* if(angleDif > 90){
      driveTrain.arcadeDrive(0, 0.9);
    }
    else if (angleDif < -90){
      driveTrain.arcadeDrive(0, -0.9);
    }
    else if (angleDif > -5 && angleDif < 5){
      driveTrain.arcadeDrive(0, 0);
    }
    else if (angleDif < 45 && angleDif > 5){
      driveTrain.arcadeDrive(0, 0.4);
    }
    else if(angleDif < -5 && angleDif > -45){
      driveTrain.arcadeDrive(0, -0.4);
    }
    else {
      driveTrain.arcadeDrive(0, turnFactor);
    }
    */
  }

  
}
