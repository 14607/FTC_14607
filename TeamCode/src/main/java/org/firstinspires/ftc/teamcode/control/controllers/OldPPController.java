package org.firstinspires.ftc.teamcode.control.controllers;

import com.qualcomm.robotcore.util.Range;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static org.firstinspires.ftc.teamcode.hardware.DriveTrain.*;
import static org.firstinspires.ftc.teamcode.control.system.Robot.*;

import org.firstinspires.ftc.teamcode.util.CurvePoint;
import org.firstinspires.ftc.teamcode.util.Point;
import static org.firstinspires.ftc.teamcode.util.MathUtil.*;

import java.util.ArrayList;


public class OldPPController {

    public static double movement_y_min = 0.091;
    public static double movement_x_min = 0.11;
    public static double movement_turn_min = 0.10;

    private static void allComponentsMinPower() {
        if(Math.abs(powers.x) > Math.abs(powers.y)){
            if(Math.abs(powers.x) > Math.abs(powers.h)){
                powers.x = minPower(powers.x,movement_x_min);
            }else{
                powers.h = minPower(powers.h,movement_turn_min);
            }
        }else{
            if(Math.abs(powers.y) > Math.abs(powers.h)){
                powers.y = minPower(powers.y, movement_y_min);
            }else{
                powers.h = minPower(powers.h,movement_turn_min);
            }
        }
    }

    public static double minPower(double val, double min){
        if(val >= 0 && val <= min){
            return min;
        }
        if(val < 0 && val > -min){
            return -min;
        }
        return val;
    }



    public static class movementResult{
        public double turnDelta_rad;
        public movementResult(double turnDelta_rad){
            this.turnDelta_rad = turnDelta_rad;
        }
    }

    private static class indexPoint {
        int index;
        Point point;
        indexPoint(int index, Point point) {
            this.index = index;
            this.point = point;
        }
    }




    public static void goToPosition(double targetX, double targetY, double moveSpeed, double prefAngle, double turnSpeed, double slowDownTurnRadians, double slowDownMovementFromTurnError, boolean stop) {
        double distance = Math.hypot(targetX - currPose.x, targetY - currPose.y);

        double absoluteAngleToTargetPoint = Math.atan2(targetY - currPose.y, targetX - currPose.x);
        double relativeAngleToTargetPoint = angleWrap(absoluteAngleToTargetPoint - (currPose.h - Math.toRadians(90)));

        double relativeXToPoint = Math.cos(relativeAngleToTargetPoint) * distance;
        double relativeYToPoint = Math.sin(relativeAngleToTargetPoint) * distance;
        double relativeAbsXToPoint = Math.abs(relativeXToPoint);
        double relativeAbsYToPoint = Math.abs(relativeYToPoint);

        double v = relativeAbsXToPoint + relativeAbsYToPoint;
        double movementXPower = relativeXToPoint / v;
        double movementYPower = relativeYToPoint / v;

        if(stop) {
            movementXPower *= relativeAbsXToPoint / 12;
            movementYPower *= relativeAbsYToPoint / 12;
        }

        powers.x = Range.clip(movementXPower, -moveSpeed, moveSpeed);
        powers.y = Range.clip(movementYPower, -moveSpeed, moveSpeed);



        // turning and smoothing shit
        double relativeTurnAngle = prefAngle - Math.toRadians(90);
        double absolutePointAngle = absoluteAngleToTargetPoint + relativeTurnAngle;
        double relativePointAngle = angleWrap(absolutePointAngle - currPose.h);

        double decelerateAngle = Math.toRadians(40);

        double movementTurnSpeed = (relativePointAngle/decelerateAngle) * turnSpeed;

        powers.h = Range.clip(movementTurnSpeed, -turnSpeed, turnSpeed);

        if(distance < 3) {
            powers.h = 0;
        }

        allComponentsMinPower();


        // smoothing
        powers.x *= Range.clip((relativeAbsXToPoint/3.0),0,1);
        powers.y *= Range.clip((relativeAbsYToPoint/3.0),0,1);
        powers.h *= Range.clip(Math.abs(relativePointAngle)/Math.toRadians(2),0,1);


        //slow down if our point angle is off
        double errorTurnSoScaleDownMovement = Range.clip(1.0-Math.abs(relativePointAngle/slowDownTurnRadians),1.0-slowDownMovementFromTurnError,1);
        //don't slow down if we aren't trying to turn (distanceToPoint < 3)
        if(Math.abs(powers.h) < 0.00001){
            errorTurnSoScaleDownMovement = 1;
        }
        powers.x *= errorTurnSoScaleDownMovement;
        powers.y *= errorTurnSoScaleDownMovement;
    }



