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
 * @author  netseeker
 */
public class UserComparator implements java.util.Comparator, GeoIRCConstants
{
    protected int sort_order;
    protected Channel channel;

    private UserComparator()
    {}

    public UserComparator(int sort_order, Channel channel)
    {
        if (!Util.isValidSortOrder(sort_order))
        {
            throw new IllegalArgumentException("Invalid sort order.");
        }

        this.sort_order = sort_order;
        this.channel = channel;
    }

    public void setChannel(Channel channel)
    {
        this.channel = channel;
    }

    /**
     * This comparator does not always impose orderings which are
     * consistent with equals.
     */
    public int compare(Object o1, Object o2)
    {
        User u1 = (User)o1;
        User u2 = (User)o2;

        int comparison;

        switch (sort_order)
        {
            case SORT_ALPHABETICAL_ASCENDING :
                comparison = u1.getNick().toLowerCase().compareTo(u2.getNick().toLowerCase());
                break;
            case SORT_TIME_SINCE_LAST_ASCENDING :
                // Reverse sort because these are "times OF", not "times SINCE"
                comparison = u2.getTimeOfLastActivity().compareTo(u1.getTimeOfLastActivity());
                break;
            case SORT_MODE_ALPHABETICAL_ASCENDING :
                if (channel != null)
                {
                    //Reverse because we have to follow the ascending order
                    comparison = compareMode(u2, u1, channel);
                    if (comparison == 0)
                    {
                        comparison = u1.getNick().toLowerCase().compareTo(u2.getNick().toLowerCase());
                    }
                }
                else
                {
                    throw new IllegalArgumentException("No valid channel set.");
                }
                break;
            case SORT_MODE_TIME_SINCE_LAST_ASCENDING :
                if (channel != null)
                {
                    comparison = compareMode(u1, u2, channel);
                    if (comparison == 0)
                    {
                        comparison = u2.getTimeOfLastActivity().compareTo(u1.getTimeOfLastActivity());
                    }
                }
                else
                {
                    throw new IllegalArgumentException("No valid channel set.");
                }
                break;
            default :
                throw new IllegalArgumentException("Invalid sort order.");
        }

        return comparison;
    }

    protected int compareMode(User u1, User u2, Channel channel)
    {
        boolean u1HasFlag = u1.hasModeFlag(channel, MODE_OP);
        boolean u2HasFlag = u2.hasModeFlag(channel, MODE_OP);

        if (u1HasFlag == true && u2HasFlag == false)
        {
            return 1;
        }
        else if (u1HasFlag == false && u2HasFlag == true)
        {
            return -1;
        }
        else if (u1HasFlag == false && u2HasFlag == false)
        {
            u1HasFlag = u1.hasModeFlag(channel, MODE_HALFOP);
            u2HasFlag = u2.hasModeFlag(channel, MODE_HALFOP);

            if (u1HasFlag == true && u2HasFlag == false)
            {
                return 1;
            }
            else if (u1HasFlag == false && u2HasFlag == true)
            {
                return -1;
            }
            else if (u1HasFlag == false && u2HasFlag == false)
            {
                u1HasFlag = u1.hasModeFlag(channel, MODE_VOICE);
                u2HasFlag = u2.hasModeFlag(channel, MODE_VOICE);

                if (u1HasFlag == true && u2HasFlag == false)
                {
                    return 1;
                }
                else if (u1HasFlag == false && u2HasFlag == true)
                {
                    return -1;
                }
            }
        }

        return 0;
    }
}
