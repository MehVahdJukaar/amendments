package net.mehvahdjukaar.amendments.common.tile;

import com.google.common.base.Preconditions;
import net.mehvahdjukaar.amendments.common.LiquidMixer;
import net.mehvahdjukaar.amendments.common.block.DyeCauldronBlock;
import net.mehvahdjukaar.amendments.common.block.LiquidCauldronBlock;
import net.mehvahdjukaar.amendments.common.block.ModCauldronBlock;
import net.mehvahdjukaar.amendments.common.item.DyeBottleItem;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.PotionBottleType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.mehvahdjukaar.amendments.common.block.LiquidCauldronBlock.getAllPotionEffects;


public class LiquidCauldronBlockTile extends BlockEntity implements IExtraModelDataProvider, ISoftFluidTankProvider {
    public static final ModelDataKey<ResourceKey<SoftFluid>> FLUID = (ModelDataKey<ResourceKey<SoftFluid>>) new ModelDataKey(ResourceKey.class);
    public static final ModelDataKey<Boolean> GLOWING = new ModelDataKey<>(Boolean.class);

    @Nullable
    private SoftFluidTank fluidTank;
    private boolean hasGlowInk = false;

    public LiquidCauldronBlockTile(BlockPos blockPos, BlockState blockState) {
        super(ModRegistry.LIQUID_CAULDRON_TILE.get(), blockPos, blockState);
    }

    @Override
    public void addExtraModelData(ExtraModelData.Builder builder) {
        builder.with(FLUID, getSoftFluidTank().getFluid().getHolder().unwrapKey().get());
        builder.with(GLOWING, hasGlowInk);
    }

    @Override
    public SoftFluidTank getSoftFluidTank() {
        initializeTank(Preconditions.checkNotNull(level).registryAccess());
        return fluidTank;
    }

