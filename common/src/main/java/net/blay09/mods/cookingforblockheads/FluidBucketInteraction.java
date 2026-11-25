package net.blay09.mods.cookingforblockheads;

import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.platform.fluid.FluidTank;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidBucketInteraction {
    public static boolean use(FluidTank tank, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        Fluid fluid = heldItem.is(Items.BUCKET) ? tank.getFluid(0) : BuiltInRegistries.FLUID.stream()
                .filter(it -> it.getBucket() == heldItem.getItem()).findFirst().orElse(Fluids.EMPTY);
        if (fluid.isSame(Fluids.EMPTY)) {
            return false;
        }

        boolean draining = heldItem.is(Items.BUCKET);
        final var remainingItem = Balm.hooks().getCraftingRemainingItem(heldItem);
        ItemStack result = draining ? new ItemStack(fluid.getBucket()) : remainingItem != null ? remainingItem.create() : ItemStack.EMPTY;
        if (result.isEmpty()) {
            return false;
        }

        int transferred = draining ? tank.drain(0, fluid, FluidUnits.BUCKET, true) : tank.fill(0, fluid, FluidUnits.BUCKET, true);
        if (transferred < FluidUnits.BUCKET) {
            return false;
        }

        player.setItemInHand(hand, ItemUtils.createFilledResult(heldItem, player, result));
        if (draining) {
            tank.drain(0, fluid, FluidUnits.BUCKET, false);
            fluid.getPickupSound().ifPresent(sound -> player.playSound(sound, 1f, 1f));
        } else {
            tank.fill(0, fluid, FluidUnits.BUCKET, false);
            player.playSound(SoundEvents.BUCKET_EMPTY, 1f, 1f);
        }
        return true;
    }
}
