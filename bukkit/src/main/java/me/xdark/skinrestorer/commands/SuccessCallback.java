package me.xdark.skinrestorer.commands;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.xdark.skinrestorer.I18n;
import org.bukkit.command.CommandSender;

import java.util.function.Consumer;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class SuccessCallback<ANY> implements Consumer<ANY> {
	private final CommandSender sender;
	private final I18n i18n;

	@Override
	public void accept(ANY any) {
		this.sender.sendMessage(this.i18n.format("profile.set.success"));
	}
}
