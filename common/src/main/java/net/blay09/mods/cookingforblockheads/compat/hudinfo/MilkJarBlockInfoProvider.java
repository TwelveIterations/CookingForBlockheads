package net.blay09.mods.cookingforblockheads.compat.hudinfo;

import net.blay09.mods.cookingforblockheads.FluidUnits;
import net.blay09.mods.balm.platform.compatibility.hudinfo.BlockInfoContext;
import net.blay09.mods.balm.platform.compatibility.hudinfo.BlockInfoProvider;
import net.blay09.mods.balm.platform.compatibility.hudinfo.HudInfoOutput;
import net.blay09.mods.cookingforblockheads.block.entity.MilkJarBlockEntity;
import net.minecraft.network.chat.Component;

public class MilkJarBlockInfoProvider implements BlockInfoProvider {
    @Override
    public void apply(BlockInfoContext context, HudInfoOutput output) {
        if (context.blockEntity() instanceof MilkJarBlockEntity milkJar) {
            output.text(Component.translatable("waila.cookingforblockheads.milk_stored", FluidUnits.toMillibuckets(milkJar.getFluidTank().getAmount()), FluidUnits.toMillibuckets(milkJar.getFluidTank().getCapacity())));
        }
    }
}
