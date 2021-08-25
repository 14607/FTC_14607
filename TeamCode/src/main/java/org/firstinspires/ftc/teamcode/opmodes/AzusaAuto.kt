package org.firstinspires.ftc.teamcode.opmodes

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import org.firstinspires.ftc.teamcode.control.path.Path
import org.firstinspires.ftc.teamcode.control.path.builders.PathBuilder
import org.firstinspires.ftc.teamcode.control.path.waypoints.StopWaypoint
import org.firstinspires.ftc.teamcode.control.path.waypoints.Waypoint
import org.firstinspires.ftc.teamcode.control.system.BaseAuto
import org.firstinspires.ftc.teamcode.util.debug.Debuggable
import org.firstinspires.ftc.teamcode.util.math.Angle
import org.firstinspires.ftc.teamcode.util.math.AngleUnit
import org.firstinspires.ftc.teamcode.util.math.Point
import org.firstinspires.ftc.teamcode.util.math.Pose
import kotlin.math.PI

@Debuggable
@Autonomous
class AzusaAuto : BaseAuto() {
    override fun startPose(): Pose = Pose(Point(38.0, 58.0), Angle(PI, AngleUnit.RAD))

    override fun initialPath(): Path {
        return PathBuilder().addPoint(Waypoint(38.0, 58.0, 0.0))
            .addPoint(Waypoint(15.0, 54.0, 16.0))
            .addPoint(Waypoint(-8.0, 32.0, 16.0))
            .addPoint(Waypoint(-12.0, 10.0, 16.0))
            .addPoint(Waypoint(-14.0, -8.0, 16.0))
            .addPoint(Waypoint(0.0, -10.0, 16.0))
            .addPoint(Waypoint(12.0, -2.0, 16.0))
            .addPoint(Waypoint(16.0, 6.0, 16.0))
            .addPoint(Waypoint(12.0, 16.0, 16.0))
            .addPoint(Waypoint(0.0, 28.0, 16.0))
            .addPoint(Waypoint(6.0, 42.0, 16.0))
            .addPoint(StopWaypoint(28.0, 54.0, 16.0, Angle(PI, AngleUnit.RAD))).build()
    }
}
