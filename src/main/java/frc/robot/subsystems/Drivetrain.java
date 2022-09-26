package frc.robot.subsystems;

import static frc.robot.Constants.DrivetrainConstants.kBackLeftModuleDriveID;
import static frc.robot.Constants.DrivetrainConstants.kBackLeftModuleEncoderID;
import static frc.robot.Constants.DrivetrainConstants.kBackLeftModuleOffset;
import static frc.robot.Constants.DrivetrainConstants.kBackLeftModuleSteerID;
import static frc.robot.Constants.DrivetrainConstants.kBackRightModuleDriveID;
import static frc.robot.Constants.DrivetrainConstants.kBackRightModuleEncoderID;
import static frc.robot.Constants.DrivetrainConstants.kBackRightModuleOffset;
import static frc.robot.Constants.DrivetrainConstants.kBackRightModuleSteerID;
import static frc.robot.Constants.DrivetrainConstants.kDrivetrainTrackwidthMeters;
import static frc.robot.Constants.DrivetrainConstants.kDrivetrainWheelbaseMeters;
import static frc.robot.Constants.DrivetrainConstants.kFrontLeftModuleDriveID;
import static frc.robot.Constants.DrivetrainConstants.kFrontLeftModuleEncoderID;
import static frc.robot.Constants.DrivetrainConstants.kFrontLeftModuleOffset;
import static frc.robot.Constants.DrivetrainConstants.kFrontLeftModuleSteerID;
import static frc.robot.Constants.DrivetrainConstants.kFrontRightModuleDriveID;
import static frc.robot.Constants.DrivetrainConstants.kFrontRightModuleEncoderID;
import static frc.robot.Constants.DrivetrainConstants.kFrontRightModuleOffset;
import static frc.robot.Constants.DrivetrainConstants.kFrontRightModuleSteerID;

