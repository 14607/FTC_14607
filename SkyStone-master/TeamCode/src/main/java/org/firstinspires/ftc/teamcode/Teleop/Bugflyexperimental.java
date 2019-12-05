package org.firstinspires.ftc.teamcode.Teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.teamcode.HelperClasses.HouseFly;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import static java.lang.System.currentTimeMillis;
import static org.firstinspires.ftc.teamcode.HelperClasses.GLOBALS.*;

@TeleOp(name = "Big Kahuna Experimental")
public class Bugflyexperimental extends OpMode {

    /**
     * LIST OF TODOS
     * TODO: add integration with hardware class
     * TODO: add field centric drive that charlie coded
     * TODO: add more state machine stuff so its easier for the driver to use robot
     * TODO: add more stuff that makes it easier for driver to drive
     */


    private HouseFly robot;



    private long time = 0;
    private int count = 0;

    private long chime = 0;
    private int counter = 0;

    private final long toMidTime = 450;
    private final long liftTime = 200;
    private final long toBackTime = 750;

    private final long toLiftTimeTo = 400;
    private final long toBackTimeTo = 700;

    private final int liftIncrement = -200;
    private final int liftIncrementer = -500;


    private double oldSlideLeft = 0;
    private double oldSlideRight = 0;
    private double newSlideLeft = 0;
    private double newSlideRight = 0;



    @Override
    public void init() {

        robot = new HouseFly(hardwareMap);

        telemetry.addData("Status", "Initialized");
        telemetry.update();
        //Homes Outtake during Initialization
        robot.flipReady();
        robot.rotaterReady();
        robot.gripper.setPosition(gripperHome);
    }

    // run until the end of the match (driver presses STOP)
    public void loop() {



        /**
         *
         * INTAKE CONTROL
         *
         */

        double leftIntakePower = gamepad2.left_stick_y - gamepad2.left_stick_x;
        double rightIntakePower = gamepad2.left_stick_y + gamepad2.left_stick_x;
        if(Math.abs(leftIntakePower) < 0.1 || Math.abs(rightIntakePower) < 0.1) {
            robot.leftIntake.setPower(0);
            robot.rightIntake.setPower(0);
        }else {
            robot.leftIntake.setPower( 0.5 * -leftIntakePower);
            robot.rightIntake.setPower( 0.5 * -rightIntakePower);
        }


        /**
         *
         * DRIVE MOTORS CONTROL
         *
         */

        double motorPower;
        if(gamepad1.left_bumper) {
            motorPower = 0.5;
        }

        else if(gamepad1.right_bumper) {
            motorPower = 0.25;
        }

        else {
            motorPower = 0.8;
        }

        double threshold = 0.157; // deadzone
        if(Math.abs(gamepad1.left_stick_y) > threshold || Math.abs(gamepad1.left_stick_x) > threshold || Math.abs(gamepad1.right_stick_x) > threshold)
        {
            robot.frontRight.setPower(motorPower * (((-gamepad1.left_stick_y) + (gamepad1.left_stick_x)) + -gamepad1.right_stick_x));
            robot.backLeft.setPower(motorPower * (((-gamepad1.left_stick_y) + (-gamepad1.left_stick_x)) + gamepad1.right_stick_x));
            robot.frontLeft.setPower(motorPower * (((-gamepad1.left_stick_y) + (gamepad1.left_stick_x)) + gamepad1.right_stick_x));
            robot.backRight.setPower(motorPower * (((-gamepad1.left_stick_y) + (-gamepad1.left_stick_x)) + -gamepad1.right_stick_x));
        }

        else
        {
            robot.frontLeft.setPower(0);
            robot.frontRight.setPower(0);
            robot.backLeft.setPower(0);
            robot.backRight.setPower(0);
        }











        // robot.flipper arm control

        if(gamepad2.dpad_down) {
            robot.flipMid();
        }

        if(gamepad2.dpad_up) {
            robot.flipMid();
        }

        if(gamepad2.right_trigger > 0.5) {
            robot.flipReady();
        }

        if(gamepad2.left_trigger > 0.5) {
            robot.flipOut();
        }






        // robot.rotater arm control
        if(gamepad2.left_bumper) {
            robot.rotaterOut();
        }
        if(gamepad2.right_bumper) {
            robot.rotaterReady();
        }




        // robot.gripper arm control
        if(gamepad2.a) {
            robot.grip();
        }
        if(gamepad2.y) {
            robot.gripReady();
        }




        //foundation mover control
        if(gamepad1.a) {
            robot.grabFoundation();
        }

        if(gamepad1.b) {
            robot.ungrabFoundation();
        }

        // AUTOMATED FLIP
        if(gamepad2.dpad_left) {
            count = 1;
        }

        //BACK IN
        if(gamepad2.dpad_right)
        {
            counter = 1;
        }






        switch(counter)
        {
                //task 1: lift up
            case 1:
                    robot.grip();
                    newSlideLeft = liftIncrementer;
                    newSlideRight = liftIncrementer;
                    robot.leftSlide.setTargetPosition((int)(newSlideLeft));
                    robot.rightSlide.setTargetPosition((int)(newSlideRight));
                    robot.leftSlide.setPower(1);
                    robot.rightSlide.setPower(1);
                    chime = currentTimeMillis();
                    counter++;

                break;
            //task 3: flip back
            case 2:
                if(currentTimeMillis() - chime >= toLiftTimeTo)
                {
                    robot.flipper.setPosition(0.95);
                    chime = currentTimeMillis();
                    counter++;
                }

                break;
            //rotate around
            case 3:
                if(currentTimeMillis() - chime >= toBackTimeTo)
                {
                    robot.rotaterReady();
                    counter++;
                }
                break;
            case 4:
                robot.leftSlide.setTargetPosition((int)(-25.0/2));
                robot.rightSlide.setTargetPosition((int)(-25.0/2));
                robot.leftSlide.setPower(0.75);
                robot.rightSlide.setPower(0.75);
                chime = currentTimeMillis();
                counter++;
                break;

            // grip to home
            case 5:
                if(currentTimeMillis() - chime >= 500) {
                    robot.gripper.setPosition(gripperHome);
                }
                counter++;
                break;
            case 6:
                oldSlideLeft = robot.leftSlide.getCurrentPosition();
                oldSlideRight = robot.rightSlide.getCurrentPosition();
                newSlideLeft = -25.0/2;
                newSlideRight = -25.0/2;
                robot.flipReady();
                robot.rotaterReady();
                robot.gripper.setPosition(gripperHome);
                counter = 0;
                break;
        }




        switch(count)
        {
            //task 1: flip to center
            case 1:
                //THIS IS ALL IN A SUPER PRE-ALPHA STAGE AS OF 11/30/19 at 9:33 pm
                /*Collects Data into a .txt file so Grab Data can be analyzed (To Check for Variances in the Slide Position during Grabbing)
                probably just end up pumping the data into excel or google sheets to visualize the slide positioning and difference variation
                difference variation causes intake to get caught so looking at slide data can help us determing whats going on during the
                state automated movement*/
                FileWriter fileWriter = null;
                try {
                    fileWriter = new FileWriter("Position Data.txt");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PrintWriter printWriter = new PrintWriter(fileWriter);
                printWriter.println("Slide Data when block is grabbed");
                printWriter.println("left slide pos: " + robot.leftSlide.getCurrentPosition());
                printWriter.println("right slide pos: " + robot.rightSlide.getCurrentPosition());
                printWriter.println("left slide pid coefffs: " + robot.leftSlide.getPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION).toString());
                printWriter.println("right slide pid coeffs: " + robot.rightSlide.getPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION).toString());
            //task 2: lift up
            case 2:
                if(currentTimeMillis() - time >= toMidTime)
                {
                    newSlideLeft = liftIncrement;
                    newSlideRight = liftIncrement;
                    robot.leftSlide.setTargetPosition((int)(newSlideLeft));
                    robot.rightSlide.setTargetPosition((int)(newSlideRight));
                    robot.leftSlide.setPower(1);
                    robot.rightSlide.setPower(1);
                    time = currentTimeMillis();
                    count++;
                }
                break;
            //task 3: flip back
            case 3:
                if(currentTimeMillis() - time >= liftTime)
                {
                    robot.flipper.setPosition(0.25);
                    time = currentTimeMillis();
                    count++;
                }

                break;

        /*    case 4:
                if(System.currentTimeMillis() - time >= 100) {
                    robot.flipper.setPosition(robot.flipperBetweenBetween);
                    time = System.currentTimeMillis();
                    count++;
                }*/

            //rotate around
            case 4:
                if(currentTimeMillis() - time >= toBackTime)
                {
                    robot.rotaterOut();
                    count = 0;
                }
                break;
        }


