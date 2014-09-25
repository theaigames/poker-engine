package nl.starapple.poker;
/**
 * Class that represents one Robot object and stores additional information such as the name that the bot receives and
 * which person is the author.
 */
public class PokerBot
{
	private Robot bot;
	private String name;
	
	public PokerBot(Robot bot, String name)
	{
		this.bot = bot;
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public Robot getBot()
	{
		return bot;
	}
}
