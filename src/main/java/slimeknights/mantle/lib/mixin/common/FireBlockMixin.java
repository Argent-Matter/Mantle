package slimeknights.mantle.lib.mixin.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import slimeknights.mantle.lib.extensions.FireBlockExtensions;

import java.util.Map;
import java.util.Random;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin extends BaseFireBlock implements FireBlockExtensions {
	@Shadow
	@Final
	private static Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION;

	@Shadow
	protected abstract int getBurnOdd(BlockState blockState);

	private FireBlockMixin(Properties properties, float f) {
		super(properties, f);
	}

	@Override
	public boolean canCatchFire(BlockGetter world, BlockPos pos, Direction face) {
		return world.getBlockState(pos).isFlammable(world, pos, face);
	}

	@Override
	public int invokeGetBurnOdd(BlockState state) {
		return getBurnOdd(state);
	}

	/**
	 * Even though this is an overwrite it's 100x cleaner than the alternatives I tried
	 * @reason fire mojank
	 * @author Tropheus Jay
	 */
	@Overwrite
	public BlockState getStateForPlacement(BlockGetter iBlockReader, BlockPos blockPos) {
		BlockPos blockPos2 = blockPos.below();
		BlockState blockState = iBlockReader.getBlockState(blockPos2);
		if (!this.canBurn(blockState) && !blockState.isFaceSturdy(iBlockReader, blockPos2, Direction.UP)) {
			BlockState blockState2 = this.defaultBlockState();
			Direction[] var6 = Direction.values();
			int var7 = var6.length;

			for (int var8 = 0; var8 < var7; ++var8) {
				Direction direction = var6[var8];
				BooleanProperty booleanProperty = PROPERTY_BY_DIRECTION.get(direction);
				if (booleanProperty != null) {
					blockState2 = blockState2.setValue(booleanProperty, this.canBurn(blockState2) || canCatchFire(iBlockReader, blockPos, direction));
				}
			}

			return blockState2;
		} else {
			return this.defaultBlockState();
		}
	}

	@Inject(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/Random;nextInt(I)I",
					ordinal = 1,
					shift = At.Shift.BEFORE
			),
			cancellable = true
	)
	public void scheduledTick(BlockState blockState, ServerLevel serverWorld, BlockPos blockPos, Random random, CallbackInfo ci) {
		if (blockState.getValue(FireBlock.AGE) == 15 && random.nextInt(4) == 0 && !canCatchFire(serverWorld, blockPos.below(), Direction.UP)) {
			serverWorld.removeBlock(blockPos, false);
			ci.cancel();
		}
	}

	@Inject(
			method = "isValidFireLocation",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/FireBlock;canBurn(Lnet/minecraft/world/level/block/state/BlockState;)Z",
					shift = At.Shift.BEFORE
			),
			locals = LocalCapture.CAPTURE_FAILHARD,
			cancellable = true
	)
	private void areNeighborsFlammable(BlockGetter iBlockReader, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir,
											  Direction[] var3, int var4, int var5, Direction direction) {
		if (this.canCatchFire(iBlockReader, blockPos.relative(direction), direction.getOpposite())) {
			cir.setReturnValue(true);
		}
	}
}
