// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Intake.IntakeState;

public class SetIntake extends CommandBase {
	private final Intake intake;
	private final IntakeState state;

	public SetIntake(Intake intake, IntakeState state) {
		this.intake = intake;
		this.state = state;
		addRequirements(this.intake);
	}

	@Override
	public void initialize() {
		intake.setState(state);
	}

	@Override
	public void execute() {
	}

	@Override
	public void end(boolean interrupted) {
		intake.setState(IntakeState.IDLE);
	}

	@Override
	public boolean isFinished() {
		return false;
	}
}
