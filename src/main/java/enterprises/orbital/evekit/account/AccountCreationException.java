package enterprises.orbital.evekit.account;

public class AccountCreationException
extends Exception
{
    private static final long serialVersionUID = 1141644791349107192L;

    public AccountCreationException()
    {
        super();
    }

    public AccountCreationException(String msg)
    {
        super(msg);
    }
}
