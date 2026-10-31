package thunder.hack.utility.discord.callbacks;

import com.sun.jna.Callback;
import thunder.hack.utility.discord.DiscordUser;

public interface JoinRequestCallback extends Callback {
   void apply(DiscordUser var1);
}
