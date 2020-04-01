package me.xdark.skinrestorer.commands;

import com.mojang.authlib.GameProfile;
import lombok.RequiredArgsConstructor;
import lombok.val;
import me.xdark.skinrestorer.I18n;
import me.xdark.skinrestorer.SkinRestorer;
import me.xdark.skinrestorer.cache.ProfileCache;
import me.xdark.skinrestorer.util.UtilFuture;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public final class CommandSkin implements CommandExecutor {
	private final SkinRestorer skinRestorer;
	private final ExecutorService ioService;
	private final ExecutorService syncExecutorService;
	private final I18n i18n;
	private final ProfileCache profileCache;

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("§cOnly players might use that command");
			return true;
		}
		val len = args.length;
		if (len == 0) {
			sendHelp(sender, label);
			return true;
		}
		val arg0 = args[0];
		if (arg0.equalsIgnoreCase("reset")) {
			if (this.skinRestorer.resetProfile(((Player) sender).getUniqueId())) {
				sender.sendMessage(this.i18n.format("profile.reset.success"));
			} else {
				sender.sendMessage(this.i18n.format("profile.reset.failed"));
			}
			return true;
		} else if (arg0.equalsIgnoreCase("set")) {
			if (len < 2) {
				sender.sendMessage(this.i18n.format("command.arguments-lack"));
				return true;
			}
			val arg1 = args[1];
			CompletableFuture<GameProfile> future;
			try {
				val uuid = UUID.fromString(arg1);
				future = this.profileCache.get(uuid, this.ioService);
			} catch (IllegalArgumentException ex) {
				future = this.profileCache.get(arg1, this.ioService);
			}
			val p = (Player) sender;
			val i18n = this.i18n;
			UtilFuture.failed(future, new ErrorCallback(sender, i18n));
			future.thenAccept(result -> {
				val set = CompletableFuture.runAsync(() -> this.skinRestorer.setProfile(p.getUniqueId(), result), this.syncExecutorService);
				UtilFuture.success(set, new SuccessCallback<>(sender, i18n));
				UtilFuture.failed(set, new ErrorCallback(sender, i18n));
			});
		} else {
			sendHelp(sender, label);
		}
		return true;
	}

	private void sendHelp(CommandSender sender, String label) {
		sender.sendMessage(String.format("/%s set", label));
		sender.sendMessage(String.format("/%s reset", label));
	}
}
