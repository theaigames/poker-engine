package nl.starapple.poker;
public class HandHoldem extends Hand
{		
	/**
	 * A hand containing two cards
	 * @param firstCard : the first card
	 * @param secondCard : the second card
	 */
	public HandHoldem(Card firstCard, Card secondCard)
	{
		cards = new Card[2];
		cards[0] = firstCard;
		cards[1] = secondCard;
	}
}

