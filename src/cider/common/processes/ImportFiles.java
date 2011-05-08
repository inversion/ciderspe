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

package cider.common.processes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * 
 * Import file(s) recursively into path <-> contents tuples.
 * 
 * @author Andrew
 * 
 */

public class ImportFiles
{

    /**
     * Used for testing only
     * 
     * @param args
     */

    public static void main(String[] args)
    {
        ImportFiles program = null;
        try
        {
            program = new ImportFiles("src");
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        Iterator<Entry<String, String>> itr = program.getFiles().entrySet()
                .iterator();

        while (itr.hasNext())
        {
            System.out.println("Source Document - " + itr.next().getKey());
            break;
        }
    }

    // TODO: NEEDs to avoid binary files
    HashMap<String, String> files = new HashMap<String, String>();

    public ImportFiles(String path) throws FileNotFoundException, IOException
    {
        importFrom(path);
    }

    /**
     * 
     * @return A mapping of paths -> contents which can be uploaded in turn with
     *         createDocument()
     */
    public HashMap<String, String> getFiles()
    {
        return files;
    }

    /**
     * Import source files from the client's file system. Currently only imports
     * files ending in .java.
     * 
     * @param path
     *            If path is a file, a single file will be returned in the list,
     *            if it's a directory it will be recursed.
     * 
     * @author Andrew
     * @throws FileNotFoundException
     */
    private void importFrom(String path) throws FileNotFoundException,
            IOException
    {
        File root = new File(path);

        if (!root.canRead())
            throw new FileNotFoundException("Can't read file/dir at path "
                    + path);

        if (root.isFile())
        {
            files.put(path, readFile(root));
            return;
        }

        File[] list = root.listFiles();
        for (File file : list)
        {
            if (file.isFile() && file.getName().endsWith(".java"))
                files.put(file.getPath(), readFile(file));
            else if (file.isDirectory())
                importFrom(file.getPath());
        }

        return;
    }

    /**
     * Read file from file system
     * 
     * @throws FileNotFoundException
     *             , IOException
     * @return String of file contents
     */
    private String readFile(File file) throws FileNotFoundException,
            IOException
    {
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        StringBuffer str = new StringBuffer();
        String line = null;

        while ((line = br.readLine()) != null)
            str.append(line + '\n');

        br.close();
        return str.toString();
    }
}
