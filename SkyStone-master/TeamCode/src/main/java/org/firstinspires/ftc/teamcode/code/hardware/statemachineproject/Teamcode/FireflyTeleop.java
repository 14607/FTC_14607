package org.firstinspires.ftc.teamcode.code.hardware.statemachineproject.Teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.code.hardware.statemachineproject.HelperClasses.Firefly;
import org.firstinspires.ftc.teamcode.code.hardware.statemachineproject.HelperClasses.TimeProfiler;
import static org.firstinspires.ftc.teamcode.code.GLOBALS.*;

@TeleOp(name = "new test teleop", group = "teleop")
public class FireflyTeleop extends Firefly {

    /**
     * variable declaration
     */

    private double newSlideLeft = 0;
    private double newSlideRight = 0;

    private int count = 0;
    private double time = 0;
    private double chime = 0;
    private int counter = 0;



    @Override
    public void init() {
        super.init();
    }

    @Override
    public void init_loop() {
        super.init_loop();
    }

    @Override
    public void start() {
        super.start();
    }


    private TimeProfiler tp1 = new TimeProfiler(1000);
    private TimeProfiler tp2 = new TimeProfiler(1000);
    private TimeProfiler tp3 = new TimeProfiler(1000);
    private TimeProfiler tp4 = new TimeProfiler(1000);
    private TimeProfiler tp5 = new TimeProfiler(1000);

    @Override
    public void loop() {


        tp1.markStart();
        super.loop();
        tp1.markEnd();


        tp2.markStart();
        teleopDrivetrainControl();
        tp2.markEnd();


        tp3.markStart();
        slideControl();
        tp3.markEnd();


        tp4.markStart();
        intakeControl();
        tp4.markEnd();


        tp5.markStart();
        foundationGripperControl();
        tp5.markEnd();




        telemetry.addLine("-------------- FIREFLY TELEOP TELEMETRY -----------------");
        telemetry.addLine("profiler 1: " + tp1.getAverageTimePerUpdateMillis());
        telemetry.addLine("profiler 2: " + tp2.getAverageTimePerUpdateMillis());
        telemetry.addLine("profiler 3: " + tp3.getAverageTimePerUpdateMillis());
        telemetry.addLine("profiler 4: " + tp4.getAverageTimePerUpdateMillis());
        telemetry.addLine("profiler 5: " + tp5.getAverageTimePerUpdateMillis());

    }



    private void foundationGripperControl() {
        if(gamepad1.a) {
            myOuttake.grabFoundation();
        }

        if(gamepad1.b) {
            myOuttake.ungrabFoundation();
        }
    }



    /**
     * our slide control
     */
    private void slideControl() {

        double increment = gamepad2.right_stick_y * 100;

        if(Math.abs(increment) > 25) { mySlide.manualMovement(increment, true); }
        if(gamepad2.x) { mySlide.goPsuedohome(); }
        if(gamepad2.b) { mySlide.goHome(); }



        // automated slide control



        if(gamepad1.dpad_left) {
            count = 1;
        }

        if(gamepad1.dpad_right) {
            counter = 1;
        }


        switch(count) {

            // task 1: flip to center
            case 1:
                myOuttake.flipMid();
                time = System.currentTimeMillis();
                count ++;
                break;

            // task 2: lift up
            case 2:
                if(System.currentTimeMillis() - time >= toMidTime){
                    mySlide.manualMovement(liftIncrement, false);
                    time = System.currentTimeMillis();
                    count++;
                }
                break;

            case 3:
                // task 3: flip black
                if(System.currentTimeMillis() - time >= liftTime) {
                    myOuttake.flipOut();
                    time = System.currentTimeMillis();
                    count++;
                }
                break;

            case 4:
                // task 4: rotate around
                if(System.currentTimeMillis() - time >= toBackTime) {
                    myOuttake.rotaterOut();
                    count = 0;
                }
                break;


        }


        switch(counter) {
            case 1:
                // task 1: lift up a bit
                myOuttake.grip();
                mySlide.manualMovement(liftIncrementer, false);
                chime = System.currentTimeMillis();
                counter++;
                break;

            case 2:
                // task 2: flip back
                if(System.currentTimeMillis() - chime >= toLiftTimeTo) {
                    myOuttake.flipReady();
                    counter++;
                }
                break;

            case 3:
                // task 3: rotate around
                if(System.currentTimeMillis() - chime >= toBackTimeTo) {
                    myOuttake.rotaterReady();
                    counter++;
                }
                break;

            case 4:
                // task 4: psuedo home
                mySlide.goPsuedohome();
                myOuttake.gripReady();
                counter = 0;
                break;
        }

    }





    /**
     * our intake control
     */
    private void intakeControl() {

        double leftIntakePower = gamepad2.left_stick_y - gamepad2.left_stick_x;
        double rightIntakePower = gamepad2.left_stick_y + gamepad2.left_stick_x;

        myIntake.manualControl(leftIntakePower > 0.1 ? 0.5 * -leftIntakePower : 0,
                rightIntakePower > 0.1 ? 0.5 * -rightIntakePower : 0);

    }

}
