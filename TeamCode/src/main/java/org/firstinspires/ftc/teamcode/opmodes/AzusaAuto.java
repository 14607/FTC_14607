package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.control.path.Path;
import org.firstinspires.ftc.teamcode.control.path.PathPoints;
import org.firstinspires.ftc.teamcode.control.path.builders.PathBuilder;
import org.firstinspires.ftc.teamcode.control.system.BaseAuto;
import org.firstinspires.ftc.teamcode.util.Pose;

import java.util.LinkedList;

import static org.firstinspires.ftc.teamcode.control.path.PathPoints.*;

@Autonomous
public class AzusaAuto extends BaseAuto {
    @Override
    public Pose startPose() {
        return new Pose(-64, -64, Math.PI / 2);
    }

    @Override
    public Path path() {
        PathBuilder exp = new PathBuilder("lCurve exp")
                .addPoint(new PathPoints.BasePathPoint("start", -64,-64,0))
                .addPoint(new PathPoints.BasePathPoint("fw", -64, -34, 14))
                .addPoint(new PathPoints.BasePathPoint("joint 1", -46, -10, 14))
                .addPoint(new PathPoints.BasePathPoint("joint 2", -16, 8, 14))
                .addPoint(new PathPoints.BasePathPoint("track 1", 16, 12, 14))
                .addPoint(new PathPoints.StopPathPoint("track 2", 56, 12, 0, 14));
        return exp.build();
    }
}