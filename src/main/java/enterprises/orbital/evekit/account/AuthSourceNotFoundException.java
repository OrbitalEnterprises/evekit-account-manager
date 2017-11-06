package enterprises.orbital.evekit.account;

/**
 * Thrown if a specified account can not be found on an operation which requires an account.
 */
public class AuthSourceNotFoundException
extends Exception
{
    public AuthSourceNotFoundException()
    {
        super();
    }

    public AuthSourceNotFoundException(String msg)
    {
        super(msg);
    }
}
