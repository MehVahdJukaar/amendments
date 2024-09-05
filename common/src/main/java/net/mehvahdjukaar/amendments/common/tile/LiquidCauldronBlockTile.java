package net.mehvahdjukaar.amendments.common.tile;

import net.mehvahdjukaar.amendments.common.LiquidMixer;
import net.mehvahdjukaar.amendments.common.block.DyeCauldronBlock;
import net.mehvahdjukaar.amendments.common.block.LiquidCauldronBlock;
import net.mehvahdjukaar.amendments.common.block.ModCauldronBlock;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.PotionBottleType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;


public class LiquidCauldronBlockTile extends BlockEntity implements IExtraModelDataProvider, ISoftFluidTankProvider {
    public static final ModelDataKey<SoftFluid> FLUID = new ModelDataKey<>(SoftFluid.class);
    public static final ModelDataKey<Boolean> GLOWING = new ModelDataKey<>(Boolean.class);

    private final SoftFluidTank fluidTank;
    private boolean hasGlowInk = false;

    public SoftFluidTank makeTank(BlockState blockState) {
        return blockState.getBlock() instanceof DyeCauldronBlock ?
                createCauldronDyeTank() :
                createCauldronLiquidTank();
    }

    private boolean canMixPotions() {
        var config = CommonConfigs.POTION_MIXING.get();
        return config == CommonConfigs.MixingMode.ON || (config == CommonConfigs.MixingMode.ONLY_BOILING &&
                this.getBlockState().getValue(LiquidCauldronBlock.BOILING));
    }

    public LiquidCauldronBlockTile(BlockPos blockPos, BlockState blockState) {
        super(ModRegistry.LIQUID_CAULDRON_TILE.get(), blockPos, blockState);
        this.fluidTank = makeTank(blockState);
        //this.fluidHolder.setFluid(ModRegistry.DYE_SOFT_FLUID.get());
    }

    @Override
    public void addExtraModelData(ExtraModelData.Builder builder) {
        builder.with(FLUID, fluidTank.getFluidValue());
        builder.with(GLOWING, hasGlowInk);
    }

    @Override
    public SoftFluidTank getSoftFluidTank() {
        return fluidTank;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.fluidTank.load(tag);
        if (this.level != null) {
            if (this.level.isClientSide) {
                fluidTank.refreshTintCache();
                this.requestModelReload();
            }
        }
        this.hasGlowInk = tag.getBoolean("glow_ink");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        this.fluidTank.save(tag);
        if (this.hasGlowInk) tag.putBoolean("glow_ink", true);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public void setChanged() {
        if (this.level == null) return;
        //TODO: only call after you finished updating your tile so others can react properly (faucets)
        //this seems like a terrible idea
        this.level.updateNeighborsAt(worldPosition, this.getBlockState().getBlock());

        //update state based on fluid

        BlockState state = this.getBlockState();

        if (state.getBlock() instanceof ModCauldronBlock cb) {
            state = cb.updateStateOnFluidChange(state, level, worldPosition, fluidTank.getFluid());
        }

        if (state != this.getBlockState()) {
            this.level.setBlockAndUpdate(this.worldPosition, state);
        }

        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), state, Block.UPDATE_CLIENTS);

        super.setChanged();
    }


    // does all the calculation for handling player interaction.
    public boolean handleInteraction(Player player, InteractionHand hand) {
        //interact with fluid holder
        if (this.fluidTank.interactWithPlayer(player, hand, level, worldPosition)) {
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, worldPosition);
            this.setChanged();
            return true;
        }
        return false;
    }

    public void consumeOneLayer() {
        this.fluidTank.getFluid().shrink(1);
        this.setChanged();
    }


    public SoftFluidTank createCauldronLiquidTank() {
        return new SoftFluidTank(PlatHelper.getPlatform().isFabric() ? 3 : 4) {
            @Override
            public boolean isFluidCompatible(SoftFluidStack fluidStack) {
                if (fluidStack.is(BuiltInSoftFluids.WATER.get())) return false;
                if (canMixPotions() && fluidStack.is(BuiltInSoftFluids.POTION.get()) && fluidStack.is(this.getFluidValue())) {
                    // just compares bottle types
                    return this.fluidStack.getOrDefault(MoonlightRegistry.BOTTLE_TYPE.get(), PotionBottleType.REGULAR)
                            == fluidStack.getOrDefault(MoonlightRegistry.BOTTLE_TYPE.get(), PotionBottleType.REGULAR);
                }
                return super.isFluidCompatible(fluidStack);
            }

            @Override
            protected void addFluidOntoExisting(SoftFluidStack incoming) {
                if (canMixPotions() && incoming.is(BuiltInSoftFluids.POTION.get())) {
                    SoftFluidStack newStack = LiquidMixer.mixPotions(this.fluidStack, incoming);
                    this.setFluid(newStack);
                    needsColorRefresh = true;
                }
                super.addFluidOntoExisting(incoming);
            }
        };
    }

    public SoftFluidTank createCauldronDyeTank() {
        return new SoftFluidTank(3) {

            @Override
            public boolean isFluidCompatible(SoftFluidStack fluidStack) {
                if (fluidStack.is(ModRegistry.DYE_SOFT_FLUID.get()) && fluidStack.is(this.getFluidValue())) {
                    return true; //discard nbt
                } else return super.isFluidCompatible(fluidStack);
            }


            @Override
            protected void addFluidOntoExisting(SoftFluidStack fluidStack) {
                if (fluidStack.is(ModRegistry.DYE_SOFT_FLUID.get())) {
                    LiquidMixer.mixDye(this.fluidStack, fluidStack);
                }
                super.addFluidOntoExisting(fluidStack);
            }
        };
    }

    public boolean isGlowing() {
        return hasGlowInk;
    }

    public void setGlowing(boolean b) {
        this.hasGlowInk = b;
        this.setChanged();
    }
}
