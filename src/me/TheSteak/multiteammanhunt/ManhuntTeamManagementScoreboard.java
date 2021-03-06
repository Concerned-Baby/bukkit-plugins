package me.TheSteak.multiteammanhunt;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
/*
 * TODO:
 * 
 * IDEA:
 * add a get teams method
 * 
 * ERROR:
 * can't switch teams without error
 * you can join a team twice
 * 
 */
public class ManhuntTeamManagementScoreboard implements CommandExecutor, Listener
{
	
	private Main plugin;
	
	private ArrayList<Player> hunters, runners;
	private ArrayList<Integer> hunterpoint, runnerpoint;
	
	private Server server;
	
	private Scoreboard board;
	
	public ManhuntTeamManagementScoreboard (Main in)
	{
		plugin = in;
		
		plugin.getCommand("teamhunter").setExecutor(this);
		plugin.getCommand("teamrunner").setExecutor(this);
		plugin.getCommand("switchtrack").setExecutor(this);
		
		hunters = new ArrayList<Player>();
		runners = new ArrayList<Player>();
		hunterpoint = new ArrayList<Integer>();
		runnerpoint = new ArrayList<Integer>();
		
		server = Bukkit.getServer();
		
		server.getScheduler().runTaskTimer(plugin, new updateClass(), 1, 8);
		
		server.broadcastMessage("timer started");
		
		createBoard();
			
	}
	
	public void onDeath(PlayerDeathEvent event) 
	{
		if (runners.contains((Player)event.getEntity()))
			updateBoard((Player)event.getEntity());
		for (Player p : runners)
			p.setScoreboard(board);
		for (Player p : hunters)
			p.setScoreboard(board);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] other) 
	{
		server.broadcastMessage("command called");
		Player p = (Player)sender;
		if ("teamhunter".equals(cmd.getName()))
		{
			if (hunters.contains(p))
			{
				p.sendMessage("You are already on the " + ChatColor.RED + "hunter team");
				return true;
			}
			if (runners.contains(p))
			{
				int i = runners.indexOf(p);
				runnerpoint.remove(i);
				runners.remove(i);
				for (int j = 0; j < hunterpoint.size(); j++)
				{
					if (hunterpoint.get(j) == i)
					{
						int k = runners.size();
						hunterpoint.set(j, k - 1);
						if (k <= 0) hunters.get(j).sendMessage("There is no one to track");
						else hunters.get(j).sendMessage("Your compass is now pointing to " + ChatColor.GREEN  + runners.get(j).getPlayerListName().toUpperCase());
					}
				}
			}
			hunters.add(p);
			if (runners.isEmpty())
			{
				hunterpoint.add(-1);
				p.sendMessage("There is no one to track yet");
			}
			else
			{
				hunterpoint.add(0);
				p.sendMessage("Your compass is now pointing to " + ChatColor.GREEN  + runners.get(0).getPlayerListName().toUpperCase());
			}
			server.broadcastMessage(p.getPlayerListName() + " has been added to the " + ChatColor.RED + "hunter team");
			server.broadcastMessage(ChatColor.BLUE + "Runner Team " + playerArrToString(runners) + runnerpoint.toString());
			server.broadcastMessage(ChatColor.RED + "Hunter Team " + playerArrToString(hunters) + hunterpoint.toString());
			p.setScoreboard(board);
			return true;
		}
		else if ("teamrunner".equals(cmd.getName()))
		{
			if (runners.contains(p))
			{
				p.sendMessage("You are already on the " + ChatColor.BLUE + "runner team");
				return true;
			}
			if (hunters.contains(p))
			{
				hunterpoint.remove(hunters.indexOf(p));
				hunters.remove(p);
			}
			addRunnerToScoreboard(p);
			runners.add(p);	
			if (runners.size() == 1)
			{
				for (int i = 0; i < hunters.size(); ++i)
				{
					hunterpoint.set(i, 0);
					hunters.get(i).sendMessage("Your compass is now pointing to " + ChatColor.GREEN + p.getPlayerListName().toUpperCase());
				}
				runnerpoint.add(-1);
				p.sendMessage("There is no teammates to track (yet)");
			}
			else if (runners.size() == 2)
			{
				runnerpoint.set(0, 1);
				runnerpoint.set(1, 0);
				runners.get(0).sendMessage("Your compass is now pointing to " + ChatColor.GREEN + 
						" " + p.getPlayerListName().toUpperCase() + 
						" [Distance " + runners.get(0).getLocation().distance(p.getLocation()) + "]");
				p.sendMessage("Your compass is now pointing to " + ChatColor.GREEN + 
						" " + runners.get(0).getPlayerListName().toUpperCase() + 
						" [Distance " + runners.get(0).getLocation().distance(runners.get(0).getLocation()) + "]");
			}
			server.broadcastMessage(p.getPlayerListName() + " has been added to the " + ChatColor.BLUE + "runner team");
			server.broadcastMessage(ChatColor.BLUE + "Runner Team " + playerArrToString(runners) + runnerpoint.toString());
			server.broadcastMessage(ChatColor.RED + "Hunter Team " + playerArrToString(hunters) + hunterpoint.toString());
			p.setScoreboard(board);
			return true;
			
		}
		else if ("switchtrack".equals(cmd.getName()))
		{
			int point = hunters.indexOf(p);
			if (point != -1)
			{
				hunterpoint.set(point, (hunterpoint.get(point) + 1) % runners.size());
				p.sendMessage("Your compass is now pointing to " + ChatColor.GREEN + " " + runners.get(hunterpoint.get(point)).getPlayerListName().toUpperCase());
			}
			point = runners.indexOf(p);
			if (point != -1)
			{
				int i = runnerpoint.get(point);
				if (i + 1 == point) runnerpoint.set(point, (i + 2) % runners.size());
				else runnerpoint.set(point, (i + 1) % runners.size());
			}
			return true;
		}
		return false;
	}
	
	private void createBoard()
	{
		board = Bukkit.getScoreboardManager().getNewScoreboard();
		board.registerNewObjective("MultiplayerManhuntScoreboard", "", "").getScore("Runners With Lives Left").setScore(0);
	}
	
	private void updateBoard(Player p)
	{
		Score score = board.getObjective("MultiplayerManhuntScoreboard").getScore("Runners With Lives Left");
		score.setScore(score.getScore() - 1);
		board.getObjective(p.getName()).unregister();
	}
	
	private void addRunnerToScoreboard(Player p)
	{
		Score score = board.getObjective("MultiplayerManhuntScoreboard").getScore("Runners With Lives Left");
		score.setScore(score.getScore() + 1);
		board.registerNewObjective(p.getName(), "", "");
	}
	
	private void updatePositions()
	{
		for (int i = 0; i < hunters.size(); i++)
		{
			int k = hunterpoint.get(i);
			if (k >= 0 && hunters.get(i).getWorld().getEnvironment() == runners.get(k).getWorld().getEnvironment()) hunters.get(i).setCompassTarget(runners.get(k).getLocation());
		}
		for (int i = 0; i < runners.size(); i++)
		{
			int k = runnerpoint.get(i);
			if (k >= 0 && hunters.get(k).getWorld().getEnvironment() == runners.get(i).getWorld().getEnvironment()) runners.get(i).setCompassTarget(hunters.get(k).getLocation());
		}
	}
	
	private String playerArrToString(ArrayList<Player> arr)
	{
		StringBuilder s = new StringBuilder();
		for (Player p : arr)
			s.append(p.getPlayerListName() + " ");
		return s.toString();
	}
	
	private class updateClass implements Runnable
	{
		public void run() 
		{
			updatePositions();
		}
	}
	
	
}
