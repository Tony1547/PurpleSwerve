// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.drive;

import java.util.HashMap;
import java.util.List;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.path.EventMarker;
import com.pathplanner.lib.path.GoalEndState;
import com.pathplanner.lib.path.PathConstraints;
import com.pathplanner.lib.path.PathPlannerPath;
import com.pathplanner.lib.path.PathPoint;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

public class AutoTrajectory {
  DriveSubsystem m_driveSubsystem;
  Command m_swerveCommand;
  PathPlannerPath m_path;

  /**
   * Create new path trajectory using PathPlanner path
   * @param driveSubsystem DriveSubsystem to drive the robot
   * @param pathName PathPlanner path name
   * @param maxVelocity Maximum velocity of robot during path (m/s)
   * @param maxAcceleration Maximum acceleration of robot during path (m/s^2)
   */
  public AutoTrajectory(DriveSubsystem driveSubsystem, String pathName) {
    this.m_driveSubsystem = driveSubsystem;

    // Get path
    m_path = PathPlannerPath.fromPathFile(pathName);
  }

  /**
   * Creates new path trajectory using a physical x,y coordinate points
   * @param driveSubsystem DriveSubsystem required for drivetrain movement
   * @param waypoints list of x, y coordinate pairs in trajectory
   * @param reversed whether the trajectory followed should be in reverse
   * @param maxVelocity Maximum velocity of robot during path (m/s)
   * @param maxAcceleration Maximum acceleration of robot during path (m/s^2)
   */
  public AutoTrajectory(DriveSubsystem driveSubsystem, List<PathPoint> waypoints, PathConstraints pathConstraints) {
    this.m_driveSubsystem = driveSubsystem;

    // Generate path from waypoints
    m_path = PathPlannerPath.fromPathPoints(
      waypoints,
      pathConstraints,
      new GoalEndState(0.0, waypoints.get(waypoints.size() - 1).holonomicRotation)
    );
  }

  /**
   * Reset drive odometry to beginning of this path
   */
  private void resetOdometry() {
    m_driveSubsystem.resetPose(getInitalPose());
  }

  /**
   * Get markers of path
   * @return A list of markers within the path
   */
  public List<EventMarker> getEventMarkers() {
    return m_path.getEventMarkers();
  }

  /**
   * Get initial pose for path
   * @return Path initial pose
   */
  public Pose2d getInitalPose() {
    if (m_path == null) return new Pose2d();
    return new Pose2d(m_path.getPoint(0).position, m_path.getPoint(0).holonomicRotation);
  }

  /**
   * Get Ramsete command to run
   * @return Ramsete command that will stop when complete
   */
  public SequentialCommandGroup getCommandAndStop() {
    return AutoBuilder.followPathWithEvents(m_path)
           .andThen(() -> {
              m_driveSubsystem.resetTurnPID();
              m_driveSubsystem.lock();
              m_driveSubsystem.stop();
           });
  }

  /**
   * Get auto command to execute path, resetting odometry first
   * @param isFirstPath true if path is the first one in autonomous
   * @return Ramsete command that will stop when complete
   */
  public SequentialCommandGroup getCommandAndStop(boolean isFirstPath) {
    if (isFirstPath) {
      return Commands.runOnce(() -> resetOdometry())
              .andThen(AutoBuilder.followPathWithEvents(m_path))
              .andThen(() -> {
                m_driveSubsystem.resetTurnPID();
                m_driveSubsystem.lock();
                m_driveSubsystem.stop();
              });
    } else return getCommandAndStop();
  }

  /**
   * Get auto command to execute path and events along the way
   * @param eventMap Map of event marker names to the commands that should run when reaching that
   *     marker. This SHOULD NOT contain any commands requiring the same subsystems as the path
   *     following command.
   * @return Command to execute actions in autonomous
   */
  public Command getCommandAndStopWithEvents(HashMap<String, Command> eventMap) {
    return AutoBuilder.followPathWithEvents(m_path)
           .andThen(() -> {
              m_driveSubsystem.resetTurnPID();
              m_driveSubsystem.lock();
              m_driveSubsystem.stop();
            });
  }

  /**
   * Get auto command to execute path and events along the way, resetting odometry first
   * @param isFirstPath true if path is the first one in autonomous
   * @param eventMap Map of event marker names to the commands that should run when reaching that
   *     marker. This SHOULD NOT contain any commands requiring the same subsystems as the path
   *     following command.
   * @return Command to execute actions in autonomous
   */
  public Command getCommandAndStopWithEvents(boolean isFirstPath, HashMap<String, Command> eventMap) {
    if (isFirstPath) {
      return AutoBuilder.followPathWithEvents(m_path)
             .andThen(() -> {
              m_driveSubsystem.resetTurnPID();
              m_driveSubsystem.lock();
              m_driveSubsystem.stop();
            });
    } else return getCommandAndStopWithEvents(eventMap);
  }

  /**
   * Get auto command to execute path
   * @return Ramsete command that does NOT stop when complete
   */
  public Command getCommand() {
    return AutoBuilder.followPathWithEvents(m_path).andThen(() -> m_driveSubsystem.resetTurnPID());
  }

  /**
   * Get auto command to execute path, resetting odometry first
   * @param isFirstPath true if path is first one in autonomous
   * @return Ramsete command that does NOT stop when complete
   */
  public Command getCommand(boolean isFirstPath) {
    if (isFirstPath) {
      return Commands.runOnce(() -> resetOdometry())
             .andThen(AutoBuilder.followPathWithEvents(m_path))
             .andThen(() -> m_driveSubsystem.resetTurnPID());
    } else return getCommand();
  }
}