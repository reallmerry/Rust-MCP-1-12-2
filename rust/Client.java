package asslock.rust;
import asslock.rust.Utils.FontUtils.FontRenderer;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;

import java.awt.*;
import java.io.File;

public class Client {

    public static final String VIA_NAME = "JavaRust";
    public static final String client_name = "JavaRust";
    public static final String client_name_small = "JavaRust";
    public static final String client_version = "1.0.0";

    private static Client instance;
    private final File clientDir = new File(Minecraft.getMinecraft().mcDataDir + "\\asslock");
    private final File filesDir = new File(Minecraft.getMinecraft().mcDataDir + "\\asslock\\files");



    public Client() {
        instance = this;

        if (!clientDir.exists()) {
            clientDir.mkdirs();
        }
        if (!filesDir.exists()) {
            filesDir.mkdirs();
        }
        clientLoad();
    }

    private void clientLoad() {
        Display.setTitle(VIA_NAME + " " + client_version);
    }


}
