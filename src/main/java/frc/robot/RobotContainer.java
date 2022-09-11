package frc.robot;

import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.commands.FieldOrientedDrive;
import frc.robot.commands.ResetGyro;
import frc.robot.commands.RobotOrientedDrive;
import frc.robot.commands.SetCenterer;
import frc.robot.commands.SetHopper;
import frc.robot.commands.SetIntake;
import frc.robot.commands.SetShooter;
import frc.robot.commands.auto.SimpleTaxi;
import frc.robot.subsystems.Centerer;
import frc.robot.subsystems.Drivetrain;
import frc.robot.subsystems.Hopper;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Shooter;
import frc.robot.subsystems.Centerer.CentererState;
import frc.robot.subsystems.Hopper.HopperState;
import frc.robot.subsystems.Intake.IntakeState;
import frc.robot.subsystems.Shooter.ShooterState;

import static frc.robot.Constants.OIConstants.*;

public class RobotContainer {
	private final Centerer centerer = new Centerer();
	private final Drivetrain drivetrain = new Drivetrain();
	private final Hopper hopper = new Hopper();
	private final Intake intake = new Intake();
	private final Shooter shooter = new Shooter();
	// private final Wrist wrist = new Wrist();

	private final XboxController driverController = new XboxController(kDriverControllerID);
	private final XboxController operatorController = new XboxController(kOperatorControllerID);

	public RobotContainer() {
		configureButtonBindings();

		drivetrain.setDefaultCommand(
				new FieldOrientedDrive(
						drivetrain,
						// Forwards/backwards translation
						() -> deadzone(((-driverController.getLeftY() * 2.25) * (-driverController.getLeftY() * 2.25))
								* -driverController.getLeftY(), 0.1),
						// Left/right translation
						() -> deadzone(((-driverController.getLeftX() * 2.25) * (-driverController.getLeftX() * 2.25))
								* -driverController.getLeftX(), 0.1),
						// Rotation
						() -> deadzone(((-driverController.getRightX() * 2.25) * (-driverController.getRightX() * 2.25))
								* -driverController.getRightX(), 0.1)));
	}

	private void configureButtonBindings() {
		// TODO: Add gyro offset for auto
		new JoystickButton(driverController, kGyroResetButton).whenPressed(new ResetGyro(drivetrain));
		new JoystickButton(driverController, kRobotOrientedButton).whileHeld(
				new RobotOrientedDrive(
						drivetrain,
						// Forwards/backwards translation
						() -> deadzone(((-driverController.getLeftY() * 2.25) * (-driverController.getLeftY() * 2.25))
								* -driverController.getLeftY(), 0.1),
						// Left/right translation
						() -> deadzone(((-driverController.getLeftX() * 2.25) * (-driverController.getLeftX() * 2.25))
								* -driverController.getLeftX(), 0.1),
						// Rotation
						() -> deadzone(((-driverController.getRightX() * 2.25) * (-driverController.getRightX() * 2.25))
								* -driverController.getRightX(), 0.1)));

		new JoystickButton(driverController, kSlowSpeedDrive).whileHeld(
				new FieldOrientedDrive(
						drivetrain,
						// Forwards/backwards translation
						() -> deadzone(-driverController.getLeftY() * 1, 0.1),
						// Left/right translation
						() -> deadzone(-driverController.getLeftX() * 1, 0.1),
						// Rotation
						() -> deadzone(-driverController.getRightX() * 1, 0.1)));

		new Trigger(() -> {
			return driverController.getRawAxis(3) > .75;
		}).whileActiveContinuous(new SetIntake(intake, IntakeState.INTAKE).alongWith(new SetCenterer(centerer, CentererState.CENTER)));

		new JoystickButton(driverController, 3).whileHeld(new SetHopper(hopper, HopperState.LOAD));
		new JoystickButton(driverController, 4).whileHeld(new SetHopper(hopper, HopperState.OUTTAKE));
		new JoystickButton(driverController, 2).whileHeld(new SetShooter(shooter, ShooterState.SHOOT));
	}

	public Command getAutoCommand() {
		return new SimpleTaxi(drivetrain);
	}

	private double deadzone(double input, double deadzone) {
		if (Math.abs(input) > deadzone)
			return input;
		return 0;
	}
}
