package net.mehvahdjukaar.amendments.common.network;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.common.LecternEditMenu;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.TextFilter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WritableBookItem;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.LecternBlockEntity;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ServerBoundSyncLecternBookMessage implements Message {


    public static final TypeAndCodec<RegistryFriendlyByteBuf, ServerBoundSyncLecternBookMessage> TYPE = Message.makeType(
            Amendments.res("server_bound_sync_lectern_book"), ServerBoundSyncLecternBookMessage::new);


    private final List<String> pages;
    private final Optional<String> title;
    private final BlockPos pos;
    private final boolean takeBook;

    public ServerBoundSyncLecternBookMessage(BlockPos pos, List<String> list, Optional<String> optional, boolean takeBook) {
        this.pos = pos;
        this.pages = ImmutableList.copyOf(list);
        this.title = optional;
        this.takeBook = takeBook;
    }

    public ServerBoundSyncLecternBookMessage(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.pages = buffer.readCollection(FriendlyByteBuf.limitValue(Lists::newArrayListWithCapacity, 200),
                b -> b.readUtf(8192));
        this.title = buffer.readOptional(b -> b.readUtf(128));
        this.takeBook = buffer.readBoolean();
    }

    @Override
    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
        buffer.writeCollection(this.pages, (friendlyByteBuf, string) -> {
            friendlyByteBuf.writeUtf(string, 8192);
        });
        buffer.writeOptional(this.title, (friendlyByteBuf, string) -> {
            friendlyByteBuf.writeUtf(string, 128);
        });
        buffer.writeBoolean(this.takeBook);
    }

    @Override
    public void handle(Context context) {

        ServerPlayer player = (ServerPlayer) context.getPlayer();
        Level level = player.level();
        if (level.getBlockEntity(pos) instanceof LecternBlockEntity be) {
            ItemStack book = be.getBook();
            if (book.getItem() instanceof WritableBookItem) {
                List<String> list = Lists.newArrayList();
                Objects.requireNonNull(list);
                title.ifPresent(list::add);
                Stream<String> limit = pages.stream().limit(100L);
                Objects.requireNonNull(list);
                limit.forEach(list::add);
                Consumer<List<FilteredText>> consumer = title.isPresent() ?
                        l -> this.signBook(be, player, book, l.get(0), l.subList(1, l.size())) :
                        l -> this.updateBookContents(be, player, book, l);
                this.filterTextPacket(player, list, TextFilter::processMessageBundle)
                        .thenAcceptAsync(consumer, level.getServer());
            }
        }
    }

    private <T, R> CompletableFuture<R> filterTextPacket(ServerPlayer player,
                                                         T message, BiFunction<TextFilter, T, CompletableFuture<R>> processor) {
        return processor.apply(player.getTextFilter(), message).thenApply((object) -> {
            if (!player.connection.isAcceptingMessages()) {
                //  LOGGER.debug("Ignoring packet due to disconnection");
                throw new CancellationException("disconnected");
            } else {
                return object;
            }
        });
    }

    private void updateBookContents(LecternBlockEntity be, ServerPlayer player, ItemStack itemstack, List<FilteredText> pages) {
        if (itemstack.is(Items.WRITABLE_BOOK)) {
            List<Filterable<String>> list = pages.stream().map(t -> filterableFromOutgoing(t, player)).toList();
            itemstack.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(list));
            be.setChanged();
            be.getLevel().sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 3);
        }
        //needs to happen here otherwise the book is taken before the packet is sent
        if (takeBook) {
            if (player.containerMenu instanceof LecternEditMenu m) {
                //take button menu
                m.clickMenuButton(player, 3);
            }
        }
    }

    private void signBook(LecternBlockEntity be, ServerPlayer player, ItemStack book,
                          FilteredText title, List<FilteredText> pages) {
        ItemStack signedBook = book.transmuteCopy(Items.WRITTEN_BOOK);
        signedBook.remove(DataComponents.WRITABLE_BOOK_CONTENT);
        List<Filterable<Component>> filteredPages = pages.stream().map((t) ->
                this.filterableFromOutgoing(t, player).map(s -> (Component) Component.literal(s))).toList();
        signedBook.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(this.filterableFromOutgoing(title, player),
                player.getName().getString(), 0, filteredPages, true));


        be.setBook(signedBook);
        be.setChanged();
        player.level().sendBlockUpdated(pos, be.getBlockState(), be.getBlockState(), 3);

        //needs to happen here otherwise the book is taken before the packet is sent
        if (takeBook) {
            if (player.containerMenu instanceof LecternEditMenu m) {
                //take button menu
                m.clickMenuButton(player, 3);
            }
        }
    }

    private Filterable<String> filterableFromOutgoing(FilteredText filteredText, ServerPlayer player) {
        return player.isTextFilteringEnabled() ? Filterable.passThrough(filteredText.filteredOrEmpty()) : Filterable.from(filteredText);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE.type();
    }
}
