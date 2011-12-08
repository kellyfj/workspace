
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xml.sax.SAXException;

import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.TableCell;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.WebTable;


/***
 * Get the MCAS grades, munge them and then report the average ranking
 * @author kellyfj
 */
public class mcas {
	public final static boolean DEBUG = false;
	public final static boolean DEBUG2 = false;
	public final static String PROXY_HOST = "172.16.39.201"; //"172.19.160.51";
	public final static int PROXY_PORT = 8080;
	private static final int YEAR = 2010;
	int COL_RANK = 0;
	int COL_NAME = 1;
	Set<String> schoolNameCache = new HashSet<String>();
	Map<String,Integer> GradeMap10English = new HashMap<String,Integer>();
	Map<String,Integer> GradeMap10Math = new HashMap<String,Integer>();
	Map<String,Integer> GradeMap10Science = new HashMap<String,Integer>();	
	Map<String,Integer> GradeMap8English = new HashMap<String,Integer>();
	Map<String,Integer> GradeMap8Math = new HashMap<String,Integer>();
	Map<String,Integer> GradeMap8Science = new HashMap<String,Integer>();	
	Map<String,Integer> GradeMap7English = new HashMap<String,Integer>();
	Map<String,Integer> GradeMap7Math = new HashMap<String,Integer>();
	Map<String,Integer> GradeMap6English = new HashMap<String,Integer>();
	Map<String,Integer> GradeMap6Math = new HashMap<String,Integer>();
	Map<String,Integer> GradeMap5English = new HashMap<String,Integer>();
	Map<String,Integer> GradeMap5Math = new HashMap<String,Integer>();
	Map<String,Integer> GradeMap5Science = new HashMap<String,Integer>();		
	Map<String,Integer> GradeMap4English = new HashMap<String,Integer>();
	Map<String,Integer> GradeMap4Math = new HashMap<String,Integer>();
	Map<String,Integer> GradeMap3English = new HashMap<String,Integer>();
	Map<String,Integer> GradeMap3Math = new HashMap<String,Integer>();
	SortedMap<Double,String> rankings = new TreeMap<Double,String>();
	private WebConversation wc;
	
	public void doHttpUnit() throws IOException, SAXException
	{
		 wc = new WebConversation();
		 HttpUnitOptions.setScriptingEnabled( false );
		 wc.setProxyServer( PROXY_HOST, PROXY_PORT );
				
				getGradeData(10,GradeMap10English, GradeMap10Math, GradeMap10Science);			
				getGradeData(8,GradeMap8English, GradeMap8Math, GradeMap8Science);
				getGradeData(7,GradeMap7English, GradeMap7Math, null);
				getGradeData(6,GradeMap6English, GradeMap6Math, null);
				getGradeData(5,GradeMap5English, GradeMap5Math, GradeMap5Science);
				getGradeData(4,GradeMap4English, GradeMap4Math, null);
				getGradeData(3,GradeMap3English, GradeMap3Math, null);
	}
	
	private void getGradeData(int grade, Map english, Map math, Map science) throws IOException, SAXException
	{
		String url = null;
		if(YEAR==2010)
		 url = "http://www.boston.com/news/special/education/mcas/scores10/"+grade+"th_top_districts.htm";
		 else if(YEAR==2009)
			 url = "http://www.boston.com/news/special/education/mcas/scores09/"+grade+"th_top_districts.htm";
		 else
			 throw new IllegalStateException("Year ("+YEAR+") unrecognized");
		
		if(grade==3)
			url = url.replace("th", "rd");
		
		System.out.println("Getting Grade data for URL ("+url+")");
		WebResponse   resp = wc.getResponse( url );
		WebTable[] tables = resp.getTables();
		if(DEBUG) System.out.println("Got ("+tables.length+") tables");
		for(WebTable t: tables)
		{
			if(DEBUG) System.out.println("Table # Rows ("+t.getRowCount()+")");
		}
		WebTable outerDataTable = tables[1]; //second table
		
		//ENGLISH
		TableCell outerDataCell = outerDataTable.getTableCell(0, 0);				
		WebTable[] innerDataTables = outerDataCell.getTables();
		WebTable englishDataTable = innerDataTables[0];
		
		english.putAll(extractData("ENG", englishDataTable));

		//MATH
		outerDataCell = outerDataTable.getTableCell(0, 2);
		innerDataTables = outerDataCell.getTables();
		WebTable mathDataTable = innerDataTables[0];
		math.putAll(extractData("MATH", mathDataTable));
		
		//Science
		if(grade ==5 || grade == 8 || grade == 10)
		{
			outerDataTable = tables[2];
			outerDataCell = outerDataTable.getTableCell(0, 0);
			innerDataTables = outerDataCell.getTables();
			WebTable scienceDataTable = innerDataTables[0];
		
			science.putAll(extractData("SCI", scienceDataTable));
		}
	}
	
