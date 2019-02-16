import java.io.*;
import java.util.*;
import org.ini4j.Ini;
import org.openqa.selenium.*;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class Diskusage 
{
	public static WebDriver webDriver=null;
	public static BufferedWriter bw=null;
	public static void main(String[] args)  
	{
		System.setProperty("webdriver.chrome.driver", "Chromedriver.exe");
		if(args.length==0)
		{
			System.out.println("Arguments missing");
			System.exit(255);
		}
		else if(args.length==1)
		{
			System.out.println("Region is chosen is "+args[0]);
			getcompleteregion(args[0]);
		}
		else 
		{
			String usage=diskUsage(args[0],args[1],args[2]);
			System.out.println("Disk usage is for "+args[0]+" is "+usage);
		}
    }
	public static Map<String,String> getProperties(String region)
	{
		try 
		{
			Ini iniFile = new Ini(new File("config.ini"));
			Map <String,String>map = iniFile.get(region);
			return map;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			return new HashMap<String, String>();
		}
		
	}
	public static void getcompleteregion(String region)
	{
		Map <String,String> map=getProperties(region);
		if(map.size()==0 )
		{
			System.out.println("Username and Password not fetched");
			System.exit(255);
		}
		
		else
		{
		
			StringBuilder sb=new StringBuilder();
			sb.append("<html>\n<head>\n<style>\n table, th, td {border: 1px solid black;}  \n</style> \n</head> \n<body>\n<table border='1'><tr><th>Region</th><th>Usage</th></tr>");
			try 
			{	
				bw=new BufferedWriter(new FileWriter(new File("Output/Output_"+region+".html")));
				Set<String> usernameMap=map.keySet();
				for(Iterator<String> it=usernameMap.iterator();it.hasNext();)
				{
					String username=it.next();
					String password=map.get(username);
					String usage=diskUsage(username,password,region);
					System.out.println(username+" : "+usage);
					sb.append("\n<tr><td>"+username.substring((username.lastIndexOf("."))+1, username.length())+"</td><td"+usage+"</td></tr>");
				}
				sb.append("\n</table>\n</body>\n</html>");
				bw.write(sb.toString());
				bw.flush();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try 
				{
					bw.flush();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
			System.exit(0);
		}
	}
	public static String diskUsage(String username,String password,String region)
	{
		try
		{
			webDriver=(WebDriver) new ChromeDriver();
			WebElement elementuser;
			String elements[]={"setupLink","DataManagement_font","CompanyResourceDisk_font"};
			if(region.equals("Prod"))
			{
				webDriver.navigate().to("https://login.salesforce.com");
			}
			else
			{
				webDriver.navigate().to("https://test.salesforce.com");	
			}
			webDriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
			elementuser=webDriver.findElement(By.id("username"));
			elementuser.sendKeys(username);
			elementuser=webDriver.findElement(By.id("password"));
			elementuser.sendKeys(password);
			elementuser=webDriver.findElement(By.id("Login"));
			elementuser.submit();
			
			for(int i=0;i<elements.length;i++)
			{
				//System.out.println(elements[i]);
				webDriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
				elementuser=webDriver.findElement(By.id(elements[i]));
				if(i==0)
				{
					if(!elementuser.isDisplayed())
					continue;	
				}
				JavascriptExecutor executor=(JavascriptExecutor)webDriver;
				executor.executeScript("arguments[0].click();", elementuser);
			}
			webDriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
			String sourceCode=webDriver.getPageSource();
			int data=sourceCode.indexOf("Data Storage");
			if(data!=-1)
			{	
				String data2=sourceCode.substring(data,data+320);
				String data3=data2.substring((data2.indexOf("%")-3),(data2.indexOf("%")+1));
				webDriver.close();
				webDriver.quit();
				return data3;
			}
			else
			{
				return "Index of Data Storage not found!! \nSomeThing Failed";
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Exception occured");
			webDriver.getCurrentUrl();
			webDriver.close();
			return "Exception Occured"; 
		}
	}
}