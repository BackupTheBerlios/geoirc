/*
 * BoolExpEvaluator.java
 *
 * Created on June 25, 2003, 6:30 AM
 */

package geoirc;

import java.util.Vector;
import java.util.StringTokenizer;
import java.util.regex.*;

/**
 *
 * @author  Pistos
 */
public class BoolExpEvaluator
{
    protected static final String TRUE = "true";
    protected static final String FALSE = "false";
    protected static final String AND = "and";
    protected static final String OR = "or";
    protected static final String NOT = "not";
    
    // No default constructor
    private BoolExpEvaluator() { }
    
    /* Returns the evaluation of the expression, given the truths
     * provided in flag_set.
     *
     * flag_set is a whitespace-separated list of flags.  Terms in
     * expression are queries into the membership of flag_set.
     * That is, the term "A" in expression resolves to true if flag_set
     * contains "A", and resolves to false otherwise.
     *
     * As a rule, empty expressions always evaluate to false.
     */
    public static boolean evaluate( String expression, String flag_set )
        throws BadExpressionException
    {
        if( ( expression == null ) || ( expression.equals( "" ) ) )
        {
            return false;
        }
        
        String expr = expression.toLowerCase();
        
        // Put space around each parenthesis to assist tokenization.
        
        Matcher m;
        
        m = Pattern.compile( "(\\S)\\(" ).matcher( expr );
        if( m.matches() )
        {
            m.replaceAll( m.group( 1 ) + " (" );
        }

        m = Pattern.compile( "\\((\\S)" ).matcher( expr );
        if( m.matches() )
        {
            m.replaceAll( " (" + m.group( 1 ) );
        }

        m = Pattern.compile( "(\\S)\\)" ).matcher( expr );
        if( m.matches() )
        {
            m.replaceAll( m.group( 1 ) + " )" );
        }

        m = Pattern.compile( "\\)(\\S)" ).matcher( expr );
        if( m.matches() )
        {
            m.replaceAll( " )" + m.group( 1 ) );
        }

        Vector v = new Vector();
        StringTokenizer st = new StringTokenizer( expr );
        while( st.hasMoreTokens() )
        {
            v.add( st.nextToken() );
        }
        
        String [] tokenized_expr = new String[ v.size() ];
        tokenized_expr = (String []) v.toArray( tokenized_expr );
        
        String [] tokenized_flags = null;
        
        if( ( flag_set != null ) && ( ! flag_set.equals( "" ) ) )
        {
            v = new Vector();
            st = new StringTokenizer( flag_set.toLowerCase() );
            while( st.hasMoreTokens() )
            {
                v.add( st.nextToken() );
            }
            tokenized_flags = new String[ v.size() ];
            tokenized_flags = (String []) v.toArray( tokenized_flags );
        }
        
        return evaluate( tokenized_expr, tokenized_flags );
    }
    
    // start_index is inclusive, end_index is exclusive
    protected static String [] getRange( String [] array, int start_index, int end_index )
    {
        if( start_index < 0 )
        {
            throw new ArrayIndexOutOfBoundsException();
        }
        
        if( end_index > array.length )
        {
            end_index = array.length;
        }
        
        if( start_index >= end_index )
        {
            return new String[ 0 ];
        }
        
        String [] retval = new String[ end_index - start_index ];
        for( int i = start_index; i < end_index; i++ )
        {
            retval[ i - start_index ] = array[ i ];
        }
        
        return retval;
    }
    
    // start_index is inclusive, end_index is exclusive
    protected static String [] replaceRange( String [] array, int start_index, int end_index, String item )
    {
        if( start_index < 0 )
        {
            throw new ArrayIndexOutOfBoundsException();
        }
        
        if( end_index > array.length )
        {
            end_index = array.length;
        }
        
        if( start_index >= end_index )
        {
            return new String[ 0 ];
        }
        
        String [] retval = new String[ array.length - ( end_index - start_index ) + 1 ];
        int j = 0;
        for( int i = 0; i < array.length; i++ )
        {
            if( ( i < start_index ) || ( i >= end_index ) )
            {
                retval[ j ] = array[ i ];
                j++;
            }
            else if( i == end_index - 1 )
            {
                retval[ j ] = item;
                j++;
            }
        }
        
        return retval;
    }
    
