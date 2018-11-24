import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DFA {
	private int numstates, numterminals, numFinals;
	private char terminals[];
	private int path[][];
	private boolean finals[];
    private String file;
    private ArrayList<String> acceptedStrings;

	
    public DFA(String filename) throws IOException
    {
        //Reads in dfa from file
        this.file=filename;
        BufferedReader br=null;
        try
        {br=new BufferedReader(new FileReader(filename));}
        catch (Exception e){e.printStackTrace();System.exit(0);}
        //line1
        String line=br.readLine().trim();
        numstates=Integer.parseInt(line);
        //line 2
        line=br.readLine().trim();
        int colonIndex=line.indexOf(":");
        line=line.substring(colonIndex+1).trim();
        String[] lineSplit=line.split("\\s+");
        numterminals=lineSplit.length;
        terminals=new char[numterminals];
        for (int i=0;i<lineSplit.length;i++)
        {
            terminals[i]=lineSplit[i].charAt(0);
        }
        //get rid of dashed line
        br.readLine();
        //states/transitions
        path=new int[numstates][numterminals];
        for (int i=0;i<numstates;i++)
        {
            line=br.readLine().trim();
            colonIndex=line.indexOf(":");
            line=line.substring(colonIndex+1).trim();
            String[] trans=line.split("\\s+");
            for (int j=0;j<trans.length;j++)
            {
                path[i][j]=Integer.parseInt(trans[j]);
            }
        }
        //get rid of dashed line
        br.readLine();
        br.readLine();
        line=br.readLine();
        colonIndex=line.indexOf(":");
        line=line.substring(0,colonIndex);
        String[] split=line.split(",");
        finals=new boolean[numstates];
        for (int i=0;i<split.length;i++)
        {
            finals[Integer.parseInt(split[i])]=true;
        }
        br.close();
    }

	public String toString()
    {
        String tmp="Minimized DFA from "+file+"\n";
        tmp+="sigma: ";
        for (char c : terminals)
        {
            tmp+=c+" ";
        }
        tmp+="\n---------------\n";
        for (int i=0;i<numstates;i++)
        {
            tmp+=i+": ";
            for (int j=0;j<path[i].length;j++)
            {
                tmp+=path[i][j]+" ";
            }
            tmp+="\n";
        }
        tmp+="---------------\n";
        tmp+="0: Initial State\n";
        for (int i=0;i<finals.length;i++)
        {
            if (finals[i])
                tmp+=i+",";
        }
        tmp=tmp.substring(0,tmp.length()-1);
        tmp+=": Accepting State(s)";
        return tmp;
	}

	public void minimize()
    {
		boolean mark[][] = new boolean[numstates][numstates];
		boolean reachable[] = new boolean[numstates];
		boolean flag = true;
		
		//remove unreachable states
		reachable[0] = true;
		boolean visited[] = new boolean[numstates];
		while (flag)
        {
			flag = false;
			for (int state = 0;state < numstates;state++)
            {
				if (reachable[state] && !visited[state])
                {
					visited[state] = true;
					flag = true;
					for ( int term = 0;term < numterminals;term++ )
                    {
						reachable[path[state][term]] = true;
					}
				}
			}
		}
		
		for (int statenum = 0; statenum < numstates; statenum++)
        {
			if (!reachable[statenum])	continue;
			for (int statenum2 = 0; statenum2 < numstates; statenum2++)
            {
				if (!reachable[statenum2])	continue;
				if (finals[statenum] != finals[statenum2])
					mark[statenum][statenum2] = true;
				else
					mark[statenum][statenum2] = false;
			}
		}
		//mark all distinguishable states
		flag = true;
		while (flag)
        {
			flag = false;
			for (int x = 0; x < numstates; x++)
            {
				if (!reachable[x])	continue;
				for (int y = 0; y < numstates; y++)
                {
					if (x == y)	continue;
					for (int t = 0; t < numterminals; t++)
                    {
						if (mark[ path[x][t] ][ path[y][t] ] && !mark[x][y])
                        {
							mark[x][y] = true;
							flag = true;
						}
					}
				}
			}
		}
		
		int minstates[] = new int[numstates];
		for (int state = 0; state < numstates; state++)
        {
			visited[state] = false;
			if (!reachable[state])
				minstates[state] = -1;
			else
				minstates[state] = state;
		}
		
		//extract the distinguishable states
		for (int state = 0; state < numstates; state++)
        {
			if (minstates[state] == -1 || visited[state])	continue;
			for (int p = 0; p < numstates; p++)
            {
				if (p == state || minstates[p] == -1)	continue;
				if (!mark[state][p] && !visited[p])
                {
					minstates[p] = state;
					visited[p] = true;
				}
			}
		}
		
		//set minstate
		int unique = 0;
		for (int x = 0; x < minstates.length; x++)
        {
			if (minstates[x] == x && minstates[x] != -1)
            {
				for (int y = 0; y < minstates.length; y++)
                {
					if (minstates[y] == x)
                    {
						minstates[y] = unique;
					}
				}
				unique++;
			}
		}
		
		//rearrange all the paths using the minstate array
		int newpath[][] = new int[numstates][numterminals];
		boolean newfinal[] = new boolean[numstates];
		int minstatecounter = 0, minfinalcounter = 0;
		for (int state = 0; state < numstates; state++)
        {
			//find all accessible vertecies and vertecies that have not been counted already
			if (minstates[state] != -1 && minstatecounter <= minstates[state])
            {
				for (int t = 0; t < numterminals; t++)
                {
					newpath[minstatecounter][t] = minstates[ path[state][t] ];
				}
				if (finals[state])
                {
					newfinal[minstatecounter] = true;
					minfinalcounter++;
				}
				else
                {
					newfinal[minstatecounter] = false;
				}
				minstatecounter++;
			}
		}
		
		path = newpath;
		numstates = minstatecounter;
		numFinals = minfinalcounter;
		finals = newfinal;
	}

    //Reads in file of input strings tests each one
    public void readAndFeed(String fileName)
    {
        BufferedReader br=null;
        acceptedStrings=new ArrayList<String>();
        try
        {
            br=new BufferedReader(new FileReader(fileName));
            String line=br.readLine();
            while (line!=null)
            {
                if (line.equals("")){line=br.readLine();}
                if (testString(line))
                    acceptedStrings.add(line);
                line=br.readLine();
            }
        }
        catch (IOException e)
        {
            System.out.println("Could not open file: "+fileName);
            System.exit(0);
        }
        System.out.println("The following strings are accepted:");
        for (String s : acceptedStrings)
        {
            System.out.println(s);
        }
    }

    //tests the given input string
    public boolean testString(String x)
    {
        int loc=0;
        for (int i=0;i<x.length();i++)
        {
            boolean termFound=false;
            char c=x.charAt(i);
            for (int j=0;j<terminals.length;j++)
            {
                if (c==terminals[j])
                {
                    loc=path[loc][j];
                    termFound=true;
                    break;
                }
            }
            if (!termFound)
                return false;
        }
        if (finals[loc])
            return true;
        return false;
    }

    public static void main(String[] args) throws IOException
    {
		if ( args.length<2)
        {
			System.out.println("Usage: java DFA [dfa_file] [string_file]");
			System.exit(0);
		}
        DFA dfa=new DFA(args[0]);
        dfa.minimize();
        System.out.println(dfa);
        dfa.readAndFeed(args[1]);
    }

}
