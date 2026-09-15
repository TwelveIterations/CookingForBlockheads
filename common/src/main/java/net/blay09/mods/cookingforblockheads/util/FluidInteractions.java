package net.blay09.mods.cookingforblockheads.util;

import net.blay09.mods.balm.platform.fluid.FluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;

public class FluidInteractions {

    private static final int BUCKET_VOLUME = 1000;
    private static final int BOTTLE_VOLUME = 333;

    public static boolean tryInteractWithWaterTank(FluidTank fluidTank, ItemStack itemStack, Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (itemStack.is(Items.BUCKET) && fluidTank.drain(0, Fluids.WATER, BUCKET_VOLUME, true) == BUCKET_VOLUME) {
            fluidTank.drain(0, Fluids.WATER, BUCKET_VOLUME, false);
            player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.WATER_BUCKET)));
            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1f, 1f);
            return true;
        }

        if (itemStack.is(Items.WATER_BUCKET) && fluidTank.fill(0, Fluids.WATER, BUCKET_VOLUME, true) == BUCKET_VOLUME) {
            fluidTank.fill(0, Fluids.WATER, BUCKET_VOLUME, false);
            player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.BUCKET)));
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1f, 1f);
            return true;
        }

        if (itemStack.is(Items.GLASS_BOTTLE) && fluidTank.drain(0, Fluids.WATER, BOTTLE_VOLUME, true) == BOTTLE_VOLUME) {
            fluidTank.drain(0, Fluids.WATER, BOTTLE_VOLUME, false);
            ItemStack waterBottle = PotionContents.createItemStack(Items.POTION, Potions.WATER);
            player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, waterBottle));
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1f, 1f);
            return true;
        }

        PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
        if (itemStack.is(Items.POTION)
                && potionContents != null
                && potionContents.is(Potions.WATER)
                && fluidTank.fill(0, Fluids.WATER, BOTTLE_VOLUME, true) == BOTTLE_VOLUME) {
            fluidTank.fill(0, Fluids.WATER, BOTTLE_VOLUME, false);
            player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.GLASS_BOTTLE)));
            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.NEUTRAL, 1f, 1f);
            return true;
        }

        return false;
    }

}
