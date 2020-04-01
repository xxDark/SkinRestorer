package me.xdark.skinrestorer.commands;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.xdark.skinrestorer.I18n;
import me.xdark.skinrestorer.util.Formatting;
import org.bukkit.command.CommandSender;

import java.util.function.Consumer;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class ErrorCallback implements Consumer<Throwable> {
	private final CommandSender sender;
	private final I18n i18n;

	@Override
	public void accept(Throwable throwable) {
		this.sender.sendMessage(this.i18n.format("profile.set.failed", Formatting.throwable(throwable)));
	}
}
