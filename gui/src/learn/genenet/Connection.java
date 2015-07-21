package learn.genenet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Connection
{

	private double				score;
	private String				child;
	private List<String>		parents;
	private Map<String, Type>	parentToType;

	public enum Type
	{
		ACTIVATOR("activator"), REPRESSOR("repressor"), UNKNOWN("unknown");

		private final String	name;

		Type(String name)
		{
			this.name = name;
		}

	}

	public Connection(String child, double score, String type, String parent)
	{
		this.child = child;
		this.score = score;
		this.parents = new ArrayList<String>();
		this.parents.add(parent);
		this.parentToType = new HashMap<String, Type>();
		this.parentToType.put(parent, getType(type));
	}

	public Connection(String child, double score, List<Connection> connections)
	{
		this.child = child;
		this.score = score;
		this.parents = new ArrayList<String>();
		this.parentToType = new HashMap<String, Type>();

		for (Connection connection : connections)
		{
			this.parents.addAll(connection.getParents());

			for (String parent : connection.getParents())
			{
				this.parentToType.put(parent, getType(connection.getParentType(parent)));
			}
		}

	}

	public Connection(String child, double score, Connection... connections)
	{
		this.child = child;
		this.score = score;
		this.parents = new ArrayList<String>();
		this.parentToType = new HashMap<String, Type>();

		for (Connection connection : connections)
		{
			this.parents.addAll(connection.getParents());

			for (String parent : connection.getParents())
			{
				this.parentToType.put(parent, getType(connection.getParentType(parent)));
			}
		}

	}

	public Type getType(String type)
	{
		type = type.toLowerCase();

		if (type.equals("repressor"))
		{
			return Type.REPRESSOR;
		}
		else if (type.equals("activator"))
		{
			return Type.ACTIVATOR;
		}
		else
		{
			return Type.UNKNOWN;
		}
	}

	public double getScore()
	{
		return score;
	}

	public List<String> getParents()
	{
		return parents;
	}

	public String getChild()
	{
		return child;
	}

	public String getParentType(String parent)
	{
		if (parentToType.containsKey(parent))
		{
			return parentToType.get(parent).name();
		}
		else
		{
			return null;
		}
	}

	public boolean equalParents(List<String> parents)
	{
		for (String parent : parents)
		{
			if (!this.parents.contains(parent))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString()
	{
		return "Connection [score=" + score + ", child=" + child + ", parents=" + getParents() + "]";
	}

	public String getParentString()
	{
		String list = "";

		for (String parent : parents)
		{
			list = list + parent + " ";
		}

		return list;
	}

}