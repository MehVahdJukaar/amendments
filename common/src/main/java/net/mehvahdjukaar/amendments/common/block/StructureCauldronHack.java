package net.mehvahdjukaar.amendments.common.block;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.common.item.DyeBottleItem;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class StructureCauldronHack extends Block implements EntityBlock {

    public static void register() {
    }

    private static final Supplier<StructureCauldronHack> BLOCK = RegHelper.registerBlock(Amendments.res("cauldron_hack"),
            StructureCauldronHack::new);
    private static final Supplier<BlockEntityType<Tile>> TILE = RegHelper.registerBlockEntityType(Amendments.res("cauldron_hack"),
            () -> PlatHelper.newBlockEntityType(Tile::new, BLOCK.get()));

    private static final BooleanProperty POTION = BooleanProperty.create("potion");

    public StructureCauldronHack() {
        super(Properties.of().dropsLike(Blocks.CAULDRON));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POTION);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Tile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return Utils.getTicker(pBlockEntityType, TILE.get(), Tile::tick);
    }

    private static final Supplier<List<Potion>> HARMFUL_POTS = Suppliers.memoize(() ->
            BuiltInRegistries.POTION.stream().filter(p -> p.getEffects().stream().noneMatch(e -> e.getEffect().isBeneficial())).toList());

    private static class Tile extends BlockEntity {
        public Tile(BlockPos pos, BlockState blockState) {
            super(TILE.get(), pos, blockState);
        }

        public static <E extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, E e) {
            level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());

            if (state.getValue(POTION)) {
                level.setBlockAndUpdate(pos, ModRegistry.LIQUID_CAULDRON.get().defaultBlockState());
                if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {

                    var list = HARMFUL_POTS.get();
                    var pot = list.get(level.random.nextInt(list.size()));
                    CompoundTag tag = new CompoundTag();
                    tag.putString("Potion", BuiltInRegistries.POTION.getKey(pot).toString());
                    if (level.random.nextFloat() < 0.4) {
                        PotionNBTHelper.Type.SPLASH.applyToTag(tag);
                    }
                    te.getSoftFluidTank().setFluid(SoftFluidStack.of(
                            BuiltInSoftFluids.POTION.getHolder(), level.random.nextIntBetweenInclusive(1, 4), tag
                    ));
                    te.setChanged();
                }
            } else {
                level.setBlockAndUpdate(pos, ModRegistry.DYE_CAULDRON.get().defaultBlockState());
                if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
                    var dye = DyeColor.byId(level.random.nextInt(DyeColor.values().length));
                    var fluid = DyeBottleItem.toFluidStack(dye, 3);
                    te.getSoftFluidTank().setFluid(fluid);
                    te.setChanged();
                }
            }
        }
    }

}