    /* The real work is done by this protected method.
     */
    protected static boolean evaluate( String [] expression, String [] flags )
        throws BadExpressionException
    {
        /* Order of operations:
         * 1) Replace first-depth parenthesized sub-expressions by their
         *    evaluations.
         * 2) Transform terms into true/false values.
         * 3) Evaluate NOT operators.
         * 4) Evaluate AND operators.
         * 5) Evaluate OR operators.
         */
        
        String [] expr = expression;

        // 1) Parentheses
        
        int op_index = -1;
        for( int i = 0; i < expr.length; i++ )
        {
            if( expr[ i ].equals( "(" ) )
            {
                op_index = i;

                // Find matching close parenthesis.

                int cp_index = -1;
                int depth = 0;
                for( int j = i + 1; j < expr.length; j++ )
                {
                    if( expr[ j ].equals( "(" ) )
                    {
                        depth++;
                    }
                    
                    if( expr[ j ].equals( ")" ) )
                    {
                        if( depth == 0 )
                        {
                            cp_index = j;
                            break;
                        }
                        depth--;
                    }
                }
                
                if( cp_index == -1 )
                {
                    throw new BadExpressionException( "unmatched open parenthesis" );
                }
                
                // Evaluate subexpression, and replace it with its truth
                // value.
                
                String [] subexpr;
                try
                {
                    subexpr = getRange( expr, op_index + 1, cp_index );
                }
                catch( ArrayIndexOutOfBoundsException e )
                {
                    throw new BadExpressionException( "parenthetical syntax error" );
                }
                
                try
                {
                    expr = replaceRange( expr, op_index, cp_index + 1,
                        ( evaluate( subexpr, flags ) ? TRUE : FALSE )
                    );
                }
                catch( ArrayIndexOutOfBoundsException e )
                {
                    throw new BadExpressionException( "parenthetical syntax error" );
                }
                    
                    
            }
            else if( expr[ i ].equals( ")" ) )
            {
                throw new BadExpressionException( "unmatched close parenthesis" );
            }
        }
        
        // 2) Terms
        
        for( int i = 0; i < expr.length; i++ )
        {
            if(
                ( ! expr[ i ].equals( TRUE ) )
                && ( ! expr[ i ].equals( FALSE ) )
                && ( ! expr[ i ].equals( AND ) )
                && ( ! expr[ i ].equals( OR ) )
                && ( ! expr[ i ].equals( NOT ) )
            )
            {
                boolean present = false;
                for( int j = 0; j < flags.length; j++ )
                {
                    if( flags[ j ].equals( expr[ i ] ) )
                    {
                        present = true;
                        break;
                    }
                }
                expr[ i ] = ( present ? TRUE : FALSE );
            }
        }
        
        // 3) NOT operators
        
        for( int i = 0; i < expr.length; i++ )
        {
            if( expr[ i ].equals( NOT ) )
            {
                if( i == expr.length - 1 )
                {
                    throw new BadExpressionException( "missing NOT operand" );
                }
                
                if( expr[ i + 1 ].equals( TRUE ) )
                {
                    expr = replaceRange( expr, i, i + 1 + 1, FALSE );
                }
                else if( expr[ i + 1 ].equals( FALSE ) )
                {
                    expr = replaceRange( expr, i, i + 1 + 1, TRUE );
                }
                else
                {
                    throw new BadExpressionException( "missing/invalid NOT operand" );
                }
            }
        }

        // 4) AND operators
        
        for( int i = 0; i < expr.length; i++ )
        {
            if( expr[ i ].equals( AND ) )
            {
                if( ( i == 0 ) || ( i == expr.length - 1 ) )
                {
                    throw new BadExpressionException( "missing AND operand" );
                }
                
                String left = expr[ i - 1 ];
                String right = expr[ i + 1 ];
                if(
                    ( ( ! left.equals( TRUE ) ) && ( ! left.equals( FALSE ) ) )
                    ||
                    ( ( ! right.equals( TRUE ) ) && ( ! right.equals( FALSE ) ) )
                )
                {
                    throw new BadExpressionException( "missing/invalid AND operand" );
                }
                else
                {
                    expr = replaceRange(
                        expr, i - 1, i + 1 + 1,
                        (
                            ( left.equals( TRUE ) && right.equals( TRUE ) )
                            ? TRUE
                            : FALSE
                        )
                    );
                    i = i - 1;
                }
            }
        }

        // 5) OR operators
        
        for( int i = 0; i < expr.length; i++ )
        {
            if( expr[ i ].equals( OR ) )
            {
                if( ( i == 0 ) || ( i == expr.length - 1 ) )
                {
                    throw new BadExpressionException( "missing OR operand" );
                }
                
                String left = expr[ i - 1 ];
                String right = expr[ i + 1 ];
                if(
                    ( ( ! left.equals( TRUE ) ) && ( ! left.equals( FALSE ) ) )
                    ||
                    ( ( ! right.equals( TRUE ) ) && ( ! right.equals( FALSE ) ) )
                )
                {
                    throw new BadExpressionException( "missing/invalid OR operand" );
                }
                else
                {
                    expr = replaceRange(
                        expr, i - 1, i + 1 + 1,
                        (
                            ( left.equals( TRUE ) || right.equals( TRUE ) )
                            ? TRUE
                            : FALSE
                        )
                    );
                    i = i - 1;
                }
            }
        }
        
        // We should be left with either "true" or "false"
        
        if( expr.length != 1 )
        {
            throw new BadExpressionException( "malformed expression" );
        }
        
        return ( expr[ 0 ].equals( TRUE ) );
    }
    
}
