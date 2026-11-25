package net.blay09.mods.cookingforblockheads.block;

import net.blay09.mods.cookingforblockheads.FluidUnits;
import com.mojang.serialization.MapCodec;
import net.blay09.mods.cookingforblockheads.FluidBucketInteraction;
import net.blay09.mods.cookingforblockheads.compat.Compat;
import net.blay09.mods.cookingforblockheads.block.entity.MilkJarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class MilkJarBlock extends BaseKitchenBlock implements BucketPickup {

    public static final MapCodec<MilkJarBlock> CODEC = simpleCodec(MilkJarBlock::new);

    private static final VoxelShape SHAPE = Block.box(4.8, 0, 4.8, 11.2, 8.0, 11.2);

    public MilkJarBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        if (itemStack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        final var blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof MilkJarBlockEntity milkJar)) {
            return InteractionResult.FAIL;
        }

        if (itemStack.is(Items.MILK_BUCKET) || itemStack.is(Items.BUCKET)) {
            FluidBucketInteraction.use(milkJar.getFluidTank(), player, hand);
            return InteractionResult.SUCCESS;
        }

        return super.useItemOn(itemStack, state, level, pos, player, hand, blockHitResult);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MilkJarBlockEntity(pos, state);
    }

    @Override
    public ItemStack pickupBlock(@Nullable LivingEntity entity, LevelAccessor level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MilkJarBlockEntity milkJar && milkJar.getFluidTank().getAmount(0) >= FluidUnits.BUCKET) {
            int drained = milkJar.getFluidTank().drain(0, Compat.getMilkFluid(), FluidUnits.BUCKET, false);
            return drained >= FluidUnits.BUCKET ? new ItemStack(Items.MILK_BUCKET) : ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.empty();
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
