// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.drive;

import java.util.Arrays;

import com.pathplanner.lib.path.GoalEndState;
import com.pathplanner.lib.path.PathConstraints;
import com.pathplanner.lib.path.PathPlannerPath;
import com.pathplanner.lib.path.PathPoint;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;

/** PurplePath Pose */
public class PurplePathPose {
  Pose2d m_bluePose, m_redPose;
  Pose2d m_blueFinalApproachPose, m_redFinalApproachPose;
  PathPlannerPath m_blueFinalApproachPath, m_redFinalApproachPath;
  double m_finalApproachDistance;

  /**
   * Create alliance specific goal for PurplePath
   * <p>
   * MUST call {@link PurplePathPose#calculateFinalApproach(PathConstraints)} before using
   * @param bluePose Pose if blue alliance
   * @param redPose Pose if red alliance
   * @param finalApproachDistance Distance of final approach
   * @param isReversed True if robot's rear is facing object
   */
  public PurplePathPose(Pose2d bluePose, Pose2d redPose, double finalApproachDistance, boolean isReversed) {
    this.m_bluePose = bluePose;
    this.m_redPose = redPose;
    this.m_finalApproachDistance = finalApproachDistance;

    Rotation2d finalApproachDirectionOffset = isReversed ? Rotation2d.fromRadians(0.0) : Rotation2d.fromRadians(Math.PI);

    this.m_blueFinalApproachPose = new Pose2d(
      bluePose.getTranslation()
        .plus(new Translation2d(finalApproachDistance, m_bluePose.getRotation().plus(finalApproachDirectionOffset))),
      bluePose.getRotation()
    );
    this.m_redFinalApproachPose = new Pose2d(
      redPose.getTranslation()
        .plus(new Translation2d(finalApproachDistance, m_redPose.getRotation().plus(finalApproachDirectionOffset))),
      redPose.getRotation()
    );
  }

  /**
   * Create alliance specific goal for PurplePath
   * <p>
   * MUST call {@link PurplePathPose#calculateFinalApproach(PathConstraints)} before using
   * @param bluePose Pose if blue alliance
   * @param redPose Pose if red alliance
   * @param finalApproachDistance Distance of final approach
   */
  public PurplePathPose(Pose2d bluePose, Pose2d redPose, double finalApproachDistance) {
    this(bluePose, redPose, finalApproachDistance, false);
  }

  /**
   * Create shared goal for PurplePath
   * <p>
   * MUST call {@link PurplePathPose#calculateFinalApproach(PathConstraints)} before using
   * @param pose Goal pose
   * @param finalApproachDistance Distance of final approach
   * @param isReversed True if robot's rear is facing object
   */
  public PurplePathPose(Pose2d pose, double finalApproachDistance, boolean isReversed) {
    this(pose, pose, finalApproachDistance, isReversed);
  }

  /**
   * Create shared goal for PurplePath
   * <p>
   * MUST call {@link PurplePathPose#calculateFinalApproach(PathConstraints)} before using
   * @param pose Goal pose
   * @param finalApproachDistance Distance of final approach
   */
  public PurplePathPose(Pose2d pose, double finalApproachDistance) {
    this(pose, finalApproachDistance, false);
  }

  /**
   * Get final approach distance
   * @return Final approach distance
   */
  public double getFinalApproachDistance() {
    return m_finalApproachDistance;
  }

  /**
   * Calculate final approach paths
   * @param pathConstraints Path constraints to apply to final approach paths
   */
  public void calculateFinalApproach(PathConstraints pathConstraints) {
    m_blueFinalApproachPath = PathPlannerPath.fromPathPoints(
      Arrays.asList(
        new PathPoint(m_blueFinalApproachPose.getTranslation(), m_blueFinalApproachPose.getRotation()),
        new PathPoint(m_bluePose.getTranslation(), m_bluePose.getRotation())
      ),
      pathConstraints,
      new GoalEndState(0.0, m_blueFinalApproachPose.getRotation())
    );

    m_redFinalApproachPath = PathPlannerPath.fromPathPoints(
      Arrays.asList(
        new PathPoint(m_redFinalApproachPose.getTranslation(), m_redFinalApproachPose.getRotation()),
        new PathPoint(m_redPose.getTranslation(), m_redPose.getRotation())
      ),
      pathConstraints,
      new GoalEndState(0.0, m_redFinalApproachPose.getRotation())
    );
  }

  /**
   * Get goal pose for current alliance
   * @return Goal pose
   */
  public Pose2d getGoalPose() {
    if (DriverStation.getAlliance().isEmpty()) return null;
    Alliance currentAlliance = DriverStation.getAlliance().get();
    switch (currentAlliance) {
      case Red:
        return m_redPose;
      case Blue:
      default:
        return m_bluePose;
    }
  }

  /**
   * Get final approach pose for current alliance
   * @return Final approach pose
   */
  public Pose2d getFinalApproachPose() {
    if (DriverStation.getAlliance().isEmpty()) return null;
    Alliance currentAlliance = DriverStation.getAlliance().get();
    switch (currentAlliance) {
      case Red:
        return m_redFinalApproachPose;
      case Blue:
      default:
        return m_blueFinalApproachPose;
    }
  }

  /**
   * Get final approach path for current alliance
   * @return Final approach path
   */
  public PathPlannerPath getFinalApproachPath() {
    if (DriverStation.getAlliance().isEmpty()) return null;
    Alliance currentAlliance = DriverStation.getAlliance().get();
    switch (currentAlliance) {
      case Red:
        return m_redFinalApproachPath;
      case Blue:
      default:
        return m_blueFinalApproachPath;
    }
  }
}