    public static boolean followCurve(ArrayList<CurvePoint> allPoints, double followAngle){

        //now we will extend the last line so that the pointing looks smooth at the end
        ArrayList<CurvePoint> pathExtended = new ArrayList<>(allPoints);

        //first get which segment we are on
        indexPoint clippedToPath = clipToFollowPointPath(allPoints,currPose.x,currPose.y);
        int currFollowIndex = clippedToPath.index+1;

        //get the point to follow
        CurvePoint followMe = getFollowPointPath(pathExtended,currPose.x,currPose.y,
                allPoints.get(currFollowIndex).followDistance);


        // test from ehre
        CurvePoint realLastPoint = allPoints.get(allPoints.size()-1);
        //this will change the last point to be extended
        pathExtended.set(pathExtended.size()-1,
                extendLine(allPoints.get(allPoints.size()-2),allPoints.get(allPoints.size()-1),
                        allPoints.get(allPoints.size()-1).pointLength * 1.5));



        //get the point to point to
        CurvePoint pointToMe = getFollowPointPath(pathExtended,currPose.x,currPose.y,
                allPoints.get(currFollowIndex).pointLength);



        //if we are nearing the end (less than the follow dist amount to go) just manualControl point to end
        //but only if we have passed through the correct points beforehand
        double clipedDistToFinalEnd = Math.hypot(
                clippedToPath.point.x-allPoints.get(allPoints.size()-1).x,
                clippedToPath.point.y-allPoints.get(allPoints.size()-1).y);



        if(clipedDistToFinalEnd <= followMe.followDistance + 6 ||
                Math.hypot(currPose.x-allPoints.get(allPoints.size()-1).x,
                        currPose.y-allPoints.get(allPoints.size()-1).y) < followMe.followDistance + 6){

            followMe.setPoint(allPoints.get(allPoints.size()-1).toPoint());
        }





        goToPosition(followMe.x, followMe.y,followAngle,
                followMe.moveSpeed,followMe.turnSpeed,
                followMe.slowDownTurnRadians,0,true);

        //find the angle to that point using atan2
        double currFollowAngle = Math.atan2(pointToMe.y-currPose.y,pointToMe.x-currPose.x);

        //if our follow angle is different, point differently
        currFollowAngle += angleWrap(followAngle - Math.toRadians(90));

        movementResult result = pointAngle(currFollowAngle,allPoints.get(currFollowIndex).turnSpeed,Math.toRadians(45));
        powers.x *= 1 - Range.clip(Math.abs(result.turnDelta_rad) / followMe.slowDownTurnRadians,0,followMe.slowDownTurnAmount);
        powers.y *= 1 - Range.clip(Math.abs(result.turnDelta_rad) / followMe.slowDownTurnRadians,0,followMe.slowDownTurnAmount);



        return clipedDistToFinalEnd < 2;// 4
    }


    public static movementResult pointAngle(double point_angle, double point_speed, double decelerationRadians) {
        //now that we know what absolute angle to point to, we calculate how close we are to it
        double relativePointAngle = angleWrap(point_angle-currPose.h);

        //Scale down the relative angle by 40 and multiply by point speed
        double turnSpeed = (relativePointAngle/decelerationRadians)*point_speed;
        //now just clip the result to be in range
        powers.h = Range.clip(turnSpeed,-point_speed,point_speed);

        //make sure the largest component doesn't fall below it's minimum power
        allComponentsMinPower();

        //smooths down the last bit to finally settle on an angle
        powers.h *= Range.clip(Math.abs(relativePointAngle)/Math.toRadians(3),0,1);

        return new movementResult(relativePointAngle);
    }


    private static CurvePoint extendLine(CurvePoint firstPoint, CurvePoint secondPoint, double distance) {

        double lineAngle = Math.atan2(secondPoint.y - firstPoint.y,secondPoint.x - firstPoint.x);
        double lineLength = Math.hypot(secondPoint.x - firstPoint.x,secondPoint.y - firstPoint.y);
        double extendedLineLength = lineLength + distance;

        CurvePoint extended = new CurvePoint(secondPoint);
        extended.x = Math.cos(lineAngle) * extendedLineLength + firstPoint.x;
        extended.y = Math.sin(lineAngle) * extendedLineLength + firstPoint.y;
        return extended;
    }



