/**
	<Trolly is a simple shopping list application for android phones.>
	Copyright (C) 2009  Ben Caldwell
 	
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

package caldwell.ben.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for TrollyProvider
 */
public final class Trolly {
	public static final String AUTHORITY = "caldwell.ben.provider.Trolly";
	
    /**
     * Trolly table
     */
    public static final class ShoppingList implements BaseColumns {
    	
    	/**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI
                = Uri.parse("content://caldwell.ben.provider.Trolly/shoppinglist");

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of items.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.caldwell.ben.trolly";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single item.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.caldwell.ben.trolly";

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "item ASC";

        /**
         * The shopping list item
         * <P>Type: TEXT</P>
         */
        public static final String ITEM = "item";
        
        /**
         * An "off list" item
         * <P>An item that has been added to the list before 
         * but is not on the list at the moment.</P>
         */
        public static final int OFF_LIST = 0;
        
        /**
         * An "on list" item
         * <P>An item that has been added to the list 
         * and is on the list at the moment.</P>
         */
        public static final int ON_LIST = 1;
        
        /**
         * An "in trolley" item
         * <P>An item that has been added to the list and
         * is currently in the trolley - crossed off the list.</P>
         */
        public static final int IN_TROLLEY = 2;

        /**
         * The status of the shopping list item
         * <P>Type: INTEGER</P>
         */
        public static final String STATUS = "status";

        /**
         * The timestamp for when the note was created
         * <P>Type: INTEGER (long)</P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the note was last modified
         * <P>Type: INTEGER (long)</P>
         */
        public static final String MODIFIED_DATE = "modified";
    }
}