	private Map<String, Integer> extractData(String area, WebTable t)
	{
		Map<String, Integer> dataMap = new HashMap<String,Integer>();
		
		int numRows = t.getRowCount();
		int numCols = t.getColumnCount();
		if(DEBUG) System.out.println("Got ("+numRows+") rows");
		if(DEBUG) System.out.println("Got ("+numCols+") cols");

		for(int i=2; i< numRows; i++)
		{
			String name = t.getCellAsText(i, COL_NAME).trim().replace('\n', ' ');
			String rank = t.getCellAsText(i, COL_RANK).trim();
			if(!schoolNameCache.contains(name))
				schoolNameCache.add(name);
			if(DEBUG2) System.out.println(area + " ["+name+"] --> ["+rank+"] ");
			if(!dataMap.containsKey(name))
				dataMap.put(name,new Integer(rank));
			else
				System.err.println("Map already contains ("+name+")");
		}
		return dataMap;
	}
	
	private void printSchoolNames()
	{
		Iterator<String> iter = schoolNameCache.iterator();
		int count=0;
		
		while(iter.hasNext())
		{
			count++;
			String name = iter.next();
			if(DEBUG2) System.out.println("#"+count+") "+name);
		}
		System.out.println("There are "+schoolNameCache.size()+" schools");
	}
	
	private void doAnalyses()
	{
		Iterator<String> iter = schoolNameCache.iterator();
		int count=0;
		
		while(iter.hasNext())
		{
			count++;
			String name = iter.next();
			double avgRank = getAverageRank(name);
			if(DEBUG) System.out.println("School System ["+name+"] Rank ["+avgRank+"]");
			if(!rankings.containsKey(avgRank))
				rankings.put(avgRank, name);
			else
				System.out.println("WARN Duplicate rankings on ("+avgRank+") for ("+name+")");
		}
		
		Iterator<Double> iter2 = rankings.keySet().iterator();
		System.out.println("***************************");
		System.out.println("*    R A N K I N G S      *");
		System.out.println("***************************");
		NumberFormat formatter = new DecimalFormat("#0.00");
		while(iter2.hasNext())
		{
			Double rank = iter2.next();
			String name = rankings.get(rank);
			System.out.println(formatter.format(rank) + " "+name);
		}
	}
	
	private double getAverageRank(String name) {
		
		double total=0;
		int numRankings=0;
		if(GradeMap10English.containsKey(name))
		{
			total += GradeMap10English.get(name);
			numRankings++;
		}
		if(GradeMap10Math.containsKey(name))
		{
			total += GradeMap10Math.get(name);	
			numRankings++;
		}
		if(GradeMap10Science.containsKey(name))
		{
			total += GradeMap10Science.get(name);	
			numRankings++;
		}
		if(GradeMap8English.containsKey(name))
		{
			total += GradeMap8English.get(name);
			numRankings++;
		}
		if(GradeMap8Math.containsKey(name))
		{
			total += GradeMap8Math.get(name);	
			numRankings++;
		}
		if(GradeMap8Science.containsKey(name))
		{
			total += GradeMap8Science.get(name);	
			numRankings++;
		}
		if(GradeMap7English.containsKey(name))
		{
			total += GradeMap7English.get(name);
			numRankings++;
		}
		if(GradeMap7Math.containsKey(name))
		{
			total += GradeMap7Math.get(name);	
			numRankings++;
		}		
		if(GradeMap6English.containsKey(name))
		{
			total += GradeMap6English.get(name);
			numRankings++;
		}
		if(GradeMap6Math.containsKey(name))
		{
			total += GradeMap6Math.get(name);	
			numRankings++;
		}
		if(GradeMap5English.containsKey(name))
		{
			total += GradeMap5English.get(name);
			numRankings++;
		}
		if(GradeMap5Math.containsKey(name))
		{
			total += GradeMap5Math.get(name);	
			numRankings++;
		}
		if(GradeMap5Science.containsKey(name))
		{
			total += GradeMap5Science.get(name);	
			numRankings++;
		}
		if(GradeMap4English.containsKey(name))
		{
			total += GradeMap4English.get(name);
			numRankings++;
		}
		if(GradeMap4Math.containsKey(name))
		{
			total += GradeMap4Math.get(name);	
			numRankings++;
		}
		if(GradeMap3English.containsKey(name))
		{
			total += GradeMap3English.get(name);
			numRankings++;
		}
		if(GradeMap3Math.containsKey(name))
		{
			total += GradeMap3Math.get(name);	
			numRankings++;
		}
		if(numRankings==0)
		{
			System.out.println("WARN No rankings for ("+name+") ");
			return 0;
		}
		else
			return total/numRankings;
	}

	public static void main(String[] args)
	{
		mcas m = new mcas();
		try {
			m.doHttpUnit();
			m.printSchoolNames();
			m.doAnalyses();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

	}
}
