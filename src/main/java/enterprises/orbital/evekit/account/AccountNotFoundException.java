package enterprises.orbital.evekit.account;

/**
 * Thrown if a specified account can not be found on an operation which requires an account.
 */
public class AccountNotFoundException
extends Exception
{
    public AccountNotFoundException()
    {
        super();
    }

    public AccountNotFoundException(String msg)
    {
        super(msg);
    }
}
