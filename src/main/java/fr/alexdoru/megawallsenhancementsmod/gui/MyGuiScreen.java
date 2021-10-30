package fr.alexdoru.megawallsenhancementsmod.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Method;

public class MyGuiScreen extends GuiScreen {

    private static final ResourceLocation SHADER = new ResourceLocation("fkcounter", "shaders/blur.json");

    @Override
    public void initGui() {

        Method loadShaderMethod = null;
        try {
            loadShaderMethod = EntityRenderer.class.getDeclaredMethod("loadShader", ResourceLocation.class);
        } catch (NoSuchMethodException e) {
            try {
                loadShaderMethod = EntityRenderer.class.getDeclaredMethod("func_175069_a", ResourceLocation.class);
            } catch (NoSuchMethodException ignored) {
            }
        }

        if (loadShaderMethod != null) {
            loadShaderMethod.setAccessible(true);
            try {
                loadShaderMethod.invoke(mc.entityRenderer, SHADER);
            } catch (Exception ignored) {
            }
        }
        super.initGui();
    }

    @Override
    public void onGuiClosed() {
        mc.entityRenderer.stopUseShader();
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public String getSuffix(boolean enabled) {
        return enabled ? EnumChatFormatting.GREEN + "Enabled" : EnumChatFormatting.RED + "Disabled";
    }

    public void drawCenteredTitle(String title, int buttonsHeight) {
        GlStateManager.pushMatrix();
        {
            int dilatation = 2;
            GlStateManager.translate((width / 2.0f) - mc.fontRendererObj.getStringWidth(title), getyCenter() - (buttonsHeight + 4) * 3, 0);
            GlStateManager.scale(dilatation, dilatation, dilatation);
            mc.fontRendererObj.drawString(title, 0, 0, Integer.parseInt("55FF55", 16));
        }
        GlStateManager.popMatrix();
    }

    public int getxCenter() {
        return this.width / 2;
    }

    public int getyCenter() {
        return this.height / 2;
    }

}