        /**
         * slide powers here
         */

        double increment = gamepad2.right_stick_y * 100;

        if(Math.abs(increment) > 25) {
            newSlideLeft = robot.leftSlide.getCurrentPosition() + increment;
            newSlideRight = robot.rightSlide.getCurrentPosition() + increment;
        }
        if(gamepad2.x) {
            oldSlideLeft = robot.leftSlide.getCurrentPosition();
            oldSlideRight = robot.rightSlide.getCurrentPosition();
            newSlideLeft = -25;
            newSlideRight = -25;
        }

        if(gamepad2.b) {
            oldSlideLeft = robot.leftSlide.getCurrentPosition();
            oldSlideRight = robot.rightSlide.getCurrentPosition();
            newSlideLeft = -25.0/2;
            newSlideRight = -25.0/2;
        }

        if(Math.abs(newSlideLeft - robot.leftSlide.getCurrentPosition()) > 10 || Math.abs(newSlideRight - robot.rightSlide.getCurrentPosition()) > 10) {
            robot.leftSlide.setTargetPosition((int)(newSlideLeft));
            robot.rightSlide.setTargetPosition((int)(newSlideRight));
            robot.leftSlide.setPower(1);
            robot.rightSlide.setPower(1);
        }

        else {
            robot.leftSlide.setPower(0);
            robot.rightSlide.setPower(0);
        }


        telemetry.addData("robot.flipper pos", robot.flipper.getPosition());
        telemetry.addData("robot.gripper pos", robot.gripper.getPosition());
        telemetry.addData("robot.rotater pos", robot.rotater.getPosition());
        telemetry.addData("left slide pos", robot.leftSlide.getCurrentPosition());
        telemetry.addData("right slide pos", robot.rightSlide.getCurrentPosition());
        telemetry.addData("left slide pid coefffs", robot.leftSlide.getPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION).toString());
        telemetry.addData("right slide pid coeffs", robot.rightSlide.getPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION).toString());

    }
}
