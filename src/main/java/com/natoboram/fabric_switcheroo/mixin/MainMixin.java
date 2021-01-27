package com.natoboram.fabric_switcheroo.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.MinecraftClient;

@Mixin(MinecraftClient.class)
public class MainMixin {
	// @Inject(at = @At("HEAD"), method = "init()V")
	// private void init(CallbackInfo info) {
	// System.out.println("This line is printed by an example mod mixin!");
	// }
}
