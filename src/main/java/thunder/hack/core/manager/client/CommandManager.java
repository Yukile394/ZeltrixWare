package thunder.hack.core.manager.client;

import com.mojang.brigadier.CommandDispatcher;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Formatting;
import net.minecraft.command.CommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommandSource;
import org.jetbrains.annotations.NotNull;
import thunder.hack.core.manager.IManager;
import thunder.hack.features.cmd.Command;
import thunder.hack.features.cmd.impl.AddonsCommand;
import thunder.hack.features.cmd.impl.BenchMarkCommand;
import thunder.hack.features.cmd.impl.BindCommand;
import thunder.hack.features.cmd.impl.BlockESPCommand;
import thunder.hack.features.cmd.impl.CfgCommand;
import thunder.hack.features.cmd.impl.ChestStealerCommand;
import thunder.hack.features.cmd.impl.DrawCommand;
import thunder.hack.features.cmd.impl.DropAllCommand;
import thunder.hack.features.cmd.impl.EClipCommand;
import thunder.hack.features.cmd.impl.FriendCommand;
import thunder.hack.features.cmd.impl.GamemodeCommand;
import thunder.hack.features.cmd.impl.GarbageCleanerCommand;
import thunder.hack.features.cmd.impl.GetNbtCommand;
import thunder.hack.features.cmd.impl.GotoWaypointCommand;
import thunder.hack.features.cmd.impl.GpsCommand;
import thunder.hack.features.cmd.impl.HClipCommand;
import thunder.hack.features.cmd.impl.HelpCommand;
import thunder.hack.features.cmd.impl.HorseSpeedCommand;
import thunder.hack.features.cmd.impl.InvCleanerCommand;
import thunder.hack.features.cmd.impl.KitCommand;
import thunder.hack.features.cmd.impl.LoginCommand;
import thunder.hack.features.cmd.impl.MacroCommand;
import thunder.hack.features.cmd.impl.ModuleCommand;
import thunder.hack.features.cmd.impl.NukerCommand;
import thunder.hack.features.cmd.impl.OpenFolderCommand;
import thunder.hack.features.cmd.impl.PrefixCommand;
import thunder.hack.features.cmd.impl.RctCommand;
import thunder.hack.features.cmd.impl.ResetBindsCommand;
import thunder.hack.features.cmd.impl.RpcCommand;
import thunder.hack.features.cmd.impl.StaffCommand;
import thunder.hack.features.cmd.impl.TabParseCommand;
import thunder.hack.features.cmd.impl.TrackerCommand;
import thunder.hack.features.cmd.impl.TreasureCommand;
import thunder.hack.features.cmd.impl.VClipCommand;
import thunder.hack.features.cmd.impl.WayPointCommand;

public class CommandManager implements IManager {
   private String prefix = "@";
   private final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher();
   private final CommandSource source = new ClientCommandSource(null, MinecraftClient.getInstance());
   private final List<Command> commands = new ArrayList<>();

   public CommandManager() {
      this.add(new RpcCommand());
      this.add(new KitCommand());
      this.add(new GpsCommand());
      this.add(new CfgCommand());
      this.add(new RctCommand());
      this.add(new BindCommand());
      this.add(new DrawCommand());
      this.add(new HelpCommand());
      this.add(new NukerCommand());
      this.add(new EClipCommand());
      this.add(new HClipCommand());
      this.add(new LoginCommand());
      this.add(new MacroCommand());
      this.add(new StaffCommand());
      this.add(new VClipCommand());
      this.add(new AddonsCommand());
      this.add(new GetNbtCommand());
      this.add(new FriendCommand());
      this.add(new ModuleCommand());
      this.add(new PrefixCommand());
      this.add(new TrackerCommand());
      this.add(new GamemodeCommand());
      this.add(new DropAllCommand());
      this.add(new TreasureCommand());
      this.add(new WayPointCommand());
      this.add(new TabParseCommand());
      this.add(new BlockESPCommand());
      this.add(new BenchMarkCommand());
      this.add(new HorseSpeedCommand());
      this.add(new OpenFolderCommand());
      this.add(new ResetBindsCommand());
      this.add(new InvCleanerCommand());
      this.add(new GotoWaypointCommand());
      this.add(new ChestStealerCommand());
      this.add(new GarbageCleanerCommand());
   }

   private void add(@NotNull Command command) {
      command.register(this.dispatcher);
      this.commands.add(command);
   }

   public String getPrefix() {
      return this.prefix;
   }

   public void setPrefix(String prefix) {
      this.prefix = prefix;
   }

   public Command get(Class<? extends Command> commandClass) {
      for (Command command : this.commands) {
         if (command.getClass().equals(commandClass)) {
            return command;
         }
      }

      return null;
   }

   @NotNull
   public static String getClientMessage() {
      return Formatting.WHITE + "\u230a" + Formatting.GOLD + "\u26a1" + Formatting.WHITE + "\u2309" + Formatting.RESET;
   }

   public List<Command> getCommands() {
      return this.commands;
   }

   public CommandSource getSource() {
      return this.source;
   }

   public CommandDispatcher<CommandSource> getDispatcher() {
      return this.dispatcher;
   }

   public void registerCommand(Command command) {
      if (command != null) {
         command.register(this.dispatcher);
         this.commands.add(command);
      }
   }
}
