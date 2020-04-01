package me.xdark.skinrestorer.schedule;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public final class SchedulerExecutorService extends AbstractExecutorService {
	@NotNull
	private final Plugin plugin;
	@NotNull
	private final BukkitScheduler scheduler;
	private final boolean async;

	@Override
	public void shutdown() { }

	@NotNull
	@Override
	public List<Runnable> shutdownNow() {
		return Collections.emptyList();
	}

	@Override
	public boolean isShutdown() {
		return false;
	}

	@Override
	public boolean isTerminated() {
		return false;
	}

	@Override
	public boolean awaitTermination(long timeout, @NotNull TimeUnit unit) throws InterruptedException {
		return false;
	}

	@Override
	public void execute(@NotNull Runnable command) {
		if (this.async) {
			this.scheduler.runTaskAsynchronously(this.plugin, command);
		} else {
			this.scheduler.runTask(this.plugin, command);
		}
	}

	public static SchedulerExecutorService create(boolean async, Plugin plugin) {
		return new SchedulerExecutorService(plugin, Bukkit.getScheduler(), async);
	}
}
