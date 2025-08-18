package asslock.rust.Gui.widget;

import asslock.rust.Gui.CustomMenu;
import asslock.rust.Utils.FontUtils.FontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;


public class TextMenuButton extends GuiButton {

    public static FontRenderer RobotoCondensedbold = new FontRenderer("RobotoCondensed-Bold", 20, Font.PLAIN, true, true);

    private final String labelText;
    private final int colorNormal;
    private final int colorHover;

    public TextMenuButton(int buttonId, int x, int y, int width, int height,
                          String labelText, int colorNormal, int colorHover) {
        super(buttonId, x, y, width, height, "");
        this.labelText = labelText;
        this.colorNormal = colorNormal;
        this.colorHover = colorHover;
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition &&
                    mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;

            int color = this.hovered ? colorHover : colorNormal;
            int textY = this.yPosition + (this.height - RobotoCondensedbold.getHeight()) / 2;

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RobotoCondensedbold.drawString(labelText, this.xPosition, textY, color);
            GlStateManager.disableBlend();
        }
    }
}