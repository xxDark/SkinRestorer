package me.xdark.skinrestorer.commands;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.xdark.skinrestorer.I18n;
import me.xdark.skinrestorer.util.Formatting;
import org.bukkit.command.CommandSender;

import java.util.function.BiConsumer;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class NotificationCallback implements BiConsumer<Object, Throwable> {
	private final I18n i18n;
	private final CommandSender sender;
	private final boolean notifySuccess;

	@Override
	public void accept(Object t, Throwable throwable) {
		if (throwable == null) {
			if (this.notifySuccess) {
				this.sender.sendMessage(this.i18n.format("profile.set.success"));
			}
		} else {
			this.sender.sendMessage(this.i18n.format("profile.set.failed", Formatting.throwable(throwable)));
		}
	}
}
