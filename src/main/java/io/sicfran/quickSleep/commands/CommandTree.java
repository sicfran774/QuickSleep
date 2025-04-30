package io.sicfran.quickSleep.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.sicfran.quickSleep.QuickSleep;

@SuppressWarnings("UnstableApiUsage")
public class CommandTree {

    private final SleepCommand sleepCommand;
    private final MessageCommand messageCommand;

    public CommandTree(QuickSleep plugin){
        sleepCommand = new SleepCommand(plugin);
        messageCommand = new MessageCommand(plugin);
    }

    public LiteralArgumentBuilder<CommandSourceStack> createCommand(){
        return Commands.literal("sleep")
                .then(Commands.literal("confirm") // /sleep confirm
                    .requires(ctx -> ctx.getSender().hasPermission(CommandsPermissions.CONFIRM))
                    .executes(sleepCommand::sleepConfirm)
                )
                .then(Commands.literal("cancel") // /sleep cancel
                    .requires(ctx -> ctx.getSender().hasPermission(CommandsPermissions.CANCEL))
                    .executes(sleepCommand::sleepCancel)
                ).then(Commands.literal("timer")
                        .then(Commands.argument("seconds", IntegerArgumentType.integer(5,15))
                            .requires(ctx -> ctx.getSender().hasPermission(CommandsPermissions.TIMER))
                            .executes(sleepCommand::sleepTimer)
                        )
                )
                .then(Commands.literal("message")
                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                .executes(messageCommand::changeWakeupMessage)
                        )
                        .then(Commands.argument("player", ArgumentTypes.player())
                                .then(Commands.argument("message", StringArgumentType.greedyString())
                                        .executes(messageCommand::changeWakeupMessage)
                                )
                        )
                );
    }
}
