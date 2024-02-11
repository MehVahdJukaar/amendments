package net.mehvahdjukaar.amendments.common.item.placement;

import net.mehvahdjukaar.amendments.common.block.WallLanternBlock;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Supplier;

public class WallLanternPlacement extends AdditionalItemPlacement {

    public WallLanternPlacement() {
        super(ModRegistry.WALL_LANTERN.get());
    }

    @Override
    public InteractionResult overridePlace(BlockPlaceContext pContext) {
        if (CompatHandler.TORCHSLAB) {
            double y = pContext.getClickLocation().y() % 1;
            if (y < 0.5) return null;
        }
        return super.overridePlace(pContext);
    }


    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        if (ClientConfigs.PLACEABLE_TOOLTIP.get()) {
            pTooltipComponents.add(Component.translatable("message.supplementaries.wall_lantern").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
        }
    }
}
