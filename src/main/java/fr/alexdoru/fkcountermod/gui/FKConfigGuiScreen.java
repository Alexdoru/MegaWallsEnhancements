package fr.alexdoru.fkcountermod.gui;

import fr.alexdoru.fkcountermod.FKCounterMod;
import fr.alexdoru.fkcountermod.gui.elements.ButtonFancy;
import fr.alexdoru.fkcountermod.gui.elements.ButtonToggle;
import fr.alexdoru.megawallsenhancementsmod.config.ConfigHandler;
import fr.alexdoru.megawallsenhancementsmod.gui.MyGuiScreen;
import fr.alexdoru.megawallsenhancementsmod.gui.guiapi.PositionEditGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiSlider;

public class FKConfigGuiScreen extends MyGuiScreen implements GuiSlider.ISlider {

    private static final ResourceLocation BACKGROUND = new ResourceLocation("fkcounter", "background.png");
    private static final int columns = 4;
    private static final int rows = 2;
    private static final int buttonSize = 50;
    private static final int widthBetweenButtons = 10;
    private static final int heightBetweenButtons = 30;

    private ButtonToggle buttoncompacthud;
    private ButtonToggle buttonsidebar;
    private ButtonToggle buttonshowplayers;

    public FKConfigGuiScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.add(new ButtonFancy(100, getxCenter() + widthBetweenButtons / 2 + (widthBetweenButtons + buttonSize) + 10, getyCenter() - findMenuHeight() / 2 + heightBetweenButtons + buttonSize + 10, 30, 14, "Move HUD", 0.5));

        buttonList.add(addSettingButton(ConfigHandler.show_fkcHUD, 0, 0, 0, "Show HUD"));
        buttonList.add(buttoncompacthud = addSettingButton(ConfigHandler.compact_hud, 1, 0, 1, "Compact HUD"));
        buttonList.add(buttonsidebar = addSettingButton(ConfigHandler.FKHUDinSidebar, 7, 0, 2, "HUD in Sidebar"));
        buttonList.add(addSettingButton(ConfigHandler.finalsInTablist, 8, 0, 3, "FK in tablist"));

        buttonList.add(addSettingButton(ConfigHandler.draw_background, 3, 1, 0, "HUD Background"));
        buttonList.add(addSettingButton(ConfigHandler.text_shadow, 4, 1, 1, "Text Shadow"));
        buttonList.add(buttonshowplayers = addSettingButton(ConfigHandler.show_players, 2, 1, 2, "Show Players"));

        buttonList.add(new GuiSlider(5, getxCenter() - 150 / 2, getyCenter() + 70, "HUD Size : ", 0.1d, 4d, ConfigHandler.fkc_hud_size, this));
        buttonList.add(new GuiSlider(6, getxCenter() - 150 / 2, getyCenter() + 94, 150, 20, "Player amount : ", "", 1d, 10d, ConfigHandler.playerAmount, false, true, this));
        if (parent != null) {
            buttonList.add(new GuiButton(200, getxCenter() - 150 / 2, getyCenter() + 122, 150, 20, "Done"));
        }
        super.initGui();
    }

    @Override
    public void actionPerformed(GuiButton button) {

        switch (button.id) {
            case 100:
                mc.displayGuiScreen(new PositionEditGuiScreen(FKCounterGui.instance, this));
                break;
            case 200:
                mc.displayGuiScreen(parent);
                break;
            case 0:
                ConfigHandler.show_fkcHUD = !ConfigHandler.show_fkcHUD;
                ((ButtonToggle) button).setting = ConfigHandler.show_fkcHUD;
                break;
            case 1:
                ConfigHandler.compact_hud = !ConfigHandler.compact_hud;
                ((ButtonToggle) button).setting = ConfigHandler.compact_hud;
                if (ConfigHandler.compact_hud) {
                    ConfigHandler.show_players = false;
                    buttonshowplayers.setting = false;
                } else {
                    ConfigHandler.FKHUDinSidebar = false;
                    buttonsidebar.setting = false;
                }
                break;
            case 2:
                ConfigHandler.show_players = !ConfigHandler.show_players;
                ((ButtonToggle) button).setting = ConfigHandler.show_players;
                if (ConfigHandler.show_players) {
                    ConfigHandler.compact_hud = false;
                    buttoncompacthud.setting = false;
                    ConfigHandler.FKHUDinSidebar = false;
                    buttonsidebar.setting = false;
                }
                break;
            case 3:
                ConfigHandler.draw_background = !ConfigHandler.draw_background;
                ((ButtonToggle) button).setting = ConfigHandler.draw_background;
                break;
            case 4:
                ConfigHandler.text_shadow = !ConfigHandler.text_shadow;
                ((ButtonToggle) button).setting = ConfigHandler.text_shadow;
                break;
            case 7:
                ConfigHandler.FKHUDinSidebar = !ConfigHandler.FKHUDinSidebar;
                ((ButtonToggle) button).setting = ConfigHandler.FKHUDinSidebar;
                if (ConfigHandler.FKHUDinSidebar) {
                    ConfigHandler.show_players = false;
                    buttonshowplayers.setting = false;
                    ConfigHandler.compact_hud = true;
                    buttoncompacthud.setting = true;
                }
                break;
            case 8:
                ConfigHandler.finalsInTablist = !ConfigHandler.finalsInTablist;
                ((ButtonToggle) button).setting = ConfigHandler.finalsInTablist;
                break;
        }

        if (button instanceof ButtonToggle) {
            FKCounterGui.instance.updateDisplayText();
        }

    }

    private ButtonToggle addSettingButton(boolean setting, int buttonid, int row, int column, String buttonText) {
        int x;
        final int i = (widthBetweenButtons + buttonSize) * (column - columns / 2);
        if (columns % 2 == 0) { // even
            x = getxCenter() + widthBetweenButtons / 2 + i;
        } else { // odd
            x = getxCenter() - buttonSize / 2 + i;
        }
        int y = getyCenter() - findMenuHeight() / 2 + heightBetweenButtons + row * buttonSize;
        return new ButtonToggle(setting, buttonid, x + 10, y + 10, buttonText);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int rectWidth = findMenuWidth();
        int rectHeight = findMenuHeight();
        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, 0.7F);
        mc.getTextureManager().bindTexture(BACKGROUND);
        drawModalRectWithCustomSizedTexture(getxCenter() - rectWidth / 2, getyCenter() - rectHeight / 2, 0, 0, rectWidth, rectHeight, rectWidth, rectHeight);
        drawCenteredTitle("Final Kill Counter v" + FKCounterMod.VERSION, 2, (width / 2.0f), getYposForButton(-5), Integer.parseInt("55FFFF", 16));
        String msg = "for Mega Walls";
        drawCenteredString(fontRendererObj, msg, getxCenter() + fontRendererObj.getStringWidth(msg), getYposForButton(-5) + 2 * fontRendererObj.FONT_HEIGHT, Integer.parseInt("AAAAAA", 16));
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private int findMenuWidth() {
        return buttonSize * columns + widthBetweenButtons * (columns - 1);
    }

    private int findMenuHeight() {
        return heightBetweenButtons + buttonSize * rows;
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider) {
        switch (slider.id) {
            case 5:
                final double newvalue = Math.floor(slider.getValue() / 0.05d) * 0.05d;
                ConfigHandler.fkc_hud_size = newvalue;
                slider.setValue(newvalue);
                break;
            case 6:
                ConfigHandler.playerAmount = (int) slider.getValue();
                FKCounterGui.instance.updateDisplayText();
                break;
        }

    }

}
