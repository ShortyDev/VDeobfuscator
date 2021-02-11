package at.shortydev.vdeobfuscator.modules;

public interface DeobfuscatorModule {

    default void init(String... args) throws Throwable {}
    
}
