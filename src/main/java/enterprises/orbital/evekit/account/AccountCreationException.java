package enterprises.orbital.evekit.account;

/**
 * Thrown if account creation parameters are invalid.
 */
public class AccountCreationException
extends Exception
{
    public AccountCreationException()
    {
        super();
    }

    public AccountCreationException(String msg)
    {
        super(msg);
    }
}
