package at.shortydev.vdeobfuscator;

import at.shortydev.vdeobfuscator.modules.impl.ClassFilterDeobfuscatorModule;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class VDeobfuscator {

    public static void main(String[] args) {
        try {
            for (File file : new File("out").listFiles()) {
                file.delete();
            }
        } catch (Exception ignored) {}
        System.out.println("Please enter a deobfuscator module you would like to use:");
        System.out.println("Options:");
        System.out.println("(1) ClassFilterDeobfuscationModule: Made for AntiV - Args: ReferenceJar WorkerJar MinStringLength");
        try {
            int module = Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
            switch (module) {
                case 1:
                    System.out.println("loading " + ClassFilterDeobfuscatorModule.class.getName() + " and calling init...");
                    new ClassFilterDeobfuscatorModule().init(args);
                    break;
                default:
                    System.exit(500);
                    break;
            }
            System.out.println("Done. Have a nice day. Files to be found in out directory (sub of root).");
            System.exit(-1);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    
}