import com.kauailabs.navx.frc.AHRS;
import com.swervedrivespecialties.swervelib.Mk4ModuleConfiguration;
import com.swervedrivespecialties.swervelib.Mk4iSwerveModuleHelper;
import com.swervedrivespecialties.swervelib.SdsModuleConfigurations;
import com.swervedrivespecialties.swervelib.SwerveModule;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.SerialPort.Port;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Drivetrain extends SubsystemBase {
	/**
	 * The maximum voltage that will be delivered to the drive motors.
	 * <p>
	 * This can be reduced to cap the robot's maximum speed. Typically, this is
	 * useful during initial testing of the robot.
	 */
	public static final double kMaxVoltage = 12.0;
	// The formula for calculating the theoretical maximum velocity is:
	// <Motor free speed RPM> / 60 * <Drive reduction> * <Wheel diameter meters> *
	// pi
	// By default this value is setup for a Mk3 standard module using Falcon500s to
	// drive.
	// An example of this constant for a Mk4 L2 module with NEOs to drive is:
	// 5880.0 / 60.0 / SdsModuleConfigurations.MK4_L2.getDriveReduction() *
	// SdsModuleConfigurations.MK4_L2.getWheelDiameter() * Math.PI
	/**
	 * The maximum velocity of the robot in meters per second.
	 * <p>
	 * This is a measure of how fast the robot should be able to drive in a straight
	 * line.
	 */
	public static final double kMaxVelocityMetersPerSecond = 6380.0 / 60.0 *
			SdsModuleConfigurations.MK4I_L2.getDriveReduction() *
			SdsModuleConfigurations.MK4I_L2.getWheelDiameter() * Math.PI;
	/**
	 * 0
	 * The maximum angular velocity of the robot in radians per second.
	 * <p>
	 * This is a measure of how fast the robot can rotate in place.
	 */
	// Here we calculate the theoretical maximum angular velocity. You can also
	// replace this with a measured amount.
	public static final double maxAngularVelocityRadiansPerSecond = kMaxVelocityMetersPerSecond /
			Math.hypot(kDrivetrainTrackwidthMeters / 2.0, kDrivetrainWheelbaseMeters / 2.0);

	private final SwerveDriveKinematics kinematics = new SwerveDriveKinematics(
			// Front left
			new Translation2d(kDrivetrainTrackwidthMeters / 2.0, kDrivetrainWheelbaseMeters / 2.0),
			// Front right
			new Translation2d(kDrivetrainTrackwidthMeters / 2.0, -kDrivetrainWheelbaseMeters / 2.0),
			// Back left
			new Translation2d(-kDrivetrainTrackwidthMeters / 2.0, kDrivetrainWheelbaseMeters / 2.0),
			// Back right
			new Translation2d(-kDrivetrainTrackwidthMeters / 2.0, -kDrivetrainWheelbaseMeters / 2.0));

	private SwerveDriveOdometry odometry = new SwerveDriveOdometry(kinematics, getGyroAngle(),
			new Pose2d(8, 4, Rotation2d.fromDegrees(0)));

	// private final PigeonIMU pigeon = new PigeonIMU(kPigeonID);
	// private final ADXRS450_Gyro IMU = new ADXRS450_Gyro();
	public static AHRS IMU = new AHRS(Port.kUSB1);

	// These are our modules. We initialize them in the constructor.
	private final SwerveModule frontLeftModule;
	private final SwerveModule frontRightModule;
	private final SwerveModule backLeftModule;
	private final SwerveModule backRightModule;

	private ChassisSpeeds chassisSpeeds = new ChassisSpeeds(0.0, 0.0, 0.0);

	private final Mk4ModuleConfiguration configuration = new Mk4ModuleConfiguration();

	public Drivetrain() {
		//configuration.setDriveCurrentLimit(35);
		//configuration.setSteerCurrentLimit(20);

		frontLeftModule = Mk4iSwerveModuleHelper.createFalcon500(
				configuration,
				Mk4iSwerveModuleHelper.GearRatio.L2,
				kFrontLeftModuleDriveID,
				kFrontLeftModuleSteerID,
				kFrontLeftModuleEncoderID,
				// This is how much the steer encoder is offset from true zero (In our case,
				// zero is facing straight forward)
				kFrontLeftModuleOffset);

		frontRightModule = Mk4iSwerveModuleHelper.createFalcon500(
				configuration,
				Mk4iSwerveModuleHelper.GearRatio.L2,
				kFrontRightModuleDriveID,
				kFrontRightModuleSteerID,
				kFrontRightModuleEncoderID,
				kFrontRightModuleOffset);

		backLeftModule = Mk4iSwerveModuleHelper.createFalcon500(
				configuration,
				Mk4iSwerveModuleHelper.GearRatio.L2,
				kBackLeftModuleDriveID,
				kBackLeftModuleSteerID,
				kBackLeftModuleEncoderID,
				kBackLeftModuleOffset);

		backRightModule = Mk4iSwerveModuleHelper.createFalcon500(
				configuration,
				Mk4iSwerveModuleHelper.GearRatio.L2,
				kBackRightModuleDriveID,
				kBackRightModuleSteerID,
				kBackRightModuleEncoderID,
				kBackRightModuleOffset);

		System.out.println(kMaxVelocityMetersPerSecond);
	}

	public void zeroGyro() {
		IMU.reset();
	}

	public void calibrateGyro() {
		IMU.calibrate();
	}

	public Rotation2d getGyroAngle() {
		return Rotation2d.fromDegrees(-IMU.getYaw());
	}

	public void drive(ChassisSpeeds chassisSpeeds) {
		this.chassisSpeeds = chassisSpeeds;
	}

	private Field2d field = new Field2d();

	@Override
	public void periodic() {
		SmartDashboard.putNumber("Gyro", IMU.getAngle());
		SwerveModuleState[] states = kinematics.toSwerveModuleStates(chassisSpeeds);
		SwerveDriveKinematics.desaturateWheelSpeeds(states, kMaxVelocityMetersPerSecond);

		frontLeftModule.set(states[0].speedMetersPerSecond / kMaxVelocityMetersPerSecond * kMaxVoltage,
				states[0].angle.getRadians());
		frontRightModule.set(states[1].speedMetersPerSecond / kMaxVelocityMetersPerSecond * kMaxVoltage,
				states[1].angle.getRadians());
		backLeftModule.set(states[2].speedMetersPerSecond / kMaxVelocityMetersPerSecond * kMaxVoltage,
				states[2].angle.getRadians());
		backRightModule.set(states[3].speedMetersPerSecond / kMaxVelocityMetersPerSecond * kMaxVoltage,
				states[3].angle.getRadians());

		odometry.update(getGyroAngle(), states);
		field.setRobotPose(odometry.getPoseMeters());
		// SmartDashboard.putData(field);
	}
}