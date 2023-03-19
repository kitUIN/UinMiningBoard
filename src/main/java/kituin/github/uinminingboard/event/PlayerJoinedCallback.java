package kituin.github.uinminingboard.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

public interface PlayerJoinedCallback {
    Event<PlayerJoinedCallback> EVENT = EventFactory.createArrayBacked(PlayerJoinedCallback.class,
            (listeners) -> (player) -> {
                for (PlayerJoinedCallback event : listeners) {
                    ActionResult result = event.interact(player);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            }
    );

    ActionResult interact(ServerPlayerEntity player);
}

 
