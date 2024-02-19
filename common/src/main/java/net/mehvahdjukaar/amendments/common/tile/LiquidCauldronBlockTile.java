package net.mehvahdjukaar.amendments.common.tile;

import net.mehvahdjukaar.amendments.AmendmentsPlatformStuff;
import net.mehvahdjukaar.amendments.common.block.DyeCauldronBlock;
import net.mehvahdjukaar.amendments.common.block.HangingFlowerPotBlock;
import net.mehvahdjukaar.amendments.common.block.LiquidCauldronBlock;
import net.mehvahdjukaar.amendments.common.item.DyeBottleItem;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class LiquidCauldronBlockTile extends BlockEntity implements IExtraModelDataProvider, ISoftFluidTankProvider {
    public static final ModelDataKey<SoftFluid> FLUID = new ModelDataKey<>(SoftFluid.class);

    private final SoftFluidTank fluidTank;

    public SoftFluidTank makeTank(BlockState blockState) {
        return blockState.getBlock() instanceof DyeCauldronBlock ?
                AmendmentsPlatformStuff.createCauldronDyeTank() :
                AmendmentsPlatformStuff.createCauldronLiquidTank(this::canMixPotions);
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

    public static void mixPotions(SoftFluidStack tankFluid, SoftFluidStack newFluid) {
        CompoundTag tankTag = tankFluid.getTag();
        CompoundTag newTag = newFluid.getTag();
        if (tankTag == null || newTag == null) return;
        int oldCount = tankFluid.getCount();
        int newCount = oldCount + newFluid.getCount();
        List<MobEffectInstance> combinedEffects = new ArrayList<>();
        List<MobEffectInstance> existingEffects = PotionUtils.getAllEffects(tankTag);
        List<MobEffectInstance> newEffects = PotionUtils.getAllEffects(newFluid.getTag());
        float oldMult = oldCount / (float) newCount;
        float newMult = 1 - oldMult;
        combineEffects(combinedEffects, existingEffects, oldMult);
        combineEffects(combinedEffects, newEffects, newMult);
        //merge similar. assumes there are no triple effects. if we merge each time there shouldnt

        Map<MobEffect, MobEffectInstance> mergedMap = combinedEffects.stream()
                .collect(Collectors.toMap(
                        MobEffectInstance::getEffect,
                        effectInstance -> effectInstance,
                        LiquidCauldronBlockTile::mergeEffects));


        tankTag.putInt("CustomPotionColor", PotionUtils.getColor(mergedMap.values()));
        tankTag.remove("Potion"); //remove normal potion
        saveEffects(tankTag, mergedMap.values());
    }

    @NotNull
    private static MobEffectInstance mergeEffects(MobEffectInstance e, MobEffectInstance e1) {
        return new MobEffectInstance(e.getEffect(), (int) ((e.getDuration() + e1.getDuration()) / 2f),
                (int) ((e.getAmplifier() + e1.getAmplifier()) / 2f));
    }

    public static void saveEffects(CompoundTag tag, Collection<MobEffectInstance> effects) {
        ListTag listTag = new ListTag();
        for (MobEffectInstance mobEffectInstance : effects) {
            listTag.add(mobEffectInstance.save(new CompoundTag()));
        }
        tag.put("CustomPotionEffects", listTag);
    }

    private static void combineEffects(List<MobEffectInstance> combinedEffects,
                                       List<MobEffectInstance> current, float mult) {
        for (var e : current) {
            MobEffect effect = e.getEffect();
            MobEffectInstance newInstance;
            if (effect.isInstantenous()) {
                newInstance = new MobEffectInstance(effect, e.getDuration(), (int) (e.getAmplifier() * mult));
            } else {
                newInstance = new MobEffectInstance(effect, (int) (e.getDuration() * mult), e.getAmplifier());
            }
            combinedEffects.add(newInstance);
        }
    }

    public static void mixDye(SoftFluidStack tankFluid, SoftFluidStack newFluid) {
        CompoundTag tankTag = tankFluid.getTag();
        CompoundTag newTag = newFluid.getTag();
        if (tankTag == null || newTag == null) return;

        int oldColor = tankTag.getInt(DyeBottleItem.COLOR_TAG);
        int newColor = newTag.getInt(DyeBottleItem.COLOR_TAG);
        int oldAmount = tankFluid.getCount();
        int newAmount = newFluid.getCount();
        CompoundTag combinedTag = new CompoundTag();
        combinedTag.putInt(DyeBottleItem.COLOR_TAG, new RGBColor(oldColor).asHCL()
                .mixWith(new RGBColor(newColor).asHCL(), (float) newAmount / (oldAmount + newAmount))
                .asRGB().toInt());

        tankFluid.setTag(combinedTag);
    }



    @Override
    public ExtraModelData getExtraModelData() {
        return ExtraModelData.builder()
                .with(FLUID, fluidTank.getFluidValue())
                .build();
    }

    @Override
    public SoftFluidTank getSoftFluidTank() {
        return fluidTank;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.fluidTank.load(compound);
        if (this.level != null) {
            if (this.level.isClientSide) {
                fluidTank.refreshTintCache();
                this.requestModelReload();
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.fluidTank.save(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public void setChanged() {
        if (this.level == null) return;
        //TODO: only call after you finished updating your tile so others can react properly (faucets)
        this.level.updateNeighborsAt(worldPosition, this.getBlockState().getBlock());

        //update state based on fluid
        int light = this.fluidTank.getFluidValue().getLuminosity();
        BlockState state = this.getBlockState();
        if (light != state.getValue(ModBlockProperties.LIGHT_LEVEL)) {
            state = state.setValue(ModBlockProperties.LIGHT_LEVEL, light);
        }
        int height = this.fluidTank.getFluidCount();
        if (fluidTank.isEmpty()) {
            state = Blocks.CAULDRON.defaultBlockState();
        } else if (state.hasProperty(LiquidCauldronBlock.LEVEL)) {
            state = state.setValue(LiquidCauldronBlock.LEVEL, height);
        } else if (state.hasProperty(DyeCauldronBlock.LEVEL)) {
            state = state.setValue(DyeCauldronBlock.LEVEL, height);
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
             this.setChanged();
            return true;
        }
        return false;
    }

}
