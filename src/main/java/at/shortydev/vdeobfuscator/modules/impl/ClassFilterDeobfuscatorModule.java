package at.shortydev.vdeobfuscator.modules.impl;

import at.shortydev.vdeobfuscator.modules.DeobfuscatorModule;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class ClassFilterDeobfuscatorModule extends ClassLoader implements DeobfuscatorModule {

    @Override
    public void init(String... args) throws Throwable {
        List<String> filterStrings = new ArrayList<>();
        File referenceFile = new File(args[0]);
        File workerJarFile = new File(args[1]);
        int minStringLength = Integer.parseInt(args[2]);
        
        /*
        Extract from reference jar
         */

        JarFile referenceJar = new JarFile(referenceFile);
        Enumeration<? extends JarEntry> enumeration = referenceJar.entries();
        while (enumeration.hasMoreElements()) {
            ZipEntry zipEntry = enumeration.nextElement();
            if (zipEntry.getName().endsWith(".class")) {
                ClassReader classReader = new ClassReader(referenceJar.getInputStream(zipEntry));
                classReader.accept(new ClassVisitor(Opcodes.ASM4) {
                    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                        return new MethodVisitor(Opcodes.ASM4) {
                            public void visitLdcInsn(Object cst) {
                                if (cst instanceof String && ((String) cst).length() >= minStringLength)
                                    filterStrings.add((String) cst);
                            }
                        };
                    }
                }, 0);
            }
        }
        System.out.println("Total: " + filterStrings.size());
        
        /*
        Compare to worker jar
         */

        List<ZipEntry> newFile = new ArrayList<>();
        JarFile antiVJar = new JarFile(workerJarFile);
        Enumeration<? extends JarEntry> enumAntiV = antiVJar.entries();
        while (enumAntiV.hasMoreElements()) {
            ZipEntry zipEntry = enumAntiV.nextElement();
            if (zipEntry.getName().endsWith(".class")) {
                ClassReader classReader = new ClassReader(antiVJar.getInputStream(zipEntry));
                classReader.accept(new ClassVisitor(Opcodes.ASM5) {
                    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                        return new MethodVisitor(Opcodes.ASM5) {
                            public void visitLdcInsn(Object cst) {
                                if (cst instanceof String 
                                        && !filterStrings.contains(cst)
                                        && !newFile.contains(zipEntry))
                                    newFile.add(zipEntry);
                            }
                        };
                    }
                }, 0);
            }
        }
        
        /*
        Remove classes from worker jar
         */

        for (ZipEntry zipEntry : newFile) {
            if (new File("out").mkdir())
                System.out.println("\"out\" directory created.");
            System.out.println("Entry " + zipEntry.getName() + " added.");
            InputStream inputStream = antiVJar.getInputStream(zipEntry);
            Files.copy(inputStream, Paths.get("out/" + zipEntry.getName().replace("/", ".")));
        }
    }
}
