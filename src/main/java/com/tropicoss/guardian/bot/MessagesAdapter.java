package com.tropicoss.guardian.bot;

import static com.tropicoss.guardian.Mod.LOGGER;

import com.tropicoss.guardian.callbacks.DiscordChatCallback;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessagesAdapter extends ListenerAdapter {
  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    DiscordChatCallback.EVENT.invoker().dispatch(event.getMessage());
    LOGGER.info(event.getMessage().getContentDisplay());
  }
}
