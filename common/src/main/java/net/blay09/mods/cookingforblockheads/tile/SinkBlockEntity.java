package net.blay09.mods.cookingforblockheads.tile;

import com.google.common.collect.Lists;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.container.ContainerUtils;
import net.blay09.mods.balm.api.fluid.BalmFluidTankProvider;
import net.blay09.mods.balm.api.fluid.FluidTank;
import net.blay09.mods.balm.api.provider.BalmProvider;
import net.blay09.mods.balm.common.BalmBlockEntity;
import net.blay09.mods.cookingforblockheads.FluidUnits;
import net.blay09.mods.cookingforblockheads.CookingForBlockheadsConfig;
import net.blay09.mods.cookingforblockheads.api.SourceItem;
import net.blay09.mods.cookingforblockheads.api.capability.AbstractKitchenItemProvider;
import net.blay09.mods.cookingforblockheads.api.capability.IKitchenItemProvider;
import net.blay09.mods.cookingforblockheads.registry.CookingRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

public class SinkBlockEntity extends BalmBlockEntity implements BalmFluidTankProvider {

    private static final int SYNC_INTERVAL = 10;

    private class SinkItemProvider extends AbstractKitchenItemProvider {
        private final NonNullList<ItemStack> itemStacks = NonNullList.create();
        private final FluidTank fluidTank;
        private long waterUsed;

        public SinkItemProvider(FluidTank fluidTank) {
            this.fluidTank = fluidTank;
            itemStacks.addAll(CookingRegistry.getWaterItems());
        }

        @Override
        public void resetSimulation() {
            waterUsed = 0;
        }

        @Override
        public int getSimulatedUseCount(int slot) {
            return (int) Math.min(Integer.MAX_VALUE, waterUsed / FluidUnits.BUCKET);
        }

        @Override
        public ItemStack useItemStack(int slot, int amount, boolean simulate, List<IKitchenItemProvider> inventories, boolean requireBucket) {
            long availableAmount = fluidTank.getAmount();
            long requiredAmount = (long) amount * FluidUnits.BUCKET;
            if(simulate) {
                availableAmount -= waterUsed;
            }
            if (!CookingForBlockheadsConfig.getActive().sinkRequiresWater || availableAmount >= requiredAmount) {
                if (requireBucket && itemStacks.get(slot).getItem() == Items.MILK_BUCKET) {
                    if (!CookingRegistry.consumeBucket(inventories, simulate)) {
                        return ItemStack.EMPTY;
                    }
                }
                if (simulate) {
                    waterUsed += requiredAmount;
                } else {
                    fluidTank.drain(Fluids.WATER, (int) Math.min(Integer.MAX_VALUE, requiredAmount), false);
                    setChanged();
                }
                return ContainerUtils.copyStackWithSize(itemStacks.get(slot), amount);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack returnItemStack(ItemStack itemStack, SourceItem sourceItem) {
            for (ItemStack providedStack : itemStacks) {
                if (Balm.getHooks().canItemsStack(itemStack, providedStack)) {
                    fluidTank.fill(Fluids.WATER, FluidUnits.BUCKET, false);
                    break;
                }
            }

            return ItemStack.EMPTY;
        }

        @Override
        public int getSlots() {
            return itemStacks.size();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (CookingForBlockheadsConfig.getActive().sinkRequiresWater && fluidTank.getAmount() - waterUsed < FluidUnits.BUCKET) {
                return ItemStack.EMPTY;
            }

            return itemStacks.get(slot);
        }
    }

    private final FluidTank sinkTank = new FluidTank(16 * FluidUnits.BUCKET) {

        @Override
        public Fluid getFluid() {
            if (!CookingForBlockheadsConfig.getActive().sinkRequiresWater) {
                return Fluids.WATER;
            }

            return super.getFluid();
        }

        @Override
        public int getAmount() {
            if (!CookingForBlockheadsConfig.getActive().sinkRequiresWater) {
                return Integer.MAX_VALUE;
            }

            return super.getAmount();
        }

        @Override
        public int getCapacity() {
            if (!CookingForBlockheadsConfig.getActive().sinkRequiresWater) {
                return Integer.MAX_VALUE;
            }

            return super.getCapacity();
        }

        @Override
        public int drain(Fluid fluid, int maxDrain, boolean simulate) {
            if (!CookingForBlockheadsConfig.getActive().sinkRequiresWater && fluid == Fluids.WATER) {
                return maxDrain;
            }

            if (fluid.isSame(Fluids.EMPTY) || !fluid.isSame(fluid))
            {
                return 0;
            }

            SinkBlockEntity.this.setChanged();

            return super.drain(fluid, maxDrain, simulate);
        }

        @Override
        public int fill(Fluid fluid, int maxFill, boolean simulate) {
            if (!CookingForBlockheadsConfig.getActive().sinkRequiresWater) {
                return maxFill;
            }

            SinkBlockEntity.this.setChanged();

            return super.fill(fluid, maxFill, simulate);
        }
    };

    private final SinkItemProvider itemProvider = new SinkItemProvider(sinkTank);

    private int ticksSinceSync;
    private boolean isDirty;

    private DyeColor color = DyeColor.WHITE;

    public SinkBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.sink.get(), pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("FluidTank", FluidUnits.serialize(sinkTank));
        tag.putByte("Color", (byte) color.getId());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        FluidUnits.deserialize(sinkTank, tag.getCompound("FluidTank"), 16 * FluidUnits.BUCKET);
        color = DyeColor.byId(tag.getByte("Color"));
    }

    @Override
    public void writeUpdateTag(CompoundTag tag) {
        saveAdditional(tag);
    }

    @Override
    public List<BalmProvider<?>> getProviders() {
        return Lists.newArrayList(new BalmProvider<>(IKitchenItemProvider.class, itemProvider));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SinkBlockEntity blockEntity) {
        blockEntity.serverTick(level, pos, state);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        // Sync to clients
        ticksSinceSync++;
        if (ticksSinceSync >= SYNC_INTERVAL) {
            ticksSinceSync = 0;
            if (isDirty) {
                sync();
                isDirty = false;
            }
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        isDirty = true;
    }

    @Override
    public FluidTank getFluidTank() {
        return sinkTank;
    }
}
