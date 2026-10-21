package com.zully.zulrah;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/** Starts a local developer client with this private plugin registered. */
public final class DevClientLauncher
{
	private DevClientLauncher()
	{
	}

	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ZulrahPlugin.class);
		RuneLite.main(args);
	}
}
