package net.mehvahdjukaar.amendments.common.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mehvahdjukaar.amendments.common.block.BoilingWaterCauldronBlock;
import net.mehvahdjukaar.amendments.common.block.ModCauldronBlock;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.platform.network.ChannelHandler;
import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

// we must send everything because when packet is received item entity might have been killed already
public record ClientBoundPlaySplashParticlesMessage(Vec3 hitPos, double speed, float width) implements Message {

    public ClientBoundPlaySplashParticlesMessage(FriendlyByteBuf buffer) {
        this(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                buffer.readDouble(), buffer.readFloat());
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeDouble(this.hitPos.x);
        friendlyByteBuf.writeDouble(this.hitPos.y);
        friendlyByteBuf.writeDouble(this.hitPos.z);
        friendlyByteBuf.writeDouble(this.speed);
        friendlyByteBuf.writeFloat(this.width);
    }

    @Override
    public void handle(ChannelHandler.Context context) {
        doOnClient();
    }

    @Environment(EnvType.CLIENT)
    public void doOnClient() {
        ClientLevel level = Minecraft.getInstance().level;

        BlockPos pos = BlockPos.containing(this.hitPos);
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ModCauldronBlock mc &&
                level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile tile) {
            int color = tile.getSoftFluidTank().getCachedParticleColor(level, pos);
            int light = tile.getSoftFluidTank().getFluidValue().getEmissivity();
            playSplashAnimation(level, pos, color, light);
        } else if (state.getBlock() instanceof BoilingWaterCauldronBlock) {
            int color = BoilingWaterCauldronBlock.getWaterColor(state, level, pos, 1);
            playSplashAnimation(level, pos, color, 0);
        }
    }

    public void playSplashAnimation(Level level, BlockPos pos,
                                    int color, int light) {

        RandomSource rand = level.random;

        float radius = 1.5f;

        spawnSplashParticles(level, hitPos, pos, rand, color, light,
                ModRegistry.BOILING_PARTICLE.get(), radius, width);

        spawnSplashParticles(level, hitPos, pos, rand, color, light,
                ModRegistry.SPLASH_PARTICLE.get(), radius, width);

    }

    public static void spawnSplashParticles(Level level, Vec3 hitPos, BlockPos pos,
                                            RandomSource rand,
                                            int color, int light,
                                            ParticleOptions particleOptions,
                                            float radius, float width) {
        float mx = pos.getX() + 0.125f;
        float Mx = pos.getX() + 1 - 0.125f;
        float mz = pos.getZ() + 0.125f;
        float Mz = pos.getZ() + 1 - 0.125f;

        double z;
        double x;
        double surface = hitPos.y();
        for (int i = 0; i < 1.0F + width * 20.0F; ++i) {
            x = hitPos.x() + (rand.nextDouble() - 0.5) * width * radius;
            z = hitPos.z() + (rand.nextDouble() - 0.5) * width * radius;
            if (x >= mx && x <= Mx && z >= mz && z <= Mz) {
                level.addParticle(particleOptions,
                        x, surface, z, color, surface, light);
            }
        }
    }

}
