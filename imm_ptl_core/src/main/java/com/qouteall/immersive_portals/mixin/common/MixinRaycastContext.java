package com.qouteall.immersive_portals.mixin.common;

import com.qouteall.immersive_portals.McHelper;
import com.qouteall.immersive_portals.ducks.IERayTraceContext;
import com.qouteall.immersive_portals.portal.Portal;
import com.qouteall.immersive_portals.portal.PortalPlaceholderBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RaycastContext.class)
public abstract class MixinRaycastContext implements IERayTraceContext {
    @SuppressWarnings("ShadowModifiers")
    @Shadow
    private Vec3d start;
    
    @SuppressWarnings("ShadowModifiers")
    @Shadow
    private Vec3d end;
    
    @Shadow
    @Final
    private RaycastContext.ShapeType shapeType;
    
    @Shadow
    @Final
    private ShapeContext entityPosition;
    
    @Override
    public IERayTraceContext setStart(Vec3d newStart) {
        start = newStart;
        return this;
    }
    
    @Override
    public IERayTraceContext setEnd(Vec3d newEnd) {
        end = newEnd;
        return this;
    }
    
    // portal placeholder does not have outline if colliding with portal
    // placeholder blocks entity view
    @Inject(
        at = @At("HEAD"),
        method = "getBlockShape",
        cancellable = true
    )
    private void onGetBlockShape(
        BlockState blockState,
        BlockView blockView,
        BlockPos blockPos,
        CallbackInfoReturnable<VoxelShape> cir
    ) {
        if (blockState.getBlock() == PortalPlaceholderBlock.instance) {
            if (shapeType == RaycastContext.ShapeType.OUTLINE) {
                if (blockView instanceof World) {
                    boolean isIntersectingWithPortal = McHelper.getEntitiesRegardingLargeEntities(
                        (World) blockView, new Box(blockPos),
                        10, Portal.class, e -> true
                    ).isEmpty();
                    if (!isIntersectingWithPortal) {
                        cir.setReturnValue(VoxelShapes.empty());
                    }
                }
            }
            else if (shapeType == RaycastContext.ShapeType.COLLIDER) {
                cir.setReturnValue(PortalPlaceholderBlock.instance.getOutlineShape(
                    blockState, blockView, blockPos, entityPosition
                ));
            }
        }
    }
}
