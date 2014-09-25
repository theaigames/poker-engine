// Copyright 2014 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//  
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package nl.starapple.backend;

import nl.starapple.io.IORobot;
import nl.starapple.poker.PokerBot;
import nl.starapple.poker.MatchPlayer;

import java.awt.Point;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.SwingUtilities;
import java.lang.InterruptedException;
import java.lang.Thread;
import java.util.zip.*;
import java.util.Properties;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.Collections;

public class RunPoker
{
	private static int gameType;
	private static int numberOfHands;
    private static int startingStack;
    private static int placesPaid;

    String bot1Command, bot2Command;

	String playerName1, playerName2;
	
	MatchPlayer engine;
    List<PokerBot> bots;

	public static void main(String args[]) throws Exception
	{	
		RunPoker run = new RunPoker(args);
        try {
		  run.go();
        }
        catch(Exception e) {
            e.printStackTrace();
            run.finish();
        }
	}

	public RunPoker(String args[])
	{
		startingStack = Integer.valueOf(args[0]);
        this.bot1Command = args[1];
        this.bot2Command = args[2];

		this.playerName1 = "player1";
		this.playerName2 = "player2";

        bots = new ArrayList<PokerBot>();
	}
	
	private void go() throws IOException, InterruptedException
	{
		PokerBot player1, player2;
		IORobot bot1, bot2;
		
		//setup the bots
		bot1 = new IORobot(this.bot1Command);
		bot2 = new IORobot(this.bot2Command);
		player1 = new PokerBot(bot1, playerName1);
		player2 = new PokerBot(bot2, playerName2);
		bots.add(player1);
		bots.add(player2);

		//gametype is omaha tournament (with two players):
		gameType = 18;
		//number of hands is unlimited
		numberOfHands = -1;
		//one winner
		placesPaid = 1;
		
		engine = new MatchPlayer(new ArrayList<PokerBot>(bots), gameType, startingStack);
        engine.finishSetup(true);
        ArrayList<String> results = engine.run(numberOfHands, 0);
        for(int i = 0; i < results.size(); i++)
        {
        	String result = results.get(i);
            System.out.println(result);
            bot1.addToDump("Engine says: \"" + result + "\"\n");
            bot2.addToDump("Engine says: \"" + result + "\"\n");
        }

        finish();
	}
	
	private void finish() throws InterruptedException
	{
		for(int i=1; i<bots.size(); i++)
		{
			IORobot bot = (IORobot) bots.get(i).getBot();
			try { bot.finish(); } catch(Exception e){}
			Thread.sleep(200);
		}
		Thread.sleep(200);

		// write everything
        try {
		  this.saveGame((IORobot) bots.get(0).getBot(),(IORobot) bots.get(1).getBot());
        }
        catch(Exception e) {}

        System.exit(0);
	}
    
