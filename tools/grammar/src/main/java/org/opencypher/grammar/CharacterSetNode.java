package org.opencypher.grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.opencypher.tools.xml.Attribute;
import org.opencypher.tools.xml.Child;
import org.opencypher.tools.xml.Element;

import static java.util.Collections.unmodifiableList;

@Element(uri = Grammar.XML_NAMESPACE, name = "character")
class CharacterSetNode extends Node implements CharacterSet
{
    private static final String ANY = "ANY", SPACE = "SPACE", DEL = "DEL", EOI = "EOI";
    static final String DEFAULT_SET = ANY;

    /** The name of a well-known character set to base these characters on. */
    @Attribute(optional = true)
    String set = DEFAULT_SET;

    private List<Exclusion> exceptions;

    @Override
    void resolve( ProductionNode origin, ProductionResolver resolver )
    {
        resolver.verifyCharacterSet( origin, set );
    }

    @Override
    CharacterSetNode defensiveCopy()
    {
        CharacterSetNode characters = new CharacterSetNode();
        characters.set = set;
        if ( exceptions != null )
        {
            characters.exceptions = new ArrayList<>( exceptions );
        }
        return characters;
    }

    void exclude( String literal )
    {
        exclude( Exclusion.literal( literal ) );
    }

    void exclude( int codePoint )
    {
        exclude( Exclusion.codePoint( codePoint ) );
    }

    void exclude( Exclusion exclusion )
    {
        if ( exceptions == null )
        {
            exceptions = new ArrayList<>();
        }
        exceptions.add( exclusion );
    }

    @Child
    void add( Except exception )
    {
        exclude( exception.exclusion() );
    }

    @Override
    Node replaceWithVerified()
    {
        return super.replaceWithVerified();
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !(o instanceof CharacterSetNode) )
        {
            return false;
        }
        CharacterSetNode that = (CharacterSetNode) o;
        return Objects.equals( this.set, that.set ) &&
               Objects.equals( this.exceptions, that.exceptions );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( set, exceptions );
    }

    @Override
    public String toString()
    {
        return String.format( "Characters{set='%s', except=%s}",
                              set, exceptions == null ? "[]" : exceptions.toString() );
    }

    @Override
    public <P, T, EX extends Exception> T transform( TermTransformation<P, T, EX> transformation, P param ) throws EX
    {
        return transformation.transformCharacters( param, this );
    }

    @Override
    public String setName()
    {
        return set;
    }

    public List<Exclusion> exclusions()
    {
        return exceptions == null ? Collections.<Exclusion>emptyList() : unmodifiableList( exceptions );
    }

    static boolean isReserved( String name )
    {
        if ( codePoint( name ) != -1 )
        {
            return true;
        }
        if ( ANY.equals( name ) || SPACE.equals( name ) || EOI.equals( name ) )
        {
            return true;
        }
        return false;
    }

    private static final String
            NUL = "NUL", SOH = "SOH", STX = "STX", ETX = "ETX", EOT = "EOT", ENQ = "ENQ", ACK = "ACK", BEL = "BEL",
            BS = "BS", TAB = "TAB", LF = "LF", VT = "VT", FF = "FF", CR = "CR", SO = "SO", SI = "SI",
            DLE = "DLE", DC1 = "DC1", DC2 = "DC2", DC3 = "DC3", DC4 = "DC4", NAK = "NAK", SYN = "SYN", ETB = "ETB",
            CAN = "CAN", EM = "EM", SUB = "SUB", ESC = "ESC", FS = "FS", GS = "GS", RS = "RS", US = "US";
    private static final String[] CONTROL_CHARS = {
            NUL, SOH, STX, ETX, EOT, ENQ, ACK, BEL,
            BS, TAB, LF, VT, FF, CR, SO, SI,
            DLE, DC1, DC2, DC3, DC4, NAK, SYN, ETB,
            CAN, EM, SUB, ESC, FS, GS, RS, US};

    static CharacterSetNode codePoint( int codePoint )
    {
        String name = controlCharName( codePoint );
        if ( name == null )
        {
            name = Character.getName( codePoint );
        }
        CharacterSetNode result = new CharacterSetNode();
        result.set = name;
        return result;
    }

    static String controlCharName( int codePoint )
    {
        if ( codePoint < CONTROL_CHARS.length )
        {
            return CONTROL_CHARS[codePoint];
        }
        else if ( codePoint == 0x7f )
        {
            return DEL;
        }
        else
        {
            return null;
        }
    }

    static int codePoint( String name )
    {
        switch ( name )
        { // <pre>
        case NUL:   return 0x00; // -- \0 (null)
        case SOH:   return 0x01; //       (start of heading)
        case STX:   return 0x02; //       (start of text)
        case ETX:   return 0x03; //       (end of text)
        case EOT:   return 0x04; //       (end of transmission)
        case ENQ:   return 0x05; //       (enquiry)
        case ACK:   return 0x06; //       (ack)
        case BEL:   return 0x07; // -- \a (bell)
        case BS:    return 0x08; // -- \b (backspace)
        case TAB:   return 0x09; // -- \t (horizontal tab)
        case LF:    return 0x0A; // -- \n (line feed)
        case VT:    return 0x0B; // -- \v (vertical tab)
        case FF:    return 0x0C; // -- \f (form feed)
        case CR:    return 0x0D; // -- \r (carriage return)
        case SO:    return 0x0E; //       (shift out)
        case SI:    return 0x0F; //       (shift in)
        case DLE:   return 0x10; //       (data link escape)
        case DC1:   return 0x11; //       (device control 1 - XON)
        case DC2:   return 0x12; //       (device control 2)
        case DC3:   return 0x13; //       (device control 3 - XOFF)
        case DC4:   return 0x14; //       (device control 4)
        case NAK:   return 0x15; //       (negative ack)
        case SYN:   return 0x16; //       (synchronous idle)
        case ETB:   return 0x17; //       (end of transmission block)
        case CAN:   return 0x18; //       (cancel)
        case EM:    return 0x19; //       (end of medium)
        case SUB:   return 0x1A; //       (substitute)
        case ESC:   return 0x1B; // -- \e (escape)
        case FS:    return 0x1C; //       (file separator)
        case GS:    return 0x1D; //       (group separator)
        case RS:    return 0x1E; //       (record separator)
        case US:    return 0x1F; //       (unit separator)
        case SPACE: return 0x20;
        case DEL:   return 0x7F; //       (delete)
        default:    return -1;
        } //</pre>
    }
}
