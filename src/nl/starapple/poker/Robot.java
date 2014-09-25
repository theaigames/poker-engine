package nl.starapple.poker;

public interface Robot {

	public void setup(long timeOut);
	
	public void writeMove(PokerMove move);
	
	public PokerMove getMove(String botCodeName, long timeOut);

	public void writeInfo(HandInfo info);

	public void writeInfo(MatchInfo info);
	
	public void writeInfo(PreMoveInfo info);
	
	void writeResult(HandResultInfo info);
}
