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
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

import static kituin.github.uinminingboard.Middleware.*;
import static net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED;

public class UinMiningBoard implements ModInitializer {
    public static String MOD_ID = "uin_mining_board";
    public static UinMiningBoardConfig CONFIG = UinMiningBoardConfig.loadConfig();
    public static Middleware MIDDLEWARE;
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {

        // 服务器启动
        SERVER_STARTED.register((server) -> {
            SCOREBOARD = server.getScoreboard();
            try{

                MINE_OBJECTIVE = server.getScoreboard().addObjective(MOD_ID + "_mine", ScoreboardCriterion.DUMMY, Text.literal(CONFIG.displayMineName), ScoreboardCriterion.RenderType.INTEGER);
                DEATH_OBJECTIVE = server.getScoreboard().addObjective("deaths", ScoreboardCriterion.DEATH_COUNT, Text.literal(CONFIG.displayDeathName), ScoreboardCriterion.RenderType.INTEGER);
                LOGGER.info("添加计分板");
            }catch (IllegalArgumentException e){
                MINE_OBJECTIVE = server.getScoreboard().getObjective(MOD_ID + "_mine");
                DEATH_OBJECTIVE = server.getScoreboard().getObjective("deaths");
                LOGGER.info("加载计分板");
            }
            server.getScoreboard().updateObjective(MINE_OBJECTIVE);
            server.getScoreboard().updateObjective(DEATH_OBJECTIVE);
            MIDDLEWARE = new Middleware();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {

                    ScoreboardObjective objectiveForSlot = SCOREBOARD.getObjectiveForSlot(1);
                    if (objectiveForSlot == null) {
                        SCOREBOARD.setObjectiveSlot(1, MINE_OBJECTIVE);
                        LOGGER.info("更换计分板->挖掘榜");
                    } else if (objectiveForSlot.equals(MINE_OBJECTIVE)) {
                        SCOREBOARD.setObjectiveSlot(1, DEATH_OBJECTIVE);
                        LOGGER.info("更换计分板->死亡榜");
                    } else {
                        SCOREBOARD.setObjectiveSlot(1, MINE_OBJECTIVE);
                        LOGGER.info("更换计分板->挖掘榜");
                    }
                }
            }, 0, CONFIG.interval * 1000L);
        });
        // 破坏方块事件
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> Middleware.addScorePreBroken(player.getUuidAsString()));
        // 进入服务器事件
        PlayerJoinedCallback.EVENT.register((player) -> {
            MIDDLEWARE.putUuid2Name(player);
            player.getServer().getScoreboard().updateExistingObjective(MINE_OBJECTIVE);
            player.getServer().getScoreboard().updateExistingObjective(DEATH_OBJECTIVE);
            Middleware.show(player.getServer());
            Middleware.updateAll();
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
