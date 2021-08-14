package org.firstinspires.ftc.teamcode.control.system

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.hardware.BaseAzusa
import org.firstinspires.ftc.teamcode.util.AzusaTelemetry
import org.firstinspires.ftc.teamcode.util.OpModeType

abstract class BaseOpMode : LinearOpMode() {
    abstract val getRobot: BaseAzusa

    open fun onInit() {}
    open fun onInitLoop() {}
    open fun onStart() {}
    abstract fun onLoop()
    open fun onStop() {}

    lateinit var opModeType: OpModeType
        private set
    private var status: Status = Status.INIT_LOOP

    override fun runOpMode() {
        opModeType = if (javaClass.isAnnotationPresent(Autonomous::class.java)) OpModeType.AUTO
        else OpModeType.TELEOP

        telemetry = AzusaTelemetry(this)
        getRobot.init()
        onInit()

        mainLoop@ while (true) {
            when (status) {
                Status.INIT_LOOP -> {
                    onInitLoop()
                    if (isStarted)
                        status = status.next
                }

                Status.START -> {
                    onStart()
                    status = status.next
                }

                Status.LOOP -> {
                    onLoop()
                    if (isStopRequested)
                        status = status.next
                }

                Status.STOP -> {
                    onStop()
                    break@mainLoop
                }
            }
            getRobot.update()
        }
    }
}
