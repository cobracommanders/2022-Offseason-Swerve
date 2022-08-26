package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.revrobotics.ColorSensorV3;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import static frc.robot.Constants.HopperConstants.*;

public class Hopper extends SubsystemBase {
	private final TalonSRX hopperFront = new TalonSRX(kFrontHopperID);
	private final TalonSRX hopperBack = new TalonSRX(kBackHopperID);
	private final ColorSensorV3 colorSensor = new ColorSensorV3(null);

	private HopperState currentState = HopperState.IDLE;
	private Alliance alliance = Alliance.Invalid;
	//TODO: Add Limelight Program in here or make Limelight file
	//TODO: implement with limit switches
	public static int currentBallCount;

	public enum HopperState {
		LOAD,
		OUTTAKE,
		IDLE;
	}

	public Hopper() {
		hopperBack.setInverted(false);
		hopperFront.setInverted(false);
		alliance = DriverStation.getAlliance();
	}

	@Override
	public void periodic() {
		applyState(currentState);
	}

	public void setState(HopperState newState) {
		currentState = newState;
	}

	public boolean isCargoInHopperBottom() {
		return (colorSensor.getProximity() / 2047) > kProximitySensorLeniency;
	}

	public void isCargoInHopperTop() {
		return;
	}

	public boolean isCargoCorrectColor() {
		int r = colorSensor.getRed();
		int g = colorSensor.getGreen();
		int b = colorSensor.getBlue();
		int mag = r + g + b;
		double blue = b / mag;
		double red = r / mag;

		return ((blue > kColorSensorLeniency && alliance == Alliance.Blue)
				|| ((red > kColorSensorLeniency && alliance == Alliance.Red)));
	}

	private void applyState(HopperState state) {
		switch (state) {
			// Set both hopper motors to pull the ball upwards
			case LOAD:
				hopperFront.set(ControlMode.PercentOutput, kHopperSpeed);
				hopperBack.set(ControlMode.PercentOutput, kHopperSpeed);
				break;

			// Set the front motor upwards, and the back motor downwards to spin the top
			// ball in place and eject any below it
			case OUTTAKE:
				hopperFront.set(ControlMode.PercentOutput, kHopperSpeed);
				hopperBack.set(ControlMode.PercentOutput, -kHopperSpeed);
				break;

			// Idle state, pause both motors
			case IDLE:
				hopperFront.set(ControlMode.PercentOutput, 0);
				hopperBack.set(ControlMode.PercentOutput, 0);
				break;
		}
	}
}