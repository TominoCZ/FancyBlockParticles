package com.TominoCZ.FBP.handler;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.block.FBPBlockPos;
import com.TominoCZ.FBP.gui.FBPGuiFastAdd;
import com.TominoCZ.FBP.gui.FBPGuiMenuPage0;
import com.TominoCZ.FBP.keys.FBPKeyBindings;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class FBPKeyInputHandler {
	Minecraft mc;

	public static FBPKeyInputHandler INSTANCE;

	boolean wasOpened = false;

	public FBPKeyInputHandler() {
		mc = Minecraft.getMinecraft();
		INSTANCE = this;
	}

	@SubscribeEvent
	public void onKeyboardInput(InputEvent.KeyInputEvent e) {
		onInput();
	}

	@SubscribeEvent
	public void onMouseInput(InputEvent.MouseInputEvent e) {
		onInput();
	}

	public void onInput() {
		if (FBPKeyBindings.FBPMenu.isPressed())
			Minecraft.getMinecraft().displayGuiScreen(new FBPGuiMenuPage0());

		if (FBPKeyBindings.FBPToggle.isPressed())
			FBP.setEnabled(!FBP.enabled);

		if (FBPKeyBindings.FBPFreeze.isPressed())
			if (FBP.isEnabled())
				FBP.frozen = !FBP.frozen;
			else
				FBP.frozen = false;

		boolean isShiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
		boolean isKeyDown = FBPKeyBindings.FBPFastAdd.isKeyDown();

		if (isShiftDown && isKeyDown || isKeyDown) {
			Block b = null;

			boolean useHeldBlock = isShiftDown && mc.thePlayer.getHeldItem() != null
					&& mc.thePlayer.getHeldItem().getItem() != null
					&& (b = Block.getBlockFromItem(mc.thePlayer.getHeldItem().getItem())) != null
					&& b != Blocks.air;

			if (!wasOpened && isKeyDown
					&& ((mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit.equals(MovingObjectType.BLOCK))
							|| useHeldBlock)) {
				GuiScreen screen = useHeldBlock ? new FBPGuiFastAdd(new ItemStack(b))
						: new FBPGuiFastAdd(new FBPBlockPos(mc.objectMouseOver.getBlockPos()));

				mc.displayGuiScreen(screen);

				Mouse.setGrabbed(true);

				wasOpened = true;
			}
		} else if (wasOpened)
			wasOpened = false;
	}
}