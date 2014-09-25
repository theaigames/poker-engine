package nl.starapple.io;

import java.io.IOException;

import nl.starapple.poker.HandInfo;
import nl.starapple.poker.HandResultInfo;
import nl.starapple.poker.MatchInfo;
import nl.starapple.poker.PreMoveInfo;
import nl.starapple.poker.PokerMove;
import nl.starapple.poker.Robot;

public class IORobot implements Robot {

	IOHandler handler;
	StringBuilder dump;
	int errorCounter;
	long startingTime;
	final int maxErrors = 2;
	final int maxTime = 300000; //bot may run max 5 minutes

	public IORobot(String command) throws IOException {
		handler = new IOHandler(command);
		startingTime = System.currentTimeMillis();
		dump = new StringBuilder();
		errorCounter = 0;
	}

	@Override
	public void setup(long timeOut)
	{
		handler.readLine(timeOut);
	}
	
    @Override
	public void writeMove(PokerMove move) {
		String i = move.toString();
        handler.writeLine(i);
        addToDump(i + "\n");
    }
	
    @Override
	public PokerMove getMove(String botCodeName, long timeOut) {
		String line = "";
		long currentTime = System.currentTimeMillis();
		long runningTime = currentTime - startingTime;
		String error = "";

		addToDump("Action "+botCodeName+" "+timeOut + "\n");

		if(runningTime < maxTime) {
			if(errorCounter < maxErrors) {
				handler.writeLine("Action "+botCodeName+" "+timeOut);

				long timeStart = System.currentTimeMillis();
				while(line != null && line.length() < 1)
				{
	        		long timeNow = System.currentTimeMillis();
					long timeElapsed = timeNow - timeStart;
					line = handler.readLine(timeOut);

					if(timeElapsed >= timeOut) 
						line = null;
				}
				addToDump("Output from your bot: \"" + line + "\"\n");
				if(line == null) {
					errorCounter++;
				}
		    }
		    else {
		    	error = "Maximum number of idle moves returned. Action set to \"check\" (let bot return \"check 0\" instead of nothing)";
		    }
		}
		else {
			error = "Maximum running time of bot reached (" + maxTime +" ms), please make it run faster. Action set to \"check\"";
		}

	    if(!(line == null || line == "")) {
		    String[] parts = line.split("\\s");
	    	if(parts.length != 2)
	    		error = "Bot input '" + line + "' does not split into two parts. Action set to \"check\"";
	    	else
	        	return new PokerMove(parts[0], Integer.valueOf(parts[1]));
	    }
	    else if(error == "") {
	    	error = "Your bot did not return anything (on time). Action set to \"check\"";
	    }

	    System.err.println(error);
	    addToDump(error + "\n");
	    return new PokerMove("check", 0);
    }
    
    @Override
	public void writeInfo(MatchInfo info) {
    	String i = info.toString();
    	handler.writeLine(i);
    	addToDump(i + "\n");
	}

    @Override
	public void writeInfo(HandInfo info) {
        String i = info.toString();
    	handler.writeLine(i);
    	addToDump(i + "\n");
    }
    
    @Override
	public void writeInfo(PreMoveInfo info) {
        String i = info.toString();
    	handler.writeLine(i);
    	addToDump(i + "\n");
    }
    
    @Override
	public void writeResult(HandResultInfo info) {
		String i = info.toString();
    	handler.writeLine(i);
    	addToDump(i + "\n");
	}

	public void addToDump(String dumpy){
		dump.append(dumpy);
	}
	
	public void finish() {
		handler.stop();
	}

	public String getStdin() {
		return handler.getStdin();
	}

	public String getStdout() {
		return handler.getStdout();
	}

	public String getStderr() {
		return handler.getStderr();
	}

	public String getDump() {
		return dump.toString();
	}
}
