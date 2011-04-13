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

/**
 * To save moving around the name and path of a document everywhere I created
 * this class which contains the name and path of the document. This was while I
 * was writing the time region browser, there may be more places I can use this.
 * 
 * @author Lawrence
 * 
 */
public class DocumentID
{
    public String name;
    public String path;

    /**
     * 
     * @param name
     *            of the SourceDocument
     * @param path
     *            of the SourceDocument
     */
    public DocumentID(String name, String path)
    {
        this.name = name;
        this.path = path;
    }
}