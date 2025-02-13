package org.xiyu.yee.capemanages.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.xiyu.yee.capemanages.cape.CapeManager;
import org.xiyu.yee.capemanages.cape.CapeType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.client.Minecraft;
import org.xiyu.yee.capemanages.network.CapeUpdatePacket;

import javax.xml.stream.events.Comment;
import java.awt.event.ComponentListener;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class CapeCommand {
    public static final String PREFIX = "§8[§bCape§8] §r";
    public static final String ERROR = "§8[§cCape§8] §r";
    
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_CAPES = (context, builder) -> {
        String input = builder.getRemaining().toLowerCase();
        return SharedSuggestionProvider.suggest(
            Arrays.stream(CapeType.values())
                .map(CapeType::getId)
                .filter(id -> id.startsWith(input)),
            builder
        );
    };

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_SUBCOMMANDS = (context, builder) -> {
        return SharedSuggestionProvider.suggest(
            Stream.of("toggle").filter(s -> s.startsWith(builder.getRemaining().toLowerCase())), 
            builder
        );
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cape")
            .then(Commands.literal("list").executes(CapeCommand::listCapes))
            .then(Commands.literal("set")
                .then(Commands.argument("type", StringArgumentType.word())
                    .suggests(SUGGEST_CAPES)
                    .executes(context -> setCape(
                        context.getSource(),
                        StringArgumentType.getString(context, "type")
                    ))))
            .then(Commands.literal("toggle")
                .executes(CapeCommand::toggleCape))
            .then(Commands.literal("update")
                .executes(CapeCommand::updateCapes))
            .then(Commands.literal("help")
                .executes(CapeCommand::showHelp))
            .executes(CapeCommand::showHelp));

        dispatcher.register(Commands.literal("cape")
            .then(Commands.literal("custom")
                .executes(CapeCommand::setCustomCape)));
    }

    private static int setCape(CommandSourceStack source, String type) {
        try {
            CapeType capeType = CapeType.valueOf(type.toUpperCase());
            String playerName = source.getPlayerOrException().getName().getString();
            CapeManager.INSTANCE.setCape(playerName, capeType);
            source.sendSuccess(new TextComponent(PREFIX + "§a已将披风设置为: §e" + capeType.getDisplayName()), false);
            return 1;
        } catch (IllegalArgumentException e) {
            source.sendFailure(new TextComponent(ERROR + "§c未知的披风类型: §e" + type));
            showAvailableCapes(source);
            return 0;
        } catch (Exception e) {
            source.sendFailure(new TextComponent(ERROR + "§c设置披风时出错: §e" + e.getMessage()));
            return 0;
        }
    }

    private static int setCustomCape(CommandContext<CommandSourceStack> context) {
        try {
            String playerName = context.getSource().getPlayerOrException().getName().getString();
            CapeManager.INSTANCE.setCustomCape(playerName);
            context.getSource().sendSuccess(new TextComponent(PREFIX + "§a正在设置自定义披风..."), false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(new TextComponent(ERROR + "§c设置自定义披风时出错: §e" + e.getMessage()));
            return 0;
        }
    }

    private static int toggleCape(CommandContext<CommandSourceStack> context) {
        boolean enabled = !CapeManager.INSTANCE.isEnabled();
        CapeManager.INSTANCE.setEnabled(enabled);
        context.getSource().sendSuccess(
            new TextComponent(PREFIX + (enabled ? "§a披风已启用" : "§c披风已禁用")),
            false
        );
        return 1;
    }

    private static int showHelp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(new TextComponent("§b=== Cape 命令帮助 ==="), false);
        source.sendSuccess(new TextComponent("§6/cape set <类型> §7- 设置预定义披风"), false);
        source.sendSuccess(new TextComponent("§6/cape toggle §7- 开启/关闭披风显示"), false);
        source.sendSuccess(new TextComponent("§6/cape update §7- 更新披风并同步其他玩家披风"), false);
        source.sendSuccess(new TextComponent("§6/cape help §7- 显示此帮助信息"), false);
        showAvailableCapes(source);
        return 1;
    }

    private static void showAvailableCapes(CommandSourceStack source) {
        source.sendSuccess(new TextComponent("§b=== 可用的披风类型 ==="), false);
        StringBuilder types = new StringBuilder();
        for (CapeType type : CapeType.values()) {
            if (types.length() > 0) {
                types.append("§7, ");
            }
            types.append("§e").append(type.name().toLowerCase());
        }
        source.sendSuccess(new TextComponent(types.toString()), false);
    }

    private static int updateCapes(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (context.getSource().getEntity() instanceof ServerPlayer) {
            String playerName = context.getSource().getPlayerOrException().getName().getString();
            
            try {
                context.getSource().sendSuccess(new TextComponent(PREFIX + "§a正在同步披风..."), false);
                
                // 获取当前玩家的披风URL并同步
                String ownCapeUrl = CapeManager.INSTANCE.getCapeUrl(playerName);
                if (ownCapeUrl != null) {
                    CapeUpdatePacket packet = new CapeUpdatePacket(playerName, ownCapeUrl);
                    CapeManager.NETWORK.sendToServer(packet);
                }
                
                // 获取所有在线玩家并同步他们的披风
                if (Minecraft.getInstance().level != null) {
                    Minecraft.getInstance().level.players().forEach(player -> {
                        String otherPlayerName = player.getName().getString();
                        // 不需要再次同步自己的披风
                        if (!otherPlayerName.equals(playerName)) {
                            String capeUrl = CapeManager.INSTANCE.getCapeUrl(otherPlayerName);
                            if (capeUrl != null) {
                                System.out.println("Syncing cape for player: " + otherPlayerName);
                                CapeUpdatePacket packet = new CapeUpdatePacket(otherPlayerName, capeUrl);
                                CapeManager.NETWORK.sendToServer(packet);
                            }
                        }
                    });
                }
                
                context.getSource().sendSuccess(new TextComponent(PREFIX + "§a披风同步完成"), false);
                return 1;
            } catch (Exception e) {
                context.getSource().sendFailure(new TextComponent(ERROR + "§c同步披风时出错: §e" + e.getMessage()));
                return 0;
            }
        }
        return 0;
    }

    private static int listCapes(CommandContext<CommandSourceStack> context) {
        try {
            String playerName = context.getSource().getPlayerOrException().getName().getString();
            String currentCape = CapeManager.INSTANCE.getCurrentCape(playerName);
            
            context.getSource().sendSuccess(new TextComponent("§b=== 当前披风信息 ==="), false);
            if (currentCape != null) {
                context.getSource().sendSuccess(new TextComponent(PREFIX + "§a当前使用的披风: §e" + currentCape), false);
            } else {
                context.getSource().sendSuccess(new TextComponent(PREFIX + "§c当前未使用任何披风"), false);
            }
            
            // 显示所有可用的披风类型
            showAvailableCapes(context.getSource());
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(new TextComponent(ERROR + "§c获取披风信息时出错: §e" + e.getMessage()));
            return 0;
        }
    }
}