	/**
	 * Transforms the old output from the engine to a nice bson format for the javascript visualizer
	 * @param String input : old output from the engine
	 * @return String : new bson format output
	 */
    private String getPlayedGame(String input) 
    {
    	StringBuilder out = new StringBuilder();
    	String[] lines = input.trim().split("\\r?\\n");
    	
    	out.append("{");
    	
    	for(int i=0; i<lines.length; i++)
    	{
    		String[] part = lines[i].trim().split(" ");
    		if(part[0].equals("Settings")) 
    		{
    			out.append(makeKeyString("settings"));
    			out.append("{");
    			while(part[0].equals("Settings")) 
    			{
    				part = lines[i].trim().split(" ");
    				if(part[1].startsWith("seat")) {
    					out.deleteCharAt(out.length()-1);
    					out.append("},");
    					out.append(makeKeyString("players"));
    					out.append("[");
    				}
    				while(part[1].startsWith("seat"))
    				{
    					part = lines[i].trim().split(" ");
    					out.append("\"" + part[2] + "\",");
    					i++;
    					part = lines[i].trim().split(" ");
    					if(!part[1].startsWith("seat")) {
    						out.deleteCharAt(out.length()-1);
    						out.append("],");
    					}
    				}
    				if(!part[1].equals("hand")){
    					out.append(makeKeyString(part[1]));
        				out.append(part[2] + ",");
    				}
    				i++;
    				part = lines[i].trim().split(" ");
    				if(!part[0].equals("Settings")) {
    					out.append(makeKeyString("rounds"));
    					out.append("[{");
    					i--;
    				}
    			}
    		}
    		else 
    		{
    			if(part[1].equals("hand")){
    				out.append("{");
    				continue;
    			}
    			if(part[1].equals("end")){
    				out.append("},");
    				continue;
    			}
    			if(part[1].equals("dealerButton")){
    				out.append(makeKeyString("dealerButton"));
    				out.append("\"" + part[2] + "\",");
    				continue;
    			}
    			if(part[1].equals("table")){
    				out.append(makeKeyString("table"));
    				out.append("\"" + trimStuff(part[2]) + "\",");
    				continue;
    			}
    			if(part[0].equals("Result")){
    				String potName = part[1].substring(0,1).toUpperCase() + part[1].substring(1);
    				out.append(makeKeyString("result" + potName));
    				out.append("{");
    				String[] winners = trimStuff(part[2]).split(",");
    				String[] winner = winners[0].split(":");
    				out.append(makeKeyString(winner[0]));
    				out.append(winner[1]);
    				if(winners.length > 1) {
    					out.append(",");
    					winner = winners[1].split(":");
        				out.append(makeKeyString(winner[0]));
        				out.append(winner[1]);
    				}
    				out.append("}");
    				if(potName.startsWith("Sidepot"))
    					out.append(",");
    				continue;
    			}
    			if( part[0].startsWith("player") || ( part[0].equals("Match") && (part[1].contains("pot") || part[1].equals("table")) ) ){
    				out.append(makeKeyString("actions"));
    				out.append("[");
    				while( part[0].startsWith("player") || ( part[0].equals("Match") && (part[1].contains("pot") || part[1].equals("table")) ) )
    				{
    					part = lines[i].trim().split(" ");
                        out.append("\"");
                        for(String s : part){
                            out.append(trimStuff(s) + " ");
                        }
                        out.deleteCharAt(out.length()-1);
                        out.append("\",");
    					i++;
    					part = lines[i].trim().split(" ");
    					if(!(part[0].startsWith("player") || ( part[0].equals("Match") && (part[1].contains("pot") || part[1].equals("table")) )))
    					{
    						out.deleteCharAt(out.length()-1);
    						out.append("],");
    						i--;
    					}
    				}
    			}
    		}
    	}
    	
    	out.deleteCharAt(out.length()-1);
    	out.append("]}");
    	
    	return out.toString();
    }
    
    private String makeKeyString(String input) {
    	return "\"" + input + "\": "; 
    }
    
    private String trimStuff(String input) {
    	if(input.startsWith("[") && input.endsWith("]"))
    		return input.substring(1, input.length() - 1);
    	if(input.endsWith("%"))
    		return input.substring(0, input.length() - 1);
    	return input;
    }

	public void saveGame(IORobot bot1, IORobot bot2) throws Exception {
		
		PokerBot winner = this.engine.winningPokerBot();
		int score = this.engine.getHandNumber();

        System.out.println("Winner: " + winner.getName());
        System.out.println("Score: " + score);

        System.out.println("Visualization:");
        // System.out.println(getPlayedGame(this.engine.getHistory())); // output for visuals

        System.out.println("Bot 1 Errors: ");
        System.out.println(bot1.getStderr());

        System.out.println("Bot 1 Dump: ");
        System.out.println(bot1.getDump());

        System.out.println("Bot 2 Errors: ");
        System.out.println(bot2.getStderr());

        System.out.println("Bot 2 Dump: ");
        System.out.println(bot2.getDump());	
	}

}