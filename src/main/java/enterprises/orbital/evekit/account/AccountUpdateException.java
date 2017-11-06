package enterprises.orbital.evekit.account;

/**
 * Thrown if an update to an account would violate some constraint, currently:
 *
 * <ul>
 *   <li>Changing the name of an account to a name already in use.</li>
 *   <li>Changing character or corporation information inconsistent with an existing credential.</li>
 * </ul>
 */
public class AccountUpdateException
extends Exception
{
    public AccountUpdateException()
    {
        super();
    }

    public AccountUpdateException(String msg)
    {
        super(msg);
    }
}
