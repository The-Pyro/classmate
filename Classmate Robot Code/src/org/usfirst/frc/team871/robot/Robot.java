
package org.usfirst.frc.team871.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import org.usfirst.frc.team871.tools.EnhancedController;
import org.usfirst.frc.team871.tools.EnhancedController.Axis;
import org.usfirst.frc.team871.tools.EnhancedController.Button;
import org.usfirst.frc.team871.tools.EnhancedController.ButtonType;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.Relay.Value;


public class Robot extends IterativeRobot {
    final String defaultAuto = "Default";
    final String customAuto = "My Auto";
    String autoSelected;
    SendableChooser chooser;
    
    final byte numMotors = 2;
    
    NetworkTable dashBoard;
    
    Victor[] motors;
    
    Relay relay;
    
    EnhancedController joystick1, joystick2;
    
    boolean updateMotorPrev;
    
    boolean[] defaultMotor;
    
    double[] motorStates;
    
    double[] motorSpeeds;
    
    
    public void robotInit() {
        chooser = new SendableChooser();
        chooser.addDefault("Default Auto", defaultAuto);
        chooser.addObject("My Auto", customAuto);
        SmartDashboard.putData("Auto choices", chooser);
        
        dashBoard = NetworkTable.getTable("SmartDashboard");
        
        motors = new Victor[numMotors];
        for (byte i = 0; i < motors.length; i++)
        	motors[i] = new Victor(i);
        motorStates = new double[numMotors];
        for (double b : motorStates)
        	b = 0.0;
        motorSpeeds = new double[numMotors];
        for (double d : motorSpeeds)
        	d = 0.0;
                
        relay = new Relay(0);
        
        joystick1 = new EnhancedController(0);
        joystick1.setButtonMode(Button.X, ButtonType.MOMENTARY);
        joystick1.setButtonMode(Button.A, ButtonType.RISING);
        joystick2 = new EnhancedController(1);
        joystick2.setButtonMode(Button.X, ButtonType.MOMENTARY);
        joystick2.setButtonMode(Button.A, ButtonType.RISING);
        
        updateMotorPrev = false;
        
    }
    
    public void autonomousInit() {
    	autoSelected = (String) chooser.getSelected();
//		autoSelected = SmartDashboard.getString("Auto Selector", defaultAuto);
		System.out.println("Auto selected: " + autoSelected);
    }

    
    public void autonomousPeriodic() {
    	switch(autoSelected) {
    	case customAuto:
        //Put custom auto code here   
            break;
    	case defaultAuto:
    	default:
    	//Put default auto code here
            break;
    	}
    }

    public void teleopPeriodic() {
    	boolean updateMotor = dashBoard.getBoolean("updateMotor", false);
    	boolean update = (joystick1.getValue(Button.A) || joystick2.getValue(Button.A));
    	if ((updateMotor && (updateMotor != updateMotorPrev)) || update) {
    		motorStates = dashBoard.getNumberArray("motorControls", new double[numMotors]);
    		boolean[] inverts = dashBoard.getBooleanArray("motorInverts", new boolean[numMotors]);
    		for (byte i = 0; i < motors.length; i++) 
    			motors[i].setInverted(inverts[i]);    		
    		joystick1.setAxisDeadband(Axis.LEFTY, dashBoard.getNumber("deadband", 0.0));
    		joystick2.setAxisDeadband(Axis.LEFTY, dashBoard.getNumber("deadband", 0.0));
    		updateMotorPrev = (update || updateMotor);
    	} else
    		updateMotorPrev = (update || updateMotor);
    	 
        
    	
        double speed1 = joystick1.getValue(Axis.LEFTY);
        double speed2 = joystick2.getValue(Axis.LEFTY);
        
        for (byte i = 0; i < motors.length; i++) {
        	switch ((byte)motorStates[i]){
        	case 0:
        		motors[i].set(0.0);
        		motorSpeeds[i] = 0.0;
        		break;
        		
        	case 1:
        		if (joystick1.getValue(Button.X)) {
        			motors[i].set(speed1/2);
        			motorSpeeds[i] = (motors[i].getInverted()) ? -speed1/2 : speed1/2;
        		} else {
        			motors[i].set(speed1);
        			motorSpeeds[i] = (motors[i].getInverted()) ? -speed1 : speed1;
        		}
        		break;
        		
        	case 2:
        		if (joystick2.getValue(Button.X)) {
        			motors[i].set(speed2/2);
        			motorSpeeds[i] = (motors[i].getInverted()) ? -speed2/2 : speed2/2;
        		} else {
        			motors[i].set(speed2);
        			motorSpeeds[i] = (motors[i].getInverted()) ? -speed2 : speed2;
        		}
        		break;
        		
        	default:
        		motors[i].set(0.0);
        		motorSpeeds[i] = 0.0;
        		break;
           	}
        }
        
        dashBoard.putNumberArray("motorVals", motorSpeeds);
        
        
        switch ((int)dashBoard.getNumber("relay", 0.0)) {
        case 0:
        	relay.set(Value.kOff);
        	break;
        case 1:
        	relay.set(Value.kOn);
        	break;
        case 2: 
        	relay.set(Value.kForward);
        	break;
        case 3:
        	relay.set(Value.kReverse);
        	break;
        default:
        	relay.set(Value.kOff);
        	break;
        }
    }
}
