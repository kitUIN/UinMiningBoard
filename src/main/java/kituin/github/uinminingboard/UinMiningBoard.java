package kituin.github.uinminingboard;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.logging.LogUtils;
import kituin.github.uinminingboard.command.UinMiningBoardCommand;
import kituin.github.uinminingboard.config.UinMiningBoardConfig;
import kituin.github.uinminingboard.event.PlayerJoinedCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;

import static kituin.github.uinminingboard.Middleware.SCOREBOARD;
import static kituin.github.uinminingboard.Middleware.SCOREBOARD_OBJECTIVE;
import static net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED;

public class UinMiningBoard implements ModInitializer {
    public static String MOD_ID = "uin_mining_board";
    public static UinMiningBoardConfig CONFIG = UinMiningBoardConfig.loadConfig();
    public static Middleware MIDDLEWARE;
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        // 服务器启动
        SERVER_STARTED.register((server) -> {
            SCOREBOARD = new ServerScoreboard(server);
            MIDDLEWARE = new Middleware();
        });
        // 破坏方块事件
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
            Middleware.addScorePreBroken(player.getUuidAsString());
        });
        // 进入服务器事件
        PlayerJoinedCallback.EVENT.register((player) -> {

            MIDDLEWARE.putUuid2Name(player);
            SCOREBOARD.updateExistingObjective(SCOREBOARD_OBJECTIVE);
            SCOREBOARD.setObjectiveSlot(1, SCOREBOARD_OBJECTIVE);
            Middleware.updateDefault();
            return null;
        });
        // 指令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                LiteralArgumentBuilder.<ServerCommandSource>literal("uinminingboard").executes(UinMiningBoardCommand::help)
                        .then(LiteralArgumentBuilder.<ServerCommandSource>literal("help").executes(UinMiningBoardCommand::help))
                        .then(LiteralArgumentBuilder.<ServerCommandSource>literal("redirect").requires(source -> source.hasPermissionLevel(4))
                                .then(RequiredArgumentBuilder.<ServerCommandSource, EntitySelector>argument("player", EntityArgumentType.player())
                                        .executes(UinMiningBoardCommand::redirect)
                                ))
                        .then(LiteralArgumentBuilder.<ServerCommandSource>literal("ban").requires(source -> source.hasPermissionLevel(4))
                                .then(RequiredArgumentBuilder.<ServerCommandSource, EntitySelector>argument("player", EntityArgumentType.player())
                                        .executes(UinMiningBoardCommand::ban)
                                ))
                        .then(LiteralArgumentBuilder.<ServerCommandSource>literal("unban").requires(source -> source.hasPermissionLevel(4))
                                .then(RequiredArgumentBuilder.<ServerCommandSource, EntitySelector>argument("player", EntityArgumentType.player())
                                        .executes(UinMiningBoardCommand::unBan)
                                ))
                        .then(LiteralArgumentBuilder.<ServerCommandSource>literal("score").executes(UinMiningBoardCommand::findMyScore)
                                .then(RequiredArgumentBuilder.<ServerCommandSource, EntitySelector>argument("player", EntityArgumentType.player())
                                        .executes(UinMiningBoardCommand::findScore)
                                ))
        ));
    }
}