    // finds currPoint (startPoint of curr segment) on the current path
    public static indexPoint clipToFollowPointPath(ArrayList<CurvePoint> pathPoints, double xPos, double yPos) {
        double closestClip = 1000000000;

        // index of first point on line clipped
        int closestClippedIndex = 0;

        Point clipPoint = new Point();

        for(int i=0; i<pathPoints.size()-1; i++) {
            CurvePoint startPoint = pathPoints.get(i);
            CurvePoint endPoint = pathPoints.get(i+1);

            Point tempClipPoint = clipToLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, xPos, yPos);

            double distance = Math.hypot(xPos - tempClipPoint.x, yPos - tempClipPoint.y);

            if(distance < closestClip) {
                closestClip = distance;
                closestClippedIndex = i;
                clipPoint = tempClipPoint;
            }
        }
        return new indexPoint(closestClippedIndex, new Point(clipPoint.x, clipPoint.y));
    }

    public static CurvePoint getFollowPointPath(ArrayList<CurvePoint> pathPoints, double xPos, double yPos, double followRadius) {
        indexPoint clipped = clipToFollowPointPath(pathPoints, xPos, yPos);
        int currIndex = clipped.index;

        CurvePoint followMe = new CurvePoint(pathPoints.get(currIndex+1));
        //by default go to the follow point
        followMe.setPoint(new Point(clipped.point.x,clipped.point.y));


        for(int i=0; i<pathPoints.size()-1; i++) {
            CurvePoint startPoint = pathPoints.get(i);
            CurvePoint endPoint = pathPoints.get(i+1);

            ArrayList<Point> intersections = lineCircleIntersection(xPos, yPos, followRadius, startPoint.x, startPoint.y, endPoint.x, endPoint.y);

            double closestDistance = 10000000; // placeholder numb
            for(Point thisIntersection : intersections) {


                double dist = Math.hypot(thisIntersection.x - pathPoints.get(pathPoints.size()-1).x,
                        thisIntersection.y - pathPoints.get(pathPoints.size()-1).y);

                //follow if the distance to the last point is less than the closestDistance
                if(dist < closestDistance){
                    closestDistance = dist;
                    followMe.setPoint(thisIntersection);//set the point to the intersection
                }
            }
        }

        return followMe;
    }



    public static Point clipToLine(double lineX1, double lineY1, double lineX2, double lineY2,
                                   double robotX, double robotY){
        if(lineX1 == lineX2){
            lineX1 = lineX2 + 0.01;
        }
        if(lineY1 == lineY2){
            lineY1 = lineY2 + 0.01;
        }

        //calculate the slope of the line
        double m1 = (lineY2 - lineY1)/(lineX2 - lineX1);
        //calculate the slope perpendicular to this line
        double m2 = (lineX1 - lineX2)/(lineY2 - lineY1);

        //clip the robot's position to be on the line
        double xClipedToLine = ((-m2*robotX) + robotY + (m1 * lineX1) - lineY1)/(m1-m2);
        double yClipedToLine = (m1 * (xClipedToLine - lineX1)) + lineY1;
        return new Point(xClipedToLine,yClipedToLine);
    }



    public static ArrayList<CurvePoint> reversePath(ArrayList<CurvePoint> pathPoints, double xComp, double yComp) {
        ArrayList<CurvePoint> allPoints = new ArrayList<>();
        allPoints.add(new CurvePoint(currPose.x, currPose.y, 0, 0, 0, 0, 0, 0));

        for(int i=pathPoints.size()-2; i>0; i--) {
            allPoints.add(pathPoints.get(i));
        }
        CurvePoint lastPoint = new CurvePoint(pathPoints.get(0).x + xComp, pathPoints.get(0).y + yComp,
                0.4, 0.6, 20, 15, Math.toRadians(30), 0.6);
        allPoints.add(lastPoint);

        return allPoints;
    }

    public static ArrayList<Point> lineCircleIntersection(double circleX, double circleY, double r,
                                                          double lineX1, double lineY1,
                                                          double lineX2, double lineY2){
        //make sure the points don't exactly line up so the slopes work
        if(Math.abs(lineY1- lineY2) < 0.003){
            lineY1 = lineY2 + 0.003;
        }
        if(Math.abs(lineX1- lineX2) < 0.003){
            lineX1 = lineX2 + 0.003;
        }

        //calculate the slope of the line
        double m1 = (lineY2 - lineY1)/(lineX2-lineX1);

        //the first coefficient in the quadratic
        double quadraticA = 1.0 + pow(m1,2);

        //shift one of the line's points so it is relative to the circle
        double x1 = lineX1-circleX;
        double y1 = lineY1-circleY;


        //the second coefficient in the quadratic
        double quadraticB = (2.0 * m1 * y1) - (2.0 * pow(m1,2) * x1);

        //the third coefficient in the quadratic
        double quadraticC = ((pow(m1,2)*pow(x1,2)) - (2.0*y1*m1*x1) + pow(y1,2)-pow(r,2));


        ArrayList<Point> allPoints = new ArrayList<>();



        //this may give an error so we use a try catch
        try{
            //now solve the quadratic equation given the coefficients
            double xRoot1 = (-quadraticB + sqrt(pow(quadraticB,2) - (4.0 * quadraticA * quadraticC)))/(2.0*quadraticA);

            //we know the line equation so plug into that to get root
            double yRoot1 = m1 * (xRoot1 - x1) + y1;


            //now we can add back in translations
            xRoot1 += circleX;
            yRoot1 += circleY;

            //make sure it was within range of the segment
            double minX = Math.min(lineX1, lineX2);
            double maxX = Math.max(lineX1, lineX2);
            if(xRoot1 > minX && xRoot1 < maxX){
                allPoints.add(new Point(xRoot1,yRoot1));
            }

            //do the same for the other root
            double xRoot2 = (-quadraticB - sqrt(pow(quadraticB,2) - (4.0 * quadraticA * quadraticC)))/(2.0*quadraticA);

            double yRoot2 = m1 * (xRoot2 - x1) + y1;
            //now we can add back in translations
            xRoot2 += circleX;
            yRoot2 += circleY;

            //make sure it was within range of the segment
            if(xRoot2 > minX && xRoot2 < maxX){
                allPoints.add(new Point(xRoot2,yRoot2));
            }
        }catch(Exception e){
            //if there are no roots
        }
        return allPoints;
    }

}