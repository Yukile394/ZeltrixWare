package thunder.hack.features.cmd.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.io.File;
import java.util.Objects;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.command.CommandSource;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.Managers;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.cmd.args.CategoryArgumentType;
import thunder.hack.features.cmd.args.CfgArgumentType;
import thunder.hack.features.cmd.args.ModuleArgumentType;
import thunder.hack.features.modules.Module;

public class CfgCommand extends Command {
   public CfgCommand() {
      super("cfg", "config");
   }

   @Override
   public void executeBuild(@NotNull LiteralArgumentBuilder<CommandSource> builder) {
      builder.executes(
         context -> {
            StringBuilder configs = new StringBuilder("Configs: ");

            for (String str : Objects.requireNonNull(Managers.CONFIG.getConfigList())) {
               configs.append("\n- " + (str.equals(Managers.CONFIG.getCurrentConfig().getName().replace(".th", "")) ? Formatting.GREEN : ""))
                  .append(str)
                  .append(Formatting.RESET);
            }

            sendMessage(configs.toString());
            return 1;
         }
      );
      builder.then(
         literal("list")
            .executes(
               context -> {
                  StringBuilder configs = new StringBuilder("Configs: ");

                  for (String str : Objects.requireNonNull(Managers.CONFIG.getConfigList())) {
                     configs.append("\n- " + (str.equals(Managers.CONFIG.getCurrentConfig().getName().replace(".th", "")) ? Formatting.GREEN : ""))
                        .append(str)
                        .append(Formatting.RESET);
                  }

                  sendMessage(configs.toString());
                  return 1;
               }
            )
      );
      builder.then(literal("dir").executes(context -> {
         try {
            Util.getOperatingSystem().open(new File("ThunderHackRecode/configs/").toURI());
         } catch (Exception var2) {
            var2.printStackTrace();
         }

         return 1;
      }));
      builder.then(literal("save").then(arg("name", StringArgumentType.word()).executes(context -> {
         Managers.CONFIG.save((String)context.getArgument("name", String.class));
         return 1;
      })));
      builder.then(literal("loadcloud").then(arg("name", StringArgumentType.word()).executes(context -> {
         Managers.CONFIG.loadCloud((String)context.getArgument("name", String.class));
         return 1;
      })));
      builder.then(
         literal("cloudlist")
            .executes(
               context -> {
                  StringBuilder configs = new StringBuilder("Cloud Configs: \n");

                  for (String str : Objects.requireNonNull(Managers.CONFIG.getCloudConfigs())) {
                     String[] split = str.split(";");
                     configs.append(
                        "\n- "
                           + Formatting.BOLD
                           + split[0]
                           + Formatting.RESET
                           + Formatting.GRAY
                           + " author: "
                           + Formatting.RESET
                           + split[1]
                           + Formatting.GRAY
                           + " last updated: "
                           + Formatting.RESET
                           + split[2]
                     );
                  }

                  sendMessage(configs.toString());
                  return 1;
               }
            )
      );
      builder.then(literal("cloud").then(arg("name", StringArgumentType.word()).executes(context -> {
         Managers.CONFIG.loadCloud((String)context.getArgument("name", String.class));
         return 1;
      })));
      builder.then(
         literal("set")
            .then(((RequiredArgumentBuilder)arg("name", CfgArgumentType.create()).then(arg("module", ModuleArgumentType.create()).executes(context -> {
               Managers.CONFIG.loadModuleOnly((String)context.getArgument("name", String.class), (Module)context.getArgument("module", Module.class));
               return 1;
            }))).executes(context -> {
               Managers.CONFIG.load((String)context.getArgument("name", String.class));
               return 1;
            }))
      );
      builder.then(
         literal("load")
            .then(((RequiredArgumentBuilder)arg("name", CfgArgumentType.create()).then(arg("module", ModuleArgumentType.create()).executes(context -> {
               Managers.CONFIG.loadModuleOnly((String)context.getArgument("name", String.class), (Module)context.getArgument("module", Module.class));
               return 1;
            }))).executes(context -> {
               Managers.CONFIG.load((String)context.getArgument("name", String.class));
               return 1;
            }))
      );
      builder.then(literal("loadCategory").then(arg("name", CfgArgumentType.create()).then(arg("category", CategoryArgumentType.create()).executes(context -> {
         Managers.CONFIG.load((String)context.getArgument("name", String.class), (String)context.getArgument("category", String.class));
         return 1;
      }))));
      builder.then(literal("loadBinds").then(arg("name", CfgArgumentType.create()).executes(context -> {
         Managers.CONFIG.loadBinds((String)context.getArgument("name", String.class));
         return 1;
      })));
   }
}
