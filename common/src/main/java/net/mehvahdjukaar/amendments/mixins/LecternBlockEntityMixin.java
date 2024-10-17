package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.common.LecternEditMenu;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.world.level.block.LecternBlock.resetBookState;

@Mixin(LecternBlockEntity.class)
public abstract class LecternBlockEntityMixin extends BlockEntity implements Container {

    @Shadow
    @Final
    private ContainerData dataAccess;

    @Shadow
    @Final
    private Container bookAccess;

    protected LecternBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Shadow
    public abstract ItemStack getBook();

    @Shadow
    public abstract void setBook(ItemStack stack);

    @Shadow
    ItemStack book;

    @Shadow
    private int pageCount;

    @Shadow
    public abstract boolean hasBook();

    @Inject(method = "createMenu", at = @At("HEAD"), cancellable = true)
    public void createEditMenu(int i, Inventory inventory, Player player, CallbackInfoReturnable<AbstractContainerMenu> cir) {
        if (this.getBook().getItem() instanceof WritableBookItem && CommonConfigs.LECTERN_STUFF.get()) {
            cir.setReturnValue(new LecternEditMenu(i, (LecternBlockEntity) (Object) this, this.dataAccess));
        }
    }

    @Inject(method = "setPage", at = @At("HEAD"))
    public void setPage(int page, CallbackInfo ci) {
        // increments max number of pages
        if (page >= this.pageCount && page <= 100 &&
                this.book.getItem() instanceof WritableBookItem && CommonConfigs.LECTERN_STUFF.get()) {
            //weirdness because empty book has 0 pages and we want to skip to second page here
            if (pageCount == 0) this.pageCount += 2;
            else this.pageCount++;
        }
    }


    @Override
    public int getContainerSize() {
        return bookAccess.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return bookAccess.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return bookAccess.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return bookAccess.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return bookAccess.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.setBook(stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return bookAccess.stillValid(player);
    }

    @Override
    public void clearContent() {
        bookAccess.clearContent();
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return stack.is(ModTags.GOES_IN_LECTERN);
    }


    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null) {
            BlockState state = this.getBlockState();
            //not using has book so it works on more tags
            boolean hasBook = !this.getItem(0).isEmpty();
            if (state.getValue(LecternBlock.HAS_BOOK) != hasBook) {
                //set changed might be the only method called after say a hopper changes the content of this so we might beed to udpate the state
                resetBookState(null, level, worldPosition, getBlockState(), hasBook);
            }
        }
        //this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
