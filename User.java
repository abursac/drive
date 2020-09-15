package server;

public class User
{
    static int idGenerator = 0;
    String username;
    String password;
    int id;
    boolean premium;
    
    public User(String u, String p, boolean pr)
    {
	id = idGenerator++;
	username = u;
	password = p;
	premium = pr;
    }
    
    public User(String bafer)
    {
	String[] podaci = bafer.split("#");
	id = Integer.parseInt(podaci[0]);
	username = podaci[1];
	password = podaci[2];
	int pom = Integer.parseInt(podaci[3]);
	if(pom == 1)
	{
	    premium = true;
	}
	else
	{
	    premium = false;
	}
    }

    @Override
    public String toString()
    {
	StringBuilder builder = new StringBuilder();
	builder.append(id);
	builder.append("#");
	builder.append(username);
	builder.append("#");
	builder.append(password);
	builder.append("#");
	if(premium == true)
	    builder.append("1");
	else
	    builder.append("0");
	return builder.toString();
    }
    
    
}
