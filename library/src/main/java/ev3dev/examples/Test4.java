package ev3dev.examples;

import lejos.utility.Delay;
import ev3dev.hardware.Battery;
import ev3dev.hardware.Shell;
import ev3dev.hardware.Sound;
import ev3dev.hardware.port.MotorPort;
import ev3dev.hardware.motor.EV3LargeRegulatedMotor;

//java -cp ev3-lang-java-0.2-SNAPSHOT.jar ev3dev.examples.Test4
public class Test4 {
	
    public static void main(String[] args) {
    	
        EV3LargeRegulatedMotor mA = new EV3LargeRegulatedMotor(MotorPort.A);
        mA.setSpeed(100);

        //mA.resetTachoCount();
        System.out.println(mA.getTachoCount());
        Sound.beep();
        System.out.println(mA.getTachoCount());
        mA.rotateTo(0);
        Sound.beep();
        Delay.msDelay(1000);
        System.out.println(mA.getTachoCount());
        mA.rotateTo(90);
        Sound.beep();  
        Delay.msDelay(1000);
        System.out.println(mA.getTachoCount());
        mA.rotateTo(180);
        Sound.beep();
        Delay.msDelay(1000);
        System.out.println(mA.getTachoCount());
        mA.rotateTo(270);
        Sound.beep();
        Delay.msDelay(1000);
        System.out.println(mA.getTachoCount());
        mA.rotateTo(360);
        Sound.beep();
        System.out.println(mA.getTachoCount());
        mA.rotateTo(90);
        Sound.beep();
        System.out.println(mA.getTachoCount());
        
        System.out.println(Battery.getVoltage());
    }
}
