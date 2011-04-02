/**
 *  CIDER - Collaborative Integrated Development EnviRonment
    Copyright (C) 2011  Andrew Moss
                        Lawrence Watkiss
                        Jonathan Bannister
                        Alex Sheppard
                        Miles Taylor
                        Ashley Woodman

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package cider.common.network;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

/**
 * Reads the Bot.conf file and sets the config in the Bot
 * 
 * @author Andrew
 *
 */

public class ConfigurationReader
{    
    private HashMap<String,String> config = new HashMap<String,String>();
    
    public String getHost()
    {
        return config.get( "HOST" );
    }
    
    public String getServiceName()
    {
        return config.get( "SERVICE_NAME" );
    }
    
    public int getPort()
    {
        return Integer.parseInt( config.get( "PORT" ) );
    }
    
    public String getBotUsername()
    {
        return config.get( "BOT_USERNAME" );
    }
    
    public String getBotPassword()
    {
        return config.get( "BOT_PASSWORD" );
    }
    
    public String getChatroomName()
    {
        return config.get( "CHATROOM_NAME" );
    }
    
    public String getCheckerUsername()
    {
        return config.get( "CHECKER_USERNAME" );
    }
    
    public String getCheckerPassword()
    {
        return config.get( "CHECKER_PASSWORD" );
    }
    
    public ConfigurationReader( String fileName )
    {
        BufferedReader br = null;
        String line;
        
        try
        {
            br = new BufferedReader( new FileReader( fileName ) );
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            System.err.println( "Error: Failed to read " + fileName );
            e.printStackTrace();
            System.exit(1);
        }
        
        try
        {
            while( (line = br.readLine()) != null )
            {
                line = line.trim();
                
                if( line.startsWith( "//" ) || line.length() == 0 )
                    continue;
                
                int pos = line.indexOf( "=" );
                if( pos == -1 )
                    throw new Exception( "Invalid syntax in: " + line );
                
                String key = line.substring( 0, pos );
                String val = line.substring( pos + 1 );
                
                if( key.length() == 0 || val.length() == 0 )
                    throw new Exception( "Invalid syntax in: " + line );
                
                if( config.containsKey( key ) )
                    throw new Exception( "Trying to set field "  + key + "twice" );
                
                config.put( key, val );
            }
            br.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println( "Error reading bot configuration: " + e.getMessage() );
            System.exit(1);
        }
       
        
        
    }
    
    
}
