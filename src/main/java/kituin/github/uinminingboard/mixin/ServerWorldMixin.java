package kituin.github.uinminingboard.mixin;

import kituin.github.uinminingboard.event.PlayerJoinedCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @Inject(at = @At("RETURN"), method = "onPlayerConnected", cancellable = true)
    public void onPlayerConnected(ServerPlayerEntity player, CallbackInfo ci) {
        ActionResult result = PlayerJoinedCallback.EVENT.invoker().interact(player);
        if (result != ActionResult.PASS) {
            ci.cancel();
        }
    }
}
