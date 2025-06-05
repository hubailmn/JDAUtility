package cc.hubailmn.util.user;

import cc.hubailmn.util.log.CSend;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class UserUtil {

    public static void sendPrivateMessage(User user, String message) {
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(message))
                .queue(
                        success -> {
                        },
                        error -> handlePrivateMessageError(user, error)
                );
    }

    public static void sendPrivateMessage(User user, EmbedBuilder embed) {
        user.openPrivateChannel()
                .flatMap(channel -> channel.sendMessageEmbeds(embed.build()))
                .queue(
                        success -> {
                        },
                        error -> handlePrivateMessageError(user, error)
                );
    }

    private static void handlePrivateMessageError(User user, Throwable error) {
        if (error instanceof ErrorResponseException err) {
            if (err.getErrorCode() == 50007) {
                CSend.warn(String.format("❌ Cannot send DM to %s (%s): DMs are disabled.%n",
                        user.getAsTag(), user.getId())
                );
            } else {
                CSend.warn(String.format("❌ Failed to send DM to %s (%s): %s%n",
                        user.getAsTag(), user.getId(), err.getMessage())
                );
            }
        } else {
            CSend.warn(String.format("❌ Unknown error sending DM to %s (%s): %s%n",
                    user.getAsTag(), user.getId(), error.getMessage())
            );
        }
    }
}
