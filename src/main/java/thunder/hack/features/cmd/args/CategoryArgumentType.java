package thunder.hack.features.cmd.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import thunder.hack.core.Managers;
import thunder.hack.features.modules.Module;
import thunder.hack.features.modules.client.ClientSettings;

public class CategoryArgumentType implements ArgumentType<String> {
   private static final List<String> EXAMPLES = Managers.MODULE.getCategories().stream().map(Module.Category::getName).toList();

   public static CategoryArgumentType create() {
      return new CategoryArgumentType();
   }

   public String parse(StringReader reader) throws CommandSyntaxException {
      String cat = reader.readString();
      if (!EXAMPLES.contains(cat)) {
         throw new DynamicCommandExceptionType(
               name -> Text.literal(
                     ClientSettings.isRu()
                        ? "\u041a\u0430\u0442\u0435\u0433\u043e\u0440\u0438\u0438 "
                           + name.toString()
                           + " \u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442("
                        : "Category " + name.toString() + " does not exist :("
                  )
            )
            .create(cat);
      } else {
         return cat;
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching(EXAMPLES, builder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
