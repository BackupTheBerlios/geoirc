/*
 * UserComparator.java
 *
 * Created on July 25, 2003, 7:43 AM
 */

package geoirc;

import geoirc.util.Util;

/**
 * This comparator does not always impose orderings which are
 * consistent with equals.
 *
 * @author  Pistos
 */
public class UserComparator
    implements java.util.Comparator, GeoIRCConstants
{
    protected int sort_order;
    
    private UserComparator() { }
    
    public UserComparator( int sort_order )
    {
        if( ! Util.isValidSortOrder( sort_order ) )
        {
            throw new IllegalArgumentException( "Invalid sort order." );
        }

        this.sort_order = sort_order;
    }
    
    /**
     * This comparator does not always impose orderings which are
     * consistent with equals.
     */
    public int compare( Object o1, Object o2 )
    {
        User u1 = (User) o1;
        User u2 = (User) o2;
        
        int comparison;
        
        switch( sort_order )
        {
            case SORT_ALPHABETICAL_ASCENDING:
                comparison = u1.getNick().compareTo( u2.getNick() );
                break;
            case SORT_TIME_SINCE_LAST_ASCENDING:
                // Reverse sort because these are "times OF", not "times SINCE"
                comparison = u2.getTimeOfLastActivity().compareTo( u1.getTimeOfLastActivity() );
                break;
            default:
                throw new IllegalArgumentException( "Invalid sort order." );
        }
        
        return comparison;
    }
    
}
