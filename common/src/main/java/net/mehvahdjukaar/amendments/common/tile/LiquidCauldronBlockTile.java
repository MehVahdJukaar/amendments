package net.mehvahdjukaar.amendments.common.tile;

import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.AmendmentsPlatformStuff;
import net.mehvahdjukaar.amendments.common.block.DyeCauldronBlock;
import net.mehvahdjukaar.amendments.common.block.LiquidCauldronBlock;
import net.mehvahdjukaar.amendments.common.item.DyeBottleItem;
import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class LiquidCauldronBlockTile extends BlockEntity implements IExtraModelDataProvider, ISoftFluidTankProvider {
    public static final ModelDataKey<SoftFluid> FLUID = new ModelDataKey<>(SoftFluid.class);

    private final SoftFluidTank fluidHolder;

    public LiquidCauldronBlockTile(BlockPos blockPos, BlockState blockState) {
        this(blockPos, blockState, blockState.getBlock() instanceof DyeCauldronBlock ?
                AmendmentsPlatformStuff.createCauldronDyeTank() :
                AmendmentsPlatformStuff.createCauldronLiquidTank());
    }

    public LiquidCauldronBlockTile(BlockPos blockPos, BlockState blockState, SoftFluidTank tank) {
        super(ModRegistry.LIQUID_CAULDRON_TILE.get(), blockPos, blockState);
        this.fluidHolder = tank;
        //this.fluidHolder.setFluid(ModRegistry.DYE_SOFT_FLUID.get());
    }

    public static void mixPotions(SoftFluidTank softFluidTank, SoftFluid incoming, int amount, CompoundTag incomingTag) {
        CompoundTag nbt = softFluidTank.getNbt();
        if (nbt == null) return;
        int oldCount = softFluidTank.getCount();
        int newCount = oldCount + amount;
        List<MobEffectInstance> combinedEffects = new ArrayList<>();
        List<MobEffectInstance> existingEffects = PotionUtils.getAllEffects(nbt);
        List<MobEffectInstance> newEffects = PotionUtils.getAllEffects(incomingTag);
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


        nbt.putInt("CustomPotionColor", PotionUtils.getColor(mergedMap.values()));
        nbt.remove("Potion"); //remove normal potion
        saveEffects(nbt, mergedMap.values());
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

    public static Level getMeALevel() {
        MinecraftServer currentServer = PlatHelper.getCurrentServer();
        if (currentServer == null) {
            return AmendmentsClient.getClientLevel();
        }
        return currentServer.overworld();
    }

    public static void mixDye(SoftFluidTank softFluidTank, SoftFluid fluid, int amount, CompoundTag tag) {
        CompoundTag nbt = softFluidTank.getNbt();
        if (nbt == null) return;
        //TODO: attempt mix with recipe first
        DyeColor dye1 = DyeColor.RED;
        Level level = getMeALevel();
        var recipes = level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, new ColorContainer(dye1), level);
        for (var r : recipes) {
            ItemStack newDye = r.getResultItem(level.registryAccess());
        }
        int oldColor = nbt.getInt(DyeBottleItem.COLOR_TAG);
        int newColor = tag.getInt(DyeBottleItem.COLOR_TAG);
        int oldAmount = softFluidTank.getCount();
        CompoundTag nt = new CompoundTag();
        nt.putInt(DyeBottleItem.COLOR_TAG, new RGBColor(oldColor).asHCL()
                .mixWith(new RGBColor(newColor).asHCL(), (float) amount / (oldAmount + amount))
                .asRGB().toInt());
        softFluidTank.setNbt(nt);
    }

    private static class ColorContainer implements CraftingContainer {

        private final List<ItemStack> items = new ArrayList<>();

        public ColorContainer(DyeColor... it) {
            Arrays.stream(it).forEach(color -> items.add(DyeItem.byColor(color).getDefaultInstance()));
        }

        @Override
        public int getContainerSize() {
            return 4;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public ItemStack getItem(int slot) {
            return items.get(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return null;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return null;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
        }

        @Override
        public int getWidth() {
            return 2;
        }

        @Override
        public int getHeight() {
            return 2;
        }

        @Override
        public List<ItemStack> getItems() {
            return items;
        }

        @Override
        public void fillStackedContents(StackedContents helper) {

        }
    }


    @Override
    public ExtraModelData getExtraModelData() {
        return ExtraModelData.builder()
                .with(FLUID, fluidHolder.getFluid())
                .build();
    }

    @Override
    public SoftFluidTank getSoftFluidTank() {
        return fluidHolder;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.fluidHolder.load(compound);
        if (this.level != null) {
            if (this.level.isClientSide) this.requestModelReload();
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.fluidHolder.save(tag);
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
        int light = this.fluidHolder.getFluid().getLuminosity();
        BlockState state = this.getBlockState();
        if (light != state.getValue(ModBlockProperties.LIGHT_LEVEL)) {
            state = state.setValue(ModBlockProperties.LIGHT_LEVEL, light);
        }
        int height = this.fluidHolder.getCount();
        if (fluidHolder.isEmpty()) {
            state = Blocks.CAULDRON.defaultBlockState();
        } else if (height != state.getValue(LiquidCauldronBlock.LEVEL)) {
            state = state.setValue(LiquidCauldronBlock.LEVEL, height);
        }
        if (state != this.getBlockState()) {
            this.level.setBlock(this.worldPosition, state, 2);
        }
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        super.setChanged();
    }


    // does all the calculation for handling player interaction.
    public boolean handleInteraction(Player player, InteractionHand hand) {
        //interact with fluid holder
        if (this.fluidHolder.interactWithPlayer(player, hand, level, worldPosition)) {
            if (!level.isClientSide()) this.setChanged();
            return true;
        }
        SoftFluid fluid = this.fluidHolder.getFluid();
        if (fluid == ModRegistry.DYE_SOFT_FLUID.get()) {
            DyeColor dye = DyeBottleItem.getClosestDye(fluidHolder.getNbt().getInt(DyeBottleItem.COLOR_TAG));
            ItemStack stack = player.getItemInHand(hand);
            var recolored = BlocksColorAPI.changeColor(stack.getItem(), dye);

            if (recolored != null) player.setItemInHand(hand, recolored.getDefaultInstance());
            // try dye

        }
        return false;
    }

}
