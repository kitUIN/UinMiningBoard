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
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

import static kituin.github.uinminingboard.Middleware.*;
import static net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED;
import static net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING;

public class UinMiningBoard implements ModInitializer {
    public static String MOD_ID = "uin_mining_board";
    public static UinMiningBoardConfig CONFIG = UinMiningBoardConfig.loadConfig();
    public static Middleware MIDDLEWARE;
    public static Timer TIMER;
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {

        // 服务器启动
        SERVER_STARTED.register((server) -> {
            SCOREBOARD = server.getScoreboard();
            try{
                MINE_OBJECTIVE = server.getScoreboard().addObjective(
                        MOD_ID + "_mine",
                        ScoreboardCriterion.DUMMY,
                        Text.literal(CONFIG.displayMineName),
                        ScoreboardCriterion.RenderType.INTEGER,
                        false,
                        null);
                DEATH_OBJECTIVE = server.getScoreboard().addObjective(
                        MOD_ID + "_deaths",
                        ScoreboardCriterion.DEATH_COUNT,
                        Text.literal(CONFIG.displayDeathName),
                        ScoreboardCriterion.RenderType.INTEGER,
                        false,
                        null
                );
                LOGGER.info("添加计分板");
            }catch (IllegalArgumentException e){
                MINE_OBJECTIVE = server.getScoreboard().getNullableObjective(MOD_ID + "_mine");
                DEATH_OBJECTIVE = server.getScoreboard().getNullableObjective(MOD_ID + "_deaths");
                LOGGER.info("加载计分板");
            }
            server.getScoreboard().updateObjective(MINE_OBJECTIVE);
            server.getScoreboard().updateObjective(DEATH_OBJECTIVE);
            MIDDLEWARE = new Middleware();
            TIMER = new Timer();
            TIMER.schedule(new TimerTask() {
                @Override
                public void run() {
                    ScoreboardObjective objectiveForSlot = SCOREBOARD.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
                    if (objectiveForSlot == null) {
                        SCOREBOARD.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, MINE_OBJECTIVE);
                        LOGGER.info("更换计分板->挖掘榜");
                    } else if (objectiveForSlot.equals(MINE_OBJECTIVE)) {
                        SCOREBOARD.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, DEATH_OBJECTIVE);
                        LOGGER.info("更换计分板->死亡榜");
                    } else {
                        SCOREBOARD.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, MINE_OBJECTIVE);
                        LOGGER.info("更换计分板->挖掘榜");
                    }
                }
            }, 0, CONFIG.interval * 1000L);
        });
        SERVER_STOPPING.register((server)->{
            TIMER.cancel();
        });
        // 破坏方块事件
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> Middleware.updateScorePreBroken((ServerPlayerEntity) player));
        // 进入服务器事件
        PlayerJoinedCallback.EVENT.register((player) -> {
            player.getServer().getScoreboard().updateExistingObjective(MINE_OBJECTIVE);
            player.getServer().getScoreboard().updateExistingObjective(DEATH_OBJECTIVE);
            return null;
        });

        // 指令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
                LiteralArgumentBuilder.<ServerCommandSource>literal("uinminingboard").executes(UinMiningBoardCommand::help)
                        .then(LiteralArgumentBuilder.<ServerCommandSource>literal("help").executes(UinMiningBoardCommand::help))
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
                        .then(LiteralArgumentBuilder.<ServerCommandSource>literal("death").executes(UinMiningBoardCommand::findMyDeath)
                                .then(RequiredArgumentBuilder.<ServerCommandSource, EntitySelector>argument("player", EntityArgumentType.player())
                                        .executes(UinMiningBoardCommand::findDeath)
                                ))
                        .then(LiteralArgumentBuilder.<ServerCommandSource>literal("reload").requires(source -> source.hasPermissionLevel(4))
                                .executes(UinMiningBoardCommand::reload)
                        )
                        .then(LiteralArgumentBuilder.<ServerCommandSource>literal("show")
                                .executes(UinMiningBoardCommand::show)
                        )

        ));
    }
}
