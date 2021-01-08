package me.TheSteak.multiteammanhunt;

import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
/*
 * TODO:
 * account for switching teams
 * add thread for updating compasses
 */
public class ManhuntTeamManagement implements CommandExecutor 
{
	
	private Main plugin;
	
	private boolean track;
	
	private ArrayList<Player> hunters, runners;
	private ArrayList<Integer> hunterpoint;
	
	public ManhuntTeamManagement (Main in)
	{
		plugin = in;
		plugin.getCommand("teamhunter").setExecutor(this);
		plugin.getCommand("teamrunner").setExecutor(this);
		plugin.getCommand("switchtrack").setExecutor(this);
		plugin.getCommand("startcompass").setExecutor(this);
		plugin.getCommand("stopcompass").setExecutor(this);
		hunters = new ArrayList<Player>();
		runners = new ArrayList<Player>();
		hunterpoint = new ArrayList<Integer>();
		track = false;
		
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] other) 
	{
		if ("teamhunter".equals(cmd.getName()))
		{
			hunters.add((Player)sender);
			if (runners.isEmpty())
			{
				hunterpoint.add(-1);
			}
			else
			{
				hunterpoint.add(0);
			}
		}
		else if ("teamrunner".equals(cmd.getName()))
		{
			runners.add((Player)sender);
			if (runners.size() == 1)
			{
				for (int i = 0; i < hunters.size(); ++i)
				{
					hunterpoint.set(i, 0);
				}
			}
		}
		else if ("switchtrack".equals(cmd.getName()))
		{
			
		}
		else if ("startcompass".equals(cmd.getName()))
		{
			track = true;
		}
		else if ("stopcompass".equals(cmd.getName()))
		{
			track = false;
		}
		return false;
	}
	
	private void updatePositions()
	{
		for (int i = 0; i < hunters.size(); ++i)
		{
			hunters.get(i).setCompassTarget(runners.get(hunterpoint.get(i)).getLocation());
		}
			
	}
}