    private void initializeTank(HolderLookup.Provider registries) {
        if (fluidTank == null) {
            fluidTank = (this.getBlockState().getBlock() instanceof DyeCauldronBlock) ?
                    createCauldronDyeTank(registries) :
                    createCauldronLiquidTank(registries);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        initializeTank(registries);
        this.getSoftFluidTank().load(tag, registries);
        if (this.level != null) {
            if (this.level.isClientSide) {
                getSoftFluidTank().refreshTintCache();
                this.requestModelReload();
            }
        }
        this.hasGlowInk = tag.getBoolean("glow_ink");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        initializeTank(registries);
        this.getSoftFluidTank().save(tag, registries);

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
            state = cb.updateStateOnFluidChange(state, level, worldPosition, getSoftFluidTank().getFluid());
        }

        if (state != this.getBlockState()) {
            this.level.setBlockAndUpdate(this.worldPosition, state);
        }

        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), state, Block.UPDATE_CLIENTS);

        super.setChanged();
    }


    // does all the calculation for handling player interaction.
    public boolean interactWithPlayerItem(Player player, InteractionHand hand, ItemStack stack) {
        //interact with fluid holder
        if (this.getSoftFluidTank().interactWithPlayer(player, hand, level, worldPosition)) {
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, worldPosition);
            this.setChanged();

            maybeSendPotionMixMessage(this.getSoftFluidTank().getFluid(), player);
            return true;
        }
        return false;
    }

    public void consumeOneLayer() {
        this.getSoftFluidTank().getFluid().shrink(1);
        this.setChanged();
    }

    public void maybeSendPotionMixMessage(SoftFluidStack stack, Player player) {
        if (stack.is(MLBuiltinSoftFluids.POTION)) {
            List<MobEffectInstance> potionEffects = getAllPotionEffects(stack);
            int potionEffectAmount = potionEffects.size();
            if (potionEffectAmount == CommonConfigs.POTION_MIXING_LIMIT.get()) {
                player.displayClientMessage(Component.translatable("message.amendments.cauldron"), true);
            }
        }
    }


    public boolean isGlowing() {
        return hasGlowInk;
    }

    public void setGlowing(boolean b) {
        this.hasGlowInk = b;
        this.setChanged();
    }


    private SoftFluidTank createCauldronLiquidTank(HolderLookup.Provider ra) {
        return new SoftFluidTank(PlatHelper.getPlatform().isFabric() ? 3 : 4, ra) {

            private boolean canMixPotions() {
                var config = CommonConfigs.POTION_MIXING.get();
                return config == CommonConfigs.MixingMode.ON || (config == CommonConfigs.MixingMode.ONLY_BOILING &&
                        LiquidCauldronBlockTile.this.getBlockState().getValue(LiquidCauldronBlock.BOILING));
            }

            @Override
            public boolean isFluidCompatible(SoftFluidStack fluidStack) {
                if (fluidStack.is(MLBuiltinSoftFluids.WATER)) return false;
                if (canMixPotions() && fluidStack.is(MLBuiltinSoftFluids.POTION) && fluidStack.is(this.getFluid().getHolder())) {
                    // just compares bottle types
                    return this.fluidStack.getOrDefault(MoonlightRegistry.BOTTLE_TYPE.get(), PotionBottleType.REGULAR)
                            == fluidStack.getOrDefault(MoonlightRegistry.BOTTLE_TYPE.get(), PotionBottleType.REGULAR);
                }
                return super.isFluidCompatible(fluidStack);
            }

            @Override
            protected void addFluidOntoExisting(SoftFluidStack incoming) {
                if (canMixPotions() && incoming.is(MLBuiltinSoftFluids.POTION)) {
                    SoftFluidStack newStack = LiquidMixer.mixPotions(this.fluidStack, incoming);
                    if (newStack != null) {
                        this.setFluid(newStack);
                        needsColorRefresh = true;
                    }
                }
                super.addFluidOntoExisting(incoming);
            }
        };

    }


    public SoftFluidTank createCauldronDyeTank(HolderLookup.Provider ra) {
        return new SoftFluidTank(3, ra) {

            @Override
            public boolean isFluidCompatible(SoftFluidStack fluidStack) {
                if (fluidStack.is(ModRegistry.DYE_SOFT_FLUID) && fluidStack.is(this.getFluid().getHolder())) {
                    return true; //discard nbt
                } else return super.isFluidCompatible(fluidStack);
            }


            @Override
            protected void addFluidOntoExisting(SoftFluidStack fluidStack) {
                if (fluidStack.is(ModRegistry.DYE_SOFT_FLUID)) {
                    var mixed = LiquidMixer.mixDye(this.fluidStack, fluidStack);
                    if (mixed != null) {
                        this.setFluid(mixed);
                        needsColorRefresh = true;
                    }
                }
                super.addFluidOntoExisting(fluidStack);
            }

            @Override
            public @Nullable ItemStack interactWithItem(ItemStack stack, Level world, @Nullable BlockPos pos, boolean simulate) {
                //always allows adding dye. they dont add water
                if (stack.getItem() instanceof DyeItem di) {
                    if (!simulate) {
                        //can always add dye
                        addDyeItem(di, world, pos);
                    }
                    return ItemStack.EMPTY;
                }
                return super.interactWithItem(stack, world, pos, simulate);
            }

            private void addDyeItem(DyeItem dyeItem, Level world, @Nullable BlockPos pos) {
                SoftFluidStack fluid = this.getFluid();
                if (!world.isClientSide()) {
                    int count = fluid.getCount();
                    if (count == 3) fluid.setCount(2); //hack!!
                    SoftFluidStack dummyStack = DyeBottleItem.createFluidStack(dyeItem.getDyeColor(), 1, world);

                    SoftFluidStack newFluid = LiquidMixer.mixDye(fluid, dummyStack);
                    if (newFluid != null) {
                        newFluid.setCount(count);

                        this.setFluid(newFluid);
                    }
                }
                if (pos != null) {
                    world.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    world.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.3f);
                }
            }
        };

    }
}
