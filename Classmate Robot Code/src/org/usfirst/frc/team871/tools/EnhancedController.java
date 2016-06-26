package org.usfirst.frc.team871.tools;

import edu.wpi.first.wpilibj.Joystick;

/*
 * EnhancedJoystick:  A joystick with added features
 * This joystick supports advanced features such as button debouncing, toggling, and rising/falling edge detection and axis features 
 * such as deadbanding and combined triggers.
 */

public class EnhancedController extends Joystick {
	boolean[][] prevState;	//Each element is an array containing the emulated and actual previous states of the button.
	int[] POVs;				//Each element is a POV (joypad) on the controller.
	double[] axisDeadband;	//Each element is the deadband value of the corresponding joystick axis.
	ButtonType[] buttonMode;//Each element is the emulated type of the corresponding button.
	
	public enum ButtonType {
		MOMENTARY,			//Momentary button; returns the raw value of the button every call.
		TOGGLE,				//Emulates a toggle button; each press (debounced) inverts the state.
		RISING,				//Rising edge detector; returns true when the pin goes high after previously being low.
		FALLING				//Falling edge; returns true when the pin goes low after previously being high.
	}
	
	public enum Button {	//All XBox buttons mapped to their indices for getRawButton.
		A(1), B(2), X(3), Y(4), 
		LBUMPER(5), RBUMPER(6), 
		BACK(7), START(8),
		LEFTSTICK(9), RIGHTSTICK(10);
		
		private int value;
		
		Button(int num) {
			value = num;
		}
				
		int getValue() {
			return value;
		}
	}
	
	public enum Axis {		//All the XBox joystick axes mapped to their indices for getRawAxis.
		LEFTX(0), LEFTY(1), 
		LTRIGGER(2), RTRIGGER(3), 
		RIGHTX(4), RIGHTY(5),
		TRIGGER(6);			//Emulates previous years in which both triggers were on one axis
		
		private int value;
		
		Axis(int num) {
			value = num;
		}
		
		int getValue() {
			return value;
		}
	}
	
	public enum Joypad {	//The flat joystick pad.  XBox controllers only have one.
		LJOYPAD(1);
		
		private int value;
		
		Joypad(int num) {
			value = num;
		}
		
		int getValue() {
			return value;
		}
	}
	
	
	public EnhancedController(int port) {
		//Constructor; initializes everything to zero or false and creates a new Joystick on the specified port
		super(port);

		prevState = new boolean[Button.values().length][2];
		for (boolean[] b : prevState)
			b = new boolean[] {false, false};
		
		POVs = new int[Joypad.values().length];
		for (int i : POVs)
			i = 0;
		
		axisDeadband = new double[Axis.values().length];		
		for(double d : axisDeadband)
			d = 0.0;
		
		buttonMode = new ButtonType[Button.values().length];
		for (ButtonType b : buttonMode) 
			b = ButtonType.MOMENTARY;		
	}
	
	/*
	 * Setters
	 */

	public void setButtonMode(Button button, ButtonType mode) {	//Sets the emulated type of the corresponding button.
		buttonMode[button.getValue() - 1] = mode;
	}

	public void setAxisDeadband(Axis axis, double dead) {		//Sets the deadband of the corresponding button.
		axisDeadband[axis.getValue()] = ((dead >= 0) && (dead < 1)) ? dead : axisDeadband[axis.getValue()];
	}
	
	/*
	 * Getters (Overloaded)
	 */
	
	public boolean getValue(Button button) {
		//Gets the value of the button, taking into account its emulated type.
		int b = button.getValue();
		boolean curVal = super.getRawButton(b);
		ButtonType mode = buttonMode[b - 1];
		
		switch (mode) {
		case MOMENTARY:										//Momentary button, returns raw input.
			return curVal;

		case TOGGLE:										//Toggle button emulator.
			return (this.getDebouncedButton(button)) ? (prevState[b][0] = !prevState[b][0]) : prevState[b][0];

		case RISING:										//Rising edge detector.
			if (curVal && (curVal != prevState[b][0])){
				prevState[b][0] = curVal;
				return true;
			} else {
				prevState[b][0] = curVal;
				return false;
			}

		case FALLING:										//Falling edge detector.
			if (!curVal && (curVal != prevState[b][0])) {
				prevState[b][0] = curVal;
				return true;
			} else {
				prevState[b][0] = curVal;
				return false;
			}

		default:
			return false;
		}
	}
	
	public double getValue(Axis axis) {
		/*
		 * Gets the value of the joystick axis after deadbanding.  Uses the following equation to create a linear graph mapping
		 * deadbanded joystick data to output: y=((x-1)/(1-d))+1.  This means that if the axis is at or below the deadband, it will return
		 * 0.0, but after that it begins increasing from 0.0, but with a steeper sloping line so that it at the deadband it is 0.0, but at 
		 * full forward it is 1.0.  This prevents the robot from jerking and gives the driver more control.
		 * The following may be used for parabolic mapping: y=(((x-d)^2)/((1-d)^2)).
		 */
		double raw = -super.getRawAxis(axis.getValue()); 
		double dead = axisDeadband[axis.getValue()];
		
		if (dead == 0.0)
			return raw;
		
		if (!((raw <= -dead) || (raw >= dead))) 
			return 0.0;
		
		if (axis == Axis.TRIGGER)							//Emulates previous years in which both triggers were one one axis
			return (this.getValue(Axis.RTRIGGER) - this.getValue(Axis.RTRIGGER));
		
		return (raw <= 0) ? (-(((Math.abs(raw) - 1) / (1 - dead)) + 1)) : (((raw - 1) / (1 - dead)) + 1);
	}
	
	public int getValue(Joypad pad) {
		/*
		 * Gets the joypad value.  Returns an int representing the angle in degrees about the top of the pad of the position 
		 * of the direction pressed.  Will only ever be multiples of 45 or -1 if nothing is being pressed.
		 */
		return super.getPOV(pad.getValue());
	}
		
	public boolean getRawValue(Button button) {
		//Same as calling joystick.getRawButton(number), but is overloaded and uses the Button enum
		return super.getRawButton(button.getValue());
	}
	
	public double getRawValue(Axis axis) {
		//Same as calling joystick.getRawAxis(number), but is overloaded and used the Axis enum
		return super.getRawAxis(axis.getValue());
	}
	
	public boolean getDebouncedButton(Button button) {
		//Returns true when the button is rising, regardless of emulated type.
		boolean v = super.getRawButton(button.getValue());
		int b = button.getValue() - 1;
		
		if (v && (v != prevState[b][1])){
			prevState[b][1] = v;
			return true;
		} else {
			prevState[b][1] = v;
			return false;
		}
	}
	
}
