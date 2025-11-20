package net.mehvahdjukaar.amendments.integration;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.jozufozu.flywheel.vanilla.BellInstance;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.common.IBellConnection;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public class VanillinCompat {

    public static final PartialModel BELL_CHAIN = new PartialModel(AmendmentsClient.BELL_CHAIN);
    public static final PartialModel BELL_ROPE = new PartialModel(AmendmentsClient.BELL_ROPE);

    public static void init() {
        InstancedRenderRegistry.configure(BlockEntityType.BELL).alwaysSkipRender()
                .factory(AmendmentsBellInstance::new).apply();
    }

    public static class AmendmentsBellInstance extends BellInstance {

        @Nullable
        private OrientedData connection;
        private IBellConnection.Type type;

        public AmendmentsBellInstance(MaterialManager materialManager, BellBlockEntity blockEntity) {
            super(materialManager, blockEntity);
            this.type = ((IBellConnection) blockEntity).amendments$getConnection();
            this.connection = updateConnection();
        }

        private OrientedData updateConnection() {
            if (type == IBellConnection.Type.NONE || !ClientConfigs.BELL_CONNECTION.get()) {
                return null;
            } else if (type == IBellConnection.Type.ROPE) {
                return this.getOrientedMaterial().getModel(BELL_ROPE, this.blockState).createInstance()
                        .setPosition(this.getInstancePosition());
            } else if (type == IBellConnection.Type.CHAIN) {
                return this.getOrientedMaterial().getModel(BELL_CHAIN, this.blockState).createInstance()
                        .setPosition(this.getInstancePosition());
            }
            return null;
        }

        @Override
        public boolean shouldReset() {
            return super.shouldReset() || this.type != ((IBellConnection) blockEntity).amendments$getConnection();
        }

        @Override
        public void beginFrame() {
            var type = ((IBellConnection) blockEntity).amendments$getConnection();
            if (type != this.type) {
                this.type = type;
                if (this.connection != null) this.connection.delete();
                this.connection = updateConnection();
                this.updateLight();
            }
            super.beginFrame();
        }

        @Override
        public void updateLight() {
            if (this.connection != null) {
                this.relight(this.getWorldPosition(), this.connection);
            }
            super.updateLight();
        }

        @Override
        public void remove() {
            super.remove();
            if (this.connection != null) this.connection.delete();
        }
    }

}
