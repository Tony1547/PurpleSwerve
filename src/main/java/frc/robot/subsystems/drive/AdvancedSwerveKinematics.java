package frc.robot.subsystems.drive;

import static edu.wpi.first.math.Nat.N1;
import static edu.wpi.first.math.Nat.N2;
import static edu.wpi.first.math.Nat.N3;
import static edu.wpi.first.math.Nat.N4;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Twist2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N2;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.math.numbers.N4;
import frc.robot.Constants;


public class AdvancedSwerveKinematics {
  private Translation2d[] m_moduleLocations;

  /**
    * Create a SecondOrderSwerveKinematics object
    * <p>
    * Corrects for path drift when the robot is rotating
    *
    * @param wheelMeters Location of all 4 swerve modules, LF/RF/LR/RR
    */
  public AdvancedSwerveKinematics(Translation2d... wheelsMeters) {
    if (wheelsMeters.length < 2) {
      throw new IllegalArgumentException("A swerve drive requires at least two modules");
    }
    m_moduleLocations = wheelsMeters;
  }

  /**
   * Correct chassis speeds for second order kinematics
   * @param requestedSpeeds Requested chassis speeds
   * @return Corrected chassis speeds
   */
  public static ChassisSpeeds correctForDynamics(ChassisSpeeds requestedSpeeds) {
    Pose2d futureRobotPose = new Pose2d(
      requestedSpeeds.vxMetersPerSecond * Constants.Global.ROBOT_LOOP_PERIOD,
      requestedSpeeds.vyMetersPerSecond * Constants.Global.ROBOT_LOOP_PERIOD,
      Rotation2d.fromRadians(requestedSpeeds.omegaRadiansPerSecond * Constants.Global.ROBOT_LOOP_PERIOD)
    );

    Twist2d twistForPose = PoseGeometry.log(futureRobotPose);

    ChassisSpeeds correctedSpeeds = new ChassisSpeeds(
      twistForPose.dx / Constants.Global.ROBOT_LOOP_PERIOD,
      twistForPose.dy / Constants.Global.ROBOT_LOOP_PERIOD,
      twistForPose.dtheta / Constants.Global.ROBOT_LOOP_PERIOD
    );

    return correctedSpeeds;
  }

  /**
    * Convert chassis speed to states of individual modules using second order kinematics
    *
    * @param desiredSpeed desired translation and rotation speed of the robot
    * @param robotHeading heading of the robot relative to the field
    * @return Array of the speed direction of the swerve modules
    */
  public SwerveModuleState[] toSwerveModuleStates(ChassisSpeeds desiredSpeed, Rotation2d robotHeading){
    Matrix<N3, N1> firstOrderInputMatrix = new Matrix<>(N3(),N1());
    Matrix<N2, N3> firstOrderMatrix = new Matrix<>(N2(),N3());
    Matrix<N4, N1> secondOrderInputMatrix = new Matrix<>(N4(),N1());
    Matrix<N2, N4> secondOrderMatrix = new Matrix<>(N2(),N4());
    Matrix<N2, N2> rotationMatrix = new Matrix<>(N2(),N2());

    firstOrderInputMatrix.set(0, 0, desiredSpeed.vxMetersPerSecond);
    firstOrderInputMatrix.set(1, 0, desiredSpeed.vyMetersPerSecond);
    firstOrderInputMatrix.set(2, 0, desiredSpeed.omegaRadiansPerSecond);

    secondOrderInputMatrix.set(2, 0, Math.pow(desiredSpeed.omegaRadiansPerSecond, 2));

    firstOrderMatrix.set(0, 0, 1);
    firstOrderMatrix.set(1, 1, 1);

    secondOrderMatrix.set(0, 0, 1);
    secondOrderMatrix.set(1, 1, 1);

    SwerveModuleState[] swerveModuleStates = new SwerveModuleState[m_moduleLocations.length];
    double[] moduleTurnSpeeds = new double[m_moduleLocations.length];

    for (int i = 0; i < m_moduleLocations.length; i++) {
      // Angle that the module location vector makes with respect to the robot
      Rotation2d moduleAngle = new Rotation2d(Math.atan2(m_moduleLocations[i].getY(), m_moduleLocations[i].getX())); 
      // Angle that the module location vector makes with respect to the field
      Rotation2d moduleAngleFieldCentric = moduleAngle.plus(robotHeading);
      double moduleX = m_moduleLocations[i].getNorm() * Math.cos(moduleAngleFieldCentric.getRadians());
      double moduleY = m_moduleLocations[i].getNorm() * Math.sin(moduleAngleFieldCentric.getRadians());
      // -r_y
      firstOrderMatrix.set(0, 2, -moduleY);
      // +r_x 
      firstOrderMatrix.set(1, 2, +moduleX); 

      Matrix<N2, N1> firstOrderOutput = firstOrderMatrix.times(firstOrderInputMatrix);

      double moduleHeading = Math.atan2(firstOrderOutput.get(1, 0), firstOrderOutput.get(0, 0));
      double moduleSpeed = Math.sqrt(firstOrderOutput.elementPower(2).elementSum());

      secondOrderMatrix.set(0, 2, -moduleX);
      secondOrderMatrix.set(0, 3, -moduleY);
      secondOrderMatrix.set(1, 2, -moduleY);
      secondOrderMatrix.set(1, 3, +moduleX);

      rotationMatrix.set(0, 0, +Math.cos(moduleHeading));
      rotationMatrix.set(0, 1, +Math.sin(moduleHeading));
      rotationMatrix.set(1, 0, -Math.sin(moduleHeading));
      rotationMatrix.set(1, 1, +Math.cos(moduleHeading));

      Matrix<N2,N1> secondOrderOutput = rotationMatrix.times(secondOrderMatrix.times(secondOrderInputMatrix));

      swerveModuleStates[i] = new SwerveModuleState(moduleSpeed, new Rotation2d(moduleHeading).minus(robotHeading));
      moduleTurnSpeeds[i] = secondOrderOutput.get(1, 0) / moduleSpeed - desiredSpeed.omegaRadiansPerSecond;
    }

    return swerveModuleStates;
  }
}