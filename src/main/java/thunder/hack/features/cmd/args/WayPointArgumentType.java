package thunder.hack.features.cmd.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import thunder.hack.core.Managers;
import thunder.hack.core.manager.world.WayPointManager;
import thunder.hack.features.modules.client.ClientSettings;

public class WayPointArgumentType implements ArgumentType<WayPointManager.WayPoint> {
   private static final Collection<String> EXAMPLES = Managers.WAYPOINT.getWayPoints().stream().map(WayPointManager.WayPoint::getName).limit(5L).toList();

   public static WayPointArgumentType create() {
      return new WayPointArgumentType();
   }

   public WayPointManager.WayPoint parse(StringReader reader) throws CommandSyntaxException {
      WayPointManager.WayPoint wp = Managers.WAYPOINT.getWayPointByName(reader.readString());
      if (wp == null) {
         throw new DynamicCommandExceptionType(
               name -> Text.literal(
                     ClientSettings.isRu()
                        ? "\u0412\u0435\u0439\u043f\u043e\u0438\u043d\u0442\u0430 "
                           + name.toString()
                           + " \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442("
                        : "Waypoint " + name.toString() + " does not exist :("
                  )
            )
            .create(reader.readString());
      } else {
         return wp;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching(Managers.WAYPOINT.getWayPoints().stream().map(WayPointManager.WayPoint::getName), builder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